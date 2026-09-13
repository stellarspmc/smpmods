package spmc.smpmod.fishing.mechanic

import net.dv8tion.jda.api.utils.MarkdownSanitizer
import net.minecraft.ChatFormatting
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.item.Item
import spmc.smpmod.SMPMod
import spmc.smpmod.core.BiomeCategory.Companion.getPlayerCategories
import spmc.smpmod.core.ItemModifier
import spmc.smpmod.core.ItemRarity
import spmc.smpmod.fishing.FishItem
import spmc.smpmod.fishing.FishTracker.Companion.get
import spmc.smpmod.fishing.RodItem
import spmc.smpmod.quest.Quest
import spmc.smpmod.quest.QuestManager.Companion.getQuests
import spmc.smpmod.registry.FishingRegistry.getAvailableFish
import java.util.*
import kotlin.math.max
import kotlin.math.pow

object FishingLoot {
	fun rewardFish(player: ServerPlayer, item: RodItem, streak: Int) {
		val random = SMPMod.minecraftServer?.overworld()?.getRandom() ?: return

		if (streak > 3) {}//TODO("fish mob to kill (like the new game)")

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
		doubleArrayOf(6.0, 14.0, 30.0, 24.0, 15.0, 7.0, 3.2, .8), // astral, t8
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