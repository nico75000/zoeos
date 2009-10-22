package com.pcmsolutions.gui.audio;

import com.pcmsolutions.system.preferences.Impl_ZIntPref;
import com.pcmsolutions.system.preferences.ZIntPref;

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
public class SaveAccessoryPanel extends JPanel {
    public static final Preferences prefs = Preferences.userNodeForPackage(SaveAccessoryPanel.class);

    public static final int MODE_ALWAYS_OVERWRITE = 0;
    public static final int MODE_ASK_OVERWRITE = 1;
    public static final int MODE_NEVER_OVERWRITE = 2;
    private static final ZIntPref ZPREF_overwriteMode = new Impl_ZIntPref(prefs, "overwriteMode", MODE_ASK_OVERWRITE) {
        public void putValue(int i) {
            if (i != MODE_ALWAYS_OVERWRITE && i != MODE_ASK_OVERWRITE && i != MODE_NEVER_OVERWRITE)
                throw new IllegalArgumentException();
            super.putValue(i);
        }
    };

    public static final int MODE_NO_INDEX = 100;
    public static final int MODE_PREFIXED_INDEX = 101;
    public static final int MODE_POSTFIXED_INDEX = 102;
    private static final ZIntPref ZPREF_indexMode = new Impl_ZIntPref(prefs, "indexMode", MODE_NO_INDEX) {
        public void putValue(int i) {
            if (i != MODE_NO_INDEX && i != MODE_PREFIXED_INDEX && i != MODE_POSTFIXED_INDEX)
                throw new IllegalArgumentException();
            super.putValue(i);
        }
    };
    private static final ZIntPref ZPREF_fixMode = new Impl_ZIntPref(prefs, "fixMode", MODE_PREFIXED_INDEX);

    public int getIndexMode() {
        return ZPREF_indexMode.getValue();
    }

    public int getOverwriteMode() {
        return ZPREF_overwriteMode.getValue();
    }

    private JRadioButton alwaysOverwrite = new JRadioButton(new AbstractAction("Always") {
        public void actionPerformed(ActionEvent e) {
            ZPREF_overwriteMode.putValue(MODE_ALWAYS_OVERWRITE);
        }
    });

    private JRadioButton askOverwrite = new JRadioButton(new AbstractAction("Ask") {
        public void actionPerformed(ActionEvent e) {
            ZPREF_overwriteMode.putValue(MODE_ASK_OVERWRITE);
        }
    });

    private JRadioButton neverOverwrite = new JRadioButton(new AbstractAction("Never") {
        public void actionPerformed(ActionEvent e) {
            ZPREF_overwriteMode.putValue(MODE_NEVER_OVERWRITE);
        }
    });

    private ButtonGroup obg = new ButtonGroup();

    {
        obg.add(alwaysOverwrite);
        obg.add(askOverwrite);
        obg.add(neverOverwrite);
        if (ZPREF_overwriteMode.getValue() == MODE_ALWAYS_OVERWRITE)
            alwaysOverwrite.setSelected(true);
        else if (ZPREF_overwriteMode.getValue() == MODE_ASK_OVERWRITE)
            askOverwrite.setSelected(true);
        else
            neverOverwrite.setSelected(true);
    }

    private JRadioButton prefixIndex = new JRadioButton(new AbstractAction("Prefix") {
        public void actionPerformed(ActionEvent e) {
            ZPREF_indexMode.putValue(MODE_PREFIXED_INDEX);
            ZPREF_fixMode.putValue(MODE_PREFIXED_INDEX);
        }
    });

    private JRadioButton postfixIndex = new JRadioButton(new AbstractAction("Postfix") {
        public void actionPerformed(ActionEvent e) {
            ZPREF_indexMode.putValue(MODE_POSTFIXED_INDEX);
            ZPREF_fixMode.putValue(MODE_POSTFIXED_INDEX);
        }
    });

    private ButtonGroup ibg = new ButtonGroup();

    {
        ibg.add(prefixIndex);
        ibg.add(postfixIndex);
    }

    private JCheckBox applyIndex = new JCheckBox(new AbstractAction("Apply index") {
        public void actionPerformed(ActionEvent e) {
            if (((JCheckBox) e.getSource()).isSelected()) {
                prefixIndex.setEnabled(true);
                postfixIndex.setEnabled(true);
                if (prefixIndex.isSelected())
                    ZPREF_indexMode.putValue(MODE_PREFIXED_INDEX);
                else
                    ZPREF_indexMode.putValue(MODE_POSTFIXED_INDEX);
            } else {
                prefixIndex.setEnabled(false);
                postfixIndex.setEnabled(false);
                ZPREF_indexMode.putValue(MODE_NO_INDEX);
            }
        }
    });

    {
        int mode = ZPREF_indexMode.getValue();
        if (mode == MODE_NO_INDEX) {
            applyIndex.setSelected(false);
            if (ZPREF_fixMode.getValue() == MODE_PREFIXED_INDEX)
                prefixIndex.setSelected(true);
            else
                postfixIndex.setSelected(true);
            postfixIndex.setEnabled(false);
            prefixIndex.setEnabled(false);
        } else if (mode == MODE_PREFIXED_INDEX) {
            //prefixIndex.setEnabled(true);
            // postfixIndex.setEnabled(true);
            applyIndex.setSelected(true);
            prefixIndex.setSelected(true);
        } else {
            applyIndex.setSelected(true);
            postfixIndex.setSelected(true);
        }
    }


    public SaveAccessoryPanel(String title) {
        if (title != null)
            this.setBorder(new TitledBorder(title));
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        Box ob = new Box(BoxLayout.Y_AXIS);
        ob.setBorder(new TitledBorder("Overwrite"));
        ob.add(alwaysOverwrite);
        ob.add(askOverwrite);
        ob.add(neverOverwrite);
        this.add(ob);

        Box ib = new Box(BoxLayout.Y_AXIS);
        ib.setBorder(new TitledBorder("Naming"));
        ib.add(applyIndex);
        ib.add(prefixIndex);
        ib.add(postfixIndex);
        this.add(ib);
    }
}
