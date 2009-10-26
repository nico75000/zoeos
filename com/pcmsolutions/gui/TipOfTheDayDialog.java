package com.pcmsolutions.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 24-Nov-2003
 * Time: 16:20:50
 * To change this template use Options | File Templates.
 */
public class TipOfTheDayDialog extends ZDialog {
    private JPanel contentPanel;
    private JPanel topPanel;
    private JPanel bottomPanel;

    public static final String PREF_showTipsAtStartup = "showTipstStartup";

    public TipOfTheDayDialog() throws HeadlessException {
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
        final Dimension fs = new Dimension(500,300);
        topPanel.setMinimumSize(fs);
        topPanel.setPreferredSize(fs);
        topPanel.setMaximumSize(fs);

        bottomPanel = new JPanel();
        JCheckBox cb = new JCheckBox(new AbstractAction("Show tips at startup") {
            public void actionPerformed(ActionEvent e) {
                Preferences.userNodeForPackage(TipOfTheDayDialog.this.getClass()).putBoolean(PREF_showTipsAtStartup, ((JCheckBox) e.getSource()).isSelected());
            }
        });

        cb.setSelected(Preferences.userNodeForPackage(TipOfTheDayDialog.this.getClass()).getBoolean(PREF_showTipsAtStartup, true));
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
                TipOfTheDayDialog.this.hide();
            }
        }));

        contentPanel.add(topPanel);
        contentPanel.add(bottomPanel);

        setContentPane(contentPanel);
        pack();

    }
}
