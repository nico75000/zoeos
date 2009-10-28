package com.pcmsolutions.device.EMU.E4.desktop;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.ViewMessaging;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.gui.desktop.DesktopBranch;
import com.pcmsolutions.gui.desktop.DesktopElement;
import com.pcmsolutions.system.threads.Impl_ZThread;
import com.pcmsolutions.system.tasking.Ticket;

/**
 * User: paulmeehan
 * Date: 16-Feb-2004
 * Time: 18:29:34
 */
public interface ViewManager {
    interface PresetMatcher{
        boolean isMatch(ReadablePreset p);
    }

    public Ticket takeSnapshot(String title);

    public DesktopBranch[] getSnapshots();

    public void clearSnapshots();

    public void removeSnapshot(int index);

    public void removeSnapshot(DesktopBranch branch);

    public void addViewManagerListener(Listener l);

    public void removeViewManagerListener(Listener l);

    public Ticket openDeviceViews();

    public Ticket restoreDesktopElements();

    public Ticket addPresetsToPresetContextFilter(Integer[] presets);

    public Ticket selectOpenPresetsInPresetContext();

    public Ticket addSamplesToSampleContextFilter(Integer[] samples);

    public Ticket brodcastCloseIfEmpty();

    public Ticket clearDeviceWorkspace();

    public Ticket closeEmptyPresets();

    public Ticket closeEmptyVoices();

    public Ticket closeFlashPresets();

    public Ticket closeUserPresets();

    public Ticket activateDevicePalettes();

    public Ticket closeDeviceViews();

    public Ticket modifyBranch(DesktopBranch branch, boolean activate, int clipIndex);

    public Ticket openVoice(ContextEditablePreset.EditableVoice voice, boolean activate);

    public Ticket openVoices(ContextEditablePreset.EditableVoice[] voices, boolean activate);

    public Ticket openTabbedVoice(ContextEditablePreset.EditableVoice voice, boolean groupEnvelopes, boolean activate);

    public Ticket openTabbedVoices(ContextEditablePreset.EditableVoice[] voices, boolean groupEnvelopes, boolean activate);

    public Ticket openVoice(ReadablePreset.ReadableVoice voice, boolean activate);

    public Ticket openTabbedVoice(ReadablePreset.ReadableVoice voice, boolean groupEnvelopes, boolean activate);

    public Ticket openPreset(ReadablePreset p, boolean activate);

    public Ticket openPreset(ContextEditablePreset p, boolean activate);

    public Ticket openDesktopElements(DesktopElement[] elements);

    public boolean hasWorkspaceElements() throws Exception;
    
    public Ticket closePreset(final ReadablePreset p);

    public interface Listener {
        public void viewManagerStateChanged(DeviceContext device);
    }
}
