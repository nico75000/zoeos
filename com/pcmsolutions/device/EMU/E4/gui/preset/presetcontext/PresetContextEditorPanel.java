package com.pcmsolutions.device.EMU.E4.gui.preset.presetcontext;

import com.pcmsolutions.device.EMU.E4.preset.PresetContextMacros;
import com.pcmsolutions.device.EMU.E4.gui.AbstractContextTableModel;
import com.pcmsolutions.device.EMU.E4.gui.TitleProvider;
import com.pcmsolutions.device.EMU.E4.gui.TitleProviderListener;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTablePanel;
import com.pcmsolutions.device.EMU.E4.preset.PresetContext;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.gui.ZJPanel;
import com.pcmsolutions.gui.desktop.SessionableComponent;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.ZUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 24-May-2003
 * Time: 12:00:34
 * To change this template use Options | File Templates.
 */
public class PresetContextEditorPanel extends ZJPanel implements ZDisposable, TitleProvider, SessionableComponent {
    private PresetContext pc;
    private PresetContextTable pct;
    private RowHeaderedAndSectionedTablePanel pcp;

    public PresetContextEditorPanel(final PresetContext pc) {
        AbstractAction rpc = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            }
        };
        rpc.putValue("tip", "Refresh preset context");

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

    private static final String selectionTagId = PresetContextEditorPanel.class.toString() + "_SELECTION";
    private static final String filterTagId = PresetContextEditorPanel.class.toString() + "_FILTER";

    public String retrieveComponentSession() {
        StringBuffer sb = new StringBuffer();
        java.util.List<ReadablePreset> filtered =  getPresetContextTable().getFilteredElements();
        sb.append(ZUtilities.makeTaggedField(filterTagId, ZUtilities.tokenizeIntegers(PresetContextMacros.extractPresetIndexes(filtered.toArray(new ReadablePreset[filtered.size()])))));
        sb.append(ZUtilities.makeTaggedField(selectionTagId, ZUtilities.tokenizeIntegers(getPresetContextTable().getSelection().getPresetIndexes())));
        return sb.toString();
    }

    public void restoreComponentSession(String sessStr) {
        if (sessStr != null && !sessStr.equals("")) {
            String filtSess = ZUtilities.extractTaggedField(sessStr, filterTagId);
            if (filtSess != null) {
                final java.util.List sessionIndexes = Arrays.asList(ZUtilities.detokenizeIntegers(filtSess));
                ((PresetContextTableModel) getPresetContextTable().getModel()).setContextFilter(new AbstractContextTableModel.ContextFilter() {
                    public boolean filter(Integer index, String name, boolean wasFilteredPreviously) {
                        if (wasFilteredPreviously && sessionIndexes.contains(index))
                            return true;
                        return false;
                    }

                    public String getFilterName() {
                        return "Session Presets";
                    }
                });
                ((PresetContextTableModel) getPresetContextTable().getModel()).refresh(false);
                //setStatus(pcep.getPresetContextTable().getSelectedRowCount() + " of " + pcep.getPresetContextTable().getRowCount() + " presets selected");
            }
            final String selSess = ZUtilities.extractTaggedField(sessStr, selectionTagId);
            if (selSess != null) {
                getPresetContextTable().clearSelection();
                getPresetContextTable().addIndexesToSelection(ZUtilities.detokenizeIntegers(selSess));
            }
        }
    }
}
