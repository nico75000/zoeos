/*
 * @(#)CollapsiblePaneDemo.java
 *
 * Copyright 2002 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.swing.JideButton;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Demoed Component: {@link CollapsiblePane}
 * <br>
 * Required jar files: jide-common.jar, jide-components.jar
 * <br>
 * Required L&F: Jide L&F extension required
 */
public class CollapsiblePaneDemo extends JFrame {

    private static CollapsiblePaneDemo _frame;
    private static CollapsiblePane _fileFolderTaskPane;
    private static CollapsiblePane _otherPlacesPane;

    public CollapsiblePaneDemo(String title) throws HeadlessException {
        super(title);
    }

    public static void main(String[] args) {
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

        _frame = new CollapsiblePaneDemo("Demo of CollapsiblePane");
        _frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new JideBoxLayout(panel, JideBoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.setBackground(Color.WHITE);
        _fileFolderTaskPane = createFileFolderTaskPane();
        _otherPlacesPane = createOtherPlacesPane();
        panel.add(_fileFolderTaskPane);
        panel.add(Box.createVerticalStrut(12));
        panel.add(_otherPlacesPane);
        panel.add(Box.createGlue(), JideBoxLayout.VARY);

        _frame.getContentPane().setLayout(new BorderLayout());
        _frame.getContentPane().add(createOptionsPanel(), BorderLayout.AFTER_LINE_ENDS);
        _frame.getContentPane().add(new JScrollPane(panel), BorderLayout.CENTER);

        _frame.setBounds(10, 10, 350, 500);

        _frame.setVisible(true);

    }

    private static JPanel createOptionsPanel() {
        JLabel header = new JLabel("Options");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 13));
        header.setOpaque(true);
        header.setBackground(new Color(0, 0, 128));
        header.setForeground(Color.WHITE);
        header.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.white, Color.lightGray, Color.lightGray, Color.gray),
                        BorderFactory.createEmptyBorder(0, 5, 0, 0)));
        header.setPreferredSize(new Dimension(100, 30));
        final JRadioButton style1 = new JRadioButton("Tree Style");
        final JRadioButton style2 = new JRadioButton("Dropdown Style");
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(style1);
        buttonGroup.add(style2);
        JPanel switchPanel = new JPanel(new GridLayout(2, 1, 3, 3));
        switchPanel.add(style1);
        switchPanel.add(style2);
        style2.setSelected(true);

        style1.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (style1.isSelected()) {
                    _fileFolderTaskPane.setStyle(CollapsiblePane.TREE_STYLE);
                    _otherPlacesPane.setStyle(CollapsiblePane.TREE_STYLE);
                }
            }
        });
        style2.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (style2.isSelected()) {
                    _fileFolderTaskPane.setStyle(CollapsiblePane.DROPDOWN_STYLE);
                    _otherPlacesPane.setStyle(CollapsiblePane.DROPDOWN_STYLE);
                }
            }
        });
        switchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panel = new JPanel();
        panel.setLayout(new JideBoxLayout(panel, JideBoxLayout.Y_AXIS));
        panel.add(header);
        panel.add(switchPanel);
        panel.add(Box.createGlue(), JideBoxLayout.VARY);
        return panel;
    }

    // CollapsiblePane
    private static CollapsiblePane createFileFolderTaskPane() {
        CollapsiblePane panel = new CollapsiblePane("File and Folder Tasks");
// uncomment following for a different style of collapsible pane
//        panel.setStyle(CollapsiblePane.TREE_STYLE);
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new GridLayout(6, 1, 1, 0));
        labelPanel.add(createHyperlinkButton("Rename this file", IconsFactoryDemo.getImageIcon(IconsFactoryDemo.CollapsiblePane.RENAME)));
        labelPanel.add(createHyperlinkButton("Move this file", IconsFactoryDemo.getImageIcon(IconsFactoryDemo.CollapsiblePane.MOVE)));
        labelPanel.add(createHyperlinkButton("Copy this file", IconsFactoryDemo.getImageIcon(IconsFactoryDemo.CollapsiblePane.COPY)));
        labelPanel.add(createHyperlinkButton("Publish this file", IconsFactoryDemo.getImageIcon(IconsFactoryDemo.CollapsiblePane.PUBLISH)));
        labelPanel.add(createHyperlinkButton("Email this file", IconsFactoryDemo.getImageIcon(IconsFactoryDemo.CollapsiblePane.EMAIL)));
        labelPanel.add(createHyperlinkButton("Delete this file", IconsFactoryDemo.getImageIcon(IconsFactoryDemo.CollapsiblePane.DELET)));
        labelPanel.setOpaque(true);
        labelPanel.setBackground(Color.white);
        labelPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        panel.setContentPane(labelPanel);
//        panel.setBackground(UIManager.getColor("activeCaption"));
//        panel.setForeground(UIManager.getColor("activeCaptionText"));
        return panel;
    }

    private static CollapsiblePane createOtherPlacesPane() {
        CollapsiblePane panel = new CollapsiblePane("Other Places");
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new GridLayout(4, 1, 1, 0));
        labelPanel.add(createHyperlinkButton("Local Disk (C:)", IconsFactoryDemo.getImageIcon(IconsFactoryDemo.CollapsiblePane.LOCALDISK)));
        labelPanel.add(createHyperlinkButton("My Pictures", IconsFactoryDemo.getImageIcon(IconsFactoryDemo.CollapsiblePane.PICTURES)));
        labelPanel.add(createHyperlinkButton("My Computer", IconsFactoryDemo.getImageIcon(IconsFactoryDemo.CollapsiblePane.COMPUTER)));
        labelPanel.add(createHyperlinkButton("My Network Places", IconsFactoryDemo.getImageIcon(IconsFactoryDemo.CollapsiblePane.NETWORK)));
        labelPanel.setOpaque(true);
        labelPanel.setBackground(Color.white);
        labelPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        panel.setContentPane(labelPanel);
        return panel;
    }

    static JComponent createHyperlinkButton(String name, Icon icon) {
        final JideButton button = new JideButton(name, icon);
        button.setButtonStyle(JideButton.HYPERLINK_STYLE);

        button.setBackground(Color.white);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(0, 20));
        button.setHorizontalAlignment(SwingConstants.LEADING);

        button.setRequestFocusEnabled(true);
        button.setFocusable(true);

        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(_frame, "\"" + button.getText() + "\" is pressed.", "CollapsiblePane", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        return button;
    }
}
