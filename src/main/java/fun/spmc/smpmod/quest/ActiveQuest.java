package fun.spmc.smpmod.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ActiveQuest(
        Quest quest,
        int currentCount,
        boolean completed
) {
    public static final Codec<ActiveQuest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Quest.CODEC.fieldOf("quest_id").forGetter(ActiveQuest::quest),
            Codec.INT.fieldOf("current_count").forGetter(ActiveQuest::currentCount),
            Codec.BOOL.fieldOf("completed").forGetter(ActiveQuest::completed)
    ).apply(instance, ActiveQuest::new));
}
