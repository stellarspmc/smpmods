package spmc.smpmod.fishing

import net.dv8tion.jda.api.utils.MarkdownSanitizer
import net.minecraft.ChatFormatting
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import net.minecraft.server.level.ServerBossEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.Mth
import net.minecraft.util.Prediction
import net.minecraft.util.RandomSource
import net.minecraft.world.BossEvent.BossBarColor
import net.minecraft.world.BossEvent.BossBarOverlay
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.*
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.targeting.TargetingConditions
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.item.Item
import net.minecraft.world.phys.AABB
import spmc.smpmod.SMPMod
import spmc.smpmod.SMPMod.Companion.minecraftServer
import spmc.smpmod.core.BiomeCategory.Companion.getPlayerCategories
import spmc.smpmod.core.ItemModifier
import spmc.smpmod.core.ItemRarity
import spmc.smpmod.fishing.FishTracker.Companion.get
import spmc.smpmod.quest.Quest
import spmc.smpmod.quest.QuestManager.Companion.getQuests
import spmc.smpmod.registry.FishingRegistry.getAvailableFish
import spmc.smpmod.utils.*
import java.util.*
import kotlin.math.max
import kotlin.math.pow

object FishingManager: SessionManager<FishingSession>() {
	@JvmStatic
	fun startMinigame(player: ServerPlayer, hook: FishingHook): Boolean {
		if (checkNotCreative(player)) return false
		val rodItem = player.mainHandItem.item as? RodItem ?: return false

		val session = FishingSession(player, hook, rodItem)
		return startSession(session)
	}
}

class FishingSession(val player: ServerPlayer, private val hook: FishingHook, private val item: RodItem): GameSession(listOf(player)) {
	private var cursor = 0f
	private var movingRight = true
	private var wasJumping = player.isJumping
	private var ticksLeft = 100
	private var streak = 0

	private val greenStart: Float
	private val greenEnd: Float

	init {
		val zoneWidth: Float = item.stats.greenZone
		this.greenStart = .5f - (zoneWidth / 2f)
		this.greenEnd = .5f + (zoneWidth / 2f)
	}

	override fun tick(): Boolean {
		if (hook.isRemoved || ticksLeft-- <= 0) {
			finish(SessionEndReason.FAIL)
			return true
		}

		val speed = .05f
		if (movingRight) {
			cursor += speed
			if (cursor >= 1f) {
				cursor = 1f
				movingRight = false
			}
		} else {
			cursor -= speed
			if (cursor <= 0f) {
				cursor = 0f
				movingRight = true
			}
		}

		player.sendSystemMessage(buildActionBarComponent(), true)

		val isJumping = player.lastClientInput.jump()
		if (isJumping && !wasJumping) {
			val hit = cursor in greenStart..greenEnd
			finish(if (hit) SessionEndReason.SUCCESS else SessionEndReason.FAIL)
			return true
		}

		this.wasJumping = isJumping
		return false
	}

	override fun onEnd(reason: SessionEndReason) {
		hook.discard()

		when (reason) {
			SessionEndReason.SUCCESS -> {
				if (streak > 0) streak++
				FishingLoot.rewardFish(player, item, streak)
			}
			SessionEndReason.FAIL -> {
				streak = 0
				sendError<Int>(player, message = "Missed the timing or time ran out!")
				player.sendSystemMessage(Component.empty(), true)
			}
			SessionEndReason.CANCELLED, SessionEndReason.DISCONNECTED -> { player.sendSystemMessage(Component.empty(), true) }
		}
	}

	private fun buildActionBarComponent(): Component {
		val bar = Component.literal("Reel in! [ ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
		val totalSegments = 24
		val cursorPos = (cursor * totalSegments).toInt()
		val gStartPos = (greenStart * totalSegments).toInt()
		val gEndPos = (greenEnd * totalSegments).toInt()

		for (i in 0..totalSegments) {
			when (i) {
				cursorPos -> bar.append(Component.literal("┃").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
				in gStartPos..gEndPos -> bar.append(Component.literal("▒").withStyle(ChatFormatting.GREEN))
				else -> bar.append(Component.literal("─").withStyle(ChatFormatting.DARK_GRAY))
			}
		}

		return bar.append(Component.literal(" ] ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
			.append(Component.literal("Jump").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
	}
}

object FishingLoot {
	fun rewardFish(player: ServerPlayer, item: RodItem, streak: Int) {
		if (streak >= 15) grant(player, "streak/s_15")
		if (streak >= 50) grant(player, "streak/s_50")
		if (streak >= 250) grant(player, "streak/s_250")

		if (streak >= 2) {
			if (player.level().random.nextFloat() < 1.075.pow(streak - 2) - 1) FishingMob.spawnMob(player, item, streak)
			else createFishItem(player, item, streak)
		} else createFishItem(player, item, streak)
	}

	internal fun rollFish(player: ServerPlayer, item: RodItem, streak: Int): FishData {
		val random = player.level().getRandom()
		val caughtFish = getRandomFishForTier(player, item)
		val modMap = mutableMapOf<ItemModifier, Int>()
		var traitChance = max(.45, (((item.tier.ordinal + 1).toDouble() / 8) * streak) / 5)
		val starQuality = rollStarQuality(random, 1 / item.stats.luck)

		val mods = ItemModifier.entries.filter(ItemModifier::isNotLocked) as MutableList<ItemModifier>
		mods.addAll(item.mods)
		while (mods.isNotEmpty() && random.nextDouble() < traitChance) {
			modMap[mods.removeAt(random.nextInt(mods.size))] = random.nextInt(5) + 1
			traitChance *= max(.55, .2 * item.stats.luck / 1.8)
		}

		player.level().playSound(null, player.x, player.y, player.z, SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.PLAYERS, 1f, 1.2f)
		player.sendSystemMessage(Component.literal("You caught a ").withStyle(ChatFormatting.GREEN).append(Component.literal(caughtFish.fishName).withColor(caughtFish.rarity.color)).append(Component.literal(".").withStyle(ChatFormatting.GREEN)))
		get()?.addFish(player.getUUID(), BuiltInRegistries.ITEM.getKey(caughtFish).path)
		if (caughtFish.rarity.shouldAnnounce()) announceLoot(caughtFish.rarity.toString().uppercase(Locale.getDefault()), caughtFish.fishName, caughtFish.rarity.color, player)
		getQuests(player).activeQuests.forEach { if (it.getQuest()?.type == Quest.QuestType.FISHING) it.increment(1) }
		return FishData(caughtFish, starQuality, modMap)
	}

	private fun createFishItem(player: ServerPlayer, item: RodItem, streak: Int) {
		val fishData = rollFish(player, item, streak)
		val fishStack = fishData.item.createFishInstance(fishData.star, fishData.mods)
		if (!player.inventory.add(fishStack)) player.drop(fishStack, false, Prediction.PREDICTED)
	}

	private val rates: List<DoubleArray> = listOf( // 8 tiers, so a 8x8 matrix
		doubleArrayOf(93.9935, 5.7, .29, .01, .005, .001, .0004, .0001),
		doubleArrayOf(81.9974, 15.0, 2.8, .19, .01, .002, .0004, .0002),
		doubleArrayOf(67.9971, 24.0, 7.0, .95, .05, .002, .0006, .0003),
		doubleArrayOf(49.9992, 32.0, 14.0, 3.5, .48, .02, .0005, .0003),
		doubleArrayOf(31.9995, 35.0, 22.0, 8.5, 2.3, .18, .02, .0005),
		doubleArrayOf(20.0, 32.0, 28.0, 13.0, 5.8, 1.0, .18, .02),
		doubleArrayOf(10.0, 22.0, 30.0, 21.0,  11.0, 4.2, 1.5, .3),
		doubleArrayOf(5.0, 13.0, 25.0, 28.0, 16.0, 8.0, 4.0, 1.0)
	)

	private fun getRandomFishForTier(player: ServerPlayer, item: RodItem): FishItem {
		val playerCategories = getPlayerCategories(player)
		val pool: MutableList<Item> = mutableListOf()
		playerCategories.forEach { pool.addAll(getAvailableFish(it)) }
		check(pool.isNotEmpty()) { "Fish pool is empty!" }
		val roll = (minecraftServer ?: return pool[0] as FishItem).overworld().getRandom().nextDouble() * 100
		var current = .0
		var selectedRarity = ItemRarity.COMMON

		val rarities = ItemRarity.entries
		for (i in rates[item.tier.ordinal].indices) {
			current += rates[item.tier.ordinal][i]
			if (roll <= current) {
				selectedRarity = rarities[i]
				break
			}
		}

		val finalRarity = selectedRarity
		val matchingFish = ArrayList(pool.map { it as FishItem }.filter { it.rarity == finalRarity }).ifEmpty { return pool[0] as FishItem }
		var tierTotalWeight = .0
		val fishWeights = DoubleArray(matchingFish.size)

		for (i in matchingFish.indices) {
			val weight = matchingFish[i].basePrice.pow(-1 + (item.tier.ordinal * .2))
			fishWeights[i] = weight
			tierTotalWeight += weight
		}

		val fishRoll = (minecraftServer ?: return matchingFish[0]).overworld().getRandom().nextDouble() * tierTotalWeight
		var fishWeight = .0

		for (i in matchingFish.indices) {
			fishWeight += fishWeights[i]
			if (fishRoll <= fishWeight) return matchingFish[i]
		}

		if (finalRarity == ItemRarity.RARE) grant(player, "rarity/rare")
		if (finalRarity == ItemRarity.EPIC) grant(player, "rarity/epic")
		if (finalRarity == ItemRarity.LEGENDARY) grant(player, "rarity/legendary")
		if (finalRarity.ordinal >= ItemRarity.MYTHIC.ordinal) grant(player, "rarity/others")

		return matchingFish[0]
	}

	private val BASE_STAR_WEIGHTS = intArrayOf(1000, 600, 300, 120, 35, 6)
	private fun rollStarQuality(random: RandomSource, luckBonus: Float): Int {
		val adjustedWeights = DoubleArray(BASE_STAR_WEIGHTS.size)
		var totalWeight = .0

		for (star in BASE_STAR_WEIGHTS.indices) {
			val weight = BASE_STAR_WEIGHTS[star].toDouble() * luckBonus.toDouble().pow(star.toDouble())
			adjustedWeights[star] = weight
			totalWeight += weight
		}

		val roll = random.nextDouble() * totalWeight
		var cumulative = .0

		for (star in adjustedWeights.indices) {
			cumulative += adjustedWeights[star]
			if (roll < cumulative) return star
		}

		return 0
	}

	private fun announceLoot(rarityName: String, fishName: String, color: TextColor, player: ServerPlayer) {
		minecraftServer?.playerList?.broadcastSystemMessage(Component.literal("★ ").withColor(color).withStyle(ChatFormatting.BOLD).append(Component.literal(player.scoreboardName).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD)).append(Component.literal(" has reeled up a ").withStyle(ChatFormatting.GRAY)).append(Component.literal(rarityName).withColor(color).withStyle(ChatFormatting.BOLD)).append(Component.literal(fishName)).withColor(color).append(Component.literal("! ★").withColor(color).withStyle(ChatFormatting.BOLD)), false)
		SMPMod.messageChannel?.sendMessage("**" + MarkdownSanitizer.escape(player.scoreboardName) + "** just reeled up a **" + rarityName + "** " + fishName + "!")?.queue()
	}

	internal data class FishData(val item: FishItem, val star: Int, val mods: MutableMap<ItemModifier, Int>)
}

object FishingMob {
	private val mobsToSpawn: List<EntityType<out Mob>> = listOf(EntityTypes.GUARDIAN, EntityTypes.ELDER_GUARDIAN, EntityTypes.PHANTOM, EntityTypes.DROWNED) // TODO: add more mobs (cave spider? slime / magma cube / breeze)
	private val bossBar: MutableMap<ServerBossEvent, Monster> = mutableMapOf()

	fun spawnMob(player: ServerPlayer, item: RodItem, streak: Int) {
		val fishData = FishingLoot.rollFish(player, item, streak)
		val mob = mobsToSpawn[player.random.nextInt(mobsToSpawn.size)].create(player.level(), EntitySpawnReason.TRIGGERED) as? Monster ?: return
		val maxHp = healthRange(fishData.item.rarity, player.random).toDouble()
		mob.getAttribute(Attributes.MAX_HEALTH)?.baseValue = maxHp
		mob.health = maxHp.toFloat()

		val dmg = damage(fishData.item.rarity, player.random).toDouble()
		mob.getAttribute(Attributes.ATTACK_DAMAGE)?.baseValue = dmg

		val rarityColor = fishData.item.rarity.color
		mob.customName = Component.literal(fishData.item.fishName).withColor(rarityColor)
		mob.isCustomNameVisible = true
		mob.target = player

		if (fishData.item.rarity.shouldAnnounce()) {
			val event = ServerBossEvent(Mth.createInsecureUUID(player.level().random), Component.literal(fishData.item.fishName), BossBarColor.BLUE, BossBarOverlay.PROGRESS)
			event.addPlayer(player)
			player.level().getNearbyPlayers(TargetingConditions.DEFAULT, player, AABB(-15.0, -15.0, -15.0, 15.0, 15.0, 15.0)).forEach { event.addPlayer(it as ServerPlayer) }

			bossBar[event] = mob
		}

		mob.setItemInHand(InteractionHand.MAIN_HAND, fishData.item.createFishInstance(fishData.star, fishData.mods))
		mob.setDropChance(EquipmentSlot.MAINHAND, 1f)
		mob.setPos(player.position().add(player.lookAngle.x * 1.5, 0.5, player.lookAngle.z * 1.5))

		player.level().addFreshEntity(mob)
	}

	private fun healthRange(rarity: ItemRarity, random: RandomSource): Int {
		return when (rarity) {
			ItemRarity.COMMON -> random.nextIntBetweenInclusive(15, 35)
			ItemRarity.UNCOMMON -> random.nextIntBetweenInclusive(35, 50)
			ItemRarity.RARE -> random.nextIntBetweenInclusive(50, 65)
			ItemRarity.EPIC -> random.nextIntBetweenInclusive(70, 85)
			ItemRarity.LEGENDARY -> random.nextIntBetweenInclusive(100, 135)
			ItemRarity.MYTHIC -> random.nextIntBetweenInclusive(145, 160)
			ItemRarity.CHROMATIC -> random.nextIntBetweenInclusive(165, 180)
			ItemRarity.ASTRAL -> random.nextIntBetweenInclusive(195, 230)
		}
	}

	private fun damage(rarity: ItemRarity, random: RandomSource): Int {
		return when (rarity) {
			ItemRarity.COMMON -> random.nextIntBetweenInclusive(1, 3)
			ItemRarity.UNCOMMON -> random.nextIntBetweenInclusive(2, 7)
			ItemRarity.RARE -> random.nextIntBetweenInclusive(5, 10)
			ItemRarity.EPIC -> random.nextIntBetweenInclusive(12, 16)
			ItemRarity.LEGENDARY -> random.nextIntBetweenInclusive(17, 21)
			ItemRarity.MYTHIC -> random.nextIntBetweenInclusive(23, 25)
			ItemRarity.CHROMATIC -> random.nextIntBetweenInclusive(27, 30)
			ItemRarity.ASTRAL -> 35
		}
	}

	fun serverTickLoop() {
		bossBar.forEach { (event, mob) -> if (!mob.isAlive || mob.isRemoved) {
			event.removeAllPlayers()
			event.isVisible = false
		} else event.progress = (mob.health / mob.maxHealth).coerceIn(0f, 1f) }
	}
}