package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor;

import com.pcmsolutions.device.EMU.E4.gui.HideablePanel;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.DefaultPresetViewerPanel;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.VoiceOverviewTableModel;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTablePanel;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterContext;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.selections.LinkSelection;
import com.pcmsolutions.device.EMU.E4.selections.PresetParameterSelection;
import com.pcmsolutions.device.EMU.E4.selections.VoiceSelection;
import com.pcmsolutions.gui.FocusAlerter;
import com.pcmsolutions.gui.ZCommandInvocationHelper;
import com.pcmsolutions.system.ZDeviceNotRunningException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
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
public class DefaultPresetEditorPanel extends DefaultPresetViewerPanel implements EditableLinkTable.LinkSelectionAcceptor, EditableVoiceOverviewTable.VoiceSelectionAcceptor, EditablePresetParameterTable.PresetParameterSelectionAcceptor {

    protected static final DefaultPresetEditorPanelTransferHandler dpepth = new DefaultPresetEditorPanelTransferHandler();

    public DefaultPresetEditorPanel(ContextEditablePreset p) throws ZDeviceNotRunningException {
        super(p);
        this.setTransferHandler(dpepth);
    }
     /*
        protected PresetListenerAdapter pla = new PresetListenerAdapter() {
        public void presetInitialized(final PresetInitializeEvent ev) {
            if (ev.getPreset().equals(preset.getPresetNumber()))
                updateTitle();
            try {
                loadPresetContent(false);
            } catch (NoSuchPresetException e) {
                e.printStackTrace();
            }
        }

        public void presetInitializationStatusChanged(final PresetInitializationStatusChangedEvent ev) {
            if (ev.getPreset().equals(preset.getPresetNumber())) {
                updateTitle();
                try {
                    double st = preset.getInitializationStatus();
                    if (st < -1 || st > 1) {
                        if (initProgressElement != null) {
                            ZoeosFrame.getInstance().endProgressElement(initProgressElement);
                            initProgressElement = null;
                        }
                    } else {
                        if (initProgressElement == null) {
                            initProgressElement = new Object();
                            ZoeosFrame.getInstance().beginProgressElement(initProgressElement, ZUtilities.makeMinimumLengthString("Refreshing " + preset.getPresetDisplayName() + "   [" + preset.getDeviceContext().getName() + "]", 60, true), 100);
                        }
                        ZoeosFrame.getInstance().updateProgressElement(initProgressElement, (int) (100 * Math.abs(st)));
                    }
                    return;
                } catch (NoSuchPresetException e) {
                } catch (PresetEmptyException e) {
                }
                if (initProgressElement != null) {
                    ZoeosFrame.getInstance().endProgressElement(initProgressElement);
                    initProgressElement = null;
                }
            }
        }

        public void presetRefreshed(final PresetRefreshEvent ev) {
            if (ev.getPreset().equals(preset.getPresetNumber()))
                updateTitle();
            try {
                loadPresetContent(false);
            } catch (NoSuchPresetException e) {
                e.printStackTrace();
            }
        }

        public void presetNameChanged(final PresetNameChangeEvent ev) {
            if (ev.getPreset().equals(preset.getPresetNumber()))
                updateTitle();
        }
    };
*/
    protected void makeGlobalPanel() throws ZDeviceNotRunningException {
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
        for (int i = 0,n = cats.size(); i < n; i++) {
            List ids = ppc.getIdsForCategory((String) cats.get(i));
            ArrayList models = new ArrayList();
            for (int j = 0,k = ids.size(); j < k; j++)
                try {
                    models.add(cp.getEditableParameterModel((Integer) ids.get(j)));
                } catch (IllegalParameterIdException e) {
                    e.printStackTrace();
                }
            EditableParameterModel[] pms = new EditableParameterModel[models.size()];
            models.toArray(pms);
            globalPanel.add(new HideablePanel(new RowHeaderedAndSectionedTablePanel().init(new EditablePresetParameterTable(cp, cats.get(i).toString(), pms, cats.get(i).toString().toUpperCase()), "SHOW " + cats.get(i).toString().toUpperCase(), UIColors.getTableBorder(), rp, false), false) {
                public Color getBackground() {
                    return UIColors.getDefaultBG();
                }

            });
        }
    }

    protected void makeLinkPanel() throws ZDeviceNotRunningException {
        ContextEditablePreset cp = (ContextEditablePreset) preset;
        RowHeaderedAndSectionedTablePanel lp;
        linkTable = new EditableLinkTable(cp);
        linkTable.setCustomAction(new AbstractAction("Hide/Show Filter Section") {
            public void actionPerformed(ActionEvent e) {
                //try {
                preset.getDeviceContext().getDevicePreferences().ZPREF_showLinkFilterSection.putValue(! preset.getDeviceContext().getDevicePreferences().ZPREF_showLinkFilterSection.getValue());
                /*updateLinkPanel();
                addComponents();
                revalidate();
                repaint();
                */
                // } catch (ZDeviceNotRunningException e1) {
                //   e1.printStackTrace();
                // }
            }
        });

        lp = new RowHeaderedAndSectionedTablePanel().init(linkTable, "SHOW LINKS", UIColors.getTableBorder(), rp);
        lp.getRowHeaderedTable().getRowHeader().addFocusListener(FocusAlerter.getInstance());
        linkPanel = new HideablePanel(lp, false) {
            public Color getBackground() {
                return UIColors.getDefaultBG();
            }
        };
    }

    protected void makeVoicePanel(java.util.List expansionMemory) throws ZDeviceNotRunningException {
        ContextEditablePreset cp = (ContextEditablePreset) preset;
        RowHeaderedAndSectionedTablePanel vp;
        voiceOverviewTable = new EditableVoiceOverviewTable(cp);
        if (expansionMemory != null)
            ((VoiceOverviewTableModel) voiceOverviewTable.getModel()).setExpansionMemory(expansionMemory);

        setVoiceOverviewTableCustomAction(voiceOverviewTable, rp);
        vp = new RowHeaderedAndSectionedTablePanel().init(voiceOverviewTable, "SHOW VOICES", UIColors.getTableBorder(), rp);
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
            ZCommandInvocationHelper.showPopup("Preset >", this, sels, e, null);
            return true;
        }
        return false;
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

    public void setSelection(PresetParameterSelection pps) {
        pps.render((ContextEditablePreset) preset);
    }
}
