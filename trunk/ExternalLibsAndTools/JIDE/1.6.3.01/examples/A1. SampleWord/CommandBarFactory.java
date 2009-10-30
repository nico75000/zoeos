/*
 * @(#)$fileName
 *
 * Copyright 2002 - 2004 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.action.CommandBar;
import com.jidesoft.action.DockableBarContext;
import com.jidesoft.action.event.CommandMenuBar;
import com.jidesoft.combobox.JideColorSplitButton;
import com.jidesoft.combobox.ListComboBox;
import com.jidesoft.swing.JideButton;
import com.jidesoft.swing.JideMenu;
import com.jidesoft.swing.JideSplitButton;
import com.jidesoft.utils.Lm;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 */
public class CommandBarFactory {
    public static CommandBar createMenuCommandBar(JMenuBar menuBar) {
        CommandBar commandBar = new CommandMenuBar("Menu Bar");
        commandBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
        commandBar.setPaintBackground(false);
        commandBar.setStretch(true);
        commandBar.setFloatable(true);
        commandBar.add(menuBar);
        return commandBar;
    }

    protected static JideButton createButton(Icon icon) {
        JideButton button = new JideButton(icon);
        if (Lm.AF_DEBUG) {
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("clicked");
                }
            });
        }
        button.setOpaque(false);
        button.setRequestFocusEnabled(false);
        button.setFocusable(false);
        return button;
    }

    protected static JideButton createButton(final String text, Icon icon) {
        JideButton button = new JideButton(text, icon);
        if (Lm.AF_DEBUG) {
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("clicked");
                }
            });
        }
        button.setOpaque(false);
        button.setRequestFocusEnabled(false);
        button.setFocusable(false);
        return button;
    }

    protected static JideSplitButton createSplitButton(Icon icon) {
        JideSplitButton splitButton = new JideSplitButton();
        if (Lm.AF_DEBUG) {
            splitButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("split clicked");
                }
            });
        }
        splitButton.setIcon(icon);
        splitButton.setOpaque(false);
        splitButton.setRequestFocusEnabled(false);
        splitButton.setFocusable(false);
        return splitButton;
    }

    protected static JideSplitButton createSplitButton(final String text, Icon icon) {
        JideSplitButton splitButton = new JideSplitButton(text);
        if (Lm.AF_DEBUG) {
            splitButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("split clicked");
                }
            });
        }
        splitButton.setIcon(icon);
        splitButton.setOpaque(true);
        splitButton.setRequestFocusEnabled(false);
        splitButton.setFocusable(false);
        return splitButton;
    }

    protected static JideSplitButton createColorButton(Icon icon) {
        JideSplitButton splitButton = new JideColorSplitButton(icon);
        if (Lm.AF_DEBUG) {
            splitButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("split clicked");
                }
            });
        }
        splitButton.setRolloverEnabled(false);
        splitButton.setOpaque(false);
        splitButton.setRequestFocusEnabled(false);
        splitButton.setFocusable(false);
        return splitButton;
    }

    protected static JideMenu createMenu(String text, char mnemonic) {
        JideMenu menu = new JideMenu(text);
        menu.setMnemonic(mnemonic);
        menu.setOpaque(false);
        return menu;
    }

    protected static JComboBox createComboBox(String value) {
        JComboBox comboBox = new JComboBox(new Object[]{value + "      "});
        comboBox.setOpaque(false);
        return comboBox;
    }

    protected static ListComboBox createListComboBox(String value) {
        ListComboBox comboBox = new ListComboBox(new Object[]{value});
        comboBox.setPrototypeDisplayValue("AAAAAAAAAAAAAAAA"); // this text will be used to calculate the preferred size of combobox
        comboBox.setOpaque(false);
        return comboBox;
    }

    protected static void addDemoMenus(JComponent menuBar, String[] menus) {
        for (int i = 0; i < menus.length; i++) {
            String s = menus[i];
            JMenuItem item;
            JMenu menu = new JideMenu(s);
            menu.setMnemonic(s.charAt(0));

            item = new JMenuItem("<< Empty >>");
            menu.add(item);
            menuBar.add(menu);
        }
    }

}
