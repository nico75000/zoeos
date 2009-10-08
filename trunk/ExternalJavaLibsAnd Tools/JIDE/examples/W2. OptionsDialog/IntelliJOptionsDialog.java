/*
 * @(#)IntelliJOptionsDialog.java
 *
 * Copyright 2002 - 2003 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.dialog.*;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.plaf.vsnet.VsnetMetalLookAndFeel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;

/**
 * Demoed Component: {@link MultiplePageDialog}
 * <br>
 * Required jar files: jide-common.jar, jide-dialogs.jar
 * <br>
 * Required L&F: Jide L&F extension required
 */
public class IntelliJOptionsDialog extends MultiplePageDialog {

    public IntelliJOptionsDialog(Frame owner, String title) throws HeadlessException {
        super(owner, title);
    }

    protected void initComponents() {
        super.initComponents();
        getContentPanel().setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 0, 10), BorderFactory.createRaisedBevelBorder()));
        getIndexPanel().setBackground(getBackground());
        getButtonPanel().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = super.createButtonPanel();
        AbstractAction okAction = new AbstractAction(ButtonResources.RB.getString("Button.ok")) {
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_AFFIRMED);
                setVisible(false);
                dispose();
                System.exit(0);
            }
        };
        AbstractAction cancelAction = new AbstractAction(ButtonResources.RB.getString("Button.cancel")) {
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_CANCELLED);
                setVisible(false);
                dispose();
                System.exit(0);
            }
        };
        ((JButton) buttonPanel.getButtonByName(ButtonNames.OK)).setAction(okAction);
        ((JButton) buttonPanel.getButtonByName(ButtonNames.CANCEL)).setAction(cancelAction);
        setDefaultCancelAction(cancelAction);
        setDefaultAction(okAction);
        return buttonPanel;
    }

    public Dimension getPreferredSize() {
        return new Dimension(800, 600);
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

        final MultiplePageDialog dialog = new IntelliJOptionsDialog(null, "IntelliJ IDEA-like Option Dialog");
        dialog.setStyle(MultiplePageDialog.ICON_STYLE);
        dialog.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                System.exit(0);
            }
        });
        PageList model = new PageList();

        AbstractDialogPage panel1 = new IntelliJOptionPage("Paths", IntelliJIconsFactory.getImageIcon(IntelliJIconsFactory.ProjectOptions.PATHS));
        AbstractDialogPage panel2 = new IntelliJOptionPage("Compiler", IntelliJIconsFactory.getImageIcon(IntelliJIconsFactory.ProjectOptions.COMPILER));
        AbstractDialogPage panel3 = new IntelliJOptionPage("Run/Debug", IntelliJIconsFactory.getImageIcon(IntelliJIconsFactory.ProjectOptions.RUNDEBUG));
        AbstractDialogPage panel4 = new IntelliJOptionPage("Debugger", IntelliJIconsFactory.getImageIcon(IntelliJIconsFactory.ProjectOptions.DEBUGGER));
        AbstractDialogPage panel5 = new IntelliJOptionPage("Local VCS", IntelliJIconsFactory.getImageIcon(IntelliJIconsFactory.ProjectOptions.LOCALVCS));
        AbstractDialogPage panel6 = new IntelliJOptionPage("VCS Support", IntelliJIconsFactory.getImageIcon(IntelliJIconsFactory.ProjectOptions.VCSSUPPORT));
        AbstractDialogPage panel7 = new IntelliJOptionPage("Web", IntelliJIconsFactory.getImageIcon(IntelliJIconsFactory.ProjectOptions.WEB));
        AbstractDialogPage panel8 = new IntelliJOptionPage("EJB", IntelliJIconsFactory.getImageIcon(IntelliJIconsFactory.ProjectOptions.EJB));
        AbstractDialogPage panel9 = new IntelliJOptionPage("JavaDoc", IntelliJIconsFactory.getImageIcon(IntelliJIconsFactory.ProjectOptions.JAVADOC));
        AbstractDialogPage panel10 = new IntelliJOptionPage("Miscellaneous", IntelliJIconsFactory.getImageIcon(IntelliJIconsFactory.ProjectOptions.MISCELLANEOUS));

        model.append(panel1);
        model.append(panel2);
        model.append(panel3);
        model.append(panel4);
        model.append(panel5);
        model.append(panel6);
        model.append(panel7);
        model.append(panel8);
        model.append(panel9);
        model.append(panel10);

        dialog.setPageList(model);

        dialog.pack();
        dialog.setVisible(true);
    }

    public static class IntelliJOptionPage extends AbstractDialogPage {
        public IntelliJOptionPage(String name) {
            super(name);
        }

        public IntelliJOptionPage(String name, Icon icon) {
            super(name, icon);
        }

        public void lazyInitialize() {
            initComponents();
        }

        public void initComponents() {
            setLayout(new BorderLayout());
            add(new JLabel("This is just a demo. \"" + getFullTitle() + "\" page is not implemented yet.", JLabel.CENTER), BorderLayout.CENTER);
        }
    }

}
