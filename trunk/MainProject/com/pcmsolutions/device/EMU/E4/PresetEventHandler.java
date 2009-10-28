package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.events.preset.PresetEvent;
import com.pcmsolutions.device.EMU.E4.preset.PresetListener;
import com.pcmsolutions.device.EMU.database.events.content.ManageableContentEventHandler;


interface PresetEventHandler {
    interface ExternalHandler{
       boolean handleEvent(PresetEvent pe);
        void setContentEventHandler(ManageableContentEventHandler ceh);
    }
    public void postPresetEvent(PresetEvent ev);

    public void postInternalPresetEvent(PresetEvent ev);

    public void postExternalPresetEvent(PresetEvent ev);

    public void addExternalHandler(ExternalHandler eh);

    public void removeExternalHandler(ExternalHandler eh);

    public void addPresetListener(PresetListener pl, Integer preset);

    public void removePresetListener(PresetListener pl, Integer preset);
}

