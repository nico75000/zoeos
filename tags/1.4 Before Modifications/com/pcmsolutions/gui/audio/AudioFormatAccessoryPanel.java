package com.pcmsolutions.gui.audio;

import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.audio.AudioUtilities;
import com.pcmsolutions.system.audio.ZAudioSystem;
import com.pcmsolutions.system.preferences.Impl_ZIntPref;
import com.pcmsolutions.system.preferences.ZIntPref;

import javax.sound.sampled.AudioFileFormat;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 04-Dec-2003
 * Time: 15:20:52
 * To change this template use Options | File Templates.
 */
public class AudioFormatAccessoryPanel extends JPanel {
    //private static final Preferences prefs = Preferences.userNodeForPackage(AudioFormatAccessoryPanel.class);
   // public static final ZIntPref ZPREF_defaultAudioType = new Impl_ZIntPref(prefs, "defaultAudioFormat", AudioUtilities.getAudioTypeIndexForExtension(AudioUtilities.defaultAudioFormat.getExtension()));
    private AudioFileFormat.Type activeType;

    private JRadioButton[] typeButtons;

    public AudioFormatAccessoryPanel(String title) {
        this.setBorder(new TitledBorder(title));
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        final AudioFileFormat.Type[] types = ZAudioSystem.getAudioTypes();
        if (types.length > 0) {
            typeButtons = new JRadioButton[types.length];
            ButtonGroup bg = new ButtonGroup();
            for (int i = 0; i < types.length; i++) {
                final int f_i = i;
                typeButtons[i] = new JRadioButton(new AbstractAction(ZUtilities.makeExactLengthString(types[i].toString(), 20)) {
                    public void actionPerformed(ActionEvent e) {
                        activeType = types[f_i];
                        ZAudioSystem.ZPREF_defaultAudioType.putValue(f_i);
                    }
                });
                this.add(typeButtons[i]);
                bg.add(typeButtons[i]);
            }
            int dt = ZAudioSystem.ZPREF_defaultAudioType.getValue();

            if (dt >= 0 && dt < typeButtons.length) {
                typeButtons[dt].setSelected(true);
                activeType = types[dt];
            } else {
                typeButtons[0].setSelected(true);
                activeType = types[0];
            }
        }
    }

    public AudioFileFormat.Type getActiveType() {
        return activeType;
    }
}
