package com.pcmsolutions.device.EMU.E4.gui.sample.samplecontext;

import com.pcmsolutions.device.EMU.E4.gui.TitleProvider;
import com.pcmsolutions.device.EMU.E4.gui.TitleProviderListener;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTablePanel;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
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
public class SampleContextEditorPanel extends JPanel implements ZDisposable, TitleProvider {
    private SampleContext sc;
    private SampleContextTable sct;
    RowHeaderedAndSectionedTablePanel scp;

    public SampleContextEditorPanel(final SampleContext sc) throws ZDeviceNotRunningException {
        AbstractAction rpc = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                new ZDBModifyThread("Refresh Sample Context") {
                    public void run() {
                    }
                }.start();
            }
        };
        rpc.putValue("tip", "Refresh Sample Context");

        sct = new SampleContextTable(sc);
        scp = new RowHeaderedAndSectionedTablePanel();
        scp.init(sct, "Show Preset Context", UIColors.getTableBorder(), rpc);
        this.sc = sc;
        add(scp);
    }

    public SampleContext getSampleContext() {
        return sc;
    }

    public SampleContextTable getSampleContextTable() {
        return sct;
    }

    public Color getBackground() {
        return UIColors.getDefaultBG();
    }

    public Color getForeground() {
        return UIColors.getDefaultFG();
    }

    public void zDispose() {
        removeAll();
        scp.zDispose();
        scp = null;
        sct = null;
        sc = null;
    }

    public String getTitle() {
        return sc.getDeviceContext().getTitle();
    }

    public String getReducedTitle() {
        return sc.getDeviceContext().getReducedTitle();
    }

    public void addTitleProviderListener(TitleProviderListener tpl) {
        sc.getDeviceContext().addTitleProviderListener(tpl);
    }

    public void removeTitleProviderListener(TitleProviderListener tpl) {
        sc.getDeviceContext().removeTitleProviderListener(tpl);
    }

    //private static final Icon icon = new SampleContextIcon(14, 14);
    public Icon getIcon() {
        //  return icon;
        return null;
    }
}
