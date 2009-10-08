package com.pcmsolutions.device.EMU.E4.gui;

import javax.swing.*;

/**
 * User: paulmeehan
 * Date: 10-Feb-2004
 * Time: 18:57:55
 */
public class StaticTitleProvider implements TitleProvider{
    private final String title;
    private final String reducedTitle;
    private final Icon icon;

    public StaticTitleProvider(String title, String reducedTitle, Icon icon) {
        this.title = title;
        this.reducedTitle = reducedTitle;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public String getReducedTitle() {
        return reducedTitle;
    }

    public void addTitleProviderListener(TitleProviderListener tpl) {
    }

    public void removeTitleProviderListener(TitleProviderListener tpl) {
    }

    public Icon getIcon() {
        return icon;
    }

    public String getToolTipText() {
        return title;
    }
}
