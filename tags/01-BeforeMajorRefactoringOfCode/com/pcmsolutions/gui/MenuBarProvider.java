package com.pcmsolutions.gui;

import javax.swing.*;

/**
 * User: paulmeehan
 * Date: 20-Jan-2004
 * Time: 04:46:59
 */
public interface MenuBarProvider {
    /*
    public static interface MenuBarProviderListener {
          public void menuBarChanged(MenuBarProvider source);
      }

      public void addMenuBarProviderListener(MenuBarProviderListener mbpl);

      public void removeMenuBarProviderListener(MenuBarProviderListener mbpl);
    */

    public boolean isMenuBarAvailable();

    public JMenuBar getMenuBar();
}
