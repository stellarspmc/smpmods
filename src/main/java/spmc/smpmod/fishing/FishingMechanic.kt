package spmc.smpmod.fishing

import net.dv8tion.jda.api.utils.MarkdownSanitizer
import spmc.smpmod.utils.*
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.item.Item
import spmc.smpmod.SMPMod
import spmc.smpmod.core.BiomeCategory.Companion.getPlayerCategories
import spmc.smpmod.core.ItemModifier
import spmc.smpmod.core.ItemRarity
import spmc.smpmod.fishing.FishTracker.Companion.get
import spmc.smpmod.quest.Quest
import spmc.smpmod.quest.QuestManager.Companion.getQuests
import spmc.smpmod.registry.FishingRegistry.getAvailableFish
import java.util.ArrayList
import java.util.Locale
import kotlin.collections.addAll
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
		val random = SMPMod.minecraftServer?.overworld()?.getRandom() ?: return

		if (streak >= 15) {
			FishingMob.spawnMob("test", player.blockPosition())
			grant(player, "streak/s_15")
		}
		if (streak >= 50) grant(player, "streak/s_50")
		if (streak >= 250) grant(player, "streak/s_250")

		val caughtFish = getRandomFishForTier(player, item)
		val modMap = mutableMapOf<ItemModifier, Int>()
		var traitChance = max(.5, (((item.tier.ordinal + 1).toDouble() / 8) * streak) * .2 * item.stats.luck) // TODO

		val mods = ItemModifier.entries.filter(ItemModifier::isNotLocked) as MutableList<ItemModifier>
		mods.addAll(item.mods)
		while (mods.isNotEmpty() && random.nextDouble() < traitChance) {
			val index = random.nextInt(mods.size)
			modMap[mods.removeAt(index)] = random.nextInt(5) + 1
			traitChance *= max(.4, .2 * item.stats.luck / 1.8)
		}

		val fishStack = caughtFish.createFishInstance(rollStarQuality(random, 1 / item.stats.luck), modMap)
		if (!player.inventory.add(fishStack)) player.drop(fishStack, false)
		player.level().playSound(null, player.x, player.y, player.z, SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.PLAYERS, 1f, 1.2f)
		player.sendSystemMessage(Component.literal("You caught a ").withStyle(ChatFormatting.GREEN).append(Component.literal(caughtFish.fishName).withColor(caughtFish.rarity.color)).append(Component.literal(".").withStyle(ChatFormatting.GREEN)))
		get()?.addFish(player.getUUID(), BuiltInRegistries.ITEM.getKey(caughtFish).path)
		if (caughtFish.rarity.shouldAnnounce()) announceLoot(caughtFish.rarity.toString().uppercase(Locale.getDefault()), caughtFish.fishName, caughtFish.rarity.color, player)
		getQuests(player).activeQuests.forEach { if (it.getQuest()?.type == Quest.QuestType.FISHING) it.increment(1) }
	}

	private val rates: List<DoubleArray> = listOf( // 8 tiers, so an 8x8 matrix
		doubleArrayOf(78.0, 18.0, 3.5, .45, .045, .004, .0008, .0002), // normal, t1
		doubleArrayOf(68.0, 23.0, 7.5, 1.2, .25, .04, .008, .002), // copper, t2
		doubleArrayOf(56.0, 27.0, 12.0, 3.8, .9, .2, .08, .02), // iron, t3
		doubleArrayOf(34.0, 31.0, 21.0, 10.0, 3.2, .65, .12, .03), // emerald, t4
		doubleArrayOf(25.0, 32.0, 25.0, 12.5, 4.2, .95, .3, .05), // t5
		doubleArrayOf(18.0, 28.0, 31.0, 15.0, 5.0, 2.0, .8, .2), // t6
		/*doubleArrayOf(20.0, 28.0, 30.0, 14.0, 5.3, 2.0, .5, .2), // toxic / uranium t6
		doubleArrayOf(15.0, 25.0, 32.0, 16.0, 8.0, 2.8, 1.0, .2), // death / redstone? t6
		doubleArrayOf(22.0, 30.0, 28.0, 12.0, 5.0, 2.1, .7, .2), // air / breeze t6
		doubleArrayOf(16.0, 26.0, 32.0, 15.5, 6.0, 2.5, 1.2, .2), // sea / pris t6
		doubleArrayOf(14.0, 24.0, 30.0, 18.0, 8.0, 3.5, 2.0, .5), // flickering / sculk t6*/
		doubleArrayOf(10.0, 20.0, 32.0, 20.0, 11.0, 4.5, 2.0, .5), // elemental, t7
		doubleArrayOf(6.0, 14.0, 30.0, 24.0, 15.0, 7.0, 3.2, .8), // astral, t8 TODO
	)

	private fun getRandomFishForTier(player: ServerPlayer, item: RodItem): FishItem {
		val playerCategories = getPlayerCategories(player)
		val pool: MutableList<Item> = mutableListOf()
		playerCategories.forEach { pool.addAll(getAvailableFish(it)) }
		check(pool.isNotEmpty()) { "Fish pool is empty!" }
		val roll = (SMPMod.minecraftServer?: return pool[0] as FishItem).overworld().getRandom().nextDouble() * 100
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

		val fishRoll = (SMPMod.minecraftServer?: return matchingFish[0]).overworld().getRandom().nextDouble() * tierTotalWeight
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
		val chatAnnouncement: Component = Component.literal("★ ").withColor(color).withStyle(ChatFormatting.BOLD).append(Component.literal(player.scoreboardName).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD)).append(Component.literal(" has reeled up a ").withStyle(ChatFormatting.GRAY)).append(Component.literal(rarityName).withColor(color).withStyle(ChatFormatting.BOLD)).append(Component.literal(fishName)).withColor(color).append(Component.literal("! ★").withColor(color).withStyle(ChatFormatting.BOLD))

		SMPMod.minecraftServer?.playerList?.broadcastSystemMessage(chatAnnouncement, false)
		SMPMod.messageChannel?.sendMessage("**" + MarkdownSanitizer.escape(player.scoreboardName) + "** just reeled up a **" + rarityName + "** " + fishName + "!")?.queue()
	}
}

object FishingMob {
	val mobsToSpawn: List<EntityType<out Entity>> = listOf(EntityTypes.GUARDIAN, EntityTypes.ELDER_GUARDIAN, EntityTypes.PHANTOM) // TODO: add more mobs
	val healthRange: IntRange = 15..1250

	fun spawnMob(id: String, pos: BlockPos) {
		// TODO
	}

}