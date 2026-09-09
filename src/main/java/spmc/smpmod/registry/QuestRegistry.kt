package spmc.smpmod.registry

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AfterDeath
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import spmc.smpmod.quest.PlayerQuestData.ActiveQuest
import spmc.smpmod.quest.Quest
import spmc.smpmod.quest.Quest.QuestReward
import spmc.smpmod.quest.QuestManager.Companion.getQuests
import java.util.*

object QuestRegistry {
	private val QUESTS: MutableMap<String, Quest> = mutableMapOf()

	fun init() {
		initDaily()
		initWeekly()

		// demo npc quests
		/**
		 * register(new Quest(
		 * "blacksmith_1", "Gathering Supplies", "Bring 10 Coal to the Blacksmith",
		 * QuestCategory.NPC,
		 * QuestType.GATHER_ITEM,
		 * Identifier.withDefaultNamespace("coal"),
		 * 10,
		 * new QuestReward(50.0, 20, List.of(new ItemStack(Items.IRON_INGOT, 3))),
		 * Optional.of("blacksmith"),
		 * Optional.empty()
		 * ));
		 * 
		 * register(new Quest(
		 * "blacksmith_2", "First Blade", "Craft an Iron Sword",
		 * QuestCategory.NPC,
		 * QuestType.CRAFTING,
		 * Identifier.withDefaultNamespace("iron_sword"),
		 * 1,
		 * new QuestReward(200.0, 100, List.of(new ItemStack(Items.DIAMOND, 1))),
		 * Optional.of("blacksmith"),
		 * Optional.of("blacksmith_1")
		 * )); */

		PlayerBlockBreakEvents.AFTER.register { _, player, _, state, _ ->
			getQuests(player as ServerPlayer).activeQuests.forEach { activeQuest -> val quest = activeQuest.getQuest() ?: return@register
				if (quest.type == Quest.QuestType.MINE_BLOCK && quest.target == BuiltInRegistries.BLOCK.getKey(state.block)) activeQuest.increment(1)
			}
		}

		ServerLivingEntityEvents.AFTER_DEATH.register { entity, damageSource ->
			getQuests(damageSource.entity as ServerPlayer).activeQuests.forEach { activeQuest -> val quest = activeQuest.getQuest() ?: return@register
				if (quest.type == Quest.QuestType.KILL_MOB && quest.target == BuiltInRegistries.ENTITY_TYPE.getKey(entity.type)) activeQuest.increment(1)
			}
		}
	}

	private fun initDaily() {
		register(Quest("daily_mine_iron", "Iron Miner", "Mine 16 Iron Ore", Quest.QuestType.MINE_BLOCK, Identifier.withDefaultNamespace("iron_ore"), 16, Quest.QuestCategory.DAILY, QuestReward(3575.0, 75, mutableListOf<ItemStack?>()), Optional.empty<String?>(), Optional.empty<String?>()))
		register(Quest("daily_mine_stone", "Stone Miner", "Mine 64 Stone", Quest.QuestType.MINE_BLOCK, Identifier.withDefaultNamespace("stone"), 64, Quest.QuestCategory.DAILY, QuestReward(2500.0, 80, mutableListOf<ItemStack?>()), Optional.empty<String?>(), Optional.empty<String?>()))
		register(Quest("daily_kill_zombie", "Zombie Killer", "Kill 15 Zombies", Quest.QuestType.KILL_MOB, Identifier.withDefaultNamespace("zombie"), 15, Quest.QuestCategory.DAILY, QuestReward(4555.0, 120, mutableListOf<ItemStack?>()), Optional.empty<String?>(), Optional.empty<String?>()))
		register(Quest("daily_kill_creeper", "Creeper Killer", "Kill 15 Creepers", Quest.QuestType.KILL_MOB, Identifier.withDefaultNamespace("creeper"), 15, Quest.QuestCategory.DAILY, QuestReward(4555.0, 120, mutableListOf<ItemStack?>()), Optional.empty<String?>(), Optional.empty<String?>()))
		register(Quest("daily_kill_skeleton", "Skeleton Killer", "Kill 15 Skeletons", Quest.QuestType.KILL_MOB, Identifier.withDefaultNamespace("skeleton"), 15, Quest.QuestCategory.DAILY, QuestReward(4555.0, 120, mutableListOf<ItemStack?>()), Optional.empty<String?>(), Optional.empty<String?>()))
		register(Quest("daily_fish_1", "Fishing Newbie", "Fish 15 Times", Quest.QuestType.FISHING, Identifier.withDefaultNamespace("fishing"), 15, Quest.QuestCategory.DAILY, QuestReward(1550.0, 45, mutableListOf()), Optional.empty<String?>(), Optional.empty<String?>()))
		register(Quest("daily_fish_2", "Fishing Amateur", "Fish 35 Times", Quest.QuestType.FISHING, Identifier.withDefaultNamespace("fishing"), 35, Quest.QuestCategory.DAILY, QuestReward(3750.0, 65, mutableListOf<ItemStack>()), Optional.empty<String?>(), Optional.empty<String?>()))
		register(Quest("daily_fish_3", "Fishing Master", "Fish 75 Times", Quest.QuestType.FISHING, Identifier.withDefaultNamespace("fishing"), 75, Quest.QuestCategory.DAILY, QuestReward(5900.0, 85, mutableListOf<ItemStack>()), Optional.empty<String?>(), Optional.empty<String?>()))
	}

	private fun initWeekly() {/*register(new Quest(
                "weekly_fish_1", "Fishing Master", "Fish 125 Times",
                Quest.QuestType.FISHING, Identifier.withDefaultNamespace("fishing"), 125, Quest.QuestCategory.WEEKLY,
                new Quest.QuestReward(5900, 85, List.of()), Optional.empty(), Optional.empty()
        ));*/
	}

	val allForWeekly = QUESTS.values.filter { q -> q.questType == Quest.QuestCategory.WEEKLY }
	val allForDaily = QUESTS.values.filter { q -> q.questType == Quest.QuestCategory.DAILY }

	private fun register(quest: Quest) { QUESTS[quest.id] = quest }
	fun get(id: String): Quest? = QUESTS[id]
	fun getAllForNpc(npcId: String): List<Quest?> = QUESTS.values.filter { q -> q.npcId.map { id -> id == npcId }.orElse(false)!! }
}