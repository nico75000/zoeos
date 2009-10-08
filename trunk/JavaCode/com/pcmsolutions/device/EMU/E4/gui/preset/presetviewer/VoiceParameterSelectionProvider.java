package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.selections.VoiceParameterSelection;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 12-Aug-2003
 * Time: 19:17:08
 * To change this template use Options | File Templates.
 */
public interface VoiceParameterSelectionProvider {
    public VoiceParameterSelection getSelection();

    public ReadablePreset.ReadableVoice getReadableVoice();

    // String from ParameterCategories
    public String getCategory();
}
