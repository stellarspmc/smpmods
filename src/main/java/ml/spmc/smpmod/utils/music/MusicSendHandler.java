package ml.spmc.smpmod.utils.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.jetbrains.annotations.Nullable;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class MusicSendHandler implements AudioSendHandler {

    private final AudioPlayer player;
    private final ByteBuffer buffer;
    private final MutableAudioFrame frame;

    public MusicSendHandler(AudioPlayer player) {
        this.player = player;
        this.buffer = ByteBuffer.allocate(1024);
        this.frame = new MutableAudioFrame();
        this.frame.setBuffer(buffer);
    }

    @Override
    public boolean canProvide() {
        return this.player.provide(this.frame);
    }

    @Nullable
    @Override
    public ByteBuffer provide20MsAudio() {
        final Buffer buffer = ((Buffer) this.buffer).flip();
        return (ByteBuffer) buffer;
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}
