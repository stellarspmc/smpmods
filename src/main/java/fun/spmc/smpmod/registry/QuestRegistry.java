package fun.spmc.smpmod.registry;

import fun.spmc.smpmod.quest.QuestManager;
import fun.spmc.smpmod.quest.data.Quest;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class QuestRegistry {
    private static final Map<String, Quest> QUESTS = new HashMap<>();

    public static void init() {
        initDaily();
        initWeekly();

        // demo npc quests
        /**
        register(new Quest(
                "blacksmith_1", "Gathering Supplies", "Bring 10 Coal to the Blacksmith",
                QuestCategory.NPC,
                QuestType.GATHER_ITEM,
                Identifier.withDefaultNamespace("coal"),
                10,
                new QuestReward(50.0, 20, List.of(new ItemStack(Items.IRON_INGOT, 3))),
                Optional.of("blacksmith"),
                Optional.empty()
        ));

        register(new Quest(
                "blacksmith_2", "First Blade", "Craft an Iron Sword",
                QuestCategory.NPC,
                QuestType.CRAFTING,
                Identifier.withDefaultNamespace("iron_sword"),
                1,
                new QuestReward(200.0, 100, List.of(new ItemStack(Items.DIAMOND, 1))),
                Optional.of("blacksmith"),
                Optional.of("blacksmith_1")
        ));*/

        PlayerBlockBreakEvents.AFTER.register((_, player, _, state, _) -> QuestManager.getQuests((ServerPlayer) player).getActiveQuests().forEach(activeQuest -> {
            Quest quest = activeQuest.getQuest();
            if (quest.type() == Quest.QuestType.MINE_BLOCK && quest.target().equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()))) activeQuest.increment(1);
        }));

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (damageSource.getEntity() instanceof ServerPlayer player) {
                Identifier mobId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());

                QuestManager.getQuests(player).getActiveQuests().forEach(activeQuest -> {
                    Quest quest = activeQuest.getQuest();
                    if (quest.type() == Quest.QuestType.KILL_MOB && quest.target().equals(mobId)) {
                        activeQuest.increment(1);
                    }
                });
            }
        });
    }

    private static void initDaily() {
        register(new Quest(
                "daily_mine_iron", "Iron Miner", "Mine 16 Iron Ore",
                Quest.QuestType.MINE_BLOCK, Identifier.withDefaultNamespace("iron_ore"), 16, Quest.QuestCategory.DAILY,
                new Quest.QuestReward(3575, 75, List.of()), Optional.empty(), Optional.empty()
        ));

        register(new Quest(
                "daily_mine_stone", "Stone Miner", "Mine 64 Stone",
                Quest.QuestType.MINE_BLOCK, Identifier.withDefaultNamespace("stone"), 64, Quest.QuestCategory.DAILY,
                new Quest.QuestReward(2500, 80, List.of()), Optional.empty(), Optional.empty()
        ));

        register(new Quest(
                "daily_kill_zombie", "Zombie Killer", "Kill 15 Zombies",
                Quest.QuestType.KILL_MOB, Identifier.withDefaultNamespace("zombie"), 15, Quest.QuestCategory.DAILY,
                new Quest.QuestReward(4555, 120, List.of()), Optional.empty(), Optional.empty()
        ));

        register(new Quest(
                "daily_kill_creeper", "Creeper Killer", "Kill 15 Creepers",
                Quest.QuestType.KILL_MOB, Identifier.withDefaultNamespace("creeper"), 15, Quest.QuestCategory.DAILY,
                new Quest.QuestReward(4555, 120, List.of()), Optional.empty(), Optional.empty()
        ));

        register(new Quest(
                "daily_kill_skeleton", "Skeleton Killer", "Kill 15 Skeletons",
                Quest.QuestType.KILL_MOB, Identifier.withDefaultNamespace("skeleton"), 15, Quest.QuestCategory.DAILY,
                new Quest.QuestReward(4555, 120, List.of()), Optional.empty(), Optional.empty()
        ));

        register(new Quest(
                "daily_fish_1", "Fishing Newbie", "Fish 15 Times",
                Quest.QuestType.FISHING, Identifier.withDefaultNamespace("fishing"), 15, Quest.QuestCategory.DAILY,
                new Quest.QuestReward(1550, 45, List.of()), Optional.empty(), Optional.empty()
        ));

        register(new Quest(
                "daily_fish_2", "Fishing Amateur", "Fish 35 Times",
                Quest.QuestType.FISHING, Identifier.withDefaultNamespace("fishing"), 35, Quest.QuestCategory.DAILY,
                new Quest.QuestReward(3750, 65, List.of()), Optional.empty(), Optional.empty()
        ));

        register(new Quest(
                "daily_fish_3", "Fishing Master", "Fish 75 Times",
                Quest.QuestType.FISHING, Identifier.withDefaultNamespace("fishing"), 75, Quest.QuestCategory.DAILY,
                new Quest.QuestReward(5900, 85, List.of()), Optional.empty(), Optional.empty()
        ));
    }

    private static void initWeekly() {
        register(new Quest(
                "daily_fish_3", "Fishing Master", "Fish 75 Times",
                Quest.QuestType.FISHING, Identifier.withDefaultNamespace("fishing"), 75, Quest.QuestCategory.DAILY,
                new Quest.QuestReward(5900, 85, List.of()), Optional.empty(), Optional.empty()
        ));
    }

    private static void register(Quest quest) { QUESTS.put(quest.id(), quest); }
    public static Quest get(String id) { return QUESTS.get(id); }
    public static List<Quest> getAllForWeekly() { return QUESTS.values().stream().filter(q -> q.questType().equals(Quest.QuestCategory.WEEKLY)).toList(); }
    public static List<Quest> getAllForDaily() { return QUESTS.values().stream().filter(q -> q.questType().equals(Quest.QuestCategory.DAILY)).toList(); }
    public static List<Quest> getAllForNpc(String npcId) { return QUESTS.values().stream().filter(q -> q.npcId().map(id -> id.equals(npcId)).orElse(false)).toList(); }
}