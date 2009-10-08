package com.pcmsolutions.gui;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.EventObject;
import java.util.Vector;

public class ZCellEditor extends FixedLengthTextField implements CellEditor {
    String value = "";
    Vector listeners = new Vector();

    // Mimic all the constructors people expect with text fields.
    public ZCellEditor() {
        this("", 64);
    }

    public ZCellEditor(int length) {
        this("", length);
    }

    public ZCellEditor(String s) {
        this(s, 64);
    }

    public ZCellEditor(String s, int length) {
        super(s, length);

        adjustFieldLength(length);
        super.setText(s);
        // Listen to our own action events so that we know when to stateStop editing.
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (stopCellEditing()) {
                    fireEditingStopped();
                }
            }
        });
    }

    // NOTE! following is a trick to get right sizing, see JavaSwing 1st edition page 773 (Chapter 20)
    private void adjustFieldLength(int length) {
        setColumns(0);
        char[] temp = new char[length];
        Arrays.fill(temp, 'X');
        setText(new StringBuffer().append(temp).toString());
        setFont(new Font("Monospaced", Font.PLAIN, 12));
        setPreferredSize(getPreferredSize());
    }

    public Vector getListeners() {
        return (Vector) listeners.clone();
    }

    // Implement the CellEditor methods.
    public void cancelCellEditing() {
        setText("");
    }

    // Stop editing only if the user entered a valid value.
    public boolean stopCellEditing() {
        value = getText();
        if (value == null || value.equals(""))
            return false;
        return true;
    }

    public Object getCellEditorValue() {
        return value;
    }

    public boolean isCellEditable(EventObject eo) {
        if ((eo == null) ||
                ((eo instanceof MouseEvent) &&
                (((MouseEvent) eo).getClickCount() == 2))) {
            return true;
        }
        return false;
    }

    public boolean shouldSelectCell(EventObject eo) {
        return true;
    }

    public void addCellEditorListener(CellEditorListener cel) {
        listeners.addElement(cel);
    }

    public void removeCellEditorListener(CellEditorListener cel) {
        listeners.removeElement(cel);
    }

    protected void fireEditingStopped() {
        if (listeners.size() > 0) {
            ChangeEvent ce = new ChangeEvent(this);
            for (int i = listeners.size() - 1; i >= 0; i--) {
                try {
                    ((CellEditorListener) listeners.elementAt(i)).editingStopped(ce);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
