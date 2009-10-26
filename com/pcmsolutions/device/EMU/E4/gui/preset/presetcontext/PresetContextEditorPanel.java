package com.pcmsolutions.device.EMU.E4.gui.preset.presetcontext;

import com.pcmsolutions.device.EMU.E4.gui.TitleProvider;
import com.pcmsolutions.device.EMU.E4.gui.TitleProviderListener;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTablePanel;
import com.pcmsolutions.device.EMU.E4.preset.PresetContext;
import com.pcmsolutions.system.ZDeviceNotRunningException;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.threads.ZDBModifyThread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 24-May-2003
 * Time: 12:00:34
 * To change this template use Options | File Templates.
 */
public class PresetContextEditorPanel extends JPanel implements ZDisposable, TitleProvider {
    private PresetContext pc;
    private PresetContextTable pct;
    private RowHeaderedAndSectionedTablePanel pcp;

    public PresetContextEditorPanel(final PresetContext pc) throws ZDeviceNotRunningException {
        AbstractAction rpc = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                new ZDBModifyThread("Refresh Preset Context") {
                    public void run() {
                    }
                }.start();
            }
        };
        rpc.putValue("tip", "Refresh Preset Context");

        pct = new PresetContextTable(pc);
        pcp = new RowHeaderedAndSectionedTablePanel();
        pcp.init(pct, "Show Preset Context", UIColors.getTableBorder(), rpc);
        this.pc = pc;
        add(pcp);
    }

    public PresetContext getPresetContext() {
        return pc;
    }

    public PresetContextTable getPresetContextTable() {
        return pct;
    }

    public Color getBackground() {
        return UIColors.getDefaultBG();
    }

    public Color getForeground() {
        return UIColors.getDefaultFG();
    }

    public void zDispose() {
        removeAll();
        pcp.zDispose();
        pcp = null;
        pct = null;
        pc = null;
    }

    public String getTitle() {
        return pc.getDeviceContext().getTitle();
    }

    public String getReducedTitle() {
        return pc.getDeviceContext().getReducedTitle();
    }

    public void addTitleProviderListener(TitleProviderListener tpl) {
        pc.getDeviceContext().addTitleProviderListener(tpl);
    }

    public void removeTitleProviderListener(TitleProviderListener tpl) {
        pc.getDeviceContext().removeTitleProviderListener(tpl);
    }

    //private static final Icon icon = new PresetContextIcon(14, 14);
    public Icon getIcon() {
        //  return icon;
        return null;
    }
}
