package com.pcmsolutions.device.EMU.E4.gui.device;

import com.jidesoft.grid.PropertyPane;
import com.jidesoft.grid.PropertyTable;
import com.jidesoft.grid.PropertyTableModel;
import com.pcmsolutions.device.EMU.E4.gui.TitleProvider;
import com.pcmsolutions.device.EMU.E4.gui.TitleProviderListener;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.gui.FuzzyLineBorder;
import com.pcmsolutions.system.ZDisposable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * User: paulmeehan
 * Date: 15-Feb-2004
 * Time: 09:17:09
 */
public class PropertiesPanel extends JPanel implements TitleProvider, ZDisposable {
    java.util.List properties;
    TitleProvider tp;

    public void zDispose() {
        removeAll();
        properties = null;
        tp = null;
    }

    public PropertiesPanel(java.util.List properties, TitleProvider tp) {
        super(new BorderLayout());
        this.properties = new ArrayList(properties);
        this.tp = tp;
        this.add(makePropertiesComponent(), BorderLayout.CENTER);
        setBorder(new FuzzyLineBorder(UIColors.getTableBorder(), UIColors.getTableBorderWidth(), true, true));
    }

    private JComponent makePropertiesComponent() {
        PropertyTableModel ptm = new PropertyTableModel(properties);
        PropertyTable pt = new PropertyTable(ptm) {
            public Color getBackground() {
                return UIColors.getDefaultBG();
            }

            public Color getForeground() {
                return UIColors.getDefaultFG();
            }
        };

        pt.expandAll();
        PropertyPane pp = new PropertyPane(pt);

        JViewport rvp = new JViewport() {
            public Color getBackground() {
                return UIColors.getDefaultBG();
            }

            public Color getForeground() {
                return UIColors.getDefaultFG();
            }
        };
        ((JScrollPane) pp.getPropertyTable().getParent().getParent()).setViewport(rvp);
        rvp.setView(pt);
        pp.setPreferredSize(new Dimension(400, 400));
        return pp;
    }

    /*public Color getBackground() {
        return UIColors.getDefaultBG();
    }

    public Color getForeground() {
        return UIColors.getDefaultFG();
    } */

    public String getTitle() {
        return tp.getTitle();
    }

    public String getReducedTitle() {
        return tp.getReducedTitle();
    }

    public void addTitleProviderListener(TitleProviderListener tpl) {
        tp.addTitleProviderListener(tpl);
    }

    public void removeTitleProviderListener(TitleProviderListener tpl) {
        tp.removeTitleProviderListener(tpl);
    }

    public Icon getIcon() {
        return tp.getIcon();
    }

    public String getToolTipText() {
        return tp.getToolTipText();
    }
}
