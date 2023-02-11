package ml.spmc.smpmod.utils;

import ml.spmc.smpmod.SMPMod;
import ml.spmc.smpmod.minecraft.events.RunEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import static ml.spmc.smpmod.SMPMod.*;

public class EventHandler extends ListenerAdapter {

    public void init() {
        ServerChatCallback.EVENT.register((playerEntity, rawMessage) -> {
            SMPMod.sendWebhookMessage(MarkdownParser.parseMarkdown(rawMessage), playerEntity.getName().getString(), playerEntity.getStringUUID());
            return Optional.empty();
        });

        PlayerDeathCallback.EVENT.register((playerEntity, text) -> {
            MESSAGECHANNEL.sendMessage(text.getString()).queue();
            Random random = new Random();
            if (random.nextDouble() > 0.9) UtilClass.getDatabaseManager().changeBalance(playerEntity.getName().getString(), random.nextDouble());
        });

        PlayerJoinCallback.EVENT.register((connection, playerEntity) -> {
            MESSAGECHANNEL.sendMessage(MarkdownSanitizer.escape(playerEntity.getName().getString()) + " has joined the server").queue();
            /*
            TODO: rewrite
            if (UtilClass.getDatabaseManager().getLastLeft(playerEntity.getStringUUID()) <= 1661249400000L) {
                playerEntity.sendSystemMessage(Component.literal("SMP 1.4.6: music"));
            }*/
        });

        ServerTickCallback.EVENT.register(() -> {
            Random random = new Random();
            if (SERVER == null) return;
            if (random.nextDouble() > 0.9985) SERVER.getPlayerList().broadcastSystemMessage(java.awt.Component.literal("Join our discord? https://discord.spmc.ml"), ChatType.SYSTEM);
            else if (random.nextDouble() > 0.999992) RunEvent.pickEvent();
            // getLevel
            AtomicReference<ServerLevel> lvl = new AtomicReference<>();
            SERVER.getAllLevels().forEach((level) -> {
                if (level.dimensionType().equals(Level.OVERWORLD)) lvl.set(level);
            });
            if (SERVER.getPlayerList().getPlayers().isEmpty()) lvl.get().setDayTime(lvl.get().dayTime());
                if (!lvl.get().isDay()) {
                // custom nights
                if (random.nextDouble() > 0.9) {
                    // blood moon
                } else if (random.nextDouble() > 0.83) {
                    // typhoon moon
                } else {
                    // normal moon / anything else
                }
            }
        });

        PlayerLeaveCallback.EVENT.register((playerEntity) -> MESSAGECHANNEL.sendMessage(MarkdownSanitizer.escape(playerEntity.getName().getString()) + " has left the server").queue());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (SERVER == null) return;
        if (e.getChannel() != MESSAGECHANNEL) return;
        if (e.getAuthor().isBot()) return;
        LOGGER.info(("<" + "[discord] " + e.getAuthor().getName() + "> " + e.getMessage().getContentRaw()));
        SERVER.getPlayerList().getPlayers().forEach(player -> player.sendSystemMessage(Component.nullToEmpty(("<" + "[discord] " + e.getAuthor().getName() + "> " + e.getMessage().getContentRaw()))));
    }

    @Override
    public void onGuildReady(@Nullable GuildReadyEvent e) {
        assert e != null;
        e.getGuild().updateCommands().addCommands(
                Commands.slash("ip", "Gets server's ip."),
                Commands.slash("info", "SMP's info."),
                Commands.slash("role", "Get roles.")
        );
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        JDA.updateCommands().addCommands(
                Commands.slash("appeal", "Appeal?")
        );
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        switch (e.getName()) {
            case "ip":
                e.reply("Java Server / Bedrock Server IP: mc.spmc.ml").queue();
                break;
            case "info":
                e.reply("Click the buttons for more info")
                        .addActionRow(
                                Button.link("https://discord.com/channels/964789575669137470/964798710070509598/998211745187377253", "SMP Info"),
                                Button.link("https://discord.com/channels/964789575669137470/964798710070509598/998209910867234926", "SMP Player Names"),
                                Button.link("https://discord.com/channels/964789575669137470/964798710070509598/999681621152243793", "Current Bugs")
                        ).queue();
                break;
            case "appeal":
                SelectMenu menu = SelectMenu.create("from")
                        .setPlaceholder("Where were you warned/ etc.") // shows the placeholder indicating what this menu is for
                        .setRequiredRange(1, 1)
                        .addOption("Discord", "discord")
                        .addOption("Minecraft", "minecraft")
                        .build();

                TextInput subject = TextInput.create("subject", "Title", TextInputStyle.SHORT)
                        .setPlaceholder("The reason you're warned / etc.")
                        .setMinLength(10)
                        .setMaxLength(100)
                        .build();

                TextInput body = TextInput.create("body", "Reason", TextInputStyle.PARAGRAPH)
                        .setPlaceholder("Why do you want to appeal?")
                        .setMinLength(30)
                        .setMaxLength(1000)
                        .build();

                Modal modal = Modal.create("appeal", "Appeal")
                        .addActionRows(ActionRow.of(menu), ActionRow.of(subject), ActionRow.of(body))
                        .build();

                e.replyModal(modal).queue();
                break;
            case "role":
                e.reply("Get role/s!").addActionRow(
                        SelectMenu.create("rank")
                        .addOption("Discord Pings", "discord", "Discord Server Pings")
                        .addOption("SMP Pings", "minecraft", "SMP Notice Pings")
                        .build()
                ).queue();
                break;
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (event.getModalId().equals("appeal")) {
            String subject = event.getValue("subject").getAsString();
            String body = event.getValue("body").getAsString();
            String from = event.getValue("from").getAsString();

            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle(subject + " from " + event.getUser().getName() + "#" + event.getUser().getDiscriminator(), null);
            eb.setColor(new Color(155, 160, 81));
            eb.addField("Warned From", from, false);
            eb.addField("Reason of Appeal", body, false);

            TextChannel channel = JDA.getTextChannelById(ConfigJava.APPEALCHANNEL);
            channel.sendMessageEmbeds(eb.build()).queue();

            event.reply("Thanks for your appeal request!").setEphemeral(true).queue();
        }
    }

    @Override
    public void onSelectMenuInteraction(SelectMenuInteractionEvent event) {
        if (event.getComponentId().equals("rank")) {
            Member member = event.getMember();
            switch (event.getValues().get(0)) {
                case "minecraft":
                    member.getGuild().addRoleToMember(member.getUser(), JDA.getRoleById(ConfigJava.ROLES.get(0))).queue();
                    break;
                case "discord":
                    member.getGuild().addRoleToMember(member.getUser(), JDA.getRoleById(ConfigJava.ROLES.get(1))).queue();
                    break;
            }
        }
    }
}
