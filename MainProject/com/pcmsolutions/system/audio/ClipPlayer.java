package com.pcmsolutions.system.audio;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 05-Dec-2003
 * Time: 04:55:08
 * To change this template use Options | File Templates.
 */
public class ClipPlayer implements LineListener {
    private Clip clip;

    public ClipPlayer(File clipFile) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(clipFile);
        AudioFormat format = audioInputStream.getFormat();
        DataLine.Info info = new DataLine.Info(Clip.class, format);
        clip = (Clip) AudioSystem.getLine(info);
        clip.addLineListener(this);
        clip.open(audioInputStream);
    }

    public Clip getClip() {
        return clip;
    }

    public void update(LineEvent event) {
        if (event.getType().equals(LineEvent.Type.STOP)) {
            clip.close();
        } else if (event.getType().equals(LineEvent.Type.CLOSE)) {
            System.exit(0);
        }
    }


    public static void main
            (String[] args) {
        if (args.length != 2) {
            out("ClipPlayer: usage:");
            out("\tjava ClipPlayer <soundfile> <#loops>");
        } else {
            File clipFile = new File(args[0]);
            int nLoopCount = Integer.parseInt(args[1]);
            try {
                final ClipPlayer clipPlayer;
                clipPlayer = new ClipPlayer(clipFile);
                clipPlayer.getClip().start();
                new Thread() {
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        clipPlayer.getClip().stop();
                    }
                }.start();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            } catch (UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
        }
    }

    private static void out
            (String
            strMessage) {
        System.out.println(strMessage);
    }
}
