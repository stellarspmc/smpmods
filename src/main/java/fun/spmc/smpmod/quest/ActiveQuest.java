package fun.spmc.smpmod.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class ActiveQuest {
    private final Quest quest;
    private int currentCount;
    private boolean completed;

    // Codec works fine with regular classes!
    public static final Codec<ActiveQuest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Quest.CODEC.fieldOf("quest").forGetter(ActiveQuest::getQuest),
            Codec.INT.fieldOf("current_count").forGetter(ActiveQuest::getCurrentCount),
            Codec.BOOL.fieldOf("completed").forGetter(ActiveQuest::isCompleted)
    ).apply(instance, ActiveQuest::new));

    public ActiveQuest(Quest quest, int currentCount, boolean completed) {
        this.quest = quest;
        this.currentCount = currentCount;
        this.completed = completed;
    }

    public ActiveQuest(Quest quest) {
        this(quest, 0, false);
    }

    public boolean increment(int amount) {
        if (this.completed) return false;

        this.currentCount += amount;
        if (this.currentCount >= this.quest.requiredCount()) {
            this.currentCount = this.quest.requiredCount();
            this.completed = true;
        }
        return this.completed;
    }

    // Getters
    public Quest getQuest() { return quest; }
    public int getCurrentCount() { return currentCount; }
    public boolean isCompleted() { return completed; }
}