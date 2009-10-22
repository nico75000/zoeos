package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.desktop.ViewManager;
import com.pcmsolutions.device.EMU.E4.events.preset.PresetInitializationStatusChangedEvent;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.PresetException;
import com.pcmsolutions.device.EMU.E4.preset.PresetListenerAdapter;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.gui.ProgressSession;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.gui.desktop.DesktopBranch;
import com.pcmsolutions.gui.desktop.DesktopElement;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.tasking.ManageableTicketedQ;
import com.pcmsolutions.system.tasking.QueueFactory;
import com.pcmsolutions.system.tasking.Ticket;
import com.pcmsolutions.system.tasking.TicketRunnable;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * User: paulmeehan
 * Date: 16-Feb-2004
 * Time: 18:32:32
 */
class Impl_ViewManager implements ViewManager, Serializable, ZDisposable {
    transient Vector listeners;
    final Vector snapshots = new Vector();
    final E4Device device;

    transient private ViewMediator.TaskDeviceWorkspace task_deviceWorkspace;
    transient private ViewMediator.TaskDevice task_device;
    transient private ViewMediator.TaskProperties task_properties;
    transient private ViewMediator.TaskPiano task_piano;
    transient private ViewMediator.TaskDefaultPresetContext task_presetContext;
    transient private ViewMediator.TaskDefaultSampleContext task_sampleContext;
    transient private ViewMediator.TaskMultiMode task_multiMode;
    transient private ViewMediator.TaskMaster task_master;
    transient private PresetListenerAdapter pla;

    transient private boolean presetInitMonitorsSetup = false;
    transient private ManageableTicketedQ viewManagerQ;

    private DesktopElement[] desktopElementsOnNextStart;

    public void zDispose() {
        viewManagerQ.stop(true);
        deregisterPresetInitializationMonitors();
        listeners.clear();
        snapshots.clear();
        desktopElementsOnNextStart = null;
        r = null;
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        makeTransients();
    }

    private transient Runnable r;

    private synchronized void restoreDesktop() {
        if (desktopElementsOnNextStart != null && device.getDevicePreferences().ZPREF_reopenPreviousEditors.getValue()) {
            /* int count = 0;
             int q = 4;
             while (count < desktopElementsOnNextStart.length) {
                 int quant = count + q < desktopElementsOnNextStart.length ? q : desktopElementsOnNextStart.length - count;
                 DesktopElement[] elems = new DesktopElement[quant];
                 System.arraycopy(desktopElementsOnNextStart, count, elems, 0, quant);
                 ViewMediator.openDesktopElements(elems);
                 count += quant;
             }*/
            ViewMediator.openDesktopElements(desktopElementsOnNextStart);
        }
        desktopElementsOnNextStart = null;
    }

    private void makeTransients() {
        viewManagerQ = QueueFactory.createTicketedQueue(this, "viewManagerUIQ", 6);
        viewManagerQ.start();
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
        pla = new PresetListenerAdapter() {
            final HashMap<Integer, ProgressSession> progs = new HashMap<Integer, ProgressSession>();

            public void presetInitializationStatusChanged(final PresetInitializationStatusChangedEvent ev) {
                try {
                    double st = ev.getStatus();
                    if (st == 0) {
                        ProgressSession ps = progs.remove(ev.getIndex());
                        if (ps != null)
                            ps.end();
                        progs.remove(ev.getIndex());
                    } else if (st > 0) {   // just incoming dumps
                        if (!progs.containsKey(ev.getIndex())) {
                            ReadablePreset p = device.getDefaultPresetContext().getReadablePreset(ev.getIndex());
                            progs.put(ev.getIndex(), Zoeos.getInstance().getProgressSession(device.makeDeviceProgressTitle("Initializing " + p.getDisplayName()), 100));
                        }
                        progs.get(ev.getIndex()).updateStatus((int) (100 * Math.abs(st)));
                    }
                    return;
                } catch (DeviceException e) {
                    e.printStackTrace();
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

    private ViewMediator.TaskPiano getPianoTask() {
        if (task_piano == null)
            task_piano = new ViewMediator.TaskPiano(device);
        return task_piano;
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

    public Impl_ViewManager(E4Device device) {
        this.device = device;
        makeTransients();
    }

    private Object makeProgressObject(PresetInitializationStatusChangedEvent ev) {
        return device.getStaticName() + ev.getIndex();
    }

    void registerPresetInitializationMonitors() {
        if (!presetInitMonitorsSetup) {
            try {
                Set s = device.getDefaultPresetContext().getDatabaseIndexes();
                device.presetDB.getRootContext().addContentListener(pla, (Integer[]) s.toArray(new Integer[s.size()]));
                presetInitMonitorsSetup = true;
            } catch (DeviceException e) {
                e.printStackTrace();
            }
        }
    }

    void deregisterPresetInitializationMonitors() {
        if (presetInitMonitorsSetup) {
            try {
                Set s = device.getDefaultPresetContext().getDatabaseIndexes();
                device.presetDB.getRootContext().removeContentListener(pla, (Integer[]) s.toArray(new Integer[s.size()]));
                presetInitMonitorsSetup = false;
            } catch (NoSuchContextException e) {
                // e.printStackTrace();
            } catch (DeviceException e) {
                //e.printStackTrace();
            }
        }
    }

    protected void retrieveDeviceDesktopElementsForNextStart() {
        try {
            //desktopElementsOnNextStart = ViewMediator.getDeviceWorkspaceDesktopElements(device);
            desktopElementsOnNextStart = ViewMediator.getAllDeviceDesktopElements(device);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected boolean hasDesktopElementsForNextStart() {
        return desktopElementsOnNextStart != null;
    }

    Ticket sendWorkspaceMessage(final String msg) {
        return viewManagerQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                ViewMediator.sendWorkspaceMessage(device, msg);
            }
        }, "Send workspace message");
    }

    Ticket sendPresetContextMessage(final String msg) {
        return viewManagerQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                ViewMediator.sendPresetContextMessage(device, msg);
            }
        }, "MSG:" + msg);
    }

    Ticket sendSampleContextMessage(final String msg) {
        return viewManagerQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                ViewMediator.sendSampleContextMessage(device, msg);
            }
        }, "MSG:" + msg);
    }

    public Ticket brodcastCloseIfEmpty() {
        return sendWorkspaceMessage(ViewMessaging.MSG_BROADCAST_CLOSE_EMPTY);
    }

    public Ticket addPresetsToPresetContextFilter(Integer[] presets) {
        return sendPresetContextMessage(ViewMessaging.applyFieldsToMessage(ViewMessaging.MSG_ADD_PRESETS_TO_PRESET_CONTEXT_FILTER, presets));
    }

    public Ticket selectOpenPresetsInPresetContext() {
        return viewManagerQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                DesktopElement[] positives = ViewMediator.evaluateWorkspaceCondition(device, ViewMessaging.CONDITION_IS_OPEN_PRESET);
                HashSet<Integer> positiveIndexes = new HashSet<Integer>();
                for (DesktopElement de : positives)
                    if (de instanceof ViewMediator.PresetIndexProvider)
                        positiveIndexes.add(((ViewMediator.PresetIndexProvider) de).getPresetIndex());
                if (positiveIndexes.size() > 0)
                    sendPresetContextMessage(ViewMessaging.applyFieldsToMessage(ViewMessaging.MSG_SELECT_OPEN_PRESETS_IN_PRESET_CONTEXT, positiveIndexes.toArray())).post();
            }
        }, "selectOpenPresetsInPresetContext");
    }

    public Ticket addSamplesToSampleContextFilter(Integer[] samples) {
        return sendSampleContextMessage(ViewMessaging.applyFieldsToMessage(ViewMessaging.MSG_ADD_SAMPLES_TO_SAMPLE_CONTEXT_FILTER, samples));
    }

    public Ticket closeEmptyPresets() {
        return sendWorkspaceMessage(ViewMessaging.MSG_CLOSE_PRESET_EMPTY);
    }

    public Ticket closeEmptyVoices() {
        return sendWorkspaceMessage(ViewMessaging.MSG_CLOSE_VOICE);
    }

    public Ticket closeFlashPresets() {
        return sendWorkspaceMessage(ViewMessaging.MSG_CLOSE_PRESET_FLASH);

    }

    public Ticket closeUserPresets() {
        return sendWorkspaceMessage(ViewMessaging.MSG_CLOSE_PRESET_USER);
    }

    public Ticket clearDeviceWorkspace() {
        return viewManagerQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                ViewMediator.modifyBranch(new DesktopBranch(new DesktopElement[]{getDeviceWorkspaceTask().getFirstDesktopElement()}), true, -1);
            }
        }, "Clear device workspace");
    }

    public Ticket openDeviceViews() {
        return viewManagerQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                getDeviceWorkspaceTask().open(false);
                getDeviceTask().open(false);
                getPropertiesTask().open(false);
                //getPianoTask().assertOpen(false);
                getDefaultPresetContextTask().open(false);
                getDefaultSampleContextTask().open(false);
                getMultiModeTask().open(false);
                getMasterTask().open(false);
                // activate preset context
                getDefaultPresetContextTask().open(true);
                linkSCAndPC();
            }
        }, "Open device views");
    }

    public synchronized void invalidateDesktopElements() {
        desktopElementsOnNextStart = null;
    }

    public Ticket restoreDesktopElements() {
        return viewManagerQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                restoreDesktop();
            }
        }, "Restore desktop");
    }

    private void linkSCAndPC() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    try {
                        ZoeosFrame.getInstance().getZDesktopManager().mutuallyLinkComponents(getDefaultPresetContextTask().getFirstDesktopElement().getViewPath(), getDefaultSampleContextTask().getFirstDesktopElement().getViewPath());
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

    public Ticket activateDevicePalettes() {
        return viewManagerQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                getDeviceTask().open(true);
                getPropertiesTask().open(true);
                //getPianoTask().assertOpen(true);
                getDefaultPresetContextTask().open(true);
                getDefaultSampleContextTask().open(true);
                getMultiModeTask().open(true);
                getMasterTask().open(true);
                linkSCAndPC();
            }
        }, "Activate device palettes");
    }

    public Ticket closeDeviceViews() {
        return viewManagerQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                //clearDeviceWorkspace().send(0);
                getDeviceWorkspaceTask().close();
                getDeviceTask().close();
                getPropertiesTask().close();
                // getPianoTask().referencedClose();
                getDefaultPresetContextTask().close();
                getDefaultSampleContextTask().close();
                getMultiModeTask().close();
                getMasterTask().close();
            }
        }, "Activate device palettes");
    }

    public Ticket modifyBranch(final DesktopBranch branch, final boolean activate, final int clipIndex) {
        return viewManagerQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                ViewMediator.modifyBranch(branch, activate, clipIndex);
            }
        }, "Modify device desktop branch");
    }

    public Ticket openVoice(final ContextEditablePreset.EditableVoice voice, final boolean activate) {
        return viewManagerQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                new ViewMediator.TaskVoice(voice).open(activate);
            }
        }, "Open voice");
    }

    public Ticket openVoices(final ContextEditablePreset.EditableVoice[] voices, final boolean activate) {
        return viewManagerQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                new ViewMediator.TaskVoice(voices).open(activate);
            }
        }, "Open voices");
    }

    public Ticket openTabbedVoice(final ContextEditablePreset.EditableVoice voice, final boolean groupEnvelopes, final boolean activate) {
        return viewManagerQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                new ViewMediator.TabbedTaskVoice(voice, groupEnvelopes).open(activate);
            }
        }, "Open tabbed voice");
    }

    public Ticket openTabbedVoices(final ContextEditablePreset.EditableVoice[] voices, final boolean groupEnvelopes, final boolean activate) {
        return viewManagerQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                new ViewMediator.TabbedTaskVoice(voices, groupEnvelopes).open(activate);
            }
        }, "Open tabbed voices");
    }

    public Ticket openVoice(final ReadablePreset.ReadableVoice voice, final boolean activate) {
        return viewManagerQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                new ViewMediator.TaskVoice(voice).open(activate);
            }
        }, "Open voice");
    }

    public Ticket openTabbedVoice(final ReadablePreset.ReadableVoice voice, final boolean groupEnvelopes, final boolean activate) {
        return viewManagerQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                new ViewMediator.TabbedTaskVoice(voice, groupEnvelopes).open(activate);
            }
        }, "Open tabbed voice");
    }

    public Ticket openPreset(final ReadablePreset p, final boolean activate) {
        return viewManagerQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                if (p instanceof ContextEditablePreset) {
                    new ViewMediator.TaskPreset((ContextEditablePreset) p).open(activate);
                    //new ViewMediator.TaskPresetUser((ContextEditablePreset) p).assertOpen(false);
                } else {
                    new ViewMediator.TaskPreset(p).open(activate);
                    //new ViewMediator.TaskPresetUser(p).assertOpen(false);
                }
            }
        }, "Open preset");
    }

    public Ticket closePreset(final ReadablePreset p) {
        return viewManagerQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                if (p instanceof ContextEditablePreset) {
                    new ViewMediator.TaskPreset((ContextEditablePreset) p).close();
                    // new ViewMediator.TaskPresetUser((ContextEditablePreset) p).referencedClose();
                } else {
                    new ViewMediator.TaskPreset(p).close();
                    //  new ViewMediator.TaskPresetUser(p).referencedClose();
                }
            }
        }, "Close preset");
    }

    public Ticket openPreset(final ContextEditablePreset p, final boolean activate) {
        return viewManagerQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                new ViewMediator.TaskPreset(p).open(activate);
            }
        }, "Open preset");
    }

    public Ticket openDesktopElements(final DesktopElement[] elements) {
        return viewManagerQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                ViewMediator.openDesktopElements(elements);
            }
        }, "Open device desktop elements");
    }

    public boolean hasWorkspaceElements() throws Exception {
        return ViewMediator.hasWorkspaceElements(device);
    }

    public Ticket takeSnapshot(final String title) {
        return viewManagerQ.getTicket(new TicketRunnable() {
            public void run() throws Exception {
                DesktopBranch db = new DesktopBranch(ViewMediator.getDeviceWorkspaceDesktopElements(device), title);
                if (db.count() == 0)
                    return;
                snapshots.add(db);
                fireStateChanged(true);
            }
        }, "Take device desktop snapshot");
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
        if (listeners != null)
            listeners.add(l);
    }

    public void removeViewManagerListener(ViewManager.Listener l) {
        if (listeners != null)
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
