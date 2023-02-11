package ml.spmc.smpmod.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import ml.spmc.smpmod.SMPMod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static ml.spmc.smpmod.music.FinalMusicPlayer.loadPlaylist;


public class TrackScheduler extends AudioEventAdapter {

    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    AudioTrack lastTrack;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void queue(AudioTrack track) {
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    public void nextTrack() {
       player.startTrack(queue.poll(), false);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        this.lastTrack = track;
        if (endReason.mayStartNext) {
            SMPMod.LOGGER.info(String.valueOf(queue.toArray()));
            if (queue.size() == 0) loadPlaylist();
            else if (lastTrack == queue.toArray()[queue.size() - 1]) loadPlaylist();
            else nextTrack();
        }
    }

    public void shufflePlaylist() {
        final List<AudioTrack> tempList = new ArrayList<>(this.queue);
        Collections.shuffle(tempList);
        this.queue.clear();
        this.queue.addAll(tempList);
    }
}
