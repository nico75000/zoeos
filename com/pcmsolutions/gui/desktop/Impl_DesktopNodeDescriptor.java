package com.pcmsolutions.gui.desktop;

import com.pcmsolutions.gui.ZCommandInvocationHelper;

import javax.swing.*;
import java.awt.*;

/**
 * User: paulmeehan
 * Date: 24-Jan-2004
 * Time: 10:59:51
 */
public class Impl_DesktopNodeDescriptor implements DesktopNodeDescriptor {
    boolean allowsChildren;
    boolean showingWhenContainer;
    String componentAsContainerTitle;
    String componentAsContainerReducedTitle;

    boolean allowsReordering = true;
    boolean allowsGrouping = true;

    private Object[] nodes;
    private boolean showingCloseButton = true;

    public Impl_DesktopNodeDescriptor() {
        this.allowsChildren = false;
    }

    private static void customizePopup(Object obj, JPopupMenu popup) {
        customizePopup(new Object[]{obj}, popup);
    }

    private static void customizePopup(Object[] objs, JPopupMenu popup) {
        popup.addSeparator();
        Component[] comps = ZCommandInvocationHelper.getMenuComponents(objs);
        for (int i = 0; i < comps.length; i++)
            popup.add(comps[i]);
    }

    public Impl_DesktopNodeDescriptor(boolean componentShowingWithChildren) {
        this(null, componentShowingWithChildren);
    }

    public Impl_DesktopNodeDescriptor(String componentAsChildTitle, String componentAsChildReducedTitle) {
        this(null, componentAsChildTitle, componentAsChildReducedTitle);
    }

    public Impl_DesktopNodeDescriptor(boolean allowsChildren, boolean componentShowingWithChildren, String componentAsChildTitle, String componentAsChildReducedTitle) {
        this(null, allowsChildren, componentShowingWithChildren, componentAsChildTitle, componentAsChildReducedTitle);
    }

    public Impl_DesktopNodeDescriptor(Object[] nodes, boolean componentShowingWithChildren) {
        this.allowsChildren = true;
        this.showingWhenContainer = componentShowingWithChildren;
        if (nodes != null)
            this.nodes = (Object[]) nodes.clone();
    }

    public Impl_DesktopNodeDescriptor(Object[] nodes, String componentAsChildTitle, String componentAsChildReducedTitle) {
        this.allowsChildren = true;
        this.showingWhenContainer = true;
        this.componentAsContainerTitle = componentAsChildTitle;
        this.componentAsContainerReducedTitle = componentAsChildReducedTitle;
        if (nodes != null)
            this.nodes = (Object[]) nodes.clone();
    }

    public Impl_DesktopNodeDescriptor(Object[] nodes, boolean allowsChildren, boolean componentShowingWithChildren, String componentAsChildTitle, String componentAsChildReducedTitle) {
        this.allowsChildren = allowsChildren;
        this.showingWhenContainer = componentShowingWithChildren;
        this.componentAsContainerTitle = componentAsChildTitle;
        this.componentAsContainerReducedTitle = componentAsChildReducedTitle;
        if (nodes != null)
            this.nodes = (Object[]) nodes.clone();
    }

    public boolean allowsChildren() {
        return allowsChildren;
    }

    public boolean showingWhenContainer() {
        return showingWhenContainer;
    }

    public String getComponentAsContainerTitle() {
        return componentAsContainerTitle;
    }

    public String getComponentAsContainerReducedTitle() {
        return componentAsContainerReducedTitle;
    }

    public boolean allowsGrouping() {
        return allowsGrouping;
    }

    public boolean allowsReordering() {
        return allowsReordering;
    }

    public void customizePopupMenu(JPopupMenu popup) {
        if (nodes != null)
            customizePopup(nodes, popup);
    }

    public boolean isShowingCloseButton() {
        return showingCloseButton;
    }

    public void setShowingCloseButton(boolean showingCloseButton) {
        this.showingCloseButton = showingCloseButton;
    }
}
