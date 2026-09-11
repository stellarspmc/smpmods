package spmc.smpmod.registry

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import spmc.smpmod.quest.Quest
import spmc.smpmod.quest.Quest.QuestReward
import spmc.smpmod.quest.QuestManager.Companion.getQuests
import java.util.*

object QuestRegistry {
	private val QUESTS: MutableMap<String, Quest> = mutableMapOf()

	fun init() {
		initDaily()
		initWeekly()

		PlayerBlockBreakEvents.AFTER.register { _, player, _, state, _ ->
			if (player.level().dimension().identifier().namespace != "minecraft") return@register
			getQuests(player as ServerPlayer).activeQuests.forEach { activeQuest -> val quest = activeQuest.getQuest() ?: return@register
				if (quest.type == Quest.QuestType.MINE_BLOCK && quest.target == BuiltInRegistries.BLOCK.getKey(state.block)) activeQuest.increment(1)
			}
		}

		ServerLivingEntityEvents.AFTER_DEATH.register { entity, damageSource ->
			if (entity.level().dimension().identifier().namespace != "minecraft") return@register
			getQuests(damageSource.entity as? ServerPlayer?: return@register).activeQuests.forEach { activeQuest -> val quest = activeQuest.getQuest() ?: return@register
				if (quest.type == Quest.QuestType.KILL_MOB && quest.target == BuiltInRegistries.ENTITY_TYPE.getKey(entity.type)) activeQuest.increment(1)
			}
		}
	}

	private fun initDaily() {
		register(Quest("daily_mine_iron", "Iron Miner", "Mine 16 Iron Ore", Quest.QuestType.MINE_BLOCK, Identifier.withDefaultNamespace("iron_ore"), 16, Quest.QuestCategory.DAILY, QuestReward(3575.0, 75, mutableListOf()), Optional.empty(), Optional.empty()))
		register(Quest("daily_mine_stone", "Stone Miner", "Mine 64 Stone", Quest.QuestType.MINE_BLOCK, Identifier.withDefaultNamespace("stone"), 64, Quest.QuestCategory.DAILY, QuestReward(2500.0, 80, mutableListOf()), Optional.empty(), Optional.empty()))
		register(Quest("daily_kill_zombie", "Zombie Killer", "Kill 15 Zombies", Quest.QuestType.KILL_MOB, Identifier.withDefaultNamespace("zombie"), 15, Quest.QuestCategory.DAILY, QuestReward(4555.0, 120, mutableListOf()), Optional.empty(), Optional.empty()))
		register(Quest("daily_kill_creeper", "Creeper Killer", "Kill 15 Creepers", Quest.QuestType.KILL_MOB, Identifier.withDefaultNamespace("creeper"), 15, Quest.QuestCategory.DAILY, QuestReward(4555.0, 120, mutableListOf()), Optional.empty(), Optional.empty()))
		register(Quest("daily_kill_skeleton", "Skeleton Killer", "Kill 15 Skeletons", Quest.QuestType.KILL_MOB, Identifier.withDefaultNamespace("skeleton"), 15, Quest.QuestCategory.DAILY, QuestReward(4555.0, 120, mutableListOf()), Optional.empty(), Optional.empty()))
		register(Quest("daily_fish_1", "Fishing Newbie", "Fish 15 Times", Quest.QuestType.FISHING, Identifier.withDefaultNamespace("fishing"), 15, Quest.QuestCategory.DAILY, QuestReward(1550.0, 45, mutableListOf()), Optional.empty(), Optional.empty()))
		register(Quest("daily_fish_2", "Fishing Amateur", "Fish 35 Times", Quest.QuestType.FISHING, Identifier.withDefaultNamespace("fishing"), 35, Quest.QuestCategory.DAILY, QuestReward(3750.0, 65, mutableListOf()), Optional.empty(), Optional.empty()))
		register(Quest("daily_fish_3", "Fishing Master", "Fish 75 Times", Quest.QuestType.FISHING, Identifier.withDefaultNamespace("fishing"), 75, Quest.QuestCategory.DAILY, QuestReward(5900.0, 85, mutableListOf()), Optional.empty(), Optional.empty()))
	}

	private fun initWeekly() {
		/*register(Quest(
                "weekly_fish_1", "Fishing Master", "Fish 125 Times",
                Quest.QuestType.FISHING, Identifier.withDefaultNamespace("fishing"), 125, Quest.QuestCategory.WEEKLY,
                Quest.QuestReward(5900, 85, List.of()), Optional.empty(), Optional.empty()
        ));*/
	}

	val allForWeekly = QUESTS.values.filter { q -> q.questType == Quest.QuestCategory.WEEKLY }
	val allForDaily = QUESTS.values.filter { q -> q.questType == Quest.QuestCategory.DAILY }

	private fun register(quest: Quest) { QUESTS[quest.id] = quest }
	fun get(id: String): Quest? = QUESTS[id]
	fun getAllForNpc(npcId: String): List<Quest?> = QUESTS.values.filter { q -> q.npcId.map { id -> id == npcId }.orElse(false)!! }
}