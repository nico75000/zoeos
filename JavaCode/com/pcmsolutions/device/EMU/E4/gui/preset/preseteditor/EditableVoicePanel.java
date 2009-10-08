package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor;

import com.pcmsolutions.device.EMU.E4.ViewIndexFactory;
import com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor.envelope.EditableAmpEnvelopePanel;
import com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor.envelope.EditableAuxEnvelopePanel;
import com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor.envelope.EditableFilterEnvelopePanel;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.VoicePanel;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.VoiceParameterTableTransferHandler;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.VoiceTitleProvider;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.selections.VoiceParameterSelection;
import com.pcmsolutions.gui.ZCommandInvocationHelper;
import com.pcmsolutions.system.Indexable;
import com.pcmsolutions.system.ZDeviceNotRunningException;

import javax.swing.*;
import java.awt.event.MouseEvent;


public class EditableVoicePanel extends VoicePanel implements VoiceParameterSelectionAcceptor, Indexable {
    protected ContextEditablePreset.EditableVoice[] voices;

    public EditableVoicePanel init(ContextEditablePreset.EditableVoice[] voices) throws ZDeviceNotRunningException, IllegalParameterIdException {
        return init(voices, true, true, true, true, true, true, true, true);
    }

    public EditableVoicePanel init(ContextEditablePreset.EditableVoice[] voices, boolean showAmpEnv, boolean showFiltEnv, boolean showAuxEnv, boolean showAmp, boolean showFilt, boolean showLFO, boolean showTuning, boolean showCords) throws ZDeviceNotRunningException, IllegalParameterIdException {
        this.voices = voices;
        super.init(voices[0], showAmpEnv, showFiltEnv, showAuxEnv, showAmp, showFilt, showLFO, showTuning, showCords);
        this.setTransferHandler(VoiceParameterTableTransferHandler.getInstance());
        return this;
    }

    public Icon getIcon() {
        return super.getIcon();
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

    protected JPanel getAmpEnvPanel() throws IllegalParameterIdException {
        return new EditableAmpEnvelopePanel().init(voices);
    }

    protected JPanel getFiltEnvPanel() throws IllegalParameterIdException {
        return new EditableFilterEnvelopePanel().init(voices);
    }

    protected JPanel getAuxEnvPanel() throws IllegalParameterIdException {
        return new EditableAuxEnvelopePanel().init(voices);
    }

    protected JPanel getCordsPanel() throws IllegalParameterIdException, ZDeviceNotRunningException {
        return new EditableCordPanel().init(voices);
    }

    protected JPanel getLFOPanel() throws ZDeviceNotRunningException, IllegalParameterIdException {
        return new EditableLFOPanel().init(voices);
    }

    protected JPanel getTuningPanel() throws ZDeviceNotRunningException, IllegalParameterIdException {
        return new EditableTuningPanel().init(voices);
    }

    protected JPanel getAmpPanel() throws ZDeviceNotRunningException, IllegalParameterIdException {
        return new EditableAmplifierPanel().init(voices);
    }

    protected JPanel getFiltPanel() throws ZDeviceNotRunningException, IllegalParameterIdException {
        return new EditableFilterPanel().init(voices);
    }

    public boolean checkPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            ReadablePreset[] presets = ((EditableVoiceTitleProvider)vtp).rle.getPresets();
            if (presets.length > 0)
                try {
                    JPopupMenu popup = new JPopupMenu();
                    for (int i = 0,j = presets.length; i < j; i++) {
                        JMenuItem jmi = ZCommandInvocationHelper.getMenu(new Object[]{presets[i]}, null, null, presets[i].getPresetDisplayName());
                        popup.add(jmi);
                    }
                    ZCommandInvocationHelper.showPopup(popup, this, e);
                    return true;
                } catch (NoSuchPresetException e1) {
                    e1.printStackTrace();
                }
        }
        return false;
    }

    public void setSelection(VoiceParameterSelection sel) {
        sel.render(voices);
    }

    public boolean willAcceptCategory(int category) {
        return true;
    }

    public ContextEditablePreset.EditableVoice getEditableVoice() {
        return voices[0];
    }

    public Integer getIndex() {
        return ViewIndexFactory.getMultiVoiceIndex(voices);
    }
}
