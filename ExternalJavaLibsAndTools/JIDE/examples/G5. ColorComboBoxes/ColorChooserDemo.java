/*
 * @(#)ColorChooserDemo.java
 *
 * Copyright 2002 - 2003 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.combobox.ColorChooserPanel;
import com.jidesoft.combobox.ColorComboBox;
import com.jidesoft.converter.ObjectConverterManager;
import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Demoed Component: {@link ColorComboBox}, {@link ColorChooserPanel}
 * <br>
 * Required jar files: jide-common.jar, jide-grids.jar
 * <br>
 * Required L&F: Jide L&F extension required
 */
public class ColorChooserDemo extends JFrame {

    private static ColorChooserDemo _frame;
    private static ColorComboBox _colorComboBox;
    private static JPanel _pane;

    public ColorChooserDemo(String title) throws HeadlessException {
        super(title);
    }

    public static void main(String[] args) {
        ObjectConverterManager.initDefaultConverter();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            LookAndFeelFactory.installJideExtension();
        }
        catch (ClassNotFoundException e) {
        }
        catch (InstantiationException e) {
        }
        catch (IllegalAccessException e) {
        }
        catch (UnsupportedLookAndFeelException e) {
        }

        _frame = new ColorChooserDemo("Demo of ColorComboBox");
        _frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        _pane = new JPanel(new BorderLayout());
        _pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        _colorComboBox = createColorComboBox();
        _pane.add(_colorComboBox, BorderLayout.BEFORE_FIRST_LINE);


        _frame.getContentPane().setLayout(new BorderLayout());
        _frame.getContentPane().add(_pane, BorderLayout.CENTER);
        _frame.getContentPane().add(_frame.createOptionsPanel(), BorderLayout.AFTER_LINE_ENDS);

        _frame.setBounds(10, 10, 300, 300);

        _frame.setVisible(true);

    }

    private static ColorComboBox createColorComboBox() {
        ColorComboBox colorComboBox = new ColorComboBox();
        colorComboBox.setSelectedColor(Color.black);
        return colorComboBox;
    }

    private JPanel createOptionsPanel() {
        JLabel header = new JLabel("Options");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 13));
        header.setOpaque(true);
        header.setBackground(new Color(0, 0, 128));
        header.setForeground(Color.WHITE);
        header.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.white, Color.lightGray, Color.lightGray, Color.gray),
                        BorderFactory.createEmptyBorder(0, 5, 0, 0)));
        header.setPreferredSize(new Dimension(100, 30));

        JPanel panel = new JPanel();
        panel.setLayout(new JideBoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] colorModes = new String[]{
            "15 Color Palette",
            "40 Color Palette",
            "216 Color Palette",
            "16 Gray Scale",
            "102 Gray Scale",
            "256 Gray Scale"
        };
        final JComboBox comboBox = new JComboBox(colorModes);
        comboBox.setSelectedIndex(_colorComboBox.getColorMode());
        comboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    _colorComboBox.setColorMode(comboBox.getSelectedIndex());
                }
            }
        });

        panel.add(new JLabel("Set Color Mode"));
        panel.add(Box.createVerticalStrut(6), JideBoxLayout.FIX);
        panel.add(comboBox);
        panel.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);
        panel.add(new JLabel("Additional Buttons"));
        panel.add(Box.createVerticalStrut(6), JideBoxLayout.FIX);
        final JCheckBox allowNone = new JCheckBox("Allow \"None\"");
        allowNone.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                _colorComboBox.setAllowDefaultColor(allowNone.isSelected());
            }
        });
        panel.add(allowNone);
        allowNone.setSelected(true);
        panel.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
        final JCheckBox allowMore = new JCheckBox("Allow \"More Color\"");
        allowMore.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                _colorComboBox.setAllowMoreColors(allowMore.isSelected());
            }
        });
        allowMore.setSelected(true);
        panel.add(allowMore);
        panel.add(Box.createGlue(), JideBoxLayout.VARY);

        JPanel optionsPanel = new JPanel(new BorderLayout());

        optionsPanel.add(header, BorderLayout.BEFORE_FIRST_LINE);
        optionsPanel.add(panel, BorderLayout.CENTER);

        return optionsPanel;
    }
}
