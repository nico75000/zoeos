package com.pcmsolutions.device.EMU.E4.preset;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.SampleContextMacros;
import com.pcmsolutions.device.EMU.E4.parameter.ID;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.sample.ReadableSample;
import com.pcmsolutions.device.EMU.database.ContentUnavailableException;
import com.pcmsolutions.device.EMU.database.ContextLocation;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.gui.ProgressCallback;
import com.pcmsolutions.gui.ProgressSession;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.system.*;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;
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
    public static ContextLocation[] getAggNames(Map<Integer, String> nameMap) {
        Iterator<Map.Entry<Integer, String>> i = nameMap.entrySet().iterator();
        ContextLocation[] aggs = new ContextLocation[nameMap.size()];
        int j = 0;
        while (i.hasNext()) {
            Map.Entry<Integer, String> entry = i.next();
            aggs[j++] = new ContextLocation(entry.getKey(), entry.getValue());
        }
        return aggs;
    }

    public static void optionToNewEmptyPreset(final ContextEditablePreset preset) {
        try {
            if (preset.isEmpty() && UserMessaging.askYesNo("Create a new preset at " + preset.getDisplayName() + " ?"))
                try {
                    preset.getPresetContext().newContent(preset.getIndex(), DeviceContext.UNTITLED_PRESET).post();
                } catch (ResourceUnavailableException e) {
                    e.printStackTrace();
                }
        } catch (PresetException e) {
            e.printStackTrace();
        }
    }

    public static int firstEmpty(PresetContext pc, Integer[] presets) {
        for (int i = 0; i < presets.length; i++) {
            try {
                if (pc.isEmpty(presets[i]))
                    return i;
            } catch (DeviceException e) {
                //e.printStackTrace();
            }
        }
        return -1;
    }

    public static boolean noLinksInPresets(ReadablePreset[] presets) throws PresetException {
        for (int i = 0; i < presets.length; i++)
            try {
                if (presets[i].numLinks() > 0)
                    return false;
            } catch (EmptyException e) {
            }
        return true;
    }

    public static boolean arePresetIndexesEmpty(PresetContext pc, Integer lowPreset, int count) throws DeviceException {
        for (int i = 0; i < count; i++)
            if (!pc.isEmpty(IntPool.get(lowPreset.intValue() + i)))
                return false;
        return true;
    }

    public static boolean arePresetIndexesEmpty(PresetContext pc, Integer[] presets) throws DeviceException {
        for (int i = 0; i < presets.length; i++)
            if (!pc.isEmpty(presets[i]))
                return false;
        return true;
    }

    public static List findEmptyPresets(ReadablePreset[] presets, int searchIndex) {
        ArrayList empties = new ArrayList();
        for (int i = searchIndex; i < presets.length; i++)
            try {
                if (presets[i].isEmpty())
                    empties.add(presets[i]);
            } catch (PresetException e) {
                e.printStackTrace();
            }

        return empties;
    }

    public static Integer[] getRomIndexes(Integer[] indexes) {
        ArrayList rom = new ArrayList();
        for (int i = 0; i < indexes.length; i++)
            if (indexes[i].intValue() >= DeviceContext.BASE_ROM_SAMPLE)
                rom.add(indexes[i]);
        return (Integer[]) rom.toArray(new Integer[rom.size()]);
    }

    public static int numEmpties(PresetContext pc, Integer[] indexes) throws DeviceException {
        int count = 0;
        for (int i = 0; i < indexes.length; i++)
            if (pc.isEmpty(indexes[i]))
                count++;
        return count;
    }

    public static Integer[] getUserIndexes(Integer[] indexes) {
        ArrayList user = new ArrayList();
        for (int i = 0; i < indexes.length; i++)
            if (indexes[i].intValue() < DeviceContext.BASE_ROM_SAMPLE)
                user.add(indexes[i]);
        return (Integer[]) user.toArray(new Integer[user.size()]);
    }

    public static void assertPresetsInitialized(ReadablePreset[] presets) throws PresetException {
        for (int i = 0; i < presets.length; i++)
            presets[i].assertInitialized(false);
    }

    public static void loadPresetsToContext(final IsolatedPreset[] presets, final Integer[] indexes, final PresetContext pc, Map sampleIndexMap, ProgressCallback prog) throws CommandFailedException {
        try {
            DecimalFormat df = new DecimalFormat("0000");
            Map linkMap = new HashMap();

            for (int i = 0; i < presets.length; i++)
                linkMap.put(presets[i].getOriginalIndex(), indexes[i]);

            ProgressCallback[] progs = prog.splitTask(presets.length, true);
            for (int i = 0; i < presets.length; i++) {
                progs[i].updateLabel("Loading preset '" + presets[i].getName() + "' to " + df.format(indexes[i]));
                if (prog.isCancelled())
                    return;
                pc.dropContent(presets[i], indexes[i], presets[i].getName(), sampleIndexMap, IntPool.get(0), linkMap, progs[i]).post();
            }
        } catch (Exception e) {
            prog.updateProgress(1);
            throw new CommandFailedException(e.getMessage());
        } finally {
        }
    }

    public static void setContextVoicesParam(ContextEditablePreset.EditableVoice[] voices, final Integer[] ids, final Integer[] vals) throws PresetException {
        for (int i = 0; i < voices.length; i++)
            for (int j = 0; j < ids.length; j++)
                voices[i].setVoiceParam(ids[j], vals[j]);
    }

    public static int presetZoneCount(PresetContext pc, Integer preset) throws ContentUnavailableException, DeviceException, EmptyException, PresetException {
        int zc = 0;
        for (int i = 0, j = pc.numVoices(preset); i < j; i++)
            zc += pc.numZones(preset, IntPool.get(i));
        return zc;
    }

    public static ReadablePreset[] getReadablePresets(PresetContext pc, Integer[] indexes) throws DeviceException {
        ReadablePreset[] presets = new ReadablePreset[indexes.length];
        for (int i = 0; i < indexes.length; i++)
            presets[i] = pc.getReadablePreset(indexes[i]);
        return presets;
    }

    public static IntegerUseMap getSampleUsage(IsolatedPreset[] presets) {
        IntegerUseMap m = new IntegerUseMap();
        for (int i = 0; i < presets.length; i++) {
            m.mergeUseMap(presets[i].referencedSampleUsage());
        }
        return m;
    }

    public static IntegerUseMap getSampleUsage(PresetContext pc, Integer[] presets) throws ContentUnavailableException, DeviceException {
        IntegerUseMap m = new IntegerUseMap();
        for (int i = 0; i < presets.length; i++)
            try {
                m.mergeUseMap(pc.presetSampleUsage(presets[i]));
            } catch (EmptyException e) {
            }
        return m;
    }

    public static boolean autoMapGroupKeyWin(PresetContext pc, Integer preset, Integer group) throws ContentUnavailableException, PresetException, DeviceException, EmptyException {
        return autoMapVoiceKeyWin(pc, preset, pc.getVoiceIndexesInGroup(preset, group));
    }

    public static boolean autoMapZoneKeyWin(ContextEditablePreset.EditableVoice.EditableZone[] zones) {
        RangePartitioner rp = new RangePartitioner(0, 127);
        try {
            for (int i = 0; i < zones.length; i++)
                rp.addPoint(zones[i].getZoneParams(new Integer[]{ID.origKey})[0].intValue());
            RangePartitioner.Point[] points = rp.getPoints();
            for (int i = 0; i < zones.length; i++) {
                zones[i].setZoneParam(ID.lowKey, IntPool.get(0));
                //zones[i].setZonesParam(ID.keyHigh, IntPool.get(127));
                zones[i].setZoneParam(ID.highKey, IntPool.get(points[i].getHigh()));
                zones[i].setZoneParam(ID.lowKey, IntPool.get(points[i].getLow()));
            }
            return true;
        } catch (IllegalParameterIdException e) {
        } catch (ParameterValueOutOfRangeException e) {
        } catch (ParameterException e) {
        } catch (PresetException e) {
        } catch (EmptyException e) {
        }
        return false;
    }

    public static boolean autoMapVoiceKeyWin(ContextEditablePreset.EditableVoice[] voices) throws EmptyException {
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
                voices[i].setVoiceParam(ID.lowKey, IntPool.get(0));
                voices[i].setVoiceParam(ID.highKey, IntPool.get(127));

                int nz = voices[i].numZones();
                if (nz > 0) {
                    for (int z = 0; z < nz; z++) {
                        voices[i].getEditableZone(IntPool.get(z)).setZoneParam(ID.lowKey, IntPool.get(0));
                        voices[i].getEditableZone(IntPool.get(z)).setZoneParam(ID.highKey, IntPool.get(points[rpi].getHigh()));
                        voices[i].getEditableZone(IntPool.get(z)).setZoneParam(ID.lowKey, IntPool.get(points[rpi++].getLow()));
                    }
                } else {
                    voices[i].setVoiceParam(ID.lowKey, IntPool.get(points[i].getLow()));
                    voices[i].setVoiceParam(ID.highKey, IntPool.get(points[i].getHigh()));
                }
            }
            return true;
        } catch (IllegalParameterIdException e) {
        } catch (ParameterValueOutOfRangeException e) {
        } catch (ParameterException e) {
        } catch (PresetException e) {
        }
        return false;
    }

    public static boolean autoMapVoiceKeyWin(final PresetContext pc, final Integer preset, Integer[] voices) throws EmptyException, DeviceException, NoSuchContextException {
        ContextReadablePreset crp = pc.getContextPreset(preset);
        if (!(crp instanceof ContextEditablePreset))
            return false;
        ContextEditablePreset cep = (ContextEditablePreset) crp;
        ContextEditablePreset.EditableVoice[] eVoices = new ContextEditablePreset.EditableVoice[voices.length];
        for (int i = 0; i < voices.length; i++)
            eVoices[i] = cep.getEditableVoice(voices[i]);
        return autoMapVoiceKeyWin(eVoices);
        /*
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
                        pc.setZonesParam(preset, voices[i], new Integer[]{IntPool.get(z)}, ID.keyLow, new Integer[]{IntPool.get(0)});
                        pc.setZonesParam(preset, voices[i], new Integer[]{IntPool.get(z)}, ID.keyHigh, new Integer[]{IntPool.get(points[rpi].getHigh())});
                        pc.setZonesParam(preset, voices[i], new Integer[]{IntPool.get(z)}, ID.keyLow, new Integer[]{IntPool.get(points[rpi++].getLow())});
                    }
                else {
                    pc.setVoicesParam(preset, new Integer[]{voices[i]}, ID.keyHigh, new Integer[]{IntPool.get(points[rpi].getHigh())});
                    pc.setVoicesParam(preset, new Integer[]{voices[i]}, ID.keyLow, new Integer[]{IntPool.get(points[rpi++].getLow())});
                }
            }
            return true;
        } catch (IllegalParameterIdException e) {
        } catch (NoSuchVoiceException e) {
        } catch (ParameterValueOutOfRangeException e) {
        } catch (NoSuchZoneException e) {
        }
        return false;
        */
    }

    public static boolean intervalMapGroupKeyWin(PresetContext pc, Integer preset, Integer group, IntervalServices.Mapper mapper) throws ContentUnavailableException, PresetException, DeviceException, EmptyException, ParameterException {
        Integer[] voices = pc.getVoiceIndexesInGroup(preset, group);
        ContextReadablePreset crp = pc.getContextPreset(preset);
        if (!(crp instanceof ContextEditablePreset))
            return false;
        ContextEditablePreset cep = (ContextEditablePreset) crp;
        ContextEditablePreset.EditableVoice[] eVoices = new ContextEditablePreset.EditableVoice[voices.length];
        for (int i = 0; i < voices.length; i++)
            eVoices[i] = cep.getEditableVoice(voices[i]);
        return intervalMapVoiceKeyWin(eVoices, mapper);
    }

    public static boolean intervalMapVoiceKeyWin(ContextEditablePreset.EditableVoice[] voices, IntervalServices.Mapper mapper) throws ParameterException, PresetException, EmptyException {
        try {
            for (int i = 0; i < voices.length; i++) {
                voices[i].setVoiceParam(ID.lowKey, IntPool.get(0));
                voices[i].setVoiceParam(ID.highKey, IntPool.get(127));

                int nz = voices[i].numZones();
                if (nz > 0) {
                    for (int z = 0; z < nz; z++) {
                        voices[i].getEditableZone(IntPool.get(z)).setZoneParam(ID.lowKey, IntPool.get(0));
                        voices[i].getEditableZone(IntPool.get(z)).setZoneParam(ID.highKey, IntPool.get(mapper.getNextKey()));
                        voices[i].getEditableZone(IntPool.get(z)).setZoneParam(ID.lowKey, IntPool.get(mapper.getNextKey()));
                    }
                } else {
                    voices[i].setVoiceParam(ID.lowKey, IntPool.get(mapper.getNextKey()));
                    voices[i].setVoiceParam(ID.highKey, IntPool.get(mapper.getNextKey()));
                }
            }
            return true;
        } catch (IllegalParameterIdException e) {
        } catch (ParameterValueOutOfRangeException e) {
        }
        return false;
    }

    public static boolean intervalMapZoneKeyWin(ContextEditablePreset.EditableVoice.EditableZone[] zones, IntervalServices.Mapper mapper) throws ParameterException, PresetException, EmptyException {
        try {
            for (int i = 0; i < zones.length; i++) {
                zones[i].setZoneParam(ID.lowKey, IntPool.get(0));
                //zones[i].setZonesParam(ID.keyHigh, IntPool.get(127));
                zones[i].setZoneParam(ID.highKey, IntPool.get(mapper.getNextKey()));
                zones[i].setZoneParam(ID.lowKey, IntPool.get(mapper.getNextKey()));
            }
            return true;
        } catch (IllegalParameterIdException e) {
        } catch (ParameterValueOutOfRangeException e) {
        }
        return false;
    }

    public static String getVoiceSampleName(PresetContext pc, Integer preset, Integer voice) throws ParameterException, ContentUnavailableException, PresetException, DeviceException, EmptyException {
        try {
            Integer sample = pc.getVoiceParams(preset, voice, new Integer[]{ID.sample})[0];
            if (sample.intValue() > 0)
                return pc.getRootSampleContext().getName(sample);
            else
                return DeviceContext.MULTISAMPLE;
        } catch (IllegalParameterIdException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getZoneSampleName(PresetContext pc, Integer preset, Integer voice, Integer zone) throws ParameterException, ContentUnavailableException, PresetException, EmptyException, DeviceException {
        try {
            Integer sample = pc.getZoneParams(preset, voice, zone, new Integer[]{ID.sample})[0];
            if (sample.intValue() > 0)
                return pc.getRootSampleContext().getName(sample);
            else
                return DeviceContext.EMPTY_SAMPLE;
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
                                try {
                                    pc.trySetOriginalKeyFromName(preset, IntPool.get(v), getVoiceSampleName(pc, preset, IntPool.get(v))).post();
                                } catch (EmptyException e) {
                                }
                        break;
                    case PresetContext.PRESET_ZONES_SELECTOR:
                        nv = pc.numVoices(preset);
                        for (int v = 0; v < nv; v++) {
                            int nz = pc.numZones(preset, IntPool.get(v));
                            for (int z = 0; z < nz; z++)
                                try {
                                    pc.trySetOriginalKeyFromName(preset, IntPool.get(v), IntPool.get(z), getZoneSampleName(pc, preset, IntPool.get(v), IntPool.get(z))).post();
                                } catch (EmptyException e) {
                                }
                        }
                        break;
                    case PresetContext.PRESET_VOICES_AND_ZONES_SELECTOR:
                        nv = pc.numVoices(preset);
                        for (int v = 0; v < nv; v++) {
                            int nz = pc.numZones(preset, IntPool.get(v));
                            if (nz == 0)
                                pc.trySetOriginalKeyFromName(preset, IntPool.get(v), getVoiceSampleName(pc, preset, IntPool.get(v))).post();
                            for (int z = 0; z < nz; z++)
                                try {
                                    pc.trySetOriginalKeyFromName(preset, IntPool.get(v), IntPool.get(z), getZoneSampleName(pc, preset, IntPool.get(v), IntPool.get(z))).post();
                                } catch (EmptyException e) {
                                }
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("illegal mode");
                }
            } catch (DeviceException e) {
                e.printStackTrace();
            } catch (EmptyException e) {
                e.printStackTrace();
            } catch (ParameterException e) {
                e.printStackTrace();
            } catch (ContentUnavailableException e) {
                e.printStackTrace();
            } catch (ResourceUnavailableException e) {
                e.printStackTrace();
            }
        }
    }

    public static int copyPresetDeep(Integer[] presets, PresetContext pc, Integer destPreset, boolean copyEmpties, boolean findEmpties, boolean translateLinks, boolean provideFeedback) throws ContentUnavailableException, DeviceException {
        Set s = new HashSet();
        for (int i = 0; i < presets.length; i++)
            s.addAll(pc.getPresetDeepSet(presets[i]));

        /*  if (provideFeedback) {
              if (JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), "Copying " + s.size() + " presets. Continue?", "Preset Copy Deep", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != 0)
                  return notCopied = s.size();
          }*/

        Integer[] srcPresets = (Integer[]) s.toArray(new Integer[s.size()]);
        Arrays.sort(srcPresets);
        Integer[] destPresets;

        if (findEmpties) {
            SortedSet<Integer> le = pc.findEmpties(IntPool.get(s.size()), destPreset, IntPool.get(Integer.MAX_VALUE)/*DeviceContext.BASE_FLASH_PRESET*/);
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

    public static int copyPresets(PresetContext pc, Integer[] srcPresets, Integer[] destPresets, boolean copyEmpties, boolean provideFeedback, String feedbackTitle) throws DeviceException, NoSuchContextException {
        return copyPresets(pc, srcPresets, destPresets, null, copyEmpties, provideFeedback, feedbackTitle);
    }

    public static int copyPresets(PresetContext pc, Integer[] srcPresets, Integer[] destPresets, Map linkPresetTranslationMap, boolean copyEmpties, boolean provideFeedback, String feedbackTitle) throws DeviceException {
        int notCopied = 0;
        if (srcPresets.length != destPresets.length)
            throw new IllegalArgumentException("number of src/dest presets mismatch");

        Zoeos z = Zoeos.getInstance();
        ProgressSession ps = null;
        if (provideFeedback) {
            String confirmStr = PresetContextMacros.getOverwriteConfirmationString(pc, destPresets);
            if (JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), confirmStr, "Confirm " + feedbackTitle, JOptionPane.YES_NO_OPTION) != 0)
                return destPresets.length;
            ps = z.getProgressSession(ZUtilities.makeExactLengthString("Preset copy", 60), srcPresets.length);
        }

        Arrays.sort(srcPresets);
        Arrays.sort(destPresets);
        try {
            for (int i = srcPresets.length - 1; i >= 0; i--) {
                if (pc.isEmpty(srcPresets[i])) {
                    if (copyEmpties)
                        try {
                            pc.erase(destPresets[i]).post();
                        } catch (ResourceUnavailableException e) {
                            notCopied++;
                        }
                    else
                        notCopied++;
                } else
                    try {
                        if (linkPresetTranslationMap == null)
                            pc.copy(srcPresets[i], destPresets[i]).post();
                        else
                            pc.copy(srcPresets[i], destPresets[i], linkPresetTranslationMap).post();
                    } catch (ResourceUnavailableException e) {
                        notCopied++;
                    } finally {
                        if (provideFeedback) {
                            ps.updateStatus();
                        }
                    }
            }
        } finally {
            if (provideFeedback)
                ps.end();
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
            name = new ContextLocation(preset, pc.getString(preset)).toString();
        } catch (DeviceException e) {
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
        } catch (DeviceException e) {
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
                name = new ContextLocation(indexes[0], pc.getString(indexes[0])).toString();
                return "OK to overwrite " + name + " ?";
            } catch (DeviceException e) {
                e.printStackTrace();
            }
            return "OK to overwrite non-empty preset at location " + new DecimalFormat("0000").format(indexes[0]) + " ?";
        }
    }

    public static ReadablePreset[] filterPresetsReferencingSamples(ReadablePreset[] presets, ReadableSample[] samples) throws PresetException {
        return filterPresetsReferencingSamples(presets, SampleContextMacros.extractSampleIndexes(samples));
    }

    public static Integer[] extractRomIndexes(Integer[] indexes) {
        ArrayList rom = new ArrayList();
        for (int i = 0; i < indexes.length; i++)
            if (indexes[i].intValue() >= DeviceContext.BASE_FLASH_PRESET)
                rom.add(indexes[i]);
        return (Integer[]) rom.toArray(new Integer[rom.size()]);
    }

    public static Integer[] extractUserIndexes(Integer[] indexes) {
        ArrayList rom = new ArrayList();
        for (int i = 0; i < indexes.length; i++)
            if (indexes[i].intValue() < DeviceContext.BASE_FLASH_PRESET)
                rom.add(indexes[i]);
        return (Integer[]) rom.toArray(new Integer[rom.size()]);
    }

    public static boolean confirmInitializationOfPresets(ReadablePreset[] presets) throws PresetException {
        int uc = PresetContextMacros.howManyUninitializedPresets(presets);
        if (uc > 0 && !UserMessaging.askYesNo(uc + (uc == 1 ? " preset" : " presets") + " will need to be initialized. Do you want to proceed?"))
            return false;
        return true;
    }

    public static ReadablePreset[] filterPresetsReferencingSamples(ReadablePreset[] presets, Integer[] samples) {
        ArrayList filtered = new ArrayList();
        for (int i = 0; i < presets.length; i++)
            try {
                if (presets[i].getSampleUsage().containsAnyOf(samples))
                    filtered.add(presets[i]);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        return (ReadablePreset[]) filtered.toArray(new ReadablePreset[filtered.size()]);
    }

    public static IntegerUseMap getPresetSampleUsage(PresetContext pc, Integer[] presets) throws DeviceException, ContentUnavailableException {
        IntegerUseMap um = new IntegerUseMap();
        for (int i = 0; i < presets.length; i++)
            try {
                um.mergeUseMap(pc.presetSampleUsage(presets[i]));
            } catch (EmptyException e) {
                // ignore
            }
        return um;
    }

    public static IntegerUseMap getPresetSampleUsage(ReadablePreset[] presets) throws PresetException {
        IntegerUseMap um = new IntegerUseMap();
        for (int i = 0; i < presets.length; i++)
            try {
                um.mergeUseMap(presets[i].getSampleUsage());
            } catch (EmptyException e) {
                // ignore
            }
        return um;
    }

    public static IntegerUseMap getPresetLinkPresetUsage(PresetContext pc, Integer[] presets) throws DeviceException, ContentUnavailableException {
        IntegerUseMap um = new IntegerUseMap();
        for (int i = 0; i < presets.length; i++)
            try {
                um.mergeUseMap(pc.presetLinkPresetUsage(presets[i]));
            } catch (EmptyException e) {
                // ignore
            }
        return um;
    }

    // returns (sub)set of specified preset indexes referencing any of the sample indexes passed
    public static Integer[] getPresetsReferencingSamples(PresetContext pc, Integer[] presets, Integer[] samples) throws DeviceException, ContentUnavailableException {
        HashSet pl = new HashSet();
        for (int i = 0; i < presets.length; i++) {
            try {
                if (pc.presetSampleUsage(presets[i]).containsAnyOf(samples))
                    pl.add(presets[i]);
            } catch (EmptyException e) {
                // ignore
            }
        }
        return (Integer[]) pl.toArray(new Integer[pl.size()]);
    }

    public static int howManyUninitializedPresets(PresetContext pc, Integer[] presets) throws DeviceException {
        int uic = 0;
        for (int i = 0; i < presets.length; i++) {
            if (!pc.isInitialized(presets[i]))
                uic++;
        }
        return uic;
    }

    public static int howManyUninitializedPresets(ReadablePreset[] presets) throws PresetException {
        int uic = 0;
        for (int i = 0; i < presets.length; i++) {
            if (!presets[i].isInitialized())
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

    public static ContextEditablePreset[] extractContextEditablePresets(Object[] objects) {
        Object[] rp1 = ClassUtility.extractInstanceOf(objects, ContextEditablePreset.class);
        ContextEditablePreset[] rp2 = new ContextEditablePreset[rp1.length];
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
            indexes[i] = presets[i].getIndex();
        return indexes;
    }

    public static Integer[] extractPresetIndexes
            (IsolatedPreset[] presets) {
        Integer[] indexes = new Integer[presets.length];
        for (int i = 0; i < presets.length; i++)
            indexes[i] = presets[i].getOriginalIndex();
        return indexes;
    }

    public static Integer[] extractVoiceIndexes
            (ReadablePreset.ReadableVoice[] voices) {
        Integer[] indexes = new Integer[voices.length];
        for (int i = 0; i < voices.length; i++)
            indexes[i] = voices[i].getVoiceNumber();
        return indexes;
    }

    // may return null if no common range
    public static Integer[] getCommonKeyRange
            (ReadablePreset.ReadableVoice[] voices) throws ParameterException, PresetException, EmptyException {
        int low = 0;
        int high = 127;
        for (int i = 0; i < voices.length; i++) {
            Integer[] lh = voices[i].getVoiceParams(new Integer[]{ID.lowKey, ID.highKey});
            if (lh[0].intValue() > high || lh[1].intValue() < low)
                return null;
            if (lh[0].intValue() > low)
                low = lh[0].intValue();
            if (lh[1].intValue() < high)
                high = lh[1].intValue();
        }
        return new Integer[]{IntPool.get(low), IntPool.get(high)};
    }

    // may return null if no common range
    public static Integer[] getCommonKeyRange
            (PresetContext pc, Integer preset, Integer[] voices) throws ParameterException, ContentUnavailableException, PresetException, DeviceException, EmptyException {
        int low = 0;
        int high = 127;
        for (int i = 0; i < voices.length; i++) {
            Integer[] lh = pc.getVoiceParams(preset, voices[i], new Integer[]{ID.lowKey, ID.highKey});
            if (lh[0].intValue() > high || lh[1].intValue() < low)
                return null;
            if (lh[0].intValue() > low)
                low = lh[0].intValue();
            if (lh[1].intValue() < high)
                high = lh[1].intValue();
        }
        return new Integer[]{IntPool.get(low), IntPool.get(high)};
    }

    public static Integer[] extractZoneIndexes
            (ReadablePreset.ReadableVoice.ReadableZone[] zones) {
        Integer[] indexes = new Integer[zones.length];
        for (int i = 0; i < zones.length; i++)
            indexes[i] = zones[i].getZoneNumber();
        return indexes;
    }

    public static Integer[] extractSampleIndexes
            (ReadablePreset.ReadableVoice.ReadableZone[] zones) throws ParameterException, PresetException, EmptyException {
        Integer[] indexes = new Integer[zones.length];
        for (int i = 0; i < zones.length; i++)
            indexes[i] = zones[i].getZoneParams(new Integer[]{ID.sample})[0];
        return indexes;
    }

    public static Integer[] extractUniquePresetIndexes
            (ReadablePreset[] presets) {
        Set s = new HashSet();
        for (int i = 0; i < presets.length; i++)
            s.add(presets[i].getIndex());
        return (Integer[]) s.toArray(new Integer[s.size()]);
    }

    public static void applySamplesToPreset
            (PresetContext
            pc, Integer
            preset, Integer[] samples) throws ResourceUnavailableException {
        if (samples.length == 0)
            return;
        int num = samples.length;
        if (num == 1) {
            int res;
            res = UserMessaging.askOptions("Apply Sample To Preset", "Apply S" + new DecimalFormat("0000").format(samples[0]) + " to ", new Object[]{"New Voice", "All Voices", "All Zones", "All Voices and Zones"}, "All Voices");
            if (res != JOptionPane.CLOSED_OPTION) {
                switch (res) {
                    // new voice
                    case 0:
                        pc.applySampleToPreset(preset, samples[0], PresetContext.MODE_APPLY_SAMPLE_TO_NEW_VOICE).post();
                        break;
                        // all voices
                    case 1:
                        pc.applySampleToPreset(preset, samples[0], PresetContext.MODE_APPLY_SAMPLE_TO_ALL_VOICES).post();
                        break;
                        // all voices and zones
                    case 3:
                        pc.applySampleToPreset(preset, samples[0], PresetContext.MODE_APPLY_SAMPLE_TO_ALL_VOICES_AND_ZONES).post();
                        break;
                        // all zones
                    case 2:
                        pc.applySampleToPreset(preset, samples[0], PresetContext.MODE_APPLY_SAMPLE_TO_ALL_ZONES).post();
                        break;
                }
            }
        } else {
            int res = JOptionPane.showOptionDialog(ZoeosFrame.getInstance(), "Apply samples to ", "Apply Samples To Preset", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{"New Voices", "New Voice and Zones"}, "New Voices");
            if (res != JOptionPane.CLOSED_OPTION) {
                switch (res) {
                    case 0:
                        pc.applySamplesToPreset(preset, samples, PresetContext.MODE_APPLY_SAMPLES_TO_NEW_VOICES).post();
                        break;
                    case 1:
                        pc.applySamplesToPreset(preset, samples, PresetContext.MODE_APPLY_SAMPLES_TO_NEW_VOICE_AND_ZONES).post();
                        break;
                }
            }
        }
    }

    public static void applyPresetLinksToPreset
            (PresetContext
            pc, Integer
            preset, Integer[] presetLinks) throws DeviceException {

        if (presetLinks.length == 0)
            return;

        if (presetLinks.length > 3)
            if (!UserMessaging.askYesNo("Apply " + presetLinks.length + " linked presets?", "Apply Linked Presets"))
                return;
        if (pc.isEmpty(preset))
            pc.newContent(preset, DeviceContext.UNTITLED_PRESET);
        try {
            pc.newLinks(preset, presetLinks).post();
        } catch (ResourceUnavailableException e) {
            e.printStackTrace();
        }
    }
}
