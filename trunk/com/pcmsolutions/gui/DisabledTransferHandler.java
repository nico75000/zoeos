package com.pcmsolutions.gui;

import javax.swing.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 18-Oct-2003
 * Time: 02:12:02
 * To change this template use Options | File Templates.
 */
public class DisabledTransferHandler extends TransferHandler {

    private static DisabledTransferHandler INSTANCE;

    private DisabledTransferHandler() {
    }

    public static DisabledTransferHandler getInstance() {
        if (INSTANCE == null)
            INSTANCE = new DisabledTransferHandler();
        return INSTANCE;
    }

    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        return false;
    }

    protected Transferable createTransferable(JComponent c) {
        return null;
    }

    public void exportAsDrag(JComponent comp, InputEvent e, int action) {
    }

    protected void exportDone(JComponent source, Transferable data, int action) {
    }

    public void exportToClipboard(JComponent comp, Clipboard clip, int action) {
    }

    public int getSourceActions(JComponent c) {
        return 0;
    }

    public Icon getVisualRepresentation(Transferable t) {
        return null;
    }

    public boolean importData(JComponent comp, Transferable t) {
        return false;
    }
}
