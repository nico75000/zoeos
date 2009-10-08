package com.pcmsolutions.gui.desktop;

import javax.swing.*;
import java.io.Serializable;

/**
 * User: paulmeehan
 * Date: 24-Jan-2004
 * Time: 11:00:28
 */
public interface DesktopNodeDescriptor extends Serializable{
    public boolean allowsChildren();

    public boolean showingWhenContainer();

    public String getComponentAsContainerTitle();

    public String getComponentAsContainerReducedTitle();

    public boolean allowsGrouping();

    public boolean allowsReordering();

    public void customizePopupMenu(JPopupMenu popup);

    public boolean isShowingCloseButton();
}
