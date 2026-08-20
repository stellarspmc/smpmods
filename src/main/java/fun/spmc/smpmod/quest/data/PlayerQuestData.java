package fun.spmc.smpmod.quest.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fun.spmc.smpmod.registry.QuestRegistry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayerQuestData {
    private final List<ActiveQuest> activeQuests = new ArrayList<>();
    private final Set<String> completedQuestIds = new HashSet<>();
    private long lastDailyResetDay = 0;
    private long lastWeeklyResetWeek = 0;

    public static final Codec<PlayerQuestData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ActiveQuest.CODEC.listOf().fieldOf("active_quests").forGetter(p -> p.activeQuests),
            Codec.STRING.listOf().xmap(Set::copyOf, List::copyOf).fieldOf("completed_quests").forGetter(p -> p.completedQuestIds),
            Codec.LONG.optionalFieldOf("last_daily_reset", 0L).forGetter(p -> p.lastDailyResetDay),
            Codec.LONG.optionalFieldOf("last_weekly_reset", 0L).forGetter(p -> p.lastWeeklyResetWeek)
    ).apply(instance, (active, completed, daily, weekly) -> {
        PlayerQuestData data = new PlayerQuestData();
        data.activeQuests.addAll(active);
        data.completedQuestIds.addAll(completed);
        data.lastDailyResetDay = daily;
        data.lastWeeklyResetWeek = weekly;
        return data;
    }));

    public void addQuest(String quest) { activeQuests.add(new ActiveQuest(quest)); }
    public List<ActiveQuest> getActiveQuests() { return activeQuests; }
    public Set<String> getCompletedQuestIds() { return completedQuestIds; }
    public long getLastDailyResetDay() { return lastDailyResetDay; }
    public void setLastDailyResetDay(long day) { this.lastDailyResetDay = day; }
    public long getLastWeeklyResetWeek() { return lastWeeklyResetWeek; }
    public void setLastWeeklyResetWeek(long week) { this.lastWeeklyResetWeek = week; }

    public static class ActiveQuest {
        private final String quest;
        private int currentCount;
        private boolean completed;
        private boolean claimed;

        public static final Codec<ActiveQuest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("quest").forGetter(ActiveQuest::getQuestId),
                Codec.INT.fieldOf("current_count").forGetter(ActiveQuest::getCurrentCount),
                Codec.BOOL.fieldOf("completed").forGetter(ActiveQuest::isCompleted),
                Codec.BOOL.optionalFieldOf("claimed", false).forGetter(ActiveQuest::isClaimed)
        ).apply(instance, ActiveQuest::new));

        public ActiveQuest(String quest, int currentCount, boolean completed, boolean claimed) {
            this.quest = quest;
            this.currentCount = currentCount;
            this.completed = completed;
            this.claimed = claimed;
        }

        public ActiveQuest(String quest) {
            this(quest, 0, false, false);
        }

        public boolean increment(int amount) {
            if (this.completed) return false;

            this.currentCount += amount;
            if (this.currentCount >= QuestRegistry.get(this.quest).requiredCount()) {
                this.currentCount = QuestRegistry.get(this.quest).requiredCount();
                this.completed = true;
            }
            return this.completed;
        }

        public String getQuestId() { return quest; }
        public Quest getQuest() { return QuestRegistry.get(this.quest); }
        public int getCurrentCount() { return currentCount; }
        public boolean isCompleted() { return completed; }
        public boolean isClaimed() { return claimed; }
        public void setClaimed(boolean claimed) { this.claimed = claimed; }
    }
}