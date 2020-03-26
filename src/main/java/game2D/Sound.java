package game2D;

import javax.sound.sampled.*;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Sound {
    private static ExecutorService playExecutor = Executors.newFixedThreadPool(4);

    public boolean finished;
    private Clip clip;

    public Sound(String path) {
        try {
            InputStream s = Util.readResource(path);
            AudioInputStream stream = AudioSystem.getAudioInputStream(s);
            AudioFormat format = stream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(stream);
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        finished = true;
    }

    public Sound(FilterInputStream filter) {
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(filter);
            AudioFormat format = stream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(stream);
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        finished = true;
    }

    public void play() {
        if (finished)
            playExecutor.submit(this::doPlay);
    }

    private void doPlay() {
        finished = false;
        clip.start();
        try {
            while (clip.getMicrosecondPosition() < clip.getMicrosecondLength()) {
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        clip.stop();
        clip.setFramePosition(0);
        finished = true;
    }
}