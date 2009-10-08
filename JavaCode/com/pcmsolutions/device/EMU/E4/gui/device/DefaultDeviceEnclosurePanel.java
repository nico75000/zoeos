package com.pcmsolutions.device.EMU.E4.gui.device;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.system.Indexable;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZDisposable;

import javax.swing.*;
import java.awt.*;


public class DefaultDeviceEnclosurePanel extends AbstractDeviceEnclosurePanel implements ZDisposable, Indexable {
    protected JComponent enclosedComponent;
    protected JScrollPane scrollPane;

    public void init(DeviceContext device, JComponent enclosedComponent) throws Exception {
        super.init(device);

        this.enclosedComponent = enclosedComponent;
        syncPanel();
    }

    protected void buildRunningPanel() {
        runningPanel = new JPanel() {
            public Color getForeground() {
                return UIColors.getDefaultFG();
            }

            public Color getBackground() {
                return UIColors.getDefaultBG();
            }
        };
        scrollPane = new JScrollPane(this.enclosedComponent);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(UIColors.getTableRowHeight());
        scrollPane.getVerticalScrollBar().setBlockIncrement(UIColors.getTableRowHeight() * 4);
        scrollPane.setWheelScrollingEnabled(true);

        runningPanel.setLayout(new BorderLayout());
        runningPanel.add(scrollPane, BorderLayout.CENTER);
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public JComponent getEnclosedComponent() {
        return enclosedComponent;
    }

    public Color getBackground() {
        return UIColors.getDefaultBG();
    }

    public Color getForeground() {
        return UIColors.getDefaultFG();
    }

    public void zDispose() {
        super.zDispose();
        if (enclosedComponent instanceof ZDisposable)
            ((ZDisposable) enclosedComponent).zDispose();
        enclosedComponent = null;
        scrollPane = null;
    }

    public Integer getIndex() {
        if (enclosedComponent instanceof Indexable)
            return ((Indexable) enclosedComponent).getIndex();
        return IntPool.get(0);
    }
    /*
    public String getTitle() {
        return ((TitleProvider) enclosedComponent).getTitle();
    }

    public String getReducedTitle() {
        return ((TitleProvider) enclosedComponent).getReducedTitle();
    }

    public final void addTitleProviderListener(TitleProviderListener tpl) {
        if (enclosedComponent instanceof TitleProvider)
            ((TitleProvider) enclosedComponent).addTitleProviderListener(tpl);
    }

    public final void removeTitleProviderListener(TitleProviderListener tpl) {
        if (enclosedComponent instanceof TitleProvider)
            ((TitleProvider) enclosedComponent).removeTitleProviderListener(tpl);
    }

    public Icon getIcon() {
        return ((TitleProvider) enclosedComponent).getIcon();
    }
    */
}
