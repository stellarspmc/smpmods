package fun.spmc.smpmod.quest;

import net.minecraft.resources.Identifier;

public record Quest(
        String id, String title,
        QuestType type,
        Identifier targetIdentifier,
        int requiredCount, double rewardMoney,
        boolean isWeekly
) {}