package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor;

import com.pcmsolutions.device.EMU.E4.events.preset.PresetNameChangeEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.PresetInitializeEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.VoiceChangeEvent;
import com.pcmsolutions.device.EMU.E4.gui.TitleProvider;
import com.pcmsolutions.device.EMU.E4.gui.preset.VoiceEditingIcon;
import com.pcmsolutions.device.EMU.E4.gui.preset.icons.PresetIcon;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.VoiceTitleProvider;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterException;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZDisposable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/**
 * User: paulmeehan
 * Date: 11-Feb-2004
 * Time: 01:55:28
 */
public class EditableVoiceTitleProvider extends VoiceTitleProvider implements TitleProvider {
    private ContextEditablePreset.EditableVoice[] voices;
    protected RunLengthEncoder rle;

    public void init(ContextEditablePreset.EditableVoice[] voices) {
        this.voices = voices;
        rle = new RunLengthEncoder(voices);
        init(voices[0]);
    }

    protected void updateTitle() {
        title = rle.toString();
        if (voices[0].getPreset().getIcon() instanceof PresetIcon)
            icon = new VoiceEditingIcon((PresetIcon) voices[0].getPreset().getIcon());
        else
            icon = null;
        tplh.fireTitleProviderDataChanged();
    }

    protected class RunLengthEncoder implements ZDisposable {
        protected ReadablePreset.ReadableVoice[] voices;
        protected HashMap voiceData = new HashMap(); // Integer -> PresetVoiceUsageProfile
        protected boolean grouped;
        protected RunLengthEncoder.GroupProfile groupData;

        public RunLengthEncoder(ContextEditablePreset.EditableVoice[] voices) {
            this.voices = voices;
            grouped = checkForGroupedVoices(voices);
            if (grouped)
                groupData = new RunLengthEncoder.GroupProfile(voices[0]);
            else
                parseVoices(voices);
        }

        public ReadablePreset[] getPresets() {
            if (grouped) {
                return new ReadablePreset[]{groupData.getVoice().getPreset()};
            } else {
                ReadablePreset[] presets = new ReadablePreset[voiceData.size()];
                int index = 0;
                for (Iterator i = voiceData.keySet().iterator(); i.hasNext();)
                    presets[index++] = ((RunLengthEncoder.PresetVoiceUsageProfile) voiceData.get(i.next())).getPreset();
                return presets;
            }
        }

        protected boolean checkForGroupedVoices(ContextEditablePreset.EditableVoice[] voices) {
            int groupedCount = 0;
            for (int i = 0,j = voices.length; i < j; i++)
                if (voices[i].getGroupMode() == true)
                    groupedCount++;

            if (groupedCount > 0 && voices.length > 1)
                throw new IllegalArgumentException("EditableVoicePanel can only handle one grouped voice");

            return (groupedCount > 0 ? true : false);
        }

        public String toString() {
            if (grouped) {
                return "G" + groupData.getGroupPrefixString() + groupData.toString() + ")";
            } else {
                String outStr;
                if (voices.length == 1) {
                    outStr = "V" + IntPool.get(voices[0].getVoiceNumber().intValue() + 1);
                    return outStr;
                }
                outStr = "V";
                for (Iterator i = voiceData.keySet().iterator(); i.hasNext();)
                    outStr += voiceData.get(i.next()).toString();
                return outStr;
            }
        }

        protected void parseGroup(ReadablePreset.ReadableVoice voice) {
            Integer preset;
            RunLengthEncoder.PresetVoiceUsageProfile prof;
            for (int i = 0,j = voices.length; i < j; i++) {
                preset = voices[i].getPresetNumber();
                prof = (RunLengthEncoder.PresetVoiceUsageProfile) voiceData.get(preset);
                if (prof == null) {
                    prof = new RunLengthEncoder.PresetVoiceUsageProfile(voices[i].getPreset(), (i == 0 ? false : true));
                    voiceData.put(preset, prof);
                    voices[i].getPreset().addListener(pla);
                }
                prof.addVoice(voices[i].getVoiceNumber());
            }
        }

        protected void parseVoices(ReadablePreset.ReadableVoice[] voices) {
            Integer preset;
            RunLengthEncoder.PresetVoiceUsageProfile prof;
            for (int i = 0,j = voices.length; i < j; i++) {
                preset = voices[i].getPresetNumber();
                prof = (RunLengthEncoder.PresetVoiceUsageProfile) voiceData.get(preset);
                if (prof == null) {
                    // should we not always pass true here to track name??
                    prof = new RunLengthEncoder.PresetVoiceUsageProfile(voices[i].getPreset(), (i == 0 ? false : true));
                    voiceData.put(preset, prof);
                    voices[i].getPreset().addListener(pla);
                }
                prof.addVoice(voices[i].getVoiceNumber());
            }
        }

        public void zDispose() {
            if (grouped)
                groupData.zDispose();
            else
                for (Iterator i = voiceData.keySet().iterator(); i.hasNext();)
                    ((RunLengthEncoder.PresetVoiceUsageProfile) voiceData.get(i.next())).zDispose();

            voices = null;
            voiceData = null;
            groupData = null;
        }

        protected class GroupProfile implements ZDisposable {
            protected ReadablePreset.ReadableVoice voice;

            protected PresetListenerAdapter pla;

            public ReadablePreset.ReadableVoice getVoice() {
                return voice;
            }

            public GroupProfile(ReadablePreset.ReadableVoice voice) {
                this.voice = voice;
                pla = new PresetListenerAdapter() {
                    public void voiceChanged(VoiceChangeEvent ev) {
                        if (ev.containsId(IntPool.get(37)))
                            updateTitle();
                    }
                };
                voice.getPreset().addListener(pla);
            }

            public String getGroupPrefixString() {
                String vs = " (v" + (voice.getVoiceNumber().intValue() + 1);
                try {
                    return voice.getVoiceParams(new Integer[]{IntPool.get(37)})[0].toString() + vs;
                } catch (EmptyException e) {
                } catch (ParameterException e) {
                } catch (PresetException e) {
                }
                return vs;
            }

            public String toString() {
                try {
                    String voiceStr = "";
                    int currIndex = 0;
                    int runLength = 0;
                    Integer[] voiceIndexes = voice.getVoiceIndexesInGroup();
                    while (true) {
                        runLength = getRunLength(voiceIndexes, currIndex);
                        if (runLength == -1)
                            break;
                        else {
                            int cvi = voiceIndexes[currIndex].intValue() + 1;
                            if (!voiceStr.equals(""))
                                voiceStr += ",";

                            if (runLength == 0)
                                voiceStr += cvi;
                            else
                                voiceStr += cvi + "-" + (cvi + runLength);

                        }
                        currIndex += runLength + 1;
                    }

                    if (voiceStr.equals(""))
                        voiceStr = "none";

                    return "[" + voiceStr + "]";

                } catch (EmptyException e) {
                    return "";
                } catch (PresetException e) {
                    return "[...]";
                }
            }

            // returns -1 for no more elements,  0 for single item run, or n for n item run
            private int getRunLength(Integer[] voiceIndexes, int index) {
                if (index >= voiceIndexes.length)
                    return -1;
                int runLength = 0;
                int prevVoiceIndex = voiceIndexes[index].intValue();
                for (int i = index + 1, j = voiceIndexes.length; i < j; i++) {
                    if (voiceIndexes[i].intValue() - 1 == prevVoiceIndex) {
                        runLength++;
                        prevVoiceIndex++;
                    } else
                        break;
                }
                return runLength;
            }

            public void zDispose() {
                voice.getPreset().removeListener(pla);
                voice = null;
            }
        }

        protected class PresetVoiceUsageProfile implements ZDisposable {
            protected ReadablePreset preset;
            protected ArrayList voiceIndexes = new ArrayList();
            protected boolean trackName = false;
            protected PresetListenerAdapter pla;

            public PresetVoiceUsageProfile(ReadablePreset preset, boolean trackName) {
                this.preset = preset;
                this.trackName = trackName;
                if (trackName) {
                    pla = new PresetListenerAdapter() {
                        public void presetRefreshed(PresetInitializeEvent ev) {
                            updateTitle();
                        }

                        public void presetNameChanged(PresetNameChangeEvent ev) {
                            updateTitle();
                        }
                    };
                    preset.addListener(pla);
                }
            }

            public String toString() {

                String voiceStr = "";
                int currIndex = 0;
                int runLength = 0;

                while (true) {
                    runLength = getRunLength(currIndex);
                    if (runLength == -1)
                        break;
                    else {
                        int cvi = ((Integer) voiceIndexes.get(currIndex)).intValue() + 1;
                        if (!voiceStr.equals(""))
                            voiceStr += ",";

                        if (runLength == 0)
                            voiceStr += cvi;
                        else
                            voiceStr += cvi + "-" + (cvi + runLength);

                    }
                    currIndex += runLength + 1;
                }

                if (voiceStr.equals(""))
                    voiceStr = "none";

                return "[" + voiceStr + "]";
            }

            // returns -1 for no more elements,  0 for single item run, or n for n item run
            private int getRunLength(int index) {
                if (index >= voiceIndexes.size())
                    return -1;
                int runLength = 0;
                int prevVoiceIndex = ((Integer) voiceIndexes.get(index)).intValue();
                for (int i = index + 1, j = voiceIndexes.size(); i < j; i++) {
                    if (((Integer) voiceIndexes.get(i)).intValue() - 1 == prevVoiceIndex) {
                        runLength++;
                        prevVoiceIndex++;
                    } else
                        break;
                }
                return runLength;
            }

            public ReadablePreset getPreset() {
                return preset;
            }

            public void addVoice(Integer voiceIndex) {
                if (!voiceIndexes.contains(voiceIndex)) {
                    voiceIndexes.add(voiceIndex);
                    Collections.sort(voiceIndexes);
                }
            }

            public void removeVoice(Integer voiceIndex) {
                voiceIndexes.remove(voiceIndex);
            }

            public void zDispose() {
                if (trackName)
                    preset.removeListener(pla);
                preset = null;
            }
        }
    }

    public void zDispose() {
        super.zDispose();
        rle.zDispose();
    }
}
