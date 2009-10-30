/*
 * @(#)Office2003CommandBarFactory.java
 *
 * Copyright 2002 - 2004 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.action.CommandBar;
import com.jidesoft.action.DefaultDockableBarHolder;
import com.jidesoft.action.DockableBarContext;
import com.jidesoft.action.event.CommandMenuBar;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.plaf.office2003.Office2003Painter;
import com.jidesoft.plaf.office2003.Office2003Theme;
import com.jidesoft.swing.JideButton;
import com.jidesoft.swing.JideMenu;
import com.jidesoft.swing.JideSplitButton;
import com.jidesoft.utils.SystemInfo;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;

/**
 */
public class Office2003CommandBarFactory extends CommandBarFactory {
    public static CommandBar createMenuCommandBar(DefaultDockableBarHolder frame) {
        CommandBar commandBar = new CommandMenuBar("Menu Bar");
        commandBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
        commandBar.getContext().setInitIndex(0);
        commandBar.getContext().setInitSideSubindex(0);
// uncomment next line will make menu bar dock only at north or south
//        commandBar.setCanSide(DockableBarContext.DOCK_SIDE_HORIZONTAL);
        commandBar.setPaintBackground(false);
        commandBar.setStretch(true);
        commandBar.setFloatable(true);

        commandBar.add(createFileMenu());
        addDemoMenus(commandBar, new String[]{"Edit", "View", "Insert", "Format", "Tools", "Table", "Window", "Help"});
        commandBar.add(createLookAndFeelMenu(frame));

        return commandBar;
    }

    private static JMenu createFileMenu() {
        JMenuItem item;

        JMenu fileMenu = new JideMenu("File");
        fileMenu.setMnemonic('F');

        item = createSplitButton("New", Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Standard.NEW));
        item.add(new JMenuItem("New Blank Document"));
        item.add(new JMenuItem("New Web Page"));
        item.add(new JMenuItem("New E-mail Message"));
        item.add(new JMenuItem("Others ..."));
        fileMenu.add(item);

        item = new JMenuItem("Open...");
        fileMenu.add(item);

        item = new JMenuItem("Close");
        fileMenu.add(item);

        fileMenu.addSeparator();

        item = new JMenuItem("Exit");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(item);

        return fileMenu;
    }


    private static JMenu createLookAndFeelMenu(final DefaultDockableBarHolder frame) {
        JMenuItem item;
        JMenu menu = new JideMenu("Look and Feel");
        menu.setMnemonic('L');

        item = new JMenuItem("Vsnet LookAndFeel (Windows)");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    UIManager.setLookAndFeel(WindowsLookAndFeel.class.getName());
                }
                catch (ClassNotFoundException e1) {
                }
                catch (InstantiationException e1) {
                }
                catch (IllegalAccessException e1) {
                }
                catch (UnsupportedLookAndFeelException e1) {
                }
                LookAndFeelFactory.installJideExtension(LookAndFeelFactory.VSNET_STYLE);
                frame.getDockableBarManager().updateComponentTreeUI();
            }
        });
        menu.add(item);

        item = new JMenuItem("Office 2003 LookAndFeel (Windows)");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    UIManager.setLookAndFeel(WindowsLookAndFeel.class.getName());
                }
                catch (ClassNotFoundException e1) {
                }
                catch (InstantiationException e1) {
                }
                catch (IllegalAccessException e1) {
                }
                catch (UnsupportedLookAndFeelException e1) {
                }
                LookAndFeelFactory.installJideExtension(LookAndFeelFactory.OFFICE2003_STYLE);
                frame.getDockableBarManager().updateComponentTreeUI();
            }
        });
        menu.add(item);

        JMenu themeMenu = new JideMenu("Themes (for Office 2003 L&F only)");
        Collection themes = ((Office2003Painter) Office2003Painter.getInstance()).getAvailableThemes();
        for (Iterator iterator = themes.iterator(); iterator.hasNext();) {
            Office2003Theme theme = (Office2003Theme) iterator.next();
            item = new JMenuItem(theme.getThemeName());
            item.addActionListener(new AbstractAction(theme.getThemeName()) {
                public void actionPerformed(ActionEvent e) {
                    ((Office2003Painter) Office2003Painter.getInstance()).setColorName((String) getValue(Action.NAME));
                    frame.getDockableBarManager().updateComponentTreeUI();
                }
            });
            themeMenu.add(item);
        }

        menu.add(themeMenu);

        menu.addSeparator();

        item = new JMenuItem("Eclipse LookAndFeel (Windows)");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    UIManager.setLookAndFeel(WindowsLookAndFeel.class.getName());
                }
                catch (ClassNotFoundException e1) {
                }
                catch (InstantiationException e1) {
                }
                catch (IllegalAccessException e1) {
                }
                catch (UnsupportedLookAndFeelException e1) {
                }
                LookAndFeelFactory.installJideExtension(LookAndFeelFactory.ECLIPSE_STYLE);
                frame.getDockableBarManager().updateComponentTreeUI();
            }
        });
        menu.add(item);

        item = new JMenuItem("Metal LookAndFeel");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    UIManager.setLookAndFeel(MetalLookAndFeel.class.getName());
                }
                catch (ClassNotFoundException e1) {
                }
                catch (InstantiationException e1) {
                }
                catch (IllegalAccessException e1) {
                }
                catch (UnsupportedLookAndFeelException e1) {
                }
                LookAndFeelFactory.installJideExtension(LookAndFeelFactory.VSNET_STYLE);
                frame.getDockableBarManager().updateComponentTreeUI();
            }
        });
        menu.add(item);

        item = new JMenuItem("Aqua LookAndFeel");
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    UIManager.setLookAndFeel(LookAndFeelFactory.AQUA_LNF);
                }
                catch (ClassNotFoundException e1) {
                }
                catch (InstantiationException e1) {
                }
                catch (IllegalAccessException e1) {
                }
                catch (UnsupportedLookAndFeelException e1) {
                }
                LookAndFeelFactory.installJideExtension(LookAndFeelFactory.VSNET_STYLE);
                frame.getDockableBarManager().updateComponentTreeUI();
            }
        });
        item.setEnabled(SystemInfo.isMacOSX());
        menu.add(item);

        return menu;
    }

    public static CommandBar createStandardCommandBar() {
        CommandBar commandBar = new CommandBar("Standard");
        commandBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
        commandBar.getContext().setInitMode(DockableBarContext.STATE_HORI_DOCKED);
        commandBar.getContext().setInitIndex(1);
        commandBar.getContext().setInitSideSubindex(0);

        JideSplitButton newButton = (JideSplitButton) commandBar.add(createSplitButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Standard.NEW)));
        newButton.add(new JMenuItem("New Blank Document"));
        newButton.add(new JMenuItem("New Web Page"));
        newButton.add(new JMenuItem("New E-mail Message"));
        newButton.add(new JMenuItem("Others ..."));

        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Standard.OPEN)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Standard.SAVE)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Standard.PERMISSION)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Standard.EMAIL)));
        commandBar.addSeparator();

        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Standard.PRINT)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Standard.PRINT_PREVIEW)));
        commandBar.addSeparator();

        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Standard.SPELLING_GRAMMAR)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Standard.RESEARCH)));
        commandBar.addSeparator();

        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Standard.CUT)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Standard.COPY)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Standard.PASTE)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Standard.FORMAT_PAINTER)));
        commandBar.addSeparator();

        commandBar.add(createSplitButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Standard.UNDO)));
        JideSplitButton redoButton = (JideSplitButton) commandBar.add(createSplitButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Standard.REDO)));
        redoButton.setEnabled(false);
        commandBar.addSeparator();

        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Standard.INSERT_HYPERLINK)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Standard.INSERT_TABLE)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Standard.INSERT_EXCEL)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Standard.COLUMNS)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Standard.DRAWING)));
        commandBar.addSeparator();

        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Standard.DOCUMENT_MAP)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Standard.SHOW_HIDE_SYMBOL)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Standard.HELP)));
        return commandBar;
    }

    public static CommandBar createFormattingCommandBar() {
        CommandBar commandBar = new CommandBar("Formatting");
        commandBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
        commandBar.getContext().setInitMode(DockableBarContext.STATE_HORI_DOCKED);
        commandBar.getContext().setInitIndex(2);
        commandBar.getContext().setInitSideSubindex(0);

        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Formatting.STYLE_FORMATTING)));
        commandBar.add(createListComboBox("Verdana"));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Formatting.FORMAT_FONT)));
        commandBar.addSeparator();

        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Formatting.BOLD)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Formatting.ITALIC)));
        commandBar.addSeparator();

        ButtonGroup alignmentGroup = new ButtonGroup();
        JideButton button = (JideButton) commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Formatting.ALIGN_LEFT)));
        button.setSelected(true);
        alignmentGroup.add(button);
        button = (JideButton) commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Formatting.ALIGN_CENTER)));
        alignmentGroup.add(button);
        button = (JideButton) commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Formatting.ALIGN_RIGHT)));
        alignmentGroup.add(button);
        button = (JideButton) commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Formatting.JUSTIFY)));
        alignmentGroup.add(button);
        button = (JideButton) commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Formatting.DISTRIBUTED)));
        alignmentGroup.add(button);

        JideSplitButton splitButton = (JideSplitButton) commandBar.add(createSplitButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Formatting.LINE_SPACING)));
        ButtonGroup lineSpaceGroup = new ButtonGroup();
        AbstractButton radioButton = (AbstractButton) splitButton.add(new JRadioButtonMenuItem("1.0"));
        lineSpaceGroup.add(radioButton);
        radioButton = (AbstractButton) splitButton.add(new JRadioButtonMenuItem("1.5"));
        lineSpaceGroup.add(radioButton);
        radioButton = (AbstractButton) splitButton.add(new JRadioButtonMenuItem("2.0"));
        lineSpaceGroup.add(radioButton);
        radioButton = (AbstractButton) splitButton.add(new JRadioButtonMenuItem("2.5"));
        lineSpaceGroup.add(radioButton);
        radioButton = (AbstractButton) splitButton.add(new JRadioButtonMenuItem("3.0"));
        lineSpaceGroup.add(radioButton);
        splitButton.add(new JMenuItem("More ..."));
        commandBar.addSeparator();

        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Formatting.NUMBERING)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Formatting.BULLETS)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Formatting.DECREASE_INDENT)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Formatting.INCREASE_INDENT)));
        commandBar.addSeparator();

        commandBar.add(createColorButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Formatting.HIGHLIGHT)));
        commandBar.add(createSplitButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Formatting.OUTSIDE_BORDER)));
        commandBar.add(createColorButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Formatting.FONT_COLOR)));
        return commandBar;
    }

    public static CommandBar createDrawingCommandBar() {
        CommandBar commandBar = new CommandBar("Drawing");
        commandBar.getContext().setInitSide(DockableBarContext.DOCK_SIDE_SOUTH);
        commandBar.getContext().setInitIndex(0);
        commandBar.getContext().setInitSideSubindex(0);

        JMenu menu = (JMenu) commandBar.add(createMenu("Draw", 'D'));
        JMenuItem item = new JMenuItem("<< Demo Application >>");
        menu.add(item);


        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Drawing.SELECT_OBJECT)));
        commandBar.addSeparator();

        menu = (JMenu) commandBar.add(createMenu("AutoShapes", 'U'));
        item = new JMenuItem("<< Demo Application >>");
        menu.add(item);

        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Drawing.LINE)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Drawing.ARROW)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Drawing.RECTANGLE)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Drawing.OVAL)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Drawing.TEXTBOX)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Drawing.VERTICAL_TEXTBOX)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Drawing.INSERT_WORDART)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Drawing.INSERT_DIAGRAM_ORGCHART)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Drawing.INSERT_CLIPART)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Drawing.INSERT_PICTURE)));
        commandBar.addSeparator();

        commandBar.add(createColorButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Drawing.FILL_COLOR)));
        commandBar.add(createColorButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Drawing.LINE_COLOR)));
        commandBar.add(createColorButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Drawing.FONT_COLOR)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Drawing.LINE_STYLE)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Drawing.DASH_STYLE)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Drawing.ARROW_STYLE)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Drawing.SHADOW_STYLE)));
        commandBar.add(createButton(Office2003IconsFactory.getImageIcon(Office2003IconsFactory.Drawing.THREED_STYLE)));

        return commandBar;
    }
}
