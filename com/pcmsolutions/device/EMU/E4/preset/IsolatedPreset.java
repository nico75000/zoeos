package com.pcmsolutions.device.EMU.E4.preset;

import com.pcmsolutions.util.IntegerUseMap;


/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 05-Aug-2003
 * Time: 13:58:33
 * To change this template use Options | File Templates.
 */

// IMMUTABLE

public interface IsolatedPreset extends IsolatedParameters {

    public int numLinks();

    public int numVoices();

    public IsolatedVoice getIsolatedVoice(Integer v) throws PresetException;

    public IsolatedLink getIsolatedLink(Integer l) throws PresetException;

    public String getName();

    public Integer getOriginalIndex();

    public String getSummary();

    public IntegerUseMap referencedPresetUsage();

    public IntegerUseMap referencedSampleUsage();

    public interface IsolatedVoice extends IsolatedParameters {
        public int numZones();

        public IsolatedZone getIsolatedZone(Integer z) throws PresetException;

        public Integer getOriginalIndex();

        public Integer getOriginalPresetIndex();

        public String getOriginalPresetName();

        public IntegerUseMap getReferencedSampleUsage();

        public interface IsolatedZone extends IsolatedParameters {
            public Integer getOriginalIndex();

            public Integer getOriginalVoiceIndex();

            public Integer getOriginalPresetIndex();

            public String getOriginalPresetName();
        }
    }

    public interface IsolatedLink extends IsolatedParameters {
        public Integer getOriginalIndex();

        public Integer getOriginalPresetIndex();

        public String getOriginalPresetName();

    }
}
