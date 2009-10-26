package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.desktop.ViewManager;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.gui.desktop.DesktopBranch;
import com.pcmsolutions.gui.desktop.DesktopElement;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.system.threads.ZDefaultThread;
import com.pcmsolutions.system.Linkable;
import com.pcmsolutions.system.ZDisposable;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Vector;
import java.lang.reflect.InvocationTargetException;

/**
 * User: paulmeehan
 * Date: 16-Feb-2004
 * Time: 18:32:32
 */
class Impl_ViewManager implements ViewManager, Serializable, ZDisposable {
    transient private Vector listeners;
    private Vector snapshots = new Vector();
    private DeviceContext device;

    transient private ViewMediator.TaskDeviceWorkspace task_deviceWorkspace;
    transient private ViewMediator.TaskDevice task_device;
    transient private ViewMediator.TaskProperties task_properties;
    transient private ViewMediator.TaskDefaultPresetContext task_presetContext;
    transient private ViewMediator.TaskDefaultSampleContext task_sampleContext;
    transient private ViewMediator.TaskMultiMode task_multiMode;
    transient private ViewMediator.TaskMaster task_master;

    private DesktopElement[] desktopElementsOnNextStart;

    public void zDispose() {
        listeners.clear();
        listeners = null;
        snapshots.clear();
        snapshots = null;

        desktopElementsOnNextStart = null;
        r = null;
        /*
        device = null;
        task_deviceWorkspace = null;
        task_device = null;
        task_properties = null;
        task_presetContext = null;
        task_sampleContext = null;
        task_multiMode = null;
        task_master = null;
        */
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        makeTransients();
    }

    private transient Runnable r;

    private void restoreDesktopElements() {
        if (desktopElementsOnNextStart != null)
            ViewMediator.openDesktopElements(desktopElementsOnNextStart);
        desktopElementsOnNextStart = null;
    }

    private void makeTransients() {
        listeners = new Vector();
        r = new Runnable() {
            public void run() {
                synchronized (listeners) {
                    for (int i = 0; i < listeners.size(); i++) {
                        try {
                            ((Listener) listeners.get(i)).viewManagerStateChanged(device);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
    }

    private ViewMediator.TaskDeviceWorkspace getDeviceWorkspaceTask() {
        if (task_deviceWorkspace == null)
            task_deviceWorkspace = new ViewMediator.TaskDeviceWorkspace(device);
        return task_deviceWorkspace;
    }

    private ViewMediator.TaskDevice getDeviceTask() {
        if (task_device == null)
            task_device = new ViewMediator.TaskDevice(device);
        return task_device;
    }

    private ViewMediator.TaskProperties getPropertiesTask() {
        if (task_properties == null)
            task_properties = new ViewMediator.TaskProperties(device);
        return task_properties;
    }

    private ViewMediator.TaskMultiMode getMultiModeTask() {
        if (task_multiMode == null)
            task_multiMode = new ViewMediator.TaskMultiMode(device);
        return task_multiMode;
    }

    private ViewMediator.TaskMaster getMasterTask() {
        if (task_master == null)
            task_master = new ViewMediator.TaskMaster(device);
        return task_master;
    }

    private ViewMediator.TaskDefaultPresetContext getDefaultPresetContextTask() {
        if (task_presetContext == null)
            task_presetContext = new ViewMediator.TaskDefaultPresetContext(device);
        return task_presetContext;
    }

    private ViewMediator.TaskDefaultSampleContext getDefaultSampleContextTask() {
        if (task_sampleContext == null)
            task_sampleContext = new ViewMediator.TaskDefaultSampleContext(device);
        return task_sampleContext;
    }

    public Impl_ViewManager(DeviceContext device) {
        this.device = device;
        makeTransients();
    }

    protected void retrieveDeviceDesktopElementsForNextStart() {
        try {
            desktopElementsOnNextStart = ViewMediator.getDeviceDesktopElements(device);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected boolean hasDesktopElementsForNextStart() {
        return desktopElementsOnNextStart != null;
    }

    public Thread clearDeviceWorkspace() {
        return new ZDefaultThread() {
            public void run() {
                ViewMediator.modifyBranch(new DesktopBranch(new DesktopElement[]{getDeviceWorkspaceTask().desktopElement}), true, -1);
            }
        };
    }

    public Thread openDeviceViews() {
        return new ZDefaultThread() {
            public void run() {
                getDeviceWorkspaceTask().open(false);
                getDeviceTask().open(false);
                getPropertiesTask().open(false);
                getDefaultPresetContextTask().open(false);
                getDefaultSampleContextTask().open(false);
                getMultiModeTask().open(false);
                getMasterTask().open(false);

                // activate preset context
                getDefaultPresetContextTask().open(true);

                linkSCAndPC();

                restoreDesktopElements();
            }
        };
    }

    private void linkSCAndPC() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    try {
                        ZoeosFrame.getInstance().getZDesktopManager().mutuallyLinkComponents(getDefaultPresetContextTask().desktopElement.getViewPath(), getDefaultSampleContextTask().desktopElement.getViewPath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public Thread activateDevicePalettes() {
        return new ZDefaultThread() {
            public void run() {
                getDeviceTask().open(true);
                getPropertiesTask().open(true);
                getDefaultPresetContextTask().open(true);
                getDefaultSampleContextTask().open(true);
                getMultiModeTask().open(true);
                getMasterTask().open(true);
                linkSCAndPC();
            }
        };
    }

    public Thread closeDeviceViews() {
        return new ZDefaultThread() {
            public void run() {
                Thread t = clearDeviceWorkspace();
                t.start();
                while(t.isAlive())
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                getDeviceWorkspaceTask().close();
                getDeviceTask().close();
                getPropertiesTask().close();
                getDefaultPresetContextTask().close();
                getDefaultSampleContextTask().close();
                getMultiModeTask().close();
                getMasterTask().close();
            }
        };
    }

    public Thread modifyBranch(final DesktopBranch branch, final boolean activate, final int clipIndex) {
        return new ZDefaultThread() {
            public void run() {
                ViewMediator.modifyBranch(branch, activate, clipIndex);
            }
        };
    }

    public void openVoice(ContextEditablePreset.EditableVoice voice, boolean activate) {
        new ViewMediator.TaskVoice(voice).open(activate);
    }

    public void openVoices(ContextEditablePreset.EditableVoice[] voices, boolean activate) {
        new ViewMediator.TaskVoice(voices).open(activate);
    }

    public void openTabbedVoice(ContextEditablePreset.EditableVoice voice, boolean groupEnvelopes, boolean activate) {
        new ViewMediator.TabbedTaskVoice(voice, groupEnvelopes).open(activate);
    }

    public void openTabbedVoices(ContextEditablePreset.EditableVoice[] voices, boolean groupEnvelopes, boolean activate) {
        new ViewMediator.TabbedTaskVoice(voices, groupEnvelopes).open(activate);
    }

    public void openVoice(ReadablePreset.ReadableVoice voice, boolean activate) {
        new ViewMediator.TaskVoice(voice).open(activate);
    }

    public void openTabbedVoice(ReadablePreset.ReadableVoice voice, boolean groupEnvelopes, boolean activate) {
        new ViewMediator.TabbedTaskVoice(voice, groupEnvelopes).open(activate);
    }

    public void openPreset(ReadablePreset p) {
        new ViewMediator.TaskPreset(p).open();
    }

    public void openPreset(ContextEditablePreset p) {
        new ViewMediator.TaskPreset(p).open();
    }

    public void openDesktopElements(DesktopElement[] elements) {
        ViewMediator.openDesktopElements(elements);
    }

    public void takeSnapshot(final String title) {
        new ZDefaultThread() {
            public void run() {
                try {
                    DesktopBranch db = new DesktopBranch(ViewMediator.getDeviceDesktopElements(device), title);
                    if (db.count() == 0)
                        return;
                    snapshots.add(db);
                    fireStateChanged(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public DesktopBranch[] getSnapshots() {
        synchronized (snapshots) {
            return (DesktopBranch[]) snapshots.toArray(new DesktopBranch[snapshots.size()]);
        }
    }

    public void clearSnapshots() {
        snapshots.removeAllElements();
        fireStateChanged(true);
    }

    public void removeSnapshot(int index) {
        try {
            snapshots.remove(index);
            fireStateChanged(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeSnapshot(DesktopBranch branch) {
        if (snapshots.remove(branch))
            fireStateChanged(true);
    }

    public void addViewManagerListener(ViewManager.Listener l) {
        listeners.add(l);
    }

    public void removeViewManagerListener(ViewManager.Listener l) {
        listeners.remove(l);
    }

    protected void fireStateChanged(boolean onUIThread) {
        if (onUIThread) {
            SwingUtilities.invokeLater(r);
        } else {
            r.run();
        }
    }
}
