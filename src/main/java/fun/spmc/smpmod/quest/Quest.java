package fun.spmc.smpmod.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record Quest(
        String id, String title,
        QuestType type,
        Identifier target,
        int requiredCount, double rewardMoney,
        boolean isWeekly
) {
    public static final Codec<Quest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(Quest::id),
            Codec.STRING.fieldOf("title").forGetter(Quest::title),
            QuestType.CODEC.fieldOf("type").forGetter(Quest::type),
            Identifier.CODEC.fieldOf("target").forGetter(Quest::target),
            Codec.INT.fieldOf("required_count").forGetter(Quest::requiredCount),
            Codec.DOUBLE.fieldOf("reward_money").forGetter(Quest::rewardMoney),
            Codec.BOOL.fieldOf("weekly").forGetter(Quest::isWeekly)
    ).apply(instance, Quest::new));
}