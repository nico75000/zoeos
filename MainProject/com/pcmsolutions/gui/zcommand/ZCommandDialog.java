package com.pcmsolutions.gui.zcommand;

import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.util.ScreenUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * User: paulmeehan
 * Date: 05-Sep-2004
 * Time: 13:41:05
 */
public class ZCommandDialog extends JDialog {
    ZCommandField[] fields;

    public static final int CANCELLED = -1;
    public static final int CANCELLED_ALL = -2;
    public static final int FAILED = -3;
    public static final int COMPLETED = 0;

    private static int summaryFieldLength = 80;
    private Executable executable;
    private JLabel summaryLabel;

    private int returnValue;
    private ProgressPanel progressPanel;
    private ButtonPanel buttonPanel;
    private int numCmds, iteration;
    private Rectangle prevDlgRect;
    JLabel[] argLabels;

    public interface Executable {
        public void execute() throws Exception;
    }

    public ZCommandDialog() throws HeadlessException {
        super(ZoeosFrame.getInstance(), true);
    }

    public void init(String title, ZCommandField[] fields) {
        init(title, fields, null);
    }

    public void init(String title, ZCommandField[] fields, Component relativeTo) {
        this.setTitle(title);
        this.fields = fields;
        returnValue = FAILED;
        if (relativeTo != null)
            setLocationRelativeTo(relativeTo);
        Container dlgPane = getContentPane();
        dlgPane.removeAll();
        dlgPane.setLayout(new BoxLayout(dlgPane, BoxLayout.Y_AXIS));
        constructArgumentFields(dlgPane);
        constructButtonPanel();
        constructSummary();
        assertProgressBar();
        setResizable(false);
        pack();
        checkWidth();
        if (prevDlgRect != null)
            setBounds(prevDlgRect);
        else
            setLocation(ScreenUtilities.centreRect(ZoeosFrame.getInstance().getBounds(), getBounds()));
    }

    private void checkWidth() {
        Rectangle r = getBounds();
        if ( r.width < 250){
            r.width = 250;
            setBounds(r);
        }
    }

    private void constructButtonPanel() {
        buttonPanel = new ButtonPanel();
        getContentPane().add(buttonPanel);
    }

    public int run(Executable executable) {
        return run(executable, 1, 1);
    }

    public int run(Executable executable, int numCmds, int iteration) {
        this.executable = executable;
        this.numCmds = numCmds;
        this.iteration = iteration;
        returnValue = FAILED;
        updateLabels();
        assertProgressBar();
        buttonPanel.updateButtons(true, numCmds > 1);
        enableElements();
        pack();
        checkWidth();
        setVisible(true);
        return returnValue;
    }

    class ProgressPanel extends JPanel {
        JProgressBar pb = new JProgressBar();
        JLabel pl = new JLabel();

        public ProgressPanel() {
            super(new FlowLayout());
            add(pl);
            add(pb);
        }

        public void updateProgress() {
            pb.setMaximum(numCmds);
            pb.setValue(iteration + 1);
            pl.setText("Command " + (iteration + 1) + " of " + numCmds);
        }
    }

    private void assertProgressBar() {
        if (numCmds > 1) {
            if (progressPanel == null)
                progressPanel = new ProgressPanel();
            getContentPane().add(progressPanel);
            progressPanel.updateProgress();
        } else if (progressPanel != null)
            getContentPane().remove(progressPanel);
    }

    private void constructSummary() {
        summaryLabel = new JLabel();
        JPanel summaryPanel = new JPanel(new FlowLayout());
        summaryPanel.add(summaryLabel);
        getContentPane().add(summaryPanel);
    }

    public void setSummaryText(String summary) {
        summaryLabel.setText(ZUtilities.makeExactLengthString(summary, summaryFieldLength));
    }

    class ButtonPanel extends JPanel {
        JButton ok;
        JButton cancel;
        JButton cancelAll;

        {
            ok = new JButton(new AbstractAction("OK") {
                public void actionPerformed(ActionEvent e) {
                    performCommand();
                }
            });
            cancel = new JButton(new AbstractAction("Cancel") {
                public void actionPerformed(ActionEvent e) {
                    returnValue = CANCELLED;
                    prevDlgRect = getBounds();
                    ZCommandDialog.this.setVisible(false);
                }
            });
            cancel.setMnemonic(KeyEvent.VK_C);
            cancelAll = null;
            cancelAll = new JButton(new AbstractAction("Cancel all") {
                public void actionPerformed(ActionEvent e) {
                    returnValue = CANCELLED_ALL;
                    prevDlgRect = getBounds();
                    ZCommandDialog.this.setVisible(false);
                }
            });
            cancelAll.setMnemonic(KeyEvent.VK_A);
            ZCommandDialog.this.getRootPane().setDefaultButton(ok);
            ZCommandDialog.this.getRootPane().registerKeyboardAction(ok.getAction(),
                    KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);

            ZCommandDialog.this.getRootPane().registerKeyboardAction(cancel.getAction(),
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);

            ZCommandDialog.this.addWindowListener(new WindowAdapter() {
                public void windowOpened(WindowEvent e) {
                    //ok.requestFocus();
                }
            });
        }

        public ButtonPanel() {
            super(new FlowLayout());
            add(ok);
            add(cancel);
        }

        public void updateButtons(boolean enable, boolean showCancelAll) {
            ok.setEnabled(enable);
            cancel.setEnabled(enable);
            if (showCancelAll) {
                add(cancelAll);
                cancelAll.setEnabled(enable);
            } else
                remove(cancelAll);
        }
    }

    private void constructArgumentFields(Container dlgPane) {
        JComponent comp;
        JPanel argPanel = new JPanel(new GridLayout(fields.length, 2));
        argLabels = new JLabel[fields.length];
        for (int n = 0; n < fields.length; n++) {
            argLabels[n] = new JLabel(fields[n].getLabelText());
            argLabels[n].setAlignmentX(Component.LEFT_ALIGNMENT);
            argPanel.add(argLabels[n]);
            comp = fields[n].getComponent();
            comp.setAlignmentX(Component.RIGHT_ALIGNMENT);
            argPanel.add(comp);
        }
        dlgPane.add(argPanel);
    }

    private void updateLabels() {
        for (int n = 0; n < fields.length; n++)
            argLabels[n].setText(fields[n].getLabelText());
    }

    private void enableElements() {
        buttonPanel.updateButtons(true, numCmds > 1);
        for (int i = 0; i < fields.length; i++)
            fields[i].getComponent().setEnabled(true);
    }

    private void disableElements() {
        buttonPanel.updateButtons(false, numCmds > 1);
        for (int i = 0; i < fields.length; i++)
            fields[i].getComponent().setEnabled(false);
    }

    public void performCommand() {
        // summaryLabel.setText("Running...wait");
        final Exception[] fe = new Exception[1];
        disableElements();
        try {
            executable.execute();
            returnValue = COMPLETED;
        } catch (Exception e) {
            returnValue = FAILED;
            fe[0] = e;
        } finally {
            prevDlgRect = getBounds();
            setVisible(false);
        }
        if (returnValue == FAILED)
            JOptionPane.showMessageDialog(ZCommandDialog.this, fe[0].getMessage(), "Command failed", JOptionPane.ERROR_MESSAGE);
    }
}
