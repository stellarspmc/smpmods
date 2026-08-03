package fun.spmc.smpmod.quest;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class QuestManager extends SavedData {
    private final Map<UUID, List<ActiveQuest>> playerQuests;

    public static final Codec<QuestManager> CODEC = Codec.unboundedMap(
            Codec.STRING.xmap(UUID::fromString, UUID::toString),
            ActiveQuest.CODEC.listOf()
    ).xmap(QuestManager::new, manager -> manager.playerQuests);

    public QuestManager(Map<UUID, List<ActiveQuest>> playerQuests) { this.playerQuests = new HashMap<>(playerQuests); }

    public QuestManager() { this(new HashMap<>()); }

    public List<ActiveQuest> getQuests(ServerPlayer player) {
        return playerQuests.getOrDefault(player.getUUID(), Collections.emptyList());
    }

    public void addQuestToPlayer(ServerPlayer player, Quest quest) {
        List<ActiveQuest> quests = playerQuests.computeIfAbsent(player.getUUID(), k -> new ArrayList<>());
        quests.add(new ActiveQuest(quest, 0, false));
        setDirty();
    }
}
