package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.parameter.ID;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.device.EMU.E4.sample.ReadableSample;
import com.pcmsolutions.device.EMU.E4.sample.SampleEmptyException;
import com.pcmsolutions.gui.ProgressMultiBox;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.system.*;
import com.pcmsolutions.util.ClassUtility;
import com.pcmsolutions.util.IntegerUseMap;
import com.pcmsolutions.util.RangePartitioner;

import javax.swing.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 12-Oct-2003
 * Time: 18:37:53
 * To change this template use Options | File Templates.
 */
public class PresetContextMacros {
    public static boolean arePresetIndexesEmpty(PresetContext pc, Integer lowPreset, int count) throws NoSuchContextException, NoSuchPresetException {
        for (int i = 0; i < count; i++)
            if (!pc.isPresetEmpty(IntPool.get(lowPreset.intValue() + i)))
                return false;
        return true;
    }

    public static boolean arePresetIndexesEmpty(PresetContext pc, Integer[] presets) throws NoSuchContextException, NoSuchPresetException {
        for (int i = 0; i < presets.length; i++)
            if (!pc.isPresetEmpty(presets[i]))
                return false;
        return true;
    }

    public static void loadPresetsToContext(final IsolatedPreset[] presets, final Integer[] indexes, final PresetContext pc, Map sampleIndexMap, boolean showProgress) throws CommandFailedException {
        DecimalFormat df = new DecimalFormat("0000");
        Map linkMap = new HashMap();

        for (int i = 0; i < presets.length; i++)
            linkMap.put(presets[i].getOriginalIndex(), indexes[i]);

        Zoeos z = Zoeos.getInstance();
        final Object pobj = new Object();
        if (showProgress)
            z.beginProgressElement(pobj, ZUtilities.makeExactLengthString("Loading presets...", 88), presets.length);

        try {
            for (int i = 0; i < presets.length; i++) {
                try {
                    z.updateProgressElementTitle(pobj, "Loading preset \"" + presets[i].getName() + "\" to " + df.format(indexes[i]));
                    pc.newPreset(presets[i], indexes[i], presets[i].getName(), sampleIndexMap, IntPool.get(0), linkMap);
                } finally {
                    if (showProgress)
                        z.updateProgressElement(pobj);
                }
            }
        } catch (NoSuchPresetException e) {
            throw new CommandFailedException("no such preset");
        } catch (NoSuchContextException e) {
            throw new CommandFailedException("problem with preset context");
        } finally {
            if (showProgress) {
                z.updateProgressElement(pobj, ProgressMultiBox.PROGRESS_DONE_TITLE);
                z.endProgressElement(pobj);
            }
        }
    }

    public static void setVoicesParam(ContextEditablePreset.EditableVoice[] voices, Integer[] ids, Integer[] vals) throws NoSuchPresetException, NoSuchContextException {
        Map vm = new HashMap();
        Set vs;
        PresetContext pc;
        for (int v = 0; v < voices.length; v++) {
            pc = voices[v].getPresetContext();
            vs = (Set) vm.get(pc);
            if (vs == null) {
                vs = new HashSet();
                vm.put(pc, vs);
            }
            vs.add(voices[v]);
        }
        for (Iterator p = vm.keySet().iterator(); p.hasNext();) {
            pc = (PresetContext) p.next();
            vs = (Set) vm.get(pc);
            setContextVoicesParam((ContextEditablePreset.EditableVoice[]) vs.toArray(new ContextEditablePreset.EditableVoice[vs.size()]), ids, vals);
        }
    }

    public static void setContextVoicesParam(ContextEditablePreset.EditableVoice[] voices, final Integer[] ids, final Integer[] vals) throws NoSuchPresetException, NoSuchContextException {
        PresetContext.VoiceParameterProfile[] ppa = new PresetContext.VoiceParameterProfile[voices.length * ids.length];
        for (int v = 0; v < voices.length; v++) {
            for (int i = 0; i < ids.length; i++) {
                final Integer f_id = ids[i];
                final Integer f_val = vals[i];
                final Integer f_preset = voices[v].getPresetNumber();
                final Integer f_voice = voices[v].getVoiceNumber();
                ppa[ids.length * v + i] = new PresetContext.VoiceParameterProfile() {
                    public Integer getVoice() {
                        return f_voice;
                    }

                    public Integer getPreset() {
                        return f_preset;
                    }

                    public Integer getId() {
                        return f_id;
                    }

                    public Integer getValue() {
                        return f_val;
                    }
                };
            }
        }
        voices[0].getPresetContext().setDiversePresetParams(ppa);
    }

    public static int presetZoneCount(PresetContext pc, Integer preset) throws NoSuchVoiceException, NoSuchContextException, NoSuchPresetException, PresetEmptyException {
        int zc = 0;
        for (int i = 0, j = pc.numVoices(preset); i < j; i++)
            zc += pc.numZones(preset, IntPool.get(i));
        return zc;
    }

    public static IntegerUseMap getSampleUsage(IsolatedPreset[] presets) {
        IntegerUseMap m = new IntegerUseMap();
        for (int i = 0; i < presets.length; i++) {
            m.mergeUseMap(presets[i].referencedSampleUsage());
        }
        return m;
    }

    public static IntegerUseMap getSampleUsage(PresetContext pc, Integer[] presets) throws NoSuchContextException, NoSuchPresetException, PresetEmptyException {
        IntegerUseMap m = new IntegerUseMap();
        for (int i = 0; i < presets.length; i++)
            m.mergeUseMap(pc.presetSampleUsage(presets[i]));
        return m;
    }

    public static boolean autoMapGroupKeyWin(PresetContext pc, Integer preset, Integer group) throws PresetEmptyException, NoSuchPresetException, NoSuchGroupException, NoSuchContextException {
        return autoMapVoiceKeyWin(pc, preset, pc.getVoiceIndexesInGroup(preset, group));
    }

    public static boolean autoMapZoneKeyWin(ContextEditablePreset.EditableVoice.EditableZone[] zones) throws PresetEmptyException, NoSuchPresetException {
        RangePartitioner rp = new RangePartitioner(0, 127);
        try {
            for (int i = 0; i < zones.length; i++)
                rp.addPoint(zones[i].getZoneParams(new Integer[]{ID.origKey})[0].intValue());
            RangePartitioner.Point[] points = rp.getPoints();
            for (int i = 0; i < zones.length; i++) {
                zones[i].setZonesParam(ID.keyLow, IntPool.get(0));
                zones[i].setZonesParam(ID.keyHigh, IntPool.get(127));
                zones[i].setZonesParam(ID.keyLow, IntPool.get(points[i].getLow()));
                zones[i].setZonesParam(ID.keyHigh, IntPool.get(points[i].getHigh()));
            }
            return true;
        } catch (IllegalParameterIdException e) {
        } catch (NoSuchVoiceException e) {
        } catch (ParameterValueOutOfRangeException e) {
        } catch (NoSuchZoneException e) {
        }
        return false;
    }

    public static boolean autoMapVoiceKeyWin(ContextEditablePreset.EditableVoice[] voices) throws PresetEmptyException, NoSuchPresetException {
        RangePartitioner rp = new RangePartitioner(0, 127);
        try {
            for (int i = 0; i < voices.length; i++) {
                int nz = voices[i].numZones();
                if (nz > 0)
                    for (int z = 0; z < nz; z++)
                        rp.addPoint(voices[i].getEditableZone(IntPool.get(z)).getZoneParams(new Integer[]{ID.origKey})[0].intValue());
                else
                    rp.addPoint(voices[i].getVoiceParams(new Integer[]{ID.origKey})[0].intValue());
            }
            RangePartitioner.Point[] points = rp.getPoints();
            int rpi = 0;
            for (int i = 0; i < voices.length; i++) {
                voices[i].setVoicesParam(ID.keyLow, IntPool.get(0));
                voices[i].setVoicesParam(ID.keyHigh, IntPool.get(127));

                int nz = voices[i].numZones();
                if (nz > 0) {
                    for (int z = 0; z < nz; z++) {
                        voices[i].getEditableZone(IntPool.get(z)).setZonesParam(ID.keyLow, IntPool.get(points[rpi].getLow()));
                        voices[i].getEditableZone(IntPool.get(z)).setZonesParam(ID.keyHigh, IntPool.get(points[rpi++].getHigh()));
                    }
                } else {
                    voices[i].setVoicesParam(ID.keyLow, IntPool.get(points[i].getLow()));
                    voices[i].setVoicesParam(ID.keyHigh, IntPool.get(points[i].getHigh()));
                }
            }
            return true;
        } catch (IllegalParameterIdException e) {
        } catch (NoSuchVoiceException e) {
        } catch (ParameterValueOutOfRangeException e) {
        } catch (NoSuchZoneException e) {
        }
        return false;
    }

    public static boolean autoMapVoiceKeyWin(final PresetContext pc, final Integer preset, Integer[] voices) throws PresetEmptyException, NoSuchPresetException, NoSuchContextException {
        RangePartitioner rp = new RangePartitioner(0, 127);
        voices = (Integer[]) voices.clone();
        try {
            for (int i = 0; i < voices.length; i++) {
                int nz = pc.numZones(preset, voices[i]);
                if (nz > 0)
                    for (int z = 0; z < nz; z++)
                        rp.addPoint(pc.getZoneParams(preset, voices[i], IntPool.get(z), new Integer[]{ID.origKey})[0].intValue());
                else
                    rp.addPoint(pc.getVoiceParams(preset, voices[i], new Integer[]{ID.origKey})[0].intValue());
            }
            RangePartitioner.Point[] points = rp.getPoints();
            int rpi = 0;
            for (int i = 0; i < voices.length; i++) {
                pc.setVoicesParam(preset, new Integer[]{voices[i]}, ID.keyLow, new Integer[]{IntPool.get(0)});
                pc.setVoicesParam(preset, new Integer[]{voices[i]}, ID.keyHigh, new Integer[]{IntPool.get(127)});
                int nz = pc.numZones(preset, voices[i]);
                if (nz > 0)
                    for (int z = 0; z < nz; z++) {
                        pc.setZonesParam(preset, voices[i], new Integer[]{IntPool.get(z)}, ID.keyLow, new Integer[]{IntPool.get(points[rpi].getLow())});
                        pc.setZonesParam(preset, voices[i], new Integer[]{IntPool.get(z)}, ID.keyHigh, new Integer[]{IntPool.get(points[rpi++].getHigh())});
                    }
                else {
                    pc.setVoicesParam(preset, new Integer[]{voices[i]}, ID.keyLow, new Integer[]{IntPool.get(points[rpi].getLow())});
                    pc.setVoicesParam(preset, new Integer[]{voices[i]}, ID.keyHigh, new Integer[]{IntPool.get(points[rpi++].getHigh())});
                }
            }
            return true;
        } catch (IllegalParameterIdException e) {
        } catch (NoSuchVoiceException e) {
        } catch (ParameterValueOutOfRangeException e) {
        } catch (NoSuchZoneException e) {
        }
        return false;
    }

    public static String getVoiceSampleName(PresetContext pc, Integer preset, Integer voice) throws NoSuchVoiceException, NoSuchPresetException, PresetEmptyException, NoSuchContextException, NoSuchSampleException, SampleEmptyException {
        try {
            Integer sample = pc.getVoiceParams(preset, voice, new Integer[]{ID.sample})[0];
            if (sample.intValue() > 0)
                return pc.getRootSampleContext().getSampleName(sample);
            else
                return DeviceContext.MULTISAMPLE;
        } catch (IllegalParameterIdException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getZoneSampleName(PresetContext pc, Integer preset, Integer voice, Integer zone) throws NoSuchVoiceException, NoSuchPresetException, PresetEmptyException, NoSuchContextException, NoSuchSampleException, SampleEmptyException, NoSuchZoneException {
        try {
            Integer sample = pc.getZoneParams(preset, voice, zone, new Integer[]{ID.sample})[0];
            if (sample.intValue() > 0)
                return pc.getRootSampleContext().getSampleName(sample);
            else
                return E4Device.EMPTY_SAMPLE;
        } catch (IllegalParameterIdException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void trySetOriginalKeyFromSampleName(PresetContext pc, Integer presets[], int mode) {
        Integer preset;
        int nv;
        //try {
        for (int i = 0; i < presets.length; i++) {
            preset = presets[i];
            try {
                switch (mode) {
                    case PresetContext.PRESET_VOICES_SELECTOR:
                        nv = pc.numVoices(preset);
                        for (int v = 0; v < nv; v++)
                            if (pc.numZones(preset, IntPool.get(v)) == 0)
                                pc.trySetOriginalKeyFromName(preset, IntPool.get(v), getVoiceSampleName(pc, preset, IntPool.get(v)));
                        break;
                    case PresetContext.PRESET_ZONES_SELECTOR:
                        nv = pc.numVoices(preset);
                        for (int v = 0; v < nv; v++) {
                            int nz = pc.numZones(preset, IntPool.get(v));
                            for (int z = 0; z < nz; z++)
                                pc.trySetOriginalKeyFromName(preset, IntPool.get(v), IntPool.get(z), getZoneSampleName(pc, preset, IntPool.get(v), IntPool.get(z)));
                        }
                        break;
                    case PresetContext.PRESET_VOICES_AND_ZONES_SELECTOR:
                        nv = pc.numVoices(preset);
                        for (int v = 0; v < nv; v++) {
                            int nz = pc.numZones(preset, IntPool.get(v));
                            if (nz == 0)
                                pc.trySetOriginalKeyFromName(preset, IntPool.get(v), getVoiceSampleName(pc, preset, IntPool.get(v)));
                            for (int z = 0; z < nz; z++)
                                pc.trySetOriginalKeyFromName(preset, IntPool.get(v), IntPool.get(z), getZoneSampleName(pc, preset, IntPool.get(v), IntPool.get(z)));
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("illegal mode");
                }
            } catch (NoSuchPresetException e) {
                e.printStackTrace();
            } catch (PresetEmptyException e) {
                e.printStackTrace();
            } catch (NoSuchContextException e) {
                e.printStackTrace();
            } catch (NoSuchVoiceException e) {
                e.printStackTrace();
            } catch (NoSuchSampleException e) {
                e.printStackTrace();
            } catch (SampleEmptyException e) {
                e.printStackTrace();
            } catch (NoSuchZoneException e) {
                e.printStackTrace();
            }
        }
        /*} catch (NoSuchPresetException e) {
        } catch (PresetEmptyException e) {
        } catch (NoSuchContextException e) {
        } */
    }

    public static int copyPresetDeep(Integer[] presets, PresetContext pc, Integer destPreset, boolean copyEmpties, boolean findEmpties, boolean translateLinks, boolean provideFeedback) throws NoSuchPresetException, NoSuchContextException {
        Set s = new HashSet();
        for (int i = 0; i < presets.length; i++)
            s.addAll(pc.getPresetSet(presets[i]));

        /*  if (provideFeedback) {
              if (JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), "Copying " + s.size() + " presets. Continue?", "Preset Copy Deep", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != 0)
                  return notCopied = s.size();
          }*/

        Integer[] srcPresets = (Integer[]) s.toArray(new Integer[s.size()]);
        Arrays.sort(srcPresets);
        Integer[] destPresets;

        if (findEmpties) {
            List le = pc.findEmptyPresetsInContext(IntPool.get(s.size()), destPreset, IntPool.get(Integer.MAX_VALUE));
            if (le.size() != s.size()) {
                JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), "Could not find " + s.size() + " beyond " + getPresetDisplayName(pc, destPreset), "Problem", JOptionPane.ERROR_MESSAGE);
                return s.size();
            }
            destPresets = (Integer[]) le.toArray(new Integer[le.size()]);
        } else
            destPresets = ZUtilities.fillIncrementally(new Integer[s.size()], destPreset.intValue());

        Map translationMap = null;
        if (translateLinks) {
            translationMap = new HashMap();
            for (int i = 0; i < srcPresets.length; i++)
                translationMap.put(srcPresets[i], destPresets[i]);
        }

        return copyPresets(pc, srcPresets, destPresets, translationMap, copyEmpties, provideFeedback, "Preset Copy Deep");
    }

    public static int copyPresets(PresetContext pc, Integer[] srcPresets, Integer[] destPresets, boolean copyEmpties, boolean provideFeedback, String feedbackTitle) throws NoSuchPresetException, NoSuchContextException {
        return copyPresets(pc, srcPresets, destPresets, null, copyEmpties, provideFeedback, feedbackTitle);
    }

    public static int copyPresets(PresetContext pc, Integer[] srcPresets, Integer[] destPresets, Map linkPresetTranslationMap, boolean copyEmpties, boolean provideFeedback, String feedbackTitle) throws NoSuchPresetException, NoSuchContextException {
        int notCopied = 0;
        if (srcPresets.length != destPresets.length)
            throw new IllegalArgumentException("number of src/dest presets mismatch");

        Zoeos z = Zoeos.getInstance();
        final Object po = new Object();
        if (provideFeedback) {
            String confirmStr = PresetContextMacros.getOverwriteConfirmationString(pc, destPresets);
            if (JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), confirmStr, "Confirm " + feedbackTitle, JOptionPane.YES_NO_OPTION) != 0)
                return destPresets.length;
            z.beginProgressElement(po, ZUtilities.makeExactLengthString("Preset Copy", 60), srcPresets.length);
        }

        Arrays.sort(srcPresets);
        Arrays.sort(destPresets);
        try {
            for (int i = srcPresets.length - 1; i >= 0; i--) {
                try {
                    if (linkPresetTranslationMap == null)
                        pc.copyPreset(srcPresets[i], destPresets[i]);
                    else
                        pc.copyPreset(srcPresets[i], destPresets[i], linkPresetTranslationMap);
                } catch (PresetEmptyException e) {
                    // source is empty
                    if (copyEmpties)
                        try {
                            pc.erasePreset(destPresets[i]);
                        } catch (PresetEmptyException e1) {
                        }
                    else
                        notCopied++;
                } finally {
                    if (provideFeedback) {
                        z.updateProgressElement(po, "Copied " + getPresetDisplayName(pc, srcPresets[i]));
                    }
                }
            }
        } finally {
            if (provideFeedback)
                z.endProgressElement(po);
        }

        if (provideFeedback) {
            if (notCopied == srcPresets.length)
                JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), (srcPresets.length > 1 ? "None of the source presets could be copied" : "The source preset could not be copied"), "Problem", JOptionPane.ERROR_MESSAGE);
            else if (notCopied > 0)
                JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), notCopied + " of " + srcPresets.length + " source presets could not be copied", "Problem", JOptionPane.ERROR_MESSAGE);
        }
        return notCopied;
    }

    public static String getPresetDisplayName(PresetContext pc, Integer preset) {
        String name = "";
        try {
            name = new AggRemoteName(preset, pc.getPresetName(preset)).toString();
        } catch (NoSuchPresetException e) {
        } catch (PresetEmptyException e) {
            name = new AggRemoteName(preset, DeviceContext.EMPTY_PRESET).toString();
        }
        return name;
    }

    public static String getOverwriteConfirmationString(PresetContext pc, Integer destIndex, int num) {
        Integer[] indexes = new Integer[num];
        ZUtilities.fillIncrementally(indexes, destIndex.intValue());
        return getOverwriteConfirmationString(pc, indexes);
    }

    public static String getOverwriteConfirmationString(PresetContext pc, Integer[] indexes) {
        int numEmpty = 0;
        int num = indexes.length;
        try {
            numEmpty = pc.numEmpties(indexes);
        } catch (NoSuchPresetException e) {
        } catch (NoSuchContextException e) {
        }
        if (num != 1) {
            if (num == numEmpty)
                return "OK to overwrite " + num + " empty preset locations ?";
            else
                return "OK to overwrite " + num + " preset locations (" + (num - numEmpty) + " non-empty) ?";
        } else if (numEmpty == 1)
            return "OK to overwrite empty preset location ?";
        else {
            String name = null;
            try {
                name = new AggRemoteName(indexes[0], pc.getPresetName(indexes[0])).toString();
                return "OK to overwrite " + name + " ?";
            } catch (NoSuchPresetException e) {
                e.printStackTrace();
            } catch (PresetEmptyException e) {
                e.printStackTrace();
            }
            return "OK to overwrite non-empty preset at location " + new DecimalFormat("0000").format(indexes[0]) + " ?";
        }
    }

    public static ReadablePreset[] filterPresetsReferencingSamples(ReadablePreset[] presets, ReadableSample[] samples) throws NoSuchPresetException {
        return filterPresetsReferencingSamples(presets, SampleContextMacros.extractSampleIndexes(samples));
    }

    public static boolean confirmInitializationOfPresets(ReadablePreset[] presets) throws NoSuchPresetException {
        int uc = PresetContextMacros.howManyUninitializedPresets(presets);
        if (uc > 0 && !UserMessaging.askYesNo(uc + (uc == 1 ? " preset" : " presets") + " will need to be initialized. Do you want to proceed?"))
            return false;
        return true;
    }

    public static ReadablePreset[] filterPresetsReferencingSamples(ReadablePreset[] presets, Integer[] samples) throws NoSuchPresetException {
        ArrayList filtered = new ArrayList();
        for (int i = 0; i < presets.length; i++)
            try {
                if (presets[i].presetSampleUsage().containsAnyOf(samples))
                    filtered.add(presets[i]);
            } catch (PresetEmptyException e) {
                e.printStackTrace();
            }
        return (ReadablePreset[]) filtered.toArray(new ReadablePreset[filtered.size()]);
    }

    public static IntegerUseMap getPresetSampleUsage(PresetContext pc, Integer[] presets) throws NoSuchPresetException, NoSuchContextException {
        IntegerUseMap um = new IntegerUseMap();
        for (int i = 0; i < presets.length; i++)
            try {
                um.mergeUseMap(pc.presetSampleUsage(presets[i]));
            } catch (PresetEmptyException e) {
                // ignore
            }
        return um;
    }

    public static IntegerUseMap getPresetSampleUsage(ReadablePreset[] presets) throws NoSuchPresetException {
        IntegerUseMap um = new IntegerUseMap();
        for (int i = 0; i < presets.length; i++)
            try {
                um.mergeUseMap(presets[i].presetSampleUsage());
            } catch (PresetEmptyException e) {
                // ignore
            }
        return um;
    }

    public static IntegerUseMap getPresetLinkPresetUsage(PresetContext pc, Integer[] presets) throws NoSuchPresetException, NoSuchContextException {
        IntegerUseMap um = new IntegerUseMap();
        for (int i = 0; i < presets.length; i++)
            try {
                um.mergeUseMap(pc.presetLinkPresetUsage(presets[i]));
            } catch (PresetEmptyException e) {
                // ignore
            }
        return um;
    }

    // returns (sub)set of specified preset indexes referencing any of the sample indexes passed
    public static Integer[] getPresetsReferencingSamples(PresetContext pc, Integer[] presets, Integer[] samples) throws NoSuchContextException, NoSuchPresetException {
        HashSet pl = new HashSet();
        for (int i = 0; i < presets.length; i++) {
            try {
                if (pc.presetSampleUsage(presets[i]).containsAnyOf(samples))
                    pl.add(presets[i]);
            } catch (PresetEmptyException e) {
                // ignore
            }
        }
        return (Integer[]) pl.toArray(new Integer[pl.size()]);
    }

    public static int howManyUninitializedPresets(PresetContext pc, Integer[] presets) throws NoSuchContextException, NoSuchPresetException {
        int uic = 0;
        for (int i = 0; i < presets.length; i++) {
            int st = pc.getPresetState(presets[i]);
            if (st != RemoteObjectStates.STATE_INITIALIZED || st != RemoteObjectStates.STATE_EMPTY)
                uic++;
        }
        return uic;
    }

    public static int howManyUninitializedPresets(ReadablePreset[] presets) throws NoSuchPresetException {
        int uic = 0;
        for (int i = 0; i < presets.length; i++) {
            int st = presets[i].getPresetState();
            if (st != RemoteObjectStates.STATE_INITIALIZED && st != RemoteObjectStates.STATE_EMPTY)
                uic++;
        }
        return uic;
    }

    public static ReadablePreset[] extractReadablePresets(Object[] objects) {
        Object[] rp1 = ClassUtility.extractInstanceOf(objects, ReadablePreset.class);
        ReadablePreset[] rp2 = new ReadablePreset[rp1.length];
        System.arraycopy(rp1, 0, rp2, 0, rp1.length);
        return rp2;
    }

    public static boolean areAllSameContext(ReadablePreset[] presets) {
        if (presets.length != 0) {
            PresetContext pc = presets[0].getPresetContext();
            for (int i = 1; i < presets.length; i++) {
                if (!presets[i].getPresetContext().equals(pc))
                    return false;
            }
            return true;
        }
        return false;
    }

    public static Integer[] extractPresetIndexes
            (ReadablePreset[] presets) {
        Integer[] indexes = new Integer[presets.length];
        for (int i = 0; i < presets.length; i++)
            indexes[i] = presets[i].getPresetNumber();
        return indexes;
    }

    public static Integer[] extractUniquePresetIndexes
            (ReadablePreset[] presets) {
        Set s = new HashSet();
        for (int i = 0; i < presets.length; i++)
            s.add(presets[i].getPresetNumber());
        return (Integer[]) s.toArray(new Integer[s.size()]);
    }

    public static void applySamplesToPreset
            (PresetContext
            pc, Integer
            preset, Integer[] samples) throws ParameterValueOutOfRangeException, PresetEmptyException, NoSuchContextException, TooManyVoicesException, NoSuchPresetException, ZDeviceNotRunningException, NoSuchSampleException, TooManyZonesException {
        if (samples.length == 0)
            return;
        int num = samples.length;
        boolean wasEmpty = false;

        if (pc.isPresetEmpty(preset)) {
            try {
                pc.newPreset(preset, pc.getDeviceContext().getDefaultSampleContext().getSampleName(samples[0]));
            } catch (SampleEmptyException e) {
                pc.newPreset(preset, DeviceContext.UNTITLED_PRESET);
            }
            wasEmpty = true;
        }
        if (num == 1) {
            int res;
            if (wasEmpty) {
                pc.applySampleToPreset(preset, samples[0], PresetContext.MODE_APPLY_SAMPLE_TO_ALL_VOICES);
                return;
            }
            res = UserMessaging.askOptions("Apply Sample To Preset", "Apply S" + new DecimalFormat("0000").format(samples[0]) + " to ", new Object[]{"New Voice", "All Voices", "All Zones", "All Voices and Zones"}, "All Voices");
            if (res != JOptionPane.CLOSED_OPTION) {
                switch (res) {
                    // new voice
                    case 0:
                        pc.applySampleToPreset(preset, samples[0], PresetContext.MODE_APPLY_SAMPLE_TO_NEW_VOICE);
                        break;
                        // all voices
                    case 1:
                        pc.applySampleToPreset(preset, samples[0], PresetContext.MODE_APPLY_SAMPLE_TO_ALL_VOICES);
                        break;
                        // all voices and zones
                    case 3:
                        pc.applySampleToPreset(preset, samples[0], PresetContext.MODE_APPLY_SAMPLE_TO_ALL_VOICES_AND_ZONES);
                        break;
                        // all zones
                    case 2:
                        pc.applySampleToPreset(preset, samples[0], PresetContext.MODE_APPLY_SAMPLE_TO_ALL_ZONES);
                        break;
                }
            }
        } else {
            int res = JOptionPane.showOptionDialog(ZoeosFrame.getInstance(), "Apply samples to ", "Apply Samples To Preset", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{"New Voices", "New Voice and Zones"}, "New Voices");
            if (res != JOptionPane.CLOSED_OPTION) {
                switch (res) {
                    case 0:
                        pc.applySamplesToPreset(preset, samples, PresetContext.MODE_APPLY_SAMPLES_TO_NEW_VOICES);
                        break;
                    case 1:
                        pc.applySamplesToPreset(preset, samples, PresetContext.MODE_APPLY_SAMPLES_TO_NEW_VOICE_AND_ZONES);
                        break;
                }
                if (wasEmpty)
                    try {
                        pc.rmvVoices(preset, new Integer[]{IntPool.get(0)});
                    } catch (NoSuchVoiceException e) {
                        e.printStackTrace();
                    } catch (CannotRemoveLastVoiceException e) {
                        e.printStackTrace();
                    }
            }
        }
    }
}
