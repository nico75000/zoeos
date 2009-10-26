package com.pcmsolutions.device.EMU.E4.gui;

import com.pcmsolutions.gui.IconAndTipCarrier;

import javax.swing.*;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 01-Jul-2003
 * Time: 01:19:15
 * To change this template use Options | File Templates.
 */
public interface TitleProvider extends IconAndTipCarrier {
    public String getTitle();

    public String getReducedTitle();

    public void addTitleProviderListener(TitleProviderListener tpl);

    public void removeTitleProviderListener(TitleProviderListener tpl);

    public static class StaticTitleProvider implements TitleProvider, Serializable {
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
}
