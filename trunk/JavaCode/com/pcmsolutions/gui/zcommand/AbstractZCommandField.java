package com.pcmsolutions.gui.zcommand;

import javax.swing.*;

/**
 * User: paulmeehan
 * Date: 05-Sep-2004
 * Time: 19:17:59
 */
public abstract class AbstractZCommandField <C extends JComponent, T> implements ZCommandField<C, T> {
    final C component;
    String label;

    public AbstractZCommandField(C component, String label) {
        this(component, label, null);
    }

    public AbstractZCommandField(C component, String label, String componentTip) {
        this.component = component;
        this.label = label;
        if (componentTip == null)
            component.setToolTipText(label);
        else
            component.setToolTipText(componentTip);
    }

    public C getComponent() {
        return component;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabelText() {
        return label;
    }

    public String toString() {
        return getValue().toString();
    }
}
