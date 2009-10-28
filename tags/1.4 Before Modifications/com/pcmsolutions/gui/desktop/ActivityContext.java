package com.pcmsolutions.gui.desktop;

import java.io.Serializable;

/**
 * User: paulmeehan
 * Date: 08-Feb-2004
 * Time: 20:36:09
 */
public interface ActivityContext extends Serializable {
    public boolean tryClosing();

    public void sendMessage(String msg);

    public boolean testCondition(String condition);

    public void closed();

    public void activated();

    public void deactivated();
}
