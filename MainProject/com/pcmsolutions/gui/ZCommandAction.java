package com.pcmsolutions.gui;

import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZCommand;
import com.pcmsolutions.system.ZCommandTargetsNotSpecifiedException;
import com.pcmsolutions.system.ZDisposable;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 14-Mar-2003
 * Time: 07:59:06
 * To change this template use Options | File Templates.
 */
public class ZCommandAction extends AbstractAction implements ZDisposable {
    private ZCommand command;

    public ZCommandAction(ZCommand command, boolean iconOnly) {
        this.command = command;
        Icon i = command.getIcon();
        if (!(iconOnly && i != null))
            this.putValue(Action.NAME, command.getPresentationString());

        this.putValue(Action.SMALL_ICON, i);
        putValue(Action.SHORT_DESCRIPTION, command.getDescriptiveString());
        putValue(Action.LONG_DESCRIPTION, command.getDescriptiveString());
    }

    public void actionPerformed(final ActionEvent e) {
        try {
            command.execute();
        } catch (CommandFailedException e1) {
            UserMessaging.showCommandFailed(e1.getMessage());
        } catch (ZCommandTargetsNotSpecifiedException e1) {
            e1.printStackTrace();
        }
    }

    public void zDispose() {
        command = null;
    }
}
