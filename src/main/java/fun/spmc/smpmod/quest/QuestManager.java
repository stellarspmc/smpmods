package fun.spmc.smpmod.quest;

import com.mojang.serialization.Codec;
import fun.spmc.smpmod.quest.data.PlayerQuestData;
import fun.spmc.smpmod.quest.data.Quest;
import fun.spmc.smpmod.registry.QuestRegistry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.time.LocalDate;
import java.util.*;

import static fun.spmc.smpmod.SMPMod.minecraftServer;

public class QuestManager extends SavedData {
    private final Map<UUID, PlayerQuestData> playerQuests;

    public static final Codec<QuestManager> CODEC = Codec.unboundedMap(UUIDUtil.CODEC, PlayerQuestData.CODEC).xmap(QuestManager::new, manager -> manager.playerQuests);

    public static final SavedDataType<QuestManager> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("smpmod", "questing"),
            QuestManager::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    public QuestManager(Map<UUID, PlayerQuestData> questData) { this.playerQuests = questData; }
    public QuestManager() { this(new HashMap<>()); }
    public static QuestManager get() { return minecraftServer.overworld().getDataStorage().computeIfAbsent(TYPE); }
    public static PlayerQuestData getQuests(ServerPlayer player) { return get().playerQuests.getOrDefault(player.getUUID(), new PlayerQuestData()); }

    public void checkAndResetRotations(ServerPlayer player) {
        PlayerQuestData data = getQuests(player);
        long currentDay = LocalDate.now().toEpochDay();
        long currentWeek = currentDay / 7;

        if (data.getLastDailyResetDay() < currentDay) {
            refreshQuestsForCategory(data, player, Quest.QuestCategory.DAILY, QuestRegistry.getAllForDaily());
            data.setLastDailyResetDay(currentDay);
            setDirty();
        } if (data.getLastWeeklyResetWeek() < currentWeek) {
            refreshQuestsForCategory(data, player, Quest.QuestCategory.WEEKLY, QuestRegistry.getAllForWeekly());
            data.setLastWeeklyResetWeek(currentWeek);
            setDirty();
        }
    }

    public List<Quest> getAvailableNpcQuests(ServerPlayer player, String npcId) {
        PlayerQuestData data = getQuests(player);

        return QuestRegistry.getAllForNpc(npcId).stream().filter(quest -> {
            if (data.getActiveQuests().stream().anyMatch(a -> a.getQuest().id().equals(quest.id())) || data.getCompletedQuestIds().contains(quest.id())) return false;
            return quest.preQuestId().map(data.getCompletedQuestIds()::contains).orElse(true);
        }).toList();
    }

    public void completeAndClaim(ServerPlayer player, PlayerQuestData.ActiveQuest activeQuest) {
        Quest quest = activeQuest.getQuest();
        if (quest == null || !activeQuest.isCompleted() || activeQuest.isClaimed()) return;

        PlayerQuestData data = getQuests(player);
        quest.questReward().grant(player);
        activeQuest.setClaimed(true);

        data.getCompletedQuestIds().add(quest.id());

        if (quest.isNpcQuest()) data.getActiveQuests().remove(activeQuest);
        setDirty();
    }

    private void refreshQuestsForCategory(PlayerQuestData data, ServerPlayer player, Quest.QuestCategory category, List<Quest> availablePool) {
        data.getActiveQuests().removeIf(q -> q.getQuest() != null && q.getQuest().questType() == category);
        if (availablePool.isEmpty()) return;

        List<Quest> pool = new ArrayList<>(availablePool);
        Collections.shuffle(pool, new Random(player.getRandom().nextLong()));
        for (int i = 0; i < Math.min(3, pool.size()); i++) data.addQuest(pool.get(i).id());
    }
}
