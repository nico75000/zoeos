/*
 * PresetListener.java
 *
 * Created on January 16, 2003, 8:33 AM
 */

package com.pcmsolutions.device.EMU.E4.preset;

import com.pcmsolutions.device.EMU.E4.events.*;


/**
 *
 * @author  pmeehan
 */
public interface PresetListener {
    public void presetInitialized(PresetInitializeEvent ev);

    public void presetInitializationStatusChanged(PresetInitializationStatusChangedEvent ev);

    public void presetRefreshed(PresetRefreshEvent ev);

    public void presetChanged(PresetChangeEvent ev);

    public void presetNameChanged(PresetNameChangeEvent ev);

    public void voiceAdded(VoiceAddEvent ev);

    public void voiceRemoved(VoiceRemoveEvent ev);

    public void voiceChanged(VoiceChangeEvent ev);

    public void linkAdded(LinkAddEvent ev);

    public void linkRemoved(LinkRemoveEvent ev);

    public void linkChanged(LinkChangeEvent ev);

    public void zoneAdded(ZoneAddEvent ev);

    public void zoneRemoved(ZoneRemoveEvent ev);

    public void zoneChanged(ZoneChangeEvent ev);
}
