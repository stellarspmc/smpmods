package ml.spmc.smpmod.utils.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import ml.spmc.smpmod.SMPMod;
import ml.spmc.smpmod.utils.ConfigLoader;

public class PlayerManager {

    private static PlayerManager INSTANCE;
    private final MusicManager guildmanager;
    private final AudioPlayerManager manager;

    public PlayerManager() {
        this.manager = new DefaultAudioPlayerManager();
        this.guildmanager = new MusicManager(this.manager);

        AudioSourceManagers.registerRemoteSources(this.manager);
        AudioSourceManagers.registerLocalSource(this.manager);
    }

    public MusicManager getGuildManager() {
        SMPMod.JDA.getGuildById(ConfigLoader.GUILD_ID).getAudioManager().setSendingHandler(guildmanager.getSendHandler());
        return guildmanager;
    }

}
