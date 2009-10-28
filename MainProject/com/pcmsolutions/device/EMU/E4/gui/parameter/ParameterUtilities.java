package com.pcmsolutions.device.EMU.E4.gui.parameter;

import com.pcmsolutions.device.EMU.E4.parameter.DeviceParameterContext;
import com.pcmsolutions.device.EMU.E4.parameter.GeneralParameterDescriptor;
import com.pcmsolutions.device.EMU.E4.selections.VoiceParameterSelection;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.system.IntPool;

import java.util.ArrayList;
import java.util.Arrays;


public class ParameterUtilities {
    public interface ParameterGroup {
        boolean containsId(Integer id);

        Integer[] getIds();
    }

    public static final Integer[] userTableIgnoreIds = new Integer[]{IntPool.get(37), IntPool.get(38)};
    public static final ParameterGroup[] userTableGroupedParameters;

    static {
        ArrayList groups = new ArrayList();
        groups.add(new ParameterGroup() {
            Integer[] keyWinIds = new Integer[]{IntPool.get(45), IntPool.get(46), IntPool.get(47), IntPool.get(48)};

            public boolean containsId(Integer id) {
                return Arrays.asList(keyWinIds).contains(id);
            }

            public Integer[] getIds() {
                return (Integer[]) keyWinIds.clone();
            }

            public String toString() {
                return "KeyWin";
            }

        });
        groups.add(new ParameterGroup() {
            Integer[] velWinIds = new Integer[]{IntPool.get(49), IntPool.get(50), IntPool.get(51), IntPool.get(52)};

            public boolean containsId(Integer id) {
                return Arrays.asList(velWinIds).contains(id);
            }

            public Integer[] getIds() {
                return (Integer[]) velWinIds.clone();
            }

            public String toString() {
                return "VelWin";
            }

        });
        groups.add(new ParameterGroup() {
            Integer[] rtWinIds = new Integer[]{IntPool.get(53), IntPool.get(54), IntPool.get(55), IntPool.get(56)};

            public boolean containsId(Integer id) {
                return Arrays.asList(rtWinIds).contains(id);
            }

            public Integer[] getIds() {
                return (Integer[]) rtWinIds.clone();
            }

            public String toString() {
                return "RTWin";
            }

        });
        userTableGroupedParameters = (ParameterGroup[]) groups.toArray(new ParameterGroup[groups.size()]);
    }

    private static final int ampToAuxEnv = 47;
    private static final int ampToFiltEnv = 23;
    private static final int filtToAuxEnv = 24;
    private static final int linkToVoiceWin = 17;
    private static final int presetToMasterFx = 222;

    static Integer[] extractIds(GeneralParameterDescriptor[] pds) {
        ArrayList ids = new ArrayList();
        for (int i = 0; i < pds.length; i++)
            ids.add(pds[i].getId());
        return (Integer[]) ids.toArray(new Integer[ids.size()]);
    }


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

    public static boolean isZoneId(int id) {
        return ((id >= 37 && id <= 40) || (id == 42) || (id >= 44 && id <= 52));
    }

    public static boolean containsOnlySampleZoneIds(DeviceParameterContext dpc, Integer[] ids) {
        for (int i = 0, j = ids.length; i < j; i++)
            if (!dpc.getZoneContext().paramExists(ids[i]))
                return false;
        return true;
    }

    public static boolean containsOnlyLinkKeyAndVelWinIds(Integer[] ids) {
        int iv;
        for (int i = 0, j = ids.length; i < j; i++) {
            iv = ids[i].intValue();
            if (!(iv >= 28 && iv <= 35))
                return false;
        }
        return true;
    }

    public static boolean containsOnlyVoiceKeyAndVelWinIds(Integer[] ids) {
        int iv;
        for (int i = 0, j = ids.length; i < j; i++) {
            iv = ids[i].intValue();
            if (!(iv >= 45 && iv <= 52))
                return false;
        }
        return true;
    }

    public static Integer[] extractSampleZoneIds(DeviceParameterContext dpc, Integer[] ids) {
        ArrayList outIds = new ArrayList();

        for (int i = 0, j = ids.length; i < j; i++)
            if (dpc.getZoneContext().paramExists(ids[i]))
                outIds.add(ids[i]);

        return (Integer[]) outIds.toArray(new Integer[outIds.size()]);
    }

    public static Integer[] extractSampleZoneIds(VoiceParameterSelection vps) throws DeviceException {
        return extractSampleZoneIds(vps.getSrcDevice().getDeviceParameterContext(), vps.getIds());
    }

    public static boolean containsOnlySampleZoneIds(VoiceParameterSelection vps) throws DeviceException {
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

    public static boolean isMainId(int id) {
        if ((id >= 37 && id <= 44) || (id >= 23 && id <= 27))
            return true;
        else
            return false;
    }

    public static boolean isLinkFilterId(int id) {
        if (id >= 251 && id <= 266)
            return true;
        else
            return false;
    }

    public static boolean isVoiceOverviewUserId(int id) {
        return !isKeyWinId(id) && !isVelWinId(id) && !isRTWinId(id) && !isMainId(id);
    }
}
