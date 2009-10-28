package com.pcmsolutions.device.EMU.E4.gui.sample.samplecontext;

import com.pcmsolutions.device.EMU.E4.SampleContextMacros;
import com.pcmsolutions.device.EMU.E4.gui.AbstractContextTableModel;
import com.pcmsolutions.device.EMU.E4.gui.TitleProvider;
import com.pcmsolutions.device.EMU.E4.gui.TitleProviderListener;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTablePanel;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.E4.sample.ReadableSample;
import com.pcmsolutions.gui.ZJPanel;
import com.pcmsolutions.gui.desktop.SessionableComponent;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.ZUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 24-May-2003
 * Time: 12:00:34
 * To change this template use Options | File Templates.
 */
public class SampleContextEditorPanel extends ZJPanel implements ZDisposable, TitleProvider, SessionableComponent {
    private SampleContext sc;
    private SampleContextTable sct;
    RowHeaderedAndSectionedTablePanel scp;

    public SampleContextEditorPanel(final SampleContext sc)  {
        AbstractAction rpc = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            }
        };
        rpc.putValue("tip", "Refresh sample context");

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

    private static final String selectionTagId = SampleContextEditorPanel.class.toString() + "SELECTION";
    private static final String filterTagId = SampleContextEditorPanel.class.toString() + "FILTER";

    public String retrieveComponentSession() {
        StringBuffer sb = new StringBuffer();
        java.util.List<ReadableSample> filtered = getSampleContextTable().getFilteredElements();
        sb.append(ZUtilities.makeTaggedField(filterTagId, ZUtilities.tokenizeIntegers(SampleContextMacros.extractSampleIndexes(filtered.toArray(new ReadableSample[filtered.size()])))));
        sb.append(ZUtilities.makeTaggedField(selectionTagId, ZUtilities.tokenizeIntegers(getSampleContextTable().getSelection().getSampleIndexes())));
        return sb.toString();
    }

    public void restoreComponentSession(String sessStr) {
        if (sessStr != null && !sessStr.equals("")) {
            String filtSess = ZUtilities.extractTaggedField(sessStr, filterTagId);
            if (filtSess != null) {
                final java.util.List sessionIndexes = Arrays.asList(ZUtilities.detokenizeIntegers(filtSess));
                ((SampleContextTableModel) getSampleContextTable().getModel()).setContextFilter(new AbstractContextTableModel.ContextFilter() {
                    public boolean filter(Integer index, String name, boolean wasFilteredPreviously) {
                        if (wasFilteredPreviously && sessionIndexes.contains(index))
                            return true;
                        return false;
                    }

                    public String getFilterName() {
                        return "Session Samples";
                    }
                });
                ((SampleContextTableModel) getSampleContextTable().getModel()).refresh(false);
            }
            final String selSess = ZUtilities.extractTaggedField(sessStr, selectionTagId);
            if (selSess != null) {
                getSampleContextTable().clearSelection();
                getSampleContextTable().addIndexesToSelection(ZUtilities.detokenizeIntegers(selSess));
            }
        }
    }

}
