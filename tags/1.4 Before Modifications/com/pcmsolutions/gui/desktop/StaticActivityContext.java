package com.pcmsolutions.gui.desktop;

import java.io.Serializable;

/**
 * User: paulmeehan
 * Date: 08-Feb-2004
 * Time: 20:37:49
 */
public class StaticActivityContext implements ActivityContext, Serializable {
    private boolean closable;
    public static StaticActivityContext TRUE = new StaticActivityContext(true);
    public static StaticActivityContext FALSE = new StaticActivityContext(false);

    public boolean tryClosing() {
        return closable;
    }

    public void sendMessage(String msg) {
    }

    public boolean testCondition(String condition) {
        return false;
    }

    public void closed() {
    }

    public void activated() {
    }

    public void deactivated() {
    }

    private StaticActivityContext(boolean closable) {
        this.closable = closable;
    }
}
