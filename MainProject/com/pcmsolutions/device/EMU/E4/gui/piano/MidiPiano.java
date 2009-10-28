package com.pcmsolutions.device.EMU.E4.gui.piano;


import com.pcmsolutions.device.EMU.E4.AuditionManager;
import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.gui.TitleProvider;
import com.pcmsolutions.device.EMU.E4.gui.TitleProviderListener;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.AuditioningDisabledException;
import com.pcmsolutions.system.preferences.*;

import javax.sound.midi.Sequencer;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Illustrates general MIDI melody instruments and MIDI controllers.
 *
 * @author Brian Lichtenwalter
 * @version @(#)MidiSynth.java	1.15 99/12/03
 */
public class MidiPiano extends JPanel implements TitleProvider, ZDisposable {
    final int NOTEON = 144;
    final int NOTEOFF = 128;
    final int SUSTAIN = 64;
    final int ON = 0, OFF = 1;
    final Color jfcBlue = new Color(204, 204, 255);
    final Color pink = new Color(255, 175, 175);
    JCheckBox mouseOverCB;
    JSlider veloS;
    JCheckBox latchCB;
    Vector keys = new Vector();
    Vector whiteKeys = new Vector();
    Piano piano;
    Controls controls;
    DeviceContext device;
    AuditionManager am;
    int numChannels;
    int channel;
    //int velocity = 0;
    //boolean latch;

    ZBoolPref latch;
    ZIntPref velocity;
    ZStringPref channels;

    String encodeChannels(Integer[] channels) {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < channels.length; i++) {
            if (i != 0)
                s.append(",");
            s.append(channels[i].toString());
        }
        return s.toString();
    }

    int[] decodeChannels(String s) {
        StringTokenizer st = new StringTokenizer(s, ",");
        int[] channels = new int[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++)
            channels[i] = Integer.parseInt(st.nextToken());
        return channels;
    }

    void makePrefs(DeviceContext device) {
        latch = new Impl_ZBoolPref(device.getDevicePreferences().getPreferences(), "pianoLatch", false, "", "");
        velocity = new Impl_ZIntPref(device.getDevicePreferences().getPreferences(), "pianoVelocity", 100, "", "");
        channels = new Impl_ZStringPref(device.getDevicePreferences().getPreferences(), "pianoChannels", "1", "", "");
    }

    public MidiPiano(DeviceContext device) throws DeviceException {
        this.device = device;
        am = device.getAuditionManager();
        numChannels = device.getAuditionManager().getMaxAudChannel();
        setLayout(new BorderLayout());
        JPanel p = new JPanel() {
            public Color getBackground() {
                return UIColors.getDefaultBG();
            }

            public Color getForeground() {
                return UIColors.getDefaultFG();
            }
        };
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        EmptyBorder eb = new EmptyBorder(5, 5, 5, 5);
        BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
        CompoundBorder cb = new CompoundBorder(eb, bb);
        p.setBorder(new CompoundBorder(cb, eb));
        JPanel pp = new JPanel(new BorderLayout()) {
            public Color getBackground() {
                return UIColors.getDefaultBG();
            }

            public Color getForeground() {
                return UIColors.getDefaultFG();
            }
        };
        pp.setBorder(new EmptyBorder(10, 20, 10, 5));
        pp.add(piano = new Piano());
        p.add(pp);
        p.add(controls = new Controls());
        add(p);
        p.add(new ChannelsPanel(false, new ActionListener(){
            public void actionPerformed(ActionEvent e) {
            }
        }));
    }


    /**
     * Black and white keys or notes on the piano.
     */
    class Key extends Rectangle {
        int noteState = OFF;
        int kNum;

        public Key(int x, int y, int width, int height, int num) {
            super(x, y, width, height);
            kNum = num;
        }

        public boolean isNoteOn() {
            return noteState == ON;
        }

        public void on() {
            setNoteState(ON);
            try {
                //am.noteOn(kNum, channel, velocity);
                am.noteOn(kNum, 1, 100);
            } catch (AuditionManager.MultimodeChannelUnreachableException e) {
                flashChannelUnreachable(channel);
            } catch (AuditioningDisabledException e) {
                e.printStackTrace();
            }
        }

        public void off() {
            setNoteState(OFF);
            try {
                am.noteOff(kNum, channel, 100);
            } catch (AuditionManager.MultimodeChannelUnreachableException e) {
                flashChannelUnreachable(channel);
            } catch (AuditioningDisabledException e) {
                e.printStackTrace();
            }
        }

        public void setNoteState(int state) {
            noteState = state;
        }
    } // End class Key


    /**
     * Piano renders black & white keys and plays the notes for a MIDI
     * channel.
     */
    class Piano extends JPanel implements MouseListener {

        Vector blackKeys = new Vector();
        Key prevKey;
        final int kw = 16, kh = 80;
        Sequencer s;

        public Piano() {
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(42 * kw, kh + 1));
            int transpose = 0;
            int whiteIDs[] = {0, 2, 4, 5, 7, 9, 11};

            for (int i = 0, x = 0; i < 10; i++) {
                for (int j = 0; j < 7; j++, x += kw) {
                    int keyNum = i * 12 + whiteIDs[j] + transpose;
                    whiteKeys.add(new Key(x, 0, kw, kh, keyNum));
                }
            }
            for (int i = 0, x = 0; i < 10; i++, x += kw) {
                int keyNum = i * 12 + transpose;
                blackKeys.add(new Key((x += kw) - 4, 0, kw / 2, kh / 2, keyNum + 1));
                blackKeys.add(new Key((x += kw) - 4, 0, kw / 2, kh / 2, keyNum + 3));
                x += kw;
                blackKeys.add(new Key((x += kw) - 4, 0, kw / 2, kh / 2, keyNum + 6));
                blackKeys.add(new Key((x += kw) - 4, 0, kw / 2, kh / 2, keyNum + 8));
                blackKeys.add(new Key((x += kw) - 4, 0, kw / 2, kh / 2, keyNum + 10));
            }
            keys.addAll(blackKeys);
            keys.addAll(whiteKeys);

            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseMoved(MouseEvent e) {
                    if (mouseOverCB.isSelected()) {
                        Key key = getKey(e.getPoint());
                        if (prevKey != null && prevKey != key) {
                            prevKey.off();
                        }
                        if (key != null && prevKey != key) {
                            key.on();
                        }
                        prevKey = key;
                        repaint();
                    }
                }
            });
            addMouseListener(this);
        }

        public Color getBackground() {
            return UIColors.getDefaultBG();
        }

        public Color getForeground() {
            return UIColors.getDefaultFG();
        }

        public void mousePressed(MouseEvent e) {
            prevKey = getKey(e.getPoint());
            if (prevKey != null) {
                prevKey.on();
                repaint();
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (prevKey != null) {
                prevKey.off();
                repaint();
            }
        }

        public void mouseExited(MouseEvent e) {
            if (prevKey != null) {
                prevKey.off();
                repaint();
                prevKey = null;
            }
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }


        public Key getKey(Point point) {
            for (int i = 0; i < keys.size(); i++) {
                if (((Key) keys.get(i)).contains(point)) {
                    return (Key) keys.get(i);
                }
            }
            return null;
        }

        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            Dimension d = getSize();

            g2.setBackground(getBackground());
            g2.clearRect(0, 0, d.width, d.height);

            g2.setColor(Color.white);
            g2.fillRect(0, 0, 42 * kw, kh);

            for (int i = 0; i < whiteKeys.size(); i++) {
                Key key = (Key) whiteKeys.get(i);
                if (key.isNoteOn()) {
                    g2.setColor(jfcBlue);
                    g2.fill(key);
                }
                g2.setColor(Color.blue);
                g2.draw(key);
            }
            for (int i = 0; i < blackKeys.size(); i++) {
                Key key = (Key) blackKeys.get(i);
                if (key.isNoteOn()) {
                    //g2.setColor(jfcBlue);
                    GradientPaint gp = new GradientPaint(key.x, key.y, Color.white, key.x, key.y + key.height, jfcBlue);
                    g2.setPaint(gp);
                    g2.fill(key);
                    g2.setColor(Color.black);
                    g2.draw(key);
                } else {
                    GradientPaint gp = new GradientPaint(key.x, key.y, Color.white, key.x, key.y + key.height, Color.blue);
                    //g2.setColor(Color.blue);
                    g2.setPaint(gp);
                    g2.fill(key);
                }
            }
        }
    } // End class Piano


    class ChannelsPanel extends Box {
        boolean is32;
        JCheckBox[] checks;

        public ChannelsPanel(boolean is32, final ActionListener al) {
            super(BoxLayout.Y_AXIS);
            this.setBorder(new TitledBorder("Output Channels"));
            this.is32 = is32;
            if (is32)
                checks = new JCheckBox[32];
            else
                checks = new JCheckBox[16];

            JPanel g16 = new JPanel(new GridLayout(1, 16));
            for (int i = 0; i < 16; i++) {
                checks[i] = new JCheckBox(new AbstractAction(""+(i+1)) {
                    public void actionPerformed(ActionEvent e) {
                        al.actionPerformed(e);
                    }
                });
                g16.add(checks[i]);
            }
            add(g16);
            if (is32) {
                JPanel g32 = new JPanel(new GridLayout(1, 16));
                for (int i = 16; i < 32; i++) {
                    checks[i] = new JCheckBox(new AbstractAction(""+(i + 1)) {
                        public void actionPerformed(ActionEvent e) {
                            al.actionPerformed(e);
                        }
                    });
                    g32.add(checks[i]);
                }
                add(g32);
            }
        }
    }

    /**
     * A collection of MIDI controllers.
     */
    class Controls extends JPanel implements ActionListener, ChangeListener, ItemListener {

        public JButton recordB;
        JMenu menu;
        int fileNum = 0;

        public Controls() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(5, 10, 5, 10));

            JPanel p = new JPanel() {
                public Color getBackground() {
                    return UIColors.getDefaultBG();
                }

                public Color getForeground() {
                    return UIColors.getDefaultFG();
                }
            };
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

            veloS = createSlider("Velocity", p);

            p.add(Box.createHorizontalStrut(10));

            //add(p);
            //p = new JPanel();
            // p.setBorder(new EmptyBorder(10, 0, 10, 0));
            // p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

            JComboBox combo = new JComboBox();
            combo.setPreferredSize(new Dimension(120, 25));
            combo.setMaximumSize(new Dimension(120, 25));

            for (int i = 1; i <= numChannels; i++) {
                combo.addItem("Channel " + String.valueOf(i));
            }
            combo.addItemListener(this);
            p.add(combo);
            p.add(Box.createHorizontalStrut(20));

            latchCB = createCheckBox("Latch", p);

            createButton("All Notes Off", p);
            p.add(Box.createHorizontalStrut(10));
            mouseOverCB = new JCheckBox("mouseOver", true) {
                public Color getBackground() {
                    return UIColors.getDefaultBG();
                }

                public Color getForeground() {
                    return UIColors.getDefaultFG();
                }
            };
            p.add(mouseOverCB);
            p.add(Box.createHorizontalStrut(10));
            add(p);
        }

        public Color getBackground() {
            return UIColors.getDefaultBG();
        }

        public Color getForeground() {
            return UIColors.getDefaultFG();
        }

        public JButton createButton(String name, JPanel p) {
            JButton b = new JButton(name);
            b.addActionListener(this);
            p.add(b);
            return b;
        }

        private JCheckBox createCheckBox(String name, JPanel p) {
            JCheckBox cb = new JCheckBox(name) {
                public Color getBackground() {
                    return UIColors.getDefaultBG();
                }

                public Color getForeground() {
                    return UIColors.getDefaultFG();
                }
            };
            cb.addItemListener(this);
            p.add(cb);
            return cb;
        }

        private JSlider createSlider(String name, JPanel p) {
            JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 127, 64) {
                public Color getBackground() {
                    return UIColors.getDefaultBG();
                }

                public Color getForeground() {
                    return UIColors.getDefaultFG();
                }
            };
            slider.addChangeListener(this);
            TitledBorder tb = new TitledBorder(new EtchedBorder());
            tb.setTitle(name + " = 64");
            slider.setBorder(tb);
            p.add(slider);
            p.add(Box.createHorizontalStrut(5));
            return slider;
        }

        private JSlider create14BitSlider(String name, JPanel p) {
            JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 16383, 8192);
            slider.addChangeListener(this);
            TitledBorder tb = new TitledBorder(new EtchedBorder());
            tb.setTitle(name + " = 8192");
            slider.setBorder(tb);
            p.add(slider);
            p.add(Box.createHorizontalStrut(5));
            return slider;
        }

        public void stateChanged(ChangeEvent e) {
            JSlider slider = (JSlider) e.getSource();
            int value = slider.getValue();
            TitledBorder tb = (TitledBorder) slider.getBorder();
            String s = tb.getTitle();
            tb.setTitle(s.substring(0, s.indexOf('=') + 1) + s.valueOf(value));
            if (s.startsWith("Velocity")) {
                //velocity = value;
            }
            slider.repaint();
        }

        public void itemStateChanged(ItemEvent e) {
            if (e.getSource() instanceof JComboBox) {
                JComboBox combo = (JComboBox) e.getSource();
                channel = combo.getSelectedIndex() + 1;
            } else {
                JCheckBox cb = (JCheckBox) e.getSource();
                String name = cb.getText();
                if (name.startsWith("Mute")) {
                    //cc.channel.setMute(cc.mute = cb.isSelected());
                } else if (name.startsWith("Solo")) {
                    //cc.channel.setSolo(cc.solo = cb.isSelected());
                }
            }
        }

        public void actionPerformed(ActionEvent e) {
            JButton button = (JButton) e.getSource();
            if (button.getText().startsWith("All")) {
                //  for (int i = 0; i < numChannels; i++) {
                try {
                    am.midiAllNotesOff(channel);
                } catch (AuditionManager.MultimodeChannelUnreachableException e1) {
                    flashChannelUnreachable(-1);
                }
            }
            // }
            for (int i = 0; i < keys.size(); i++) {
                ((Key) keys.get(i)).setNoteState(OFF);
            }
        }
    } // End class Controls

    void flashChannelUnreachable(int channel) {

    }

    public String getTitle() {
        return device.getTitle();
    }

    public String getReducedTitle() {
        return device.getReducedTitle();
    }

    public void addTitleProviderListener(TitleProviderListener tpl) {
        device.addTitleProviderListener(tpl);
    }

    public void removeTitleProviderListener(TitleProviderListener tpl) {
        device.removeTitleProviderListener(tpl);
    }

    public Icon getIcon() {
        return device.getIcon();
    }

    public void zDispose() {
        removeAll();
        device = null;
    }
}
