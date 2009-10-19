package com.pcmsolutions.device.EMU.E4.desktop;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.gui.desktop.DesktopBranch;
import com.pcmsolutions.gui.desktop.DesktopElement;

/**
 * User: paulmeehan
 * Date: 16-Feb-2004
 * Time: 18:29:34
 */
public interface ViewManager {
    public void takeSnapshot(String title);

    public DesktopBranch[] getSnapshots();

    public void clearSnapshots();

    public void removeSnapshot(int index);

    public void removeSnapshot(DesktopBranch branch);

    public void addViewManagerListener(Listener l);

    public void removeViewManagerListener(Listener l);

    public Thread openDeviceViews();

    public Thread clearDeviceWorkspace();

    public Thread activateDevicePalettes();

    public Thread closeDeviceViews();

    public Thread modifyBranch(DesktopBranch branch, boolean activate, int clipIndex);

    public void openVoice(ContextEditablePreset.EditableVoice voice, boolean activate);

    public void openVoices(ContextEditablePreset.EditableVoice[] voices, boolean activate);

    public void openTabbedVoice(ContextEditablePreset.EditableVoice voice, boolean groupEnvelopes, boolean activate);

    public void openTabbedVoices(ContextEditablePreset.EditableVoice[] voices, boolean groupEnvelopes, boolean activate);

    public void openVoice(ReadablePreset.ReadableVoice voice, boolean activate);

    public void openTabbedVoice(ReadablePreset.ReadableVoice voice, boolean groupEnvelopes, boolean activate);

    public void openPreset(ReadablePreset p);

    public void openPreset(ContextEditablePreset p);

    public void openDesktopElements(DesktopElement[] elements);

    public interface Listener {
        public void viewManagerStateChanged(DeviceContext device);
    }
}
