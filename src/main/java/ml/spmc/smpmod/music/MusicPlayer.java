package ml.spmc.smpmod.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import ml.spmc.smpmod.SMPMod;
import ml.spmc.smpmod.utils.ConfigLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.List;

public class MusicPlayer {

    private static final AudioPlayerManager manager = new DefaultAudioPlayerManager();
    private static final MusicManager musicManager = new MusicManager(manager);

    public static void playMusic() {
        final VoiceChannel channel = SMPMod.bot.getVoiceChannelById(ConfigLoader.MUSIC_CHANNEL_ID);
        final Guild guild = SMPMod.bot.getGuildById(ConfigLoader.GUILD_ID);
        assert guild != null;
        final AudioManager manager2 = guild.getAudioManager();
        AudioPlayer player = musicManager.player;
        manager2.setSendingHandler(musicManager.getSendHandler());
        if (!manager2.isConnected()) {
            manager2.openAudioConnection(channel);
            AudioSourceManagers.registerRemoteSources(manager);
            AudioSourceManagers.registerLocalSource(manager);
            manager2.setSelfDeafened(true);
            player.setVolume(50);
        }
        if (player.isPaused()) player.setPaused(false);
        if (player.getVolume() == 0) player.setVolume(50);
        loadPlaylist();
    }

    public static void loadPlaylist() {
        final String playlist1 = "https://youtube.com/playlist?list=PLy_S3qOMUL1epiuCU4kBTOpLo1xOFJSLx";
        manager.loadItem(playlist1, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                List<AudioTrack> tracks = playlist.getTracks();
                for (AudioTrack track: tracks) {
                    musicManager.scheduler.queue(track);
                } musicManager.scheduler.shufflePlaylist();
            }

            @Override
            public void noMatches() {
            }

            @Override
            public void loadFailed(FriendlyException exception) {
            }
        });
    }

}