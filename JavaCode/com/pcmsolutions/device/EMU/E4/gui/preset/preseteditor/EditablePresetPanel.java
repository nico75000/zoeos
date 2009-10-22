package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.gui.EnclosureMenuBar;
import com.pcmsolutions.device.EMU.E4.gui.HideablePanel;
import com.pcmsolutions.device.EMU.E4.gui.Impl_TableExclusiveSelectionContext;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.preset.PresetViewModes;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.PresetPanel;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.VoiceOverviewTableModel;
import com.pcmsolutions.device.EMU.E4.gui.table.PopupTable;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTablePanel;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterContext;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterException;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.selections.LinkSelection;
import com.pcmsolutions.device.EMU.E4.selections.PresetParameterSelection;
import com.pcmsolutions.device.EMU.E4.selections.VoiceSelection;
import com.pcmsolutions.gui.FocusAlerter;
import com.pcmsolutions.gui.ZCommandFactory;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.util.ClassUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 24-May-2003
 * Time: 12:00:34
 * To change this template use Options | File Templates.
 */
public class EditablePresetPanel extends PresetPanel implements EditableLinkTable.LinkSelectionAcceptor, EditableVoiceOverviewTable.VoiceSelectionAcceptor, EditablePresetParameterTable.PresetParameterSelectionAcceptor {

    protected static final EditablePresetPanelTransferHandler dpepth = new EditablePresetPanelTransferHandler();

    ZCommandFactory.ZCommandPresentationContext parameterCommandPresentationContext;
    ZCommandFactory.ZCommandPresentationContext voiceCommandPresentationContext;
    ZCommandFactory.ZCommandPresentationContext linkCommandPresentationContext;
    ZCommandFactory.ZCommandPresentationContext zoneCommandPresentationContext;

    public EditablePresetPanel(ContextEditablePreset p, boolean incVoices, boolean incLinks, boolean incGlobal, int voiceTableMode) throws DeviceException {
        super(p, incVoices, incLinks, incGlobal, voiceTableMode);
        this.setTransferHandler(dpepth);
        parameterCommandPresentationContext = ZCommandFactory.getToolbarPresentationContext(EditableParameterModel.cmdProviderHelper.getSupportedMarkers());
        voiceCommandPresentationContext = ZCommandFactory.getToolbarPresentationContext(ContextEditablePreset.EditableVoice.cmdProviderHelper.getSupportedMarkers());
        linkCommandPresentationContext = ZCommandFactory.getToolbarPresentationContext(ContextEditablePreset.EditableLink.cmdProviderHelper.getSupportedMarkers());
        zoneCommandPresentationContext = ZCommandFactory.getToolbarPresentationContext(ContextEditablePreset.EditableVoice.EditableZone.cmdProviderHelper.getSupportedMarkers());

        getTableExclusiveSelectionContext().setSelectionAction(new Impl_TableExclusiveSelectionContext.SelectionAction() {
            public void newSelection(PopupTable t) {
                if (t != null) {
                    ZCommandFactory.ZCommandPresentationContext nextContext = null;
                    Object[] selObjs = t.getSelObjects();
                    selObjs = ZUtilities.getRealObjects(ZCommandFactory.extractZCommandProviders(selObjs));

                    if (ClassUtility.areAllInstanceOf(selObjs, EditableParameterModel.class)) {
                        nextContext = parameterCommandPresentationContext;
                    } else if (ClassUtility.areAllInstanceOf(selObjs, ContextEditablePreset.EditableVoice.class)) {
                        nextContext = voiceCommandPresentationContext;
                    } else if (ClassUtility.areAllInstanceOf(selObjs, ContextEditablePreset.EditableLink.class)) {
                        nextContext = linkCommandPresentationContext;
                    } else if (ClassUtility.areAllInstanceOf(selObjs, ContextEditablePreset.EditableVoice.EditableZone.class)) {
                        nextContext = zoneCommandPresentationContext;
                    }
                    if (nextContext == null) {
                            getDefaultSelectionPresentationContext().disableContext();
                        adjustSelectionPresentationContext(getDefaultSelectionPresentationContext());
                    } else {
                        adjustSelectionPresentationContext(nextContext);
                        nextContext.setTargets(selObjs);
                    }
                } else {
                        getDefaultSelectionPresentationContext().disableContext();
                    adjustSelectionPresentationContext(getDefaultSelectionPresentationContext());
                }
            }

            public void clearedSelection(PopupTable t) {
            }
        });
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
    }

    protected void makeGlobalPanel() throws DeviceException {
        ContextEditablePreset cp = (ContextEditablePreset) preset;
        ParameterContext ppc = preset.getDeviceParameterContext().getPresetContext();
        List cats = ppc.getCategories();
        Collections.sort(cats);
        globalPanel = new JPanel() {
            public Color getBackground() {
                return UIColors.getDefaultBG();
            }
        };
        globalPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        for (int i = 0, n = cats.size(); i < n; i++) {
            List ids = ppc.getIdsForCategory((String) cats.get(i));
            ArrayList models = new ArrayList();
            for (int j = 0, k = ids.size(); j < k; j++)
                try {
                    models.add(cp.getEditableParameterModel((Integer) ids.get(j)));
                } catch (ParameterException e) {
                    e.printStackTrace();
                }
            EditableParameterModel[] pms = new EditableParameterModel[models.size()];
            models.toArray(pms);
            EditablePresetParameterTable eppt = new EditablePresetParameterTable(cp, cats.get(i).toString(), pms, cats.get(i).toString().toUpperCase());
            getTableExclusiveSelectionContext().addTableToContext(eppt);
            globalPanel.add(new HideablePanel(new RowHeaderedAndSectionedTablePanel().init(eppt, "SHOW " + cats.get(i).toString().toUpperCase(), UIColors.getTableBorder(), refreshPreset, false), false) {
                public Color getBackground() {
                    return UIColors.getDefaultBG();
                }

            });
        }
    }

    protected void makeLinkPanel() throws DeviceException {
        ContextEditablePreset cp = (ContextEditablePreset) preset;
        RowHeaderedAndSectionedTablePanel lp;
        linkTable = new EditableLinkTable(cp, PresetViewModes.LINK_MODE_MAIN_WIN);
        linkTable.setCustomAction(new AbstractAction("Hide/Show Filter Section") {
            public void actionPerformed(ActionEvent e) {
                preset.getDeviceContext().getDevicePreferences().ZPREF_showLinkFilterSection.putValue(!preset.getDeviceContext().getDevicePreferences().ZPREF_showLinkFilterSection.getValue());
            }
        });
        getTableExclusiveSelectionContext().addTableToContext(linkTable);
        lp = new RowHeaderedAndSectionedTablePanel().init(linkTable, "SHOW LINKS", UIColors.getTableBorder(), refreshPreset);
        lp.getRowHeaderedTable().getRowHeader().addFocusListener(FocusAlerter.getInstance());
        linkPanel = new HideablePanel(lp, false) {
            public Color getBackground() {
                return UIColors.getDefaultBG();
            }
        };
    }

    protected void makeVoicePanel(java.util.List expansionMemory) throws DeviceException {
        ContextEditablePreset cp = (ContextEditablePreset) preset;
        RowHeaderedAndSectionedTablePanel vp;
        voiceOverviewTable = new EditableVoiceOverviewTable(cp, getVoiceTableMode());
        if (expansionMemory != null)
            ((VoiceOverviewTableModel) voiceOverviewTable.getModel()).setExpansionMemory(expansionMemory);

        setVoiceOverviewTableCustomAction(voiceOverviewTable);
        getTableExclusiveSelectionContext().addTableToContext(voiceOverviewTable);
        vp = new RowHeaderedAndSectionedTablePanel().init(voiceOverviewTable, "SHOW VOICES", UIColors.getTableBorder(), refreshPreset);
        vp.getRowHeaderedTable().getRowHeader().addFocusListener(FocusAlerter.getInstance());
        voicePanel = new HideablePanel(vp, false) {
            public Color getBackground() {
                return UIColors.getDefaultBG();
            }
        };
    }

    public boolean checkPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            Object[] sels = new Object[]{preset};
            ZCommandFactory.showPopup("Preset >", this, sels, e);
            return true;
        }
        return false;
    }

    protected void addMenuContexts() {
        getEncMenuBar().addStaticMenuContext(ZCommandFactory.getMenu(new Object[]{preset}, "Preset"), "PRESET_MENU");
        ZCommandFactory.ZCommandPresentationContext c = ZCommandFactory.getTargetedButtonPresentationContext(new Object[]{preset});
        c.setTargets(new Object[]{preset});
        getEncMenuBar().addStaticMenuContext(c.getComponents(), "PRESET_BUTTONS");
        parameterCommandPresentationContext.disableContext();
        setActiveSelectionPresentationContext(parameterCommandPresentationContext);
        setDefaultSelectionPresentationContext(parameterCommandPresentationContext);
        getEncMenuBar().addDynamicMenuContext(getActiveSelectionPresentationContext().getComponents(), selectionContext);
    }

    public String getPostfix() {
        return "[ed]";
    }

    public void setSelection(LinkSelection ls) {
        if (linkTable.getRowHeader() instanceof EditableLinkTable.LinkSelectionAcceptor)
            ((EditableLinkTable.LinkSelectionAcceptor) linkTable.getRowHeader()).setSelection(ls);
    }

    public void setSelection(VoiceSelection vs) {
        if (voiceOverviewTable.getRowHeader() instanceof EditableVoiceOverviewTable.VoiceSelectionAcceptor)
            ((EditableVoiceOverviewTable.VoiceSelectionAcceptor) voiceOverviewTable.getRowHeader()).setSelection(vs);
    }

    public void setSelection(final PresetParameterSelection pps) {
        pps.render((ContextEditablePreset) preset);
    }

    public void zDispose() {
        super.zDispose();
        parameterCommandPresentationContext.zDispose();
        voiceCommandPresentationContext.zDispose();
        linkCommandPresentationContext.zDispose();
        zoneCommandPresentationContext.zDispose();
        parameterCommandPresentationContext = null;
        voiceCommandPresentationContext = null;
        linkCommandPresentationContext = null;
        zoneCommandPresentationContext = null;
    }
}
