package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.selections.VoiceParameterSelection;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 12-Aug-2003
 * Time: 19:18:20
 * To change this template use Options | File Templates.
 */
public interface VoiceParameterSelectionAcceptor {
    public void setSelection(VoiceParameterSelection sel);

    public boolean willAcceptCategory(int category);

    public ContextEditablePreset.EditableVoice getEditableVoice();
}
