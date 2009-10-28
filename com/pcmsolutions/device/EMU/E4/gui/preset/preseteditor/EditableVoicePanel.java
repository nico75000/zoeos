package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.ViewIndexFactory;
import com.pcmsolutions.device.EMU.E4.gui.EnclosureMenuBar;
import com.pcmsolutions.device.EMU.E4.gui.Impl_TableExclusiveSelectionContext;
import com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor.envelope.EditableAmpEnvelopePanel;
import com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor.envelope.EditableAuxEnvelopePanel;
import com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor.envelope.EditableFilterEnvelopePanel;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.VoicePanel;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.VoiceParameterTableTransferHandler;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.VoiceTitleProvider;
import com.pcmsolutions.device.EMU.E4.gui.table.PopupTable;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterException;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.PresetException;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.selections.VoiceParameterSelection;
import com.pcmsolutions.gui.ZCommandFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;


public class EditableVoicePanel extends VoicePanel implements VoiceParameterSelectionAcceptor {
    protected ContextEditablePreset.EditableVoice[] voices;

    public EditableVoicePanel init(ContextEditablePreset.EditableVoice[] voices) throws ParameterException, DeviceException {
        return init(voices, true, true, true, true, true, true, true, true);
    }

    private static final String selectionContext = "selection";

    ZCommandFactory.ZCommandPresentationContext parameterCommandPresentationContext;

    public EditableVoicePanel init(ContextEditablePreset.EditableVoice[] voices, boolean showAmpEnv, boolean showFiltEnv, boolean showAuxEnv, boolean showAmp, boolean showFilt, boolean showLFO, boolean showTuning, boolean showCords) throws ParameterException, DeviceException {
        this.voices = voices;
        super.init(voices[0], showAmpEnv, showFiltEnv, showAuxEnv, showAmp, showFilt, showLFO, showTuning, showCords);
        this.setTransferHandler(VoiceParameterTableTransferHandler.getInstance());
        parameterCommandPresentationContext = ZCommandFactory.getToolbarPresentationContext(EditableParameterModel.cmdProviderHelper.getSupportedMarkers());
        tsc.setSelectionAction(new Impl_TableExclusiveSelectionContext.SelectionAction() {
            public void newSelection(PopupTable t) {
                if (t != null) {
                    Object[] selObjs = t.getSelObjects();
                    parameterCommandPresentationContext.setTargets(selObjs);
                } else
                    parameterCommandPresentationContext.disableContext();
            }

            public void clearedSelection(PopupTable t) {
            }
        });

        return this;
    }

    public Icon getIcon() {
        return super.getIcon();
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
    }

    public void zDispose() {
        super.zDispose();
        voices = null;
        setTransferHandler(null);
        this.setDropTarget(null);
    }

    protected VoiceTitleProvider makeVoiceTitleProvider() {
        EditableVoiceTitleProvider vtp = new EditableVoiceTitleProvider();
        vtp.init(voices);
        return vtp;
    }

    protected JPanel getAmpEnvPanel() throws ParameterException {
        return new EditableAmpEnvelopePanel().init(voices, tsc);
    }

    protected JPanel getFiltEnvPanel() throws ParameterException {
        return new EditableFilterEnvelopePanel().init(voices, tsc);
    }

    protected JPanel getAuxEnvPanel() throws ParameterException {
        return new EditableAuxEnvelopePanel().init(voices, tsc);
    }

    protected JPanel getCordsPanel() throws ParameterException {
        return new EditableCordPanel().init(voices, tsc);
    }

    protected JPanel getLFOPanel() throws ParameterException, DeviceException {
        return new EditableLFOPanel().init(voices, tsc);
    }

    protected JPanel getTuningPanel() throws ParameterException, DeviceException {
        return new EditableTuningPanel().init(voices, tsc);
    }

    protected JPanel getAmpPanel() throws ParameterException, DeviceException {
        return new EditableAmplifierPanel().init(voices, tsc);
    }

    protected JPanel getFiltPanel() throws ParameterException, DeviceException {
        return new EditableFilterPanel().init(voices, tsc);
    }

    public boolean checkPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            ReadablePreset[] presets = ((EditableVoiceTitleProvider) vtp).rle.getPresets();
            if (presets.length > 0)
                try {
                    JPopupMenu popup = new JPopupMenu();
                    for (int i = 0, j = presets.length; i < j; i++) {
                        JMenuItem jmi = ZCommandFactory.getMenu(new Object[]{presets[i]}, presets[i].getDisplayName());
                        popup.add(jmi);
                    }
                    ZCommandFactory.showPopup(popup, this, e);
                    return true;
                } catch (PresetException e1) {
                    e1.printStackTrace();
                }
        }
        return false;
    }

    public void setSelection(final VoiceParameterSelection sel) {
        sel.render(voices);
    }

    public boolean willAcceptCategory(int category) {
        return true;
    }

    public ContextEditablePreset.EditableVoice getEditableVoice() {
        return voices[0];
    }

    public Integer getIndex() {
        return ViewIndexFactory.getEditableVoiceIndex(voices);
    }

    public boolean isEnclosureNorthenComponentAvailable() {
        return true;
    }

    ZCommandFactory.ZCommandPresentationContext voiceCommandPresentationContext;

    public Component getEnclosureNorthenComponent() {
        if (encMenuBar == null) {
            encMenuBar = new EnclosureMenuBar();
            encMenuBar.addStaticMenuContext(ZCommandFactory.getMenu(new Object[]{voice.getPreset()}, "Preset"), "PRESET_MENU");
            voiceCommandPresentationContext = ZCommandFactory.getToolbarPresentationContext(ContextEditablePreset.EditableVoice.cmdProviderHelper.getSupportedMarkers(), Arrays.asList(new String[]{"Audition"}));
            voiceCommandPresentationContext.setTargets(voices);
            encMenuBar.addStaticMenuContext(voiceCommandPresentationContext.getComponents(), "Voice");
            encMenuBar.addStaticMenuContext(parameterCommandPresentationContext.getComponents(), selectionContext);
            //   encMenuBar.addStaticMenuContext(ZCommandFactory.getSuitableAsButtonComponents(new Object[]{voice.getIndex()}), "PRESET_BUTTONS");
            //   encMenuBar.addStaticMenuContext(ZCommandFactory.getMenu(new Object[]{preset.getMostCapableNonContextEditablePresetDowngrade()}, "Preset"), "PRESET");
        }
        return encMenuBar.getjMenuBar();
    }
}
