package fun.spmc.smpmod.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fun.spmc.smpmod.quest.QuestManager;
import fun.spmc.smpmod.quest.data.PlayerQuestData;
import fun.spmc.smpmod.quest.data.Quest;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class QuestCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> buildQuest() {
        return Commands.literal("quests")
                .executes(QuestCommand::listQuests);
    }

    private static int listQuests(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        QuestManager.get().checkAndResetRotations(player);

        List<PlayerQuestData.ActiveQuest> activeQuests = QuestManager.getQuests(player).getActiveQuests();

        player.sendSystemMessage(Component.literal("=== Active Quests ===")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        if (activeQuests.isEmpty()) {
            player.sendSystemMessage(Component.literal(" You have no active quests.")
                    .withStyle(ChatFormatting.GRAY));
            return 1;
        }

        for (PlayerQuestData.ActiveQuest activeQuest : activeQuests) {
            Quest quest = activeQuest.getQuest();
            if (quest == null) continue;

            ChatFormatting categoryColor = switch (quest.questType()) {
                case DAILY -> ChatFormatting.YELLOW;
                case WEEKLY -> ChatFormatting.LIGHT_PURPLE;
                case NPC -> ChatFormatting.AQUA;
            };

            MutableComponent questLine = Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal("[" + quest.questType().getSerializedName().toUpperCase() + "] ").withStyle(categoryColor, ChatFormatting.BOLD))
                    .append(Component.literal(quest.title()).withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY));

            if (activeQuest.isCompleted()) {
                if (activeQuest.isClaimed()) questLine.append(Component.literal("Completed").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC));
                    else questLine.append(Component.literal("READY TO CLAIM!").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
            } else questLine.append(Component.literal(activeQuest.getCurrentCount() + "/" + quest.requiredCount()).withStyle(ChatFormatting.AQUA));
            player.sendSystemMessage(questLine);
            if (!quest.description().isEmpty()) player.sendSystemMessage(Component.literal("   " + quest.description()).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }

        return 1;
    }
}