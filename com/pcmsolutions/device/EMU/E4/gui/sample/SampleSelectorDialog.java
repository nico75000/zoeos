package com.pcmsolutions.device.EMU.E4.gui.sample;

import com.pcmsolutions.device.EMU.E4.RemoteObjectStates;
import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.device.EMU.E4.sample.ReadableSample;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 07-Jan-2004
 * Time: 20:04:14
 * To change this template use Options | File Templates.
 */
public class SampleSelectorDialog extends JDialog {
    private ReadableSample[] samples = null;
    private ReadableSample selectedSample = null;

    public SampleSelectorDialog(Frame owner, String title, ReadableSample[] samples, boolean moveToFirstEmpty) throws HeadlessException {
        super(owner, title, true);
        this.samples = samples;
        this.setResizable(false);

        int firstEmptyIndex = -1;
        if (moveToFirstEmpty)
            for (int s = 0; s < samples.length; s++) {
                try {
                    if (firstEmptyIndex == -1 && samples[s].getSampleState() == RemoteObjectStates.STATE_EMPTY)
                        firstEmptyIndex = s;
                } catch (NoSuchSampleException e) {
                    e.printStackTrace();
                }
            }

        final JComboBox j = new JComboBox(samples);
        j.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectedSample = (ReadableSample) j.getSelectedItem();
            }
        });
        if (firstEmptyIndex != -1) {
            j.setSelectedIndex(firstEmptyIndex);
            selectedSample = samples[firstEmptyIndex];
        } else
            selectedSample = samples[0];

        Box b = new Box(BoxLayout.X_AXIS);
        b.add(new JLabel("Select sample"));
        b.add(j);
        getContentPane().add(b);
        pack();
    }


    public ReadableSample[] getSamples() {
        return samples;
    }

    public void setSamples(ReadableSample[] samples) {
        this.samples = samples;
    }

    public ReadableSample getSelectedSample() {
        return selectedSample;
    }

    public void setSelectedSample(ReadableSample selectedSample) {
        this.selectedSample = selectedSample;
    }
}
