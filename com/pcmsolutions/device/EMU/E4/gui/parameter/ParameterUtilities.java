package com.pcmsolutions.device.EMU.E4.gui.parameter;

import com.pcmsolutions.device.EMU.E4.parameter.DeviceParameterContext;
import com.pcmsolutions.device.EMU.E4.selections.VoiceParameterSelection;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZDeviceNotRunningException;

import java.util.ArrayList;


public class ParameterUtilities {
    private static final int ampToAuxEnv = 47;
    private static final int ampToFiltEnv = 23;
    private static final int filtToAuxEnv = 24;
    private static final int linkToVoiceWin = 17;
    private static final int presetToMasterFx = 222;

    public static Integer convertPresetToMasterFxId(Integer id) {
        return IntPool.get(id.intValue() + presetToMasterFx);
    }

    public static Integer convertMasterToPresetFxId(Integer id) {
        return IntPool.get(id.intValue() - presetToMasterFx);
    }

    public static Integer convertAuxToAmpEnvelopeId(Integer id) {
        return IntPool.get(id.intValue() - ampToAuxEnv);
    }

    public static Integer convertAuxToFilterEnvelopeId(Integer id) {
        return IntPool.get(id.intValue() - filtToAuxEnv);
    }

    public static Integer convertAmpToFilterEnvelopeId(Integer id) {
        return IntPool.get(id.intValue() + ampToFiltEnv);
    }

    public static Integer convertAmpToAuxEnvelopeId(Integer id) {
        return IntPool.get(id.intValue() + ampToAuxEnv);
    }

    public static Integer convertFilterToAmpEnvelopeId(Integer id) {
        return IntPool.get(id.intValue() - ampToFiltEnv);
    }

    public static Integer convertFilterToAuxEnvelopeId(Integer id) {
        return IntPool.get(id.intValue() + filtToAuxEnv);
    }

    public static Integer convertVoiceToLinkWinId(Integer id) {
        return IntPool.get(id.intValue() - linkToVoiceWin);
    }

    public static Integer convertLinkToVoiceWinId(Integer id) {
        return IntPool.get(id.intValue() + linkToVoiceWin);
    }

    public static Integer convertVoiceEnvelopeId(int srcCat, int destCat, Integer id) {
        if (srcCat == VoiceParameterSelection.VOICE_AMPLIFIER_ENVELOPE) {

            if (destCat == VoiceParameterSelection.VOICE_AUX_ENVELOPE)
                id = ParameterUtilities.convertAuxToAmpEnvelopeId(id);
            else if (destCat == VoiceParameterSelection.VOICE_FILTER_ENVELOPE)
                id = ParameterUtilities.convertFilterToAmpEnvelopeId(id);

        } else if (srcCat == VoiceParameterSelection.VOICE_FILTER_ENVELOPE) {

            if (destCat == VoiceParameterSelection.VOICE_AUX_ENVELOPE)
                id = ParameterUtilities.convertAuxToFilterEnvelopeId(id);
            else if (destCat == VoiceParameterSelection.VOICE_AMPLIFIER_ENVELOPE)
                id = ParameterUtilities.convertAmpToFilterEnvelopeId(id);

        } else if (srcCat == VoiceParameterSelection.VOICE_AUX_ENVELOPE) {

            if (destCat == VoiceParameterSelection.VOICE_FILTER_ENVELOPE)
                id = ParameterUtilities.convertFilterToAuxEnvelopeId(id);
            else if (destCat == VoiceParameterSelection.VOICE_AMPLIFIER_ENVELOPE)
                id = ParameterUtilities.convertAmpToAuxEnvelopeId(id);

        }
        return id;
    }

    public static boolean containsOnlySampleZoneIds(DeviceParameterContext dpc, Integer[] ids) {
        for (int i = 0,j = ids.length; i < j; i++)
            if (!dpc.getZoneContext().paramExists(ids[i]))
                return false;
        return true;
    }


    public static boolean containsOnlyLinkKeyAndVelWinIds(Integer[] ids) {
        int iv;
        for (int i = 0,j = ids.length; i < j; i++) {
            iv = ids[i].intValue();
            if (!(iv >= 28 && iv <= 35))
                return false;
        }
        return true;
    }

    public static boolean containsOnlyVoiceKeyAndVelWinIds(Integer[] ids) {
        int iv;
        for (int i = 0,j = ids.length; i < j; i++) {
            iv = ids[i].intValue();
            if (!(iv >= 45 && iv <= 52))
                return false;
        }
        return true;
    }

    public static Integer[] extractSampleZoneIds(DeviceParameterContext dpc, Integer[] ids) {
        ArrayList outIds = new ArrayList();

        for (int i = 0,j = ids.length; i < j; i++)
            if (dpc.getZoneContext().paramExists(ids[i]))
                outIds.add(ids[i]);

        return (Integer[]) outIds.toArray(new Integer[outIds.size()]);
    }

    public static Integer[] extractSampleZoneIds(VoiceParameterSelection vps) throws ZDeviceNotRunningException {
        return extractSampleZoneIds(vps.getSrcDevice().getDeviceParameterContext(), vps.getIds());
    }

    public static boolean containsOnlySampleZoneIds(VoiceParameterSelection vps) throws ZDeviceNotRunningException {
        return containsOnlySampleZoneIds(vps.getSrcDevice().getDeviceParameterContext(), vps.getIds());
    }

    public static boolean isKeyWinId(int id) {
        if ((id >= 45 && id <= 48) || (id >= 28 && id <= 31))
            return true;
        else
            return false;
    }

    public static boolean isVelWinId(int id) {
        if ((id >= 49 && id <= 52) || (id >= 32 && id <= 35))
            return true;
        else
            return false;
    }

    public static boolean isRTWinId(int id) {
        if (id >= 53 && id <= 56)
            return true;
        else
            return false;
    }
}
