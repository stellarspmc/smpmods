package fun.spmc.smpmod.discord.config;

public record DiscordConfig(String webhook, String token, String messageChannelId) {
    public DiscordConfig {
        if (webhook == null) webhook = "";
        if (token == null) token = "";
        if (messageChannelId == null) messageChannelId = "";
    }

    public DiscordConfig() {
        this("", "", "");
    }
}