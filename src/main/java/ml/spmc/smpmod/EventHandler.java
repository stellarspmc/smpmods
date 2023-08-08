package ml.spmc.smpmod;

import ml.spmc.smpmod.utils.UtilClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.Instant;
import java.util.Objects;

import static ml.spmc.smpmod.SMPMod.*;

public class EventHandler extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (e.getChannel() != messageChannel || e.getAuthor().isBot() || minecraftServer == null) return;
        if (e.getMessage().getAttachments().isEmpty() && e.getMessage().getStickers().isEmpty()) UtilClass.broadcastMessage(e.getAuthor().getName(), e.getMessage().getContentStripped());
        else if (!e.getMessage().getStickers().isEmpty() && e.getMessage().getAttachments().isEmpty()) UtilClass.broadcastMessage(e.getAuthor().getName(), "<sticker>");
        else UtilClass.broadcastMessage(e.getAuthor().getName(), "<attachment>");
    }

    @Override
    public void onGuildReady(@Nullable GuildReadyEvent e) {
        assert e != null;
        e.getGuild().updateCommands().addCommands(
                Commands.slash("ip", "Gets server's ip."),
                Commands.slash("info", "SMP's info."),
                Commands.slash("role", "Get roles."),
                Commands.slash("idea", "Suggest ideas."),
                Commands.slash("mod", "Suggest mods.")//,
                //Commands.slash("createroom", "Create a room just for you, private.")
        ).queue();
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        switch (e.getName()) {
            case "ip" -> e.reply("Java Server / Bedrock Server IP: mc.spmc.tk").queue();
            case "info" -> e.reply("Click the buttons for more info")
                    .addActionRow(
                            Button.link("https://discord.com/channels/964789575669137470/964798710070509598/998211745187377253", "SMP Info"),
                            Button.link("https://discord.com/channels/964789575669137470/964798710070509598/998209910867234926", "SMP Player Names"),
                            Button.link("https://discord.com/channels/964789575669137470/964798710070509598/999681621152243793", "Current Bugs")
                    ).queue();
            case "idea" -> {
                TextInput menu = TextInput.create("for", "For", TextInputStyle.SHORT)
                        .setPlaceholder("The idea is for where. (Minecraft/Discord...)")
                        .build();
                TextInput body = TextInput.create("idea", "Idea", TextInputStyle.PARAGRAPH)
                        .setPlaceholder("Elaborate on the idea and explain why you want it in. (required)")
                        .setMinLength(30)
                        .setMaxLength(1000)
                        .build();
                Modal modal = Modal.create("idea", "Idea!")
                        .addActionRow(ActionRow.of(menu).getComponents())
                        .addActionRow(ActionRow.of(body).getComponents())
                        .build();
                e.replyModal(modal).queue();
            }
            case "mod" -> {
                TextInput subject = TextInput.create("mod", "Mod", TextInputStyle.SHORT)
                        .setPlaceholder("The mod you want into the server.")
                        .setMinLength(1)
                        .setMaxLength(200)
                        .build();
                TextInput body = TextInput.create("idea", "Idea", TextInputStyle.PARAGRAPH)
                        .setPlaceholder("Elaborate on the mod and explain why you want it in. (required)")
                        .setMinLength(30)
                        .setMaxLength(1000)
                        .build();
                Modal modal = Modal.create("mod", "Mod!")
                        .addActionRow(ActionRow.of(subject).getComponents())
                        .addActionRow(ActionRow.of(body).getComponents())
                        .build();
                e.replyModal(modal).queue();
            }
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        switch(event.getModalId()) {
            case "idea" -> {
                String for1 = Objects.requireNonNull(event.getValue("for")).getAsString();
                String idea = Objects.requireNonNull(event.getValue("idea")).getAsString();

                EmbedBuilder eb = new EmbedBuilder();
                eb.setAuthor(event.getUser().getName(), event.getUser().getEffectiveAvatarUrl());
                eb.setTitle("An idea!");
                eb.setColor(new Color(155, 160, 81));
                eb.addField("The idea is for:", for1, false);
                eb.addField("Idea:", idea, false);
                eb.setFooter("Provided by SMPBot.", "https://github.com/tcfplayz/images/blob/main/spmc.png?raw=true");
                idea(eb);

                event.reply("Thanks for your idea!").setEphemeral(true).queue();
            }
            case "mod" -> {
                String mod = Objects.requireNonNull(event.getValue("mod")).getAsString();
                String idea = Objects.requireNonNull(event.getValue("idea")).getAsString();

                EmbedBuilder eb = new EmbedBuilder();
                eb.setAuthor(event.getUser().getName(), event.getUser().getEffectiveAvatarUrl());
                eb.setTitle("A mod request!");
                eb.setColor(new Color(155, 160, 81));
                eb.addField("Mod:", mod, false);
                eb.addField("Reason of adding:", idea, false);
                eb.setFooter("Provided by SMPBot.", "https://github.com/tcfplayz/images/blob/main/spmc.png?raw=true");
                idea(eb);

                event.reply("Thanks for your mod request!").setEphemeral(true).queue();
            }
            case "link" -> event.reply("Unknown.").queue();
        }
    }

    private void idea(EmbedBuilder eb) {
        eb.setTimestamp(Instant.now());

        MessageCreateAction message1 = Objects.requireNonNull(bot.getNewsChannelById("1068520925068271687")).sendMessageEmbeds(eb.build());
        Message message = message1.complete();
        message.addReaction(Emoji.fromCustom("tell", 970358692648206477L, false)).queue();
        message.addReaction(Emoji.fromCustom("untell", 969424392947900496L, false)).queue();
    }
}