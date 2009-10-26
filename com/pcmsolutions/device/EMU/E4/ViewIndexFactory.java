package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.system.IntPool;

/**
 * User: paulmeehan
 * Date: 15-Feb-2004
 * Time: 19:10:07
 */
public class ViewIndexFactory {
    private static final int MULTI_VOICE_INDEX_BASE = 1000;
    private static final int GROUPED_VOICE_INDEX_BASE = -1000;

    public static Integer getMultiVoiceIndex(ContextEditablePreset.EditableVoice[] voices) {
        if (voices[0].getGroupMode())
            return IntPool.get(GROUPED_VOICE_INDEX_BASE + voices[0].getVoiceNumber().intValue());

        return IntPool.get(MULTI_VOICE_INDEX_BASE + voices[0].getVoiceNumber().intValue());
    }

    public static Integer getVoiceSectionIndex(int sections) {
      /*
      if ((sections & VoiceSections.VOICE_CORDS) != 0) {

        }
        if ((sections & VoiceSections.VOICE_AMP_FILTER) != 0) {

        } else {
            if ((sections & VoiceSections.VOICE_AMP) != 0) {

                if ((sections & VoiceSections.VOICE_FILTER) != 0) {

                }
            }
            if ((sections & VoiceSections.VOICE_LFO) != 0) {

            }
            if ((sections & VoiceSections.VOICE_TUNING) != 0) {

            }
            if ((sections & VoiceSections.VOICE_ENVELOPES) != 0) {

            } else {
                if ((sections & VoiceSections.VOICE_AMP_ENVELOPE) != 0) {

                }
                if ((sections & VoiceSections.VOICE_FILTER_ENVELOPE) != 0) {

                }
                if ((sections & VoiceSections.VOICE_AUX_ENVELOPE) != 0) {

                }
            }
        }
        */
        return IntPool.get(sections);
    }
}