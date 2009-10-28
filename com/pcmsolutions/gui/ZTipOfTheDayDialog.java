package com.pcmsolutions.gui;

import com.pcmsolutions.system.ZoeosPreferences;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 24-Nov-2003
 * Time: 16:20:50
 * To change this template use Options | File Templates.
 */
public class ZTipOfTheDayDialog extends ZDialog {
    private JPanel contentPanel;
    private JPanel topPanel;
    private JPanel bottomPanel;

    public ZTipOfTheDayDialog() throws HeadlessException {
        super(ZoeosFrame.getInstance(), "Tip of the Day", true);
        init();
    }

    private void init() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        topPanel = new JPanel(new BorderLayout());
        JTextPane t = new JTextPane();
        t.setEditable(false);
        JScrollPane sp = new JScrollPane(t);
        topPanel.add(sp, BorderLayout.CENTER);
        final Dimension fs = new Dimension(500, 300);
        topPanel.setMinimumSize(fs);
        topPanel.setPreferredSize(fs);
        topPanel.setMaximumSize(fs);

        bottomPanel = new JPanel();
        JCheckBox cb = new JCheckBox(new AbstractAction("Show tips on start-up") {
            public void actionPerformed(ActionEvent e) {
                ZoeosPreferences.ZPREF_showTipsAtStartup.putValue(((JCheckBox) e.getSource()).isSelected());
            }
        });

        cb.setSelected(ZoeosPreferences.ZPREF_showTipsAtStartup.getValue());
        bottomPanel.add(cb);
        bottomPanel.add(new JButton(new AbstractAction("Previous") {
            public void actionPerformed(ActionEvent e) {
            }
        }));
        bottomPanel.add(new JButton(new AbstractAction("Next") {
            public void actionPerformed(ActionEvent e) {
            }
        }));
        bottomPanel.add(new JButton(new AbstractAction("Close") {
            public void actionPerformed(ActionEvent e) {
                ZTipOfTheDayDialog.this.hide();
            }
        }));

        contentPanel.add(topPanel);
        contentPanel.add(bottomPanel);

        setContentPane(contentPanel);
        pack();

    }
}
