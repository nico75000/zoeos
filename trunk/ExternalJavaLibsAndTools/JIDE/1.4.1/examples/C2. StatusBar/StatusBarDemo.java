/*
 * @(#)StatusBarDemo.java
 *
 * Copyright 2002 - 2003 JIDE Software. All rights reserved.
 */

import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.status.*;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Demoed Component: {@link StatusBar}
 * <br>
 * Required jar files: jide-common.jar, jide-components.jar
 * <br>
 * Required L&F: Jide L&F extension required
 */
public class StatusBarDemo extends JFrame {

    private static StatusBarDemo _frame;

    private static StatusBar _statusBar;
    private static Timer _timer;
    private static JTextArea _textArea;

    public StatusBarDemo(String title) throws HeadlessException {
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

        _frame = new StatusBarDemo("Demo of StatusBar");
        _frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // add status bar
        _statusBar = createStatusBar();
        _textArea = new JTextArea();
        _textArea.setPreferredSize(new Dimension(800, 600));
        _frame.getContentPane().add(new JScrollPane(_textArea), BorderLayout.CENTER);
        _frame.getContentPane().add(_statusBar, BorderLayout.AFTER_LAST_LINE);

        _frame.pack();
        _frame.show();
    }

    private static StatusBar createStatusBar() {
        // setup status bar
        StatusBar statusBar = new StatusBar();
        final ProgressStatusBarItem progress = new ProgressStatusBarItem();
        progress.setCancelCallback(new ProgressStatusBarItem.CancelCallback() {
            public void cancelPerformed() {
                _timer.stop();
                _timer = null;
                progress.setStatus("Canceled");
                progress.showStatus();
            }
        });
        statusBar.add(progress, JideBoxLayout.VARY);
        ButtonStatusBarItem button = new ButtonStatusBarItem("READ-ONLY");
        button.setIcon(JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.BLANK));
        button.setPreferredWidth(20);
        statusBar.add(button, JideBoxLayout.FLEXIBLE);

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (_timer != null && _timer.isRunning())
                    return;
                _timer = new Timer(100, new ActionListener() {
                    int i = 0;

                    public void actionPerformed(ActionEvent e) {
                        if (i == 0)
                            progress.setProgressStatus("Initializing ......");
                        if (i == 10)
                            progress.setProgressStatus("Running ......");
                        if (i == 90)
                            progress.setProgressStatus("Completing ......");
                        progress.setProgress(i++);
                        if (i > 100)
                            _timer.stop();
                    }
                });
                _timer.start();
            }
        });

        final LabelStatusBarItem label = new LabelStatusBarItem("Line");
        label.setText("100:42");
        label.setAlignment(JLabel.CENTER);
        statusBar.add(label, JideBoxLayout.FLEXIBLE);

        final OvrInsStatusBarItem ovr = new OvrInsStatusBarItem();
        ovr.setPreferredWidth(100);
        ovr.setAlignment(JLabel.CENTER);
        statusBar.add(ovr, JideBoxLayout.FLEXIBLE);

        final TimeStatusBarItem time = new TimeStatusBarItem();
        statusBar.add(time, JideBoxLayout.FLEXIBLE);
        final MemoryStatusBarItem gc = new MemoryStatusBarItem();
        statusBar.add(gc, JideBoxLayout.FLEXIBLE);

        return statusBar;
    }
}
