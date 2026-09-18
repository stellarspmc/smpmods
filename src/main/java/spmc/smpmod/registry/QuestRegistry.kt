package spmc.smpmod.registry

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerPlayer
import spmc.smpmod.quest.Quest
import spmc.smpmod.quest.Quest.QuestReward
import spmc.smpmod.quest.QuestManager.Companion.getQuests
import spmc.smpmod.utils.anyInCreative

object QuestRegistry {
	private val QUESTS: MutableMap<String, Quest> = mutableMapOf()

	fun init() {
		initDaily()
		initWeekly()

		PlayerBlockBreakEvents.AFTER.register { _, player, _, state, _ ->
			if (anyInCreative(player as? ServerPlayer ?: return@register)) return@register
			getQuests(player).activeQuests.forEach { val quest = it.getQuest() ?: return@register
				if (quest.type == Quest.QuestType.MINE_BLOCK && quest.target == BuiltInRegistries.BLOCK.getKey(state.block)) it.increment(1)
			}
		}

		ServerLivingEntityEvents.AFTER_DEATH.register { entity, damageSource ->
			if (entity.level().dimension().identifier().namespace != "minecraft") return@register
			getQuests(damageSource.entity as? ServerPlayer?: return@register).activeQuests.forEach { val quest = it.getQuest() ?: return@register
				if (quest.type == Quest.QuestType.KILL_MOB && quest.target == BuiltInRegistries.ENTITY_TYPE.getKey(entity.type)) it.increment(1)
			}
		}
	}

	private fun initDaily() {
		register(Quest("dm_iro", "Iron Miner", Quest.QuestType.MINE_BLOCK, "iron_ore", 16, Quest.QuestCategory.DAILY, QuestReward(950, 75)))
		register(Quest("dm_dia", "Diamond Miner", Quest.QuestType.MINE_BLOCK, "diamond_block", 16, Quest.QuestCategory.DAILY, QuestReward(1200, 75)))
		register(Quest("dm_sto", "Stone Miner", Quest.QuestType.MINE_BLOCK, "stone", 64, Quest.QuestCategory.DAILY, QuestReward(675, 80)))
		register(Quest("dk_zom", "Zombie Killer", Quest.QuestType.KILL_MOB, "zombie", 15, Quest.QuestCategory.DAILY, QuestReward(675, 120)))
		register(Quest("dk_cre", "Creeper Killer", Quest.QuestType.KILL_MOB, "creeper", 15, Quest.QuestCategory.DAILY, QuestReward(639, 120)))
		register(Quest("dk_ske", "Skeleton Killer", Quest.QuestType.KILL_MOB, "skeleton", 15, Quest.QuestCategory.DAILY, QuestReward(779, 120)))
		register(Quest("df1", "Fishing Newbie", Quest.QuestType.FISHING, "fishing", 15, Quest.QuestCategory.DAILY, QuestReward(173, 45)))
		register(Quest("df2", "Fishing Amateur", Quest.QuestType.FISHING, "fishing", 35, Quest.QuestCategory.DAILY, QuestReward(570, 65)))
		register(Quest("df3", "Fishing Master", Quest.QuestType.FISHING, "fishing", 75, Quest.QuestCategory.DAILY, QuestReward(1000, 85)))
	}

	private fun initWeekly() {
		register(Quest("wm_obs", "Obsidian Collector", Quest.QuestType.MINE_BLOCK, "obsidian", 384, Quest.QuestCategory.WEEKLY, QuestReward(6000, 150)))
		register(Quest("wm_net", "Ancient Archaeologist", Quest.QuestType.MINE_BLOCK, "ancient_debris", 192, Quest.QuestCategory.WEEKLY, QuestReward(6500, 160)))
		register(Quest("wk_war", "Warden Harvester", Quest.QuestType.KILL_MOB, "warden", 15, Quest.QuestCategory.WEEKLY, QuestReward(4600, 220)))
		register(Quest("wk_wit", "Wither Hunter", Quest.QuestType.KILL_MOB, "wither", 3, Quest.QuestCategory.WEEKLY, QuestReward(5700, 240)))
		register(Quest("wk_dra", "End Guardian", Quest.QuestType.KILL_MOB, "ender_dragon", 2, Quest.QuestCategory.WEEKLY, QuestReward(6200, 375)))
		register(Quest("wk_elg", "Elder Reaper", Quest.QuestType.KILL_MOB, "elder_guardian", 15, Quest.QuestCategory.WEEKLY, QuestReward(7500, 400)))
		register(Quest("wf1", "Fishing Ascendant", Quest.QuestType.FISHING, "fishing", 150, Quest.QuestCategory.WEEKLY, QuestReward(2250, 115)))
		register(Quest("wf2", "Fishing Grandmaster", Quest.QuestType.FISHING, "fishing", 350, Quest.QuestCategory.WEEKLY, QuestReward(4750, 150)))
		register(Quest("wf3", "Fishing Deity", Quest.QuestType.FISHING, "fishing", 750, Quest.QuestCategory.WEEKLY, QuestReward(6800, 175)))
	}

	val allForWeekly = QUESTS.values.filter { it.questType == Quest.QuestCategory.WEEKLY }
	val allForDaily = QUESTS.values.filter { it.questType == Quest.QuestCategory.DAILY }

	private fun register(quest: Quest) { QUESTS[quest.id] = quest }
	fun get(id: String): Quest? = QUESTS[id]
	fun getAllForNpc(npcId: String): List<Quest?> = QUESTS.values.filter { q -> q.npcId.map { it == npcId }.orElse(false)!! }
}