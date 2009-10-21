/*
 * @(#)WizardDemo.java
 *
 * Copyright 2002 - 2003 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.dialog.*;
import com.jidesoft.swing.JideSwingUtilities;
import com.jidesoft.swing.MultilineLabel;
import com.jidesoft.wizard.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Demoed Component: {@link WizardDialog}
 * <br>
 * Required jar files: jide-common.jar, jide-components.jar, jide-dialogs.jar
 * <br>
 * Required L&F: Any L&F
 */
public class WizardDemo extends WizardDialog {
    public WizardDemo(Frame frame, String title) throws HeadlessException {
        super(frame, title);
    }

    protected void initComponents() {
        super.initComponents();
    }

    public static void main(String[] args) {
// switch follow lines to change style
//        int style = WizardStyle.JAVA_STYLE;
        int style = WizardStyle.WIZARD97_STYLE;
        showWizardDemo(null, true, style);
    }

    public static void showWizardDemo(Frame frame, final boolean exit, int style) {
        WizardStyle.setStyle(style);

        final WizardDemo wizard = new WizardDemo(frame, "JIDE Wizard Demo");
        wizard.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        if (style == WizardStyle.WIZARD97_STYLE) {
            wizard.setDefaultGraphic(WizardDemoIconsFactory.getImageIcon(WizardDemoIconsFactory.Wizard97.SAMPLE_IMAGE_SMALL).getImage());
        }
        else {
            wizard.setDefaultGraphic(WizardDemoIconsFactory.getImageIcon(WizardDemoIconsFactory.Metal.SAMPLE_IMAGE_SMALL).getImage());
        }

        wizard.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                if (exit) {
                    System.exit(0);
                }
                else {
                    wizard.dispose();
                }
            }
        });

        // setup model
        PageList model = new PageList();

        AbstractWizardPage page1 = new WelcomePage("Welcome to the JIDE Wizard Feature Demo",
                "This wizard will guide you through the features of Wizard Component from JIDE Software, Inc.");

        AbstractWizardPage page2 = new LicensePage("License Agreement",
                "This page shows you how to enable/disable a button based on user selection.");

        AbstractWizardPage page3 = new InfoWarningPage("Infos and Warnings",
                "This page shows you how to show info message and warning message.");

        AbstractWizardPage page4 = new ChangeNextPagePage("Change Next Page",
                "This page shows you how to change next page based on user selection.");

        AbstractWizardPage page5 = new CompletionPage("Completing the JIDE Wizard Feature Demo",
                "You have successfully run through the important features of Wizard Component from JIDE Software, Inc.");

        model.append(page1);
        model.append(page2);
        model.append(page4);
        model.append(page3);
        model.append(page5);

        wizard.setPageList(model);

        wizard.setFinishAction(new AbstractAction("Finish") {
            public void actionPerformed(ActionEvent e) {
                if (exit) {
                    System.exit(0);
                }
                else {
                    wizard.dispose();
                }
            }
        });
        wizard.setCancelAction(new AbstractAction("Cancel") {
            public void actionPerformed(ActionEvent e) {
                if (exit) {
                    System.exit(0);
                }
                else {
                    wizard.dispose();
                }
            }
        });

        wizard.pack();
        wizard.setResizable(true); // for wizard, it's better to make it not resizable.
        JideSwingUtilities.globalCenterWindow(wizard);
        wizard.setVisible(true);
    }

    public static class WelcomePage extends WelcomeWizardPage {
        public WelcomePage(String title, String description) {
            super(title, description/*, SampleWizardIconsFactory.getImageIcon(SampleWizardIconsFactory.Wizard97.SAMPLE_IMAGE_SMALL)*/);
        }

        protected void initConentPane() {
            super.initConentPane();
            addText("JIDE Software, Inc. is a leading-edge provider of Swing components for Java developers. JIDE means Java IDE (Integrated Development Environment). As the name indicated, JIDE Software's development components focus on IDE or IDE-like applications for software developers. All our products are in pure Java and Swing, to allow the most compatibilities with industrial standards.");
            addSpace();
            addText("To continue, click Next.");
        }
    }

    public static class CompletionPage extends CompletionWizardPage {
        public CompletionPage(String title, String description) {
            super(title, description);
        }

        protected void initConentPane() {
            super.initConentPane();
            addSpace();
            addText("To close this wizard, click Finish.");
        }
    }

    public static class LicensePage extends AbstractWizardPage {
        private JRadioButton _button1;
        private JRadioButton _button2;

        public LicensePage(String title, String description) {
            super(title, description, WizardDemoIconsFactory.getImageIcon(WizardDemoIconsFactory.Wizard97.SAMPLE_LOGO));
        }

        public int getLeftPaneItems() {
            return (WizardStyle.getStyle() == WizardStyle.JAVA_STYLE) ? LEFTPANE_STEPS | LEFTPANE_HELP : super.getLeftPaneItems();
        }

        public JComponent createWizardContent() {
            JPanel panel = createLicensePanel();
            panel.setBorder(getContentThinBorder());
            return panel;
        }

        public void setupWizardButtons() {
            fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.BACK);
            fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
            enableNextButton();
        }

        public JPanel createLicensePanel() {
            JPanel panel = new JPanel(new BorderLayout(4, 4));
            MultilineLabel label = new MultilineLabel("Please read the following license agreement. You must accept the terms of this agreement before continuing the wizard.");
            panel.add(label, BorderLayout.BEFORE_FIRST_LINE);
            panel.add(new JScrollPane(new JTextArea()), BorderLayout.CENTER);
            panel.add(createCheckBoxPanel(), BorderLayout.AFTER_LAST_LINE);
            return panel;
        }

        public JPanel createCheckBoxPanel() {
            _button1 = new JRadioButton("I accept the agreement");
            _button2 = new JRadioButton("I do not accept the agreement");
            _button1.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    enableNextButton();
                }
            });
            _button2.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    enableNextButton();
                }
            });
            JPanel panel = new JPanel(new GridLayout(2, 1));
            panel.add(_button1);
            panel.add(_button2);
            _button2.setSelected(true);
            ButtonGroup group = new ButtonGroup();
            group.add(_button1);
            group.add(_button2);
            JideSwingUtilities.setOpaqueRecursively(panel, false);
            return panel;
        }

        private void enableNextButton() {
            if (_button1 != null && _button2 != null) {
                if (_button1.isSelected()) {
                    fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                }
                else if (_button2.isSelected()) {
                    fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                }
            }
        }

    }

    public static class InfoWarningPage extends DefaultWizardPage {
        public InfoWarningPage(String title, String description) {
            super(title, description, WizardDemoIconsFactory.getImageIcon(WizardDemoIconsFactory.Wizard97.SAMPLE_LOGO));
        }

        protected void initConentPane() {
            super.initConentPane();
            addInfo("The information bubble icon can emphasize important yet not crucial information. " +
                    "Call addInfo(String text) in DefaultWizardPage to add this type of message.");
            addWarning("The yellow warning icon can emphasize information vital to either the success of the task or the consequences of the actions performed on the wizard page. " +
                    "Call addWarning(String text) in DefaultWizardPage to add this type of message.");
        }

        public void setupWizardButtons() {
            fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.BACK);
            fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
            fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
        }
    }

    public static class ChangeNextPagePage extends DefaultWizardPage {
        private JRadioButton _button1;
        private JRadioButton _button2;

        public ChangeNextPagePage(String title, String description) {
            super(title, description, WizardDemoIconsFactory.getImageIcon(WizardDemoIconsFactory.Wizard97.SAMPLE_LOGO));
        }

        public List getSteps() {
            List list = new ArrayList();
            list.add("Welcome to the JIDE Wizard Feature Demo");
            list.add("License Agreement");
            list.add("Change Next Page");
            list.add("...");
            return list;
        }

        public int getSelectedStepIndex() {
            return 2;
        }

        protected void initConentPane() {
            super.initConentPane();
            _button1 = new JRadioButton("If this radio button is selected, press Next button will continue to Infos and Warning page");
            _button2 = new JRadioButton("If this radio button is selected, press Next button will continue to Completion page");
            _button1.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    changeNextPage();
                }
            });
            _button2.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    changeNextPage();
                }
            });
            JPanel panel = new JPanel(new GridLayout(2, 1));
            panel.add(_button1);
            panel.add(_button2);
            ButtonGroup group = new ButtonGroup();
            group.add(_button1);
            group.add(_button2);
            addComponent(panel);
            JideSwingUtilities.setOpaqueRecursively(panel, false);
        }

        public void setupWizardButtons() {
            fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.BACK);
            fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
            fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
            changeNextPage();
        }

        private void changeNextPage() {
            if (_button1.isSelected() && getOwner() != null) {
                getOwner().setNextPage(getOwner().getPageByTitle("Infos and Warnings"));
            }
            else if (_button2.isSelected() && getOwner() != null) {
                getOwner().setNextPage(getOwner().getPageByTitle("Completing the JIDE Wizard Feature Demo"));
            }
        }
    }

}
