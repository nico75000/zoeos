package com.pcmsolutions.gui;

import com.jidesoft.status.StatusBarItem;

import javax.swing.*;
import java.awt.*;

/**
 * User: paulmeehan
 * Date: 24-Feb-2004
 * Time: 04:33:11
 */
public class ProgressElement extends StatusBarItem {
    String title;
    JProgressBar bar = new JProgressBar();
    JLabel label = new JLabel();
    JProgressBar b;
    Dimension max = new Dimension(300, 17);

    ProgressElement(String title, int maximum) {
        super();
        this.title = title;
        bar.setMaximum(maximum);
        bar.setMaximumSize(max);
        label.setText(title);
        label.setHorizontalAlignment(JLabel.LEFT);
        label.setMaximumSize(max);
        add(label);
        add(bar);
    }

    protected void maxBar() {
        bar.setValue(bar.getMaximum());
    }

    protected void setMaximum(int max) {
        bar.setMaximum(max);
    }

    protected void updateBar(final int status) {
        bar.setValue(status);
    }

    protected void updateTitle(final String title) {
        label.setText(title);
    }

    protected void setIndeterminate(boolean b) {
        bar.setIndeterminate(b);
    }

    protected void updateBar() {
        bar.setValue(bar.getValue() + 1);
    }

    public String getItemName() {
        return title;
    }
}
