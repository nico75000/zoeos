package com.pcmsolutions.device.EMU.E4.packaging;

import com.pcmsolutions.device.EMU.E4.multimode.MultiModeMap;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedPreset;
import com.pcmsolutions.util.IntegerUseMap;

import java.util.Map;

public interface PresetPackage {
    //public static final int MAX_PRESETS = 1000;

    public static final String PRESET_PKG_EXT = "zppkg";
    public static final String PRESET_PKG_CONTENT_ENTRY = "zppkg_contents";

    public IsolatedPreset[] getPresets();

    public SamplePackage getSamplePackage();       // possibly null

    public MultiModeMap getMultiModeMap();    // possibly null

    public Integer[] getMasterIds();       // possibly null

    public Integer[] getMasterVals();       // possibly null

    public Header getHeader();

    public IntegerUseMap getSampleUsage();

    public Map getCustomObjectMap();

    public static interface Header extends PackageHeader {
        public boolean isIncludingMasterSettings();

        public boolean isIncludingMultimodeSettings();

        public boolean isIncludingSamples();

        public int getPresetCount();
    }

    /* public class PresetReferencingException extends Exception {
         public PresetReferencingException(String message) {
             super(message);
         }
     }*/
}
