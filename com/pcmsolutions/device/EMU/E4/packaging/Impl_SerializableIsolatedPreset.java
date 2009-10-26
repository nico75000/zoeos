package com.pcmsolutions.device.EMU.E4.packaging;

import com.pcmsolutions.device.EMU.E4.preset.IsolatedPreset;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchLinkException;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchVoiceException;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchZoneException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.util.IntegerUseMap;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 03-Jan-2004
 * Time: 16:05:09
 * To change this template use Options | File Templates.
 */
public class Impl_SerializableIsolatedPreset extends Impl_SerializableIsolatedParameters implements IsolatedPreset, Serializable {
    static final long serialVersionUID = 1;

    private Impl_SerializableIsolatedVoice[] voices;
    private Impl_SerializableIsolatedLink[] links;

    private Integer presetIndex;
    private String name;
    private String summary;
    private IntegerUseMap refSampleUsage;
    private IntegerUseMap refPresetUsage;

    public Impl_SerializableIsolatedPreset(IsolatedPreset ip) throws NoSuchVoiceException, NoSuchLinkException, NoSuchZoneException {
        super(ip.getIdValMap());
        presetIndex = ip.getOriginalIndex();
        name = ip.getName();
        summary = ip.getSummary();
        refSampleUsage = ip.referencedSampleUsage();
        refPresetUsage = ip.referencedPresetUsage();
        voices = new Impl_SerializableIsolatedVoice[ip.numVoices()];
        links = new Impl_SerializableIsolatedLink[ip.numLinks()];
        for (int i = 0; i < voices.length; i++)
            voices[i] = new Impl_SerializableIsolatedVoice(ip.getIsolatedVoice(IntPool.get(i)));
        for (int i = 0; i < links.length; i++)
            links[i] = new Impl_SerializableIsolatedLink(ip.getIsolatedLink(IntPool.get(i)));
    }

    public int numLinks() {
        return links.length;
    }

    public int numVoices() {
        return voices.length;
    }

    public IsolatedPreset.IsolatedVoice getIsolatedVoice(Integer v) throws NoSuchVoiceException {
        int vi = v.intValue();
        if (vi >= 0 && vi < voices.length)
            return voices[vi];
        throw new NoSuchVoiceException(v.toString());
    }

    public IsolatedPreset.IsolatedLink getIsolatedLink(Integer l) throws NoSuchLinkException {
        int li = l.intValue();
        if (li >= 0 && li < links.length)
            return links[li];
        throw new NoSuchLinkException(l.toString());
    }

    public String getName() {
        return name;
    }

    public Integer getOriginalIndex() {
        return presetIndex;
    }

    public String getSummary() {
        return summary;
    }

    public IntegerUseMap referencedPresetUsage() {
        return new IntegerUseMap().mergeUseMap(refPresetUsage);
    }

    public IntegerUseMap referencedSampleUsage() {
        return new IntegerUseMap().mergeUseMap(refSampleUsage);
    }

    public class Impl_SerializableIsolatedVoice extends Impl_SerializableIsolatedParameters implements IsolatedPreset.IsolatedVoice, Serializable {
        static final long serialVersionUID = 1;

        private Impl_SerializableIsolatedVoice.Impl_SerializableIsolatedZone[] zones;

        private Integer voiceIndex;
        private IntegerUseMap refSampleUsage;

        public Impl_SerializableIsolatedVoice(IsolatedPreset.IsolatedVoice iv) throws NoSuchZoneException {
            super(iv.getIdValMap());
            voiceIndex = iv.getOriginalIndex();
            refSampleUsage = iv.getReferencedSampleUsage();
            zones = new Impl_SerializableIsolatedZone[iv.numZones()];
            for (int i = 0; i < zones.length; i++)
                zones[i] = new Impl_SerializableIsolatedZone(iv.getIsolatedZone(IntPool.get(i)));
        }

        public int numZones() {
            return zones.length;
        }

        public IsolatedPreset.IsolatedVoice.IsolatedZone getIsolatedZone(Integer z) throws NoSuchZoneException {
            int zi = z.intValue();
            if (zi >= 0 && zi < zones.length)
                return zones[zi];
            throw new NoSuchZoneException(z.toString());
        }

        public Integer getOriginalIndex() {
            return presetIndex;
        }

        public Integer getOriginalPresetIndex() {
            return presetIndex;
        }

        public String getOriginalPresetName() {
            return name;
        }

        public IntegerUseMap getReferencedSampleUsage() {
            return new IntegerUseMap().mergeUseMap(refSampleUsage);
        }

        public class Impl_SerializableIsolatedZone extends Impl_SerializableIsolatedParameters implements IsolatedPreset.IsolatedVoice.IsolatedZone, Serializable {
            static final long serialVersionUID = 1;

            private Integer zoneIndex;

            public Impl_SerializableIsolatedZone(IsolatedPreset.IsolatedVoice.IsolatedZone iz) {
                super(iz.getIdValMap());
                zoneIndex = iz.getOriginalIndex();
            }

            public Integer getOriginalIndex() {
                return zoneIndex;
            }

            public Integer getOriginalVoiceIndex() {
                return voiceIndex;
            }

            public Integer getOriginalPresetIndex() {
                return presetIndex;
            }

            public String getOriginalPresetName() {
                return name;
            }
        }
    }

    public class Impl_SerializableIsolatedLink extends Impl_SerializableIsolatedParameters implements IsolatedPreset.IsolatedLink, Serializable {
        static final long serialVersionUID = 1;

        private Integer linkIndex;

        public Impl_SerializableIsolatedLink(IsolatedPreset.IsolatedLink il) {
            super(il.getIdValMap());
            linkIndex = il.getOriginalIndex();
        }

        public Integer getOriginalIndex() {
            return linkIndex;
        }

        public Integer getOriginalPresetIndex() {
            return presetIndex;
        }

        public String getOriginalPresetName() {
            return name;
        }
    }
}
