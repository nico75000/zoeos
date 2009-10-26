package com.pcmsolutions.gui;

import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZCommand;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.threads.ZDBModifyThread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 14-Mar-2003
 * Time: 07:59:06
 * To change this template use Options | File Templates.
 */
public class ZCommandAction extends AbstractAction implements ZDisposable {

    private ZCommand[] commands;
    private Rectangle dlgRect;

    public ZCommandAction(ZCommand[] commands) {
        if (commands.length < 1)
            throw new IllegalArgumentException("Need at least one command");
        this.commands = commands;

        if (commands[0].getNumberOfArguments() > 0)
            this.putValue(Action.NAME, commands[0].getPresentationString() + ZUtilities.DOT_POSTFIX);
        else
            this.putValue(Action.NAME, commands[0].getPresentationString());

        Icon i = commands[0].getIcon();
        this.putValue(Action.SMALL_ICON, i);
        putValue(Action.SHORT_DESCRIPTION, commands[0].getDescriptiveString());
        putValue(Action.LONG_DESCRIPTION, commands[0].getDescriptiveString());
    }

    public void actionPerformed(final ActionEvent e) {
        new ZDBModifyThread("ZCommandAction") {
            public void run() {
                ZCommand command;
                int retVal = ZCommandInitializer.COMPLETED;
                int numCmds = commands.length;

                for (int n = 0; n < numCmds; n++) {
                    command = commands[n];
                    if (command.getNumberOfArguments() == 0) {
                        try {
                            tryExecute(command, null);
                            Thread.yield();
                        } catch (final Exception e1) {
                            JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), e1.getMessage(), "Command Failed", JOptionPane.ERROR_MESSAGE);
                            /*   try {
                                   SwingUtilities.invokeAndWait(new Runnable() {
                                       public void run() {
                                       }
                                   });
                               } catch (InterruptedException e2) {
                                   e2.printStackTrace();
                               } catch (InvocationTargetException e2) {
                                   e2.printStackTrace();
                               } */
                        }
                    } else {
                        ZCommandInitializer zci = new ZCommandInitializer();

                        if (dlgRect != null)
                            zci.setDlgRect(dlgRect);

                        try {
                            if (e.getSource() instanceof Component)
                                retVal = zci.run(command, ZoeosFrame.getInstance(), (Component) e.getSource(), numCmds, n);
                            else
                                retVal = zci.run(command, ZoeosFrame.getInstance(), null, numCmds, n);
                        } catch (Exception e) {
                            return;
                        }
                        dlgRect = zci.dlgRect;

                        if (retVal == ZCommandInitializer.CANCELLED_ALL)
                            return;
                    }
                }
            }
        }.start();
    }

    private void tryExecute(ZCommand command, Object[] args) throws CommandFailedException {
        String vs = command.getVerificationString();
        if (vs != null) {
            if (JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), vs, command.getPresentationString(), JOptionPane.YES_NO_OPTION) != 0)
                return;
        }
        command.execute(this, args);
    }

    public void zDispose() {
        commands = null;
    }
}
