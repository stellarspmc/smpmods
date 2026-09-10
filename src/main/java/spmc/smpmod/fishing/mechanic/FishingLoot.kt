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
import spmc.smpmod.SMPMod
import spmc.smpmod.core.ItemModifier
import spmc.smpmod.core.ItemRarity
import spmc.smpmod.fishing.BiomeCategory
import spmc.smpmod.fishing.FishItem
import spmc.smpmod.fishing.FishTracker.Companion.get
import spmc.smpmod.fishing.RodTiers
import spmc.smpmod.quest.PlayerQuestData.ActiveQuest
import spmc.smpmod.quest.Quest
import spmc.smpmod.quest.QuestManager.Companion.getQuests
import java.util.*
import java.util.function.Consumer
import kotlin.math.max
import kotlin.math.pow

object FishingLoot {
	fun rewardFish(player: ServerPlayer, tier: RodTiers, streak: Int) {
		val random = SMPMod.minecraftServer?.overworld()?.getRandom() ?: return

		if (streak > 3) TODO("fish mob to kill (like the new game)")

		val caughtFish = getRandomFishForTier(player, tier)
		val modMap = mutableMapOf<ItemModifier, Int>()
		var traitChance = max(.5, (((tier.ordinal + 1).toDouble() / RodTiers.entries.size) * streak) * .2 * tier.catchLuckBonus)

		val mods = ItemModifier.entries.filter(ItemModifier::isNotLocked) as MutableList<ItemModifier>
		mods.addAll(Arrays.stream(tier.obtainable).toList())
		while (mods.isNotEmpty() && random.nextDouble() < traitChance) {
			val index = random.nextInt(mods.size)
			modMap[mods.removeAt(index)] = random.nextInt(5) + 1
			traitChance *= max(.4, .2 * tier.catchLuckBonus / 1.8)
		}

		val fishStack = caughtFish.createFishInstance(rollStarQuality(random, 1 / tier.catchLuckBonus), modMap)
		if (!player.inventory.add(fishStack)) player.drop(fishStack, false)
		player.level().playSound(null, player.x, player.y, player.z, SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.PLAYERS, 1f, 1.2f)
		player.sendSystemMessage(Component.literal("You caught a ").withStyle(ChatFormatting.GREEN).append(Component.literal(caughtFish.fishName).withColor(caughtFish.rarity.color)).append(Component.literal(".").withStyle(ChatFormatting.GREEN)))
		get()?.addFish(player.getUUID(), BuiltInRegistries.ITEM.getKey(caughtFish).path)
		if (caughtFish.rarity.shouldAnnounce()) announceLoot(caughtFish.rarity.toString().uppercase(Locale.getDefault()), caughtFish.fishName, caughtFish.rarity.color, player)
		getQuests(player).activeQuests.forEach(Consumer { activeQuest: ActiveQuest -> if (activeQuest.getQuest()?.type == Quest.QuestType.FISHING) activeQuest.increment(1) })
	}

	private fun getRandomFishForTier(player: ServerPlayer, tier: RodTiers): FishItem {
		val pool = BiomeCategory.getAvailableFish(player)
		check(pool.isNotEmpty()) { "Fish pool is empty!" }
		val weights = tier.rates
		val roll = (SMPMod.minecraftServer?: return pool[0] as FishItem).overworld().getRandom().nextDouble() * 100
		var current = .0
		var selectedRarity = ItemRarity.COMMON

		val rarities = ItemRarity.entries
		for (i in weights.indices) {
			current += weights[i]
			if (roll <= current) {
				selectedRarity = rarities[i]
				break
			}
		}

		val finalRarity = selectedRarity
		val matchingFish = ArrayList(pool.map { it as FishItem }.filter { it.rarity == finalRarity }).ifEmpty { return pool[0] as FishItem }
		var tierTotalWeight = 0.0
		val fishWeights = DoubleArray(matchingFish.size)

		for (i in matchingFish.indices) {
			val weight = matchingFish[i].basePrice.pow(-1 + (tier.ordinal * .2))
			fishWeights[i] = weight
			tierTotalWeight += weight
		}

		val fishRoll = (SMPMod.minecraftServer?: return matchingFish[0]).overworld().getRandom().nextDouble() * tierTotalWeight
		var fishWeight = 0.0

		for (i in matchingFish.indices) {
			fishWeight += fishWeights[i]
			if (fishRoll <= fishWeight) return matchingFish[i]
		}

		return matchingFish[0]
	}

	private val BASE_STAR_WEIGHTS = doubleArrayOf(1000.0, 600.0, 300.0, 120.0, 35.0, 6.0)
	private fun rollStarQuality(random: RandomSource, luckBonus: Float): Int {
		val adjustedWeights = DoubleArray(BASE_STAR_WEIGHTS.size)
		var totalWeight = 0.0

		for (star in BASE_STAR_WEIGHTS.indices) {
			val weight = BASE_STAR_WEIGHTS[star] * luckBonus.toDouble().pow(star.toDouble())
			adjustedWeights[star] = weight
			totalWeight += weight
		}

		val roll = random.nextDouble() * totalWeight
		var cumulative = 0.0

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