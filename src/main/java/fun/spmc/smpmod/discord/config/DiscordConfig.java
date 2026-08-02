package fun.spmc.smpmod.discord.config;

public record DiscordConfig(String webhook, String token, String messageChannelId, String guildId) {
    public DiscordConfig {
        if (webhook == null) webhook = "";
        if (token == null) token = "";
        if (messageChannelId == null) messageChannelId = "";
        if (guildId == null) guildId = "";
    }

    public DiscordConfig() {
        this("", "", "", "");
    }
}