package com.pcmsolutions.gui.desktop;

/**
 * User: paulmeehan
 * Date: 20-Jan-2004
 * Time: 11:29:52
 */
public interface DesktopElementListener {
    public void desktopElementStatusChanged(DesktopElement source);

    public void desktopElementExpired(DesktopElement source);
}
