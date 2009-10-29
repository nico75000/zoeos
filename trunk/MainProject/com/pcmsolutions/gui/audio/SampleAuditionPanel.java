package com.pcmsolutions.gui.audio;

import org.tritonus.zuonics.sampled.aiff.AiffAudioFileReaderEx;
import org.tritonus.zuonics.sampled.wave.WaveAudioFileReaderEx;
import org.tritonus.zuonics.sampled.wave.WaveAudioFileReaderEx;
import org.tritonus.zuonics.sampled.aiff.AiffAudioFileReaderEx;
import com.pcmsolutions.gui.FlashMsg;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.tasking.ManageableTicketedQ;
import com.pcmsolutions.system.tasking.QueueFactory;
import com.pcmsolutions.system.tasking.TicketRunnable;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;
import com.pcmsolutions.system.audio.AudioUtilities;
import com.pcmsolutions.system.preferences.Impl_ZBoolPref;
import com.pcmsolutions.system.preferences.ZBoolPref;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 05-Dec-2003
 * Time: 03:37:19
 * To change this template use Options | File Templates.
 */
public class SampleAuditionPanel extends JPanel {

    private static final Preferences prefs = Preferences.userNodeForPackage(SampleAuditionPanel.class);
    public static final ZBoolPref ZPREF_autoAudition = new Impl_ZBoolPref(prefs, "autoAudition", true);
    public static final ZBoolPref ZPREF_loopAudition = new Impl_ZBoolPref(prefs, "loopAudition", false);
    //public static final ZBoolPref ZPREF_playWholeAudition = new Impl_ZBoolPref(sun, "playWholeAudition", true);
    //public static final ZIntPref ZPREF_auditionLength = new Impl_ZIntPref(sun, "auditionLength", 5);

    private File currentFile;
    private Clip currentClip;

    private JTextPane infoText = new JTextPane();

    {
        infoText.setEditable(false);
        infoText.setFont(new Font("MONOSPACED", Font.PLAIN, 11));
        infoText.setPreferredSize(new Dimension(300, 150));
    }

    private JCheckBox autoAudition = new JCheckBox(new AbstractAction("Auto") {
        public void actionPerformed(ActionEvent e) {
            if (autoAudition.isSelected()) {
                ZPREF_autoAudition.putValue(true);
            } else {
                ZPREF_autoAudition.putValue(false);
            }
        }
    });

    /*  private JSpinner auditionLength = new JSpinner(new SpinnerNumberModel(ZPREF_auditionLength.getDefault(), 1, 600, 1));

      {
          auditionLength.setMaximumSize(auditionLength.getPreferredSize());
      }

      private JCheckBox playWholeAudition = new JCheckBox(new AbstractAction("Play whole File") {
          public void actionPerformed(ActionEvent e) {
              if (playWholeAudition.isSelected()) {
                  ZPREF_playWholeAudition.setValue(true);
                  auditionLength.setEnabled(false);
              } else {
                  ZPREF_playWholeAudition.setValue(false);
                  auditionLength.setEnabled(true);
              }
          }
      });
      */
    private JCheckBox loopAudition = new JCheckBox(new AbstractAction("Repeat") {
        {
            setToolTipText("Loop continuously when auditioning audio files");
        }

        public void actionPerformed(ActionEvent e) {
            if (loopAudition.isSelected()) {
                ZPREF_loopAudition.putValue(true);
            } else {
                ZPREF_loopAudition.putValue(false);
            }
        }
    });

    {
        loopAudition.setSelected(ZPREF_loopAudition.getValue());
        autoAudition.setSelected(ZPREF_autoAudition.getValue());
    }

    private JButton playButton = new JButton(new AbstractAction("Play", new ImageIcon("media/Play16.gif")) {
        public void actionPerformed(ActionEvent e) {
            try {
                playClip();
            } catch (Exception e1) {
                e1.printStackTrace();
                new FlashMsg(ZoeosFrame.getInstance(),  SampleAuditionPanel.this , 750, 400, FlashMsg.colorWarning, "Audition not supported for file");
            }
        }
    });
    private JButton stopButton = new JButton(new AbstractAction("Stop", new ImageIcon("media/Stop16.gif")) {
        public void actionPerformed(ActionEvent e) {
            if (currentClip != null) {
                currentClip.stop();
                currentClip.setMicrosecondPosition(0);
            }
        }
    });
    /*  private JButton pauseButton = new JButton(new AbstractAction("Pause", new ImageIcon("media/Pause16.gif")) {
          public void actionPerformed(ActionEvent e) {
              if (currentClip != null) {
                  currentClip.stateStop();
              }
          }
      });
      */

    private Box optionBox = new Box(BoxLayout.Y_AXIS);
    private Box auditionBox = new Box(BoxLayout.Y_AXIS);
    private Component msgParent;

    {
        //auditionBox.addDesktopElement(new JScrollPane(filenameField));
        auditionBox.add(new JScrollPane(infoText));
        Box bb = new Box(BoxLayout.X_AXIS);
        bb.add(stopButton);
        bb.add(playButton);
        // bb.addDesktopElement(pauseButton);
        optionBox.add(autoAudition);
        optionBox.add(loopAudition);
        bb.add(optionBox);

        auditionBox.add(bb);
    }

    public SampleAuditionPanel(String title) {
        this(title, null);
    }

    public SampleAuditionPanel(String title, Component msgComponent) {
        if (title != null)
            this.setBorder(new TitledBorder(title));
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(auditionBox);
        //this.addDesktopElement(optionBox);
        updateFile();
        this.msgParent = msgComponent;
    }

    public File getCurrentFile() {
        return currentFile;
    }

    final static ManageableTicketedQ auditionPanelQ = QueueFactory.createTicketedQueue(SampleAuditionPanel.class.getClass(), "auditionPanelQ", 6);

    static {
        auditionPanelQ.start();
    }
    public void setCurrentFile(final File currentFile) {
        try {
            auditionPanelQ.getPostableTicket(new TicketRunnable() {
                public void run() throws Exception {
                    disposeCurrent();
                    SampleAuditionPanel.this.currentFile = currentFile;
                    updateFile();
                }
            }, "setCurrentFile", false).post();
        } catch (ResourceUnavailableException e) {
            e.printStackTrace();
        }
    }


    /*


    StringBuffer buf = new StringBuffer();

    //$$fb2002-11-01: fix for 4672864: AudioFileFormat.toString() throws unexpected NullPointerException
    if (type != null) {
        buf.append(type.toString() + " (." + type.getExtension() + ") File");
    } else {
        buf.append("unknown File format");
    }

    if (byteLength != AudioSystem.NOT_SPECIFIED) {
        buf.append(", byte length: " + byteLength);
    }

    buf.append(", data format: " + format);

    if (frameLength != AudioSystem.NOT_SPECIFIED) {
        buf.append(", frame length: " + frameLength);
    }

    return new String(buf);
    */


    /*	return getEncoding() + ", " +
	    sampleRate + " Hz, " +
	    sampleSizeInBits + " bit, " +
	    (channels == 2 ? "stereo, " : "mono, ") +
	    (sampleSizeInBits > 8 ? ((bigEndian == true ? "big-endian, " : "little-endian, ")) : "") +
	    "audio data";
*/

    private static final int DESC_FIELD_LEN = 14;
    private static final DecimalFormat df = new DecimalFormat(".00");

    private String makeDescription(AudioFileFormat f) {
        StringBuffer buf = new StringBuffer();

        AudioFileFormat.Type type = f.getType();
        int byteLength = f.getByteLength();
        int frameLength = f.getFrameLength();
        AudioFormat fmt = f.getFormat();
        if (type != null) {
            buf.append(ZUtilities.makeExactLengthString("File type:", DESC_FIELD_LEN) + type.toString() + " (." + type.getExtension() + ")");
        } else {
            buf.append(ZUtilities.makeExactLengthString("File type:", DESC_FIELD_LEN) + "Unknown");
        }
        buf.append(Zoeos.lineSeperator);
        float sr = fmt.getSampleRate();
        if (sr - (int) sr == 0)
            buf.append(ZUtilities.makeExactLengthString("Sample rate:", DESC_FIELD_LEN) + (int) sr + " Hz");
        else
            buf.append(ZUtilities.makeExactLengthString("Sample rate:", DESC_FIELD_LEN) + sr + " Hz");
        buf.append(Zoeos.lineSeperator);
        buf.append(ZUtilities.makeExactLengthString("Sample size:", DESC_FIELD_LEN) + fmt.getSampleSizeInBits() + " bit");
        buf.append(Zoeos.lineSeperator);
        if (byteLength != AudioSystem.NOT_SPECIFIED) {
            if (byteLength < 1024)
                buf.append(ZUtilities.makeExactLengthString("Size:", DESC_FIELD_LEN) + byteLength + " bytes");
            else if (byteLength < 1024 * 1024)
                buf.append(ZUtilities.makeExactLengthString("Size:", DESC_FIELD_LEN) + df.format(byteLength / 1024.0) + " KB");
            else
                buf.append(ZUtilities.makeExactLengthString("Size:", DESC_FIELD_LEN) + df.format(byteLength / (1024.0 * 1024.0)) + " MB");
            buf.append(Zoeos.lineSeperator);
        }
        if (frameLength != AudioSystem.NOT_SPECIFIED) {
            buf.append(ZUtilities.makeExactLengthString("Length:", DESC_FIELD_LEN) + frameLength + " samples");
            buf.append(Zoeos.lineSeperator);
        }
        int chnls = fmt.getChannels();
        if (chnls == 1)
            buf.append(ZUtilities.makeExactLengthString("Channels:", DESC_FIELD_LEN) + "Mono");
        else if (chnls == 2)
            buf.append(ZUtilities.makeExactLengthString("Channels:", DESC_FIELD_LEN) + "Stereo");
        else
            buf.append(ZUtilities.makeExactLengthString("Channels:", DESC_FIELD_LEN) + chnls);
        buf.append(Zoeos.lineSeperator);

        buf.append(ZUtilities.makeExactLengthString("Encoding:", DESC_FIELD_LEN) + fmt.getEncoding() /*+ "(" + (fmt.isBigEndian() ? "big-endian" : "little-endian") + ")"*/);
        buf.append(Zoeos.lineSeperator);
        return new String(buf);
    }

    private void updateFile() {
        if (currentFile != null) {
            try {
                playButton.setEnabled(true);
                stopButton.setEnabled(true);
                AudioFileFormat f = AudioSystem.getAudioFileFormat(currentFile);
                infoText.setText(makeDescription(f));
                int len = f.getByteLength();
                if (len > AudioUtilities.maxClipLen)
                    return;
                AudioInputStream audioInputStream;
                // TODO!! return this to AudioSystem
                if (f.getType() == AudioFileFormat.Type.WAVE)
                    audioInputStream = new WaveAudioFileReaderEx().getAudioInputStream(currentFile);
                else if (f.getType() == AudioFileFormat.Type.AIFF)
                    audioInputStream = new AiffAudioFileReaderEx().getAudioInputStream(currentFile);
                else
                    audioInputStream = AudioSystem.getAudioInputStream(currentFile);
                AudioFormat format = audioInputStream.getFormat();
                DataLine.Info info = new DataLine.Info(Clip.class, format, len);
                currentClip = (Clip) AudioSystem.getLine(info);
                currentClip.open(audioInputStream);
                newClip();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                new FlashMsg(ZoeosFrame.getInstance(), (msgParent == null ? this : msgParent), 750, 400, FlashMsg.colorWarning, "Audition not supported for file");
            }
        } else {
            infoText.setText("");
            //infoText.setText("No File selected");
            playButton.setEnabled(false);
            stopButton.setEnabled(false);
        }
    }

    public void stopClip() {
        try {
            auditionPanelQ.getPostableTicket(new TicketRunnable() {
                public void run() throws Exception {
                    if (currentClip != null)
                        currentClip.stop();
                }
            }, "setCurrentFile", false).post();
        } catch (ResourceUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void disposeCurrent() {
        if (currentFile != null) {
            if (currentClip != null) {
                currentClip.stop();
                currentClip.close();
                currentClip = null;
            }
            currentFile = null;
        }
    }

    private void newClip() {
        if (autoAudition.isSelected()) {
            playClip();
        }
    }

    private void playClip() {
        if (currentClip != null && !currentClip.isRunning()) {
            currentClip.setFramePosition(0);
            currentClip.setLoopPoints(0, -1/*currentClip.getFrameLength()*/);
            if (loopAudition.isSelected()) {
                currentClip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                currentClip.loop(0);
            }
        }
    }

    public static void main(String args[]) {
        try {
            JFrame f = new JFrame();
            f.getContentPane().add(new SampleAuditionPanel("Test Panel"));
            f.pack();
            f.setVisible(true);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
