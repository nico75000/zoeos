/*
 * Device.java
 *
 * Created on December 14, 2002, 12:28 AM
 */

package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.comms.RemoteDeviceDidNotRespondException;
import com.pcmsolutions.comms.RemoteMessagingException;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.DeviceLock;
import com.pcmsolutions.device.EMU.E4.desktop.ViewManager;
import com.pcmsolutions.device.EMU.E4.events.master.MasterChangedEvent;
import com.pcmsolutions.device.EMU.E4.events.master.MasterEvent;
import com.pcmsolutions.device.EMU.E4.events.master.MasterRefreshedEvent;
import com.pcmsolutions.device.EMU.E4.events.multimode.MultiModeChannelChangedEvent;
import com.pcmsolutions.device.EMU.E4.events.multimode.MultiModeEvent;
import com.pcmsolutions.device.EMU.E4.events.multimode.MultiModeRefreshedEvent;
import com.pcmsolutions.device.EMU.E4.events.preset.*;
import com.pcmsolutions.device.EMU.E4.gui.TitleProvider;
import com.pcmsolutions.device.EMU.E4.gui.TitleProviderListener;
import com.pcmsolutions.device.EMU.E4.gui.TitleProviderListenerHelper;
import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.device.DeviceIcon;
import com.pcmsolutions.device.EMU.E4.master.MasterContext;
import com.pcmsolutions.device.EMU.E4.master.MasterListener;
import com.pcmsolutions.device.EMU.E4.multimode.*;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.preset.PresetContext;
import com.pcmsolutions.device.EMU.E4.preset.PresetContextMacros;
import com.pcmsolutions.device.EMU.E4.preset.PresetListener;
import com.pcmsolutions.device.EMU.E4.remote.ParameterEditLoader;
import com.pcmsolutions.device.EMU.E4.remote.Remotable;
import com.pcmsolutions.device.EMU.E4.remote.Remotable.DeviceConfig;
import com.pcmsolutions.device.EMU.E4.remote.Remotable.DeviceExConfig;
import com.pcmsolutions.device.EMU.E4.remote.Remotable.PresetMemory;
import com.pcmsolutions.device.EMU.E4.remote.Remotable.SampleMemory;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.E4.zcommands.E4DeviceZCommandMarker;
import com.pcmsolutions.device.EMU.E4.zcommands.E4MasterContextZCommandMarker;
import com.pcmsolutions.device.EMU.E4.zcommands.E4MultiModeChannelZCommandMarker;
import com.pcmsolutions.device.EMU.E4.zcommands.E4MultiModeContextZCommandMarker;
import com.pcmsolutions.device.EMU.database.ContentUnavailableException;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.gui.*;
import com.pcmsolutions.license.LicenseKeyManager;
import com.pcmsolutions.system.*;
import com.pcmsolutions.system.callback.Callback;
import com.pcmsolutions.system.delays.DelayFactory;
import com.pcmsolutions.system.tasking.*;
import com.pcmsolutions.system.threads.ZThread;

import javax.sound.midi.MidiMessage;
import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 * @author pmeehan
 */
class E4Device extends AbstractZDevice implements DeviceContext, RemoteAssignable, ZDisposable, Serializable, TitleProvider {
    private static final int iconWidth = 18;
    private static final int iconHeight = 12;
    private static final Icon ultraIcon = new DeviceIcon(iconWidth, iconHeight, Color.white, UIColors.getUltraDeviceIcon());
    private static final Icon classicIcon = new DeviceIcon(iconWidth, iconHeight, Color.white, UIColors.getClassicDeviceIcon());

    // PARAMETERS
    protected DeviceParameterContext deviceParameterContext;

    // ZCOMMANDS
    private static final ZCommandProviderHelper cmdProviderHelper;
    private static final ZCommandProviderHelper masterCmdProviderHelper;
    private static final ZCommandProviderHelper multiModeCmdProviderHelper;
    private static final ZCommandProviderHelper multiModeChannelCmdProviderHelper;

    // EVENTS
    private TitleProviderListenerHelper tplh = new TitleProviderListenerHelper(this);

    // DATABASES
    protected PresetDatabase presetDB;
    protected SampleDatabase sampleDB;

    // CONTEXTS
    protected Impl_MultiModeContext mmContext;
    protected Impl_MasterContext masterContext;

    private final String PENDING_CONDITIONAL = "Device pending";
    private final String STOPPED_CONDITIONAL = "Device stopped";
    private final String REMOVED_CONDITIONAL = "Device removed";

    // DEVICE SYNCHRONIZATION
    final DeviceLock<DeviceException> deviceLock = new DeviceLock<DeviceException>(PENDING_CONDITIONAL) {
        public DeviceException generateConditionException() {
            return new DeviceException(getConditionString());
        }

    };
    private transient ManageableTicketedQ internalQ;
    transient Impl_DeviceQueues queues;

    // REMOTING
    protected transient com.pcmsolutions.device.EMU.E4.remote.Remotable remote;
    transient protected Vector deviceExceptions;
    RemotePresetSynchronizer presetSync;
    RemoteSampleSynchronizer sampleSync;

    // PREFERENCES
    transient private DevicePreferences devicePreferences;

    // AUDITIONING
    transient private Impl_AuditionManager auditionManager;

    // VIEWS
    private Impl_ViewManager viewManager = new Impl_ViewManager(this);

    // DEVICE CONFIGURATION
    private volatile int maxPreset;
    private volatile int maxSample;
    private DeviceConfig deviceConfig;
    private DeviceExConfig exDeviceConfig;
    private SampleMemory sampleMemory;
    private PresetMemory presetMemory;
    private volatile String deviceConfigReport;
    private final String[][] tabularDeviceConfigReport = new String[23][2];

    private interface ConfigTableModel extends TableModel, Serializable {
    };
    private ConfigTableModel configTableModel = new ConfigTableModel() {
        public int getRowCount() {
            return tabularDeviceConfigReport.length;
        }

        public int getColumnCount() {
            return 2;
        }

        public String getColumnName(int columnIndex) {
            return "";
        }

        public Class getColumnClass(int columnIndex) {
            return String.class.getClass();
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex >= 0 && rowIndex < tabularDeviceConfigReport.length && columnIndex >= 0 && columnIndex < 2)
                return tabularDeviceConfigReport[rowIndex][columnIndex];
            return "";
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        }

        public void addTableModelListener(TableModelListener l) {
        }

        public void removeTableModelListener(TableModelListener l) {
        }
    };

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        makeTransients();
    }

    private void makeTransients() {
        deviceExceptions = new Vector();
        internalQ = QueueFactory.createTicketedQueue(this, "Internal queue", 6);
        internalQ.start();
        queues = new Impl_DeviceQueues(this);
    }

    protected StdStateMachineHelper getStateMachineHelper() {
        return sts;
    }

    public DeviceQueues getQueues() {
        return queues;
    }

    public void zDispose() {
        //SMDIAgent.removeSmdiListener(this);
        deviceExceptions.clear();
        ZUtilities.zdispose(presetDB);
        ZUtilities.zdispose(sampleDB);
        ZUtilities.zdispose(masterContext);
        ZUtilities.zdispose(mmContext);
        ZUtilities.zdispose(remote);
        ZUtilities.zdispose(deviceParameterContext);
        ZUtilities.zdispose(devicePreferences);
        ZUtilities.zdispose(viewManager);

        tplh.clearListeners();
        //   smdiTarget = null;
        //deviceSampleMediator = null;

        // Don't null remote, tplh, listeners, or devicePreferences - they may be required during the device's exit from the system
        deviceConfig = null;
        exDeviceConfig = null;
        sampleMemory = null;
        presetMemory = null;
        presetDB = null;
        sampleDB = null;
        deviceParameterContext = null;
        deviceExceptions = null;
        configTableModel = null;
        // viewManager = null;
        mmContext = null;
        masterContext = null;
        ZUtilities.zdispose(queues);
        ZUtilities.zdispose(internalQ);
    }

    static {
        cmdProviderHelper = new ZCommandProviderHelper(E4DeviceZCommandMarker.class);
        masterCmdProviderHelper = new ZCommandProviderHelper(E4MasterContextZCommandMarker.class);
        multiModeCmdProviderHelper = new ZCommandProviderHelper(E4MultiModeContextZCommandMarker.class);
        multiModeChannelCmdProviderHelper = new ZCommandProviderHelper(E4MultiModeChannelZCommandMarker.class);
    }

    // CONSTRUCTORS
    public E4Device(com.pcmsolutions.device.EMU.E4.remote.Remotable remote, DevicePreferences prefs) {
        super(remote.getIdentityMessage());
        this.remote = remote;
        makeTransients();
        makeDeviceConfigReportHeader();
        this.devicePreferences = prefs;
        devicePreferences.device = this;
    }

    public void setRemote(Remotable r) {
        this.remote = r;
        presetSync.setRemote(r);
        sampleSync.setRemote(r);
    }

    protected void setPreferences(DevicePreferences dprefs) {
        devicePreferences = dprefs;
        dprefs.device = this;
        //sampleDB.setDevicePreferences(dprefs);
    }

    private final int VERIFY_LEAD = 4;
    private final int VERIFY_COUNT = 8;
    private final int VERIFY_INTERVAL = 16;
    private Integer[] defVerifyPresets;

    {
        defVerifyPresets = new Integer[VERIFY_COUNT + VERIFY_LEAD];
        for (int i = 0; i < VERIFY_LEAD; i++)
            defVerifyPresets[i] = IntPool.get(i);
        for (int i = VERIFY_LEAD; i < VERIFY_COUNT + VERIFY_LEAD; i++)
            defVerifyPresets[i] = IntPool.get(i * VERIFY_INTERVAL);
    }

    protected boolean verifyRemotePresetNames() {
        return verifyRemotePresetNames(defVerifyPresets);
    }

    protected boolean verifyRemotePresetNames(Integer[] presets) {
        deviceLock.immuneConfigure();
        try {
            String name;
            for (int i = 0; i < presets.length; i++) {
                try {
                    if (presetDB.isPending(presets[i]))
                        continue;
                    name = presetDB.getRootContext().getName(presets[i]);
                } catch (EmptyException e) {
                    name = DeviceContext.EMPTY_PRESET;
                } catch (ContentUnavailableException e) {
                    return false;
                } catch (DeviceException e) {
                    return false;
                }
                if (!remote.getPresetContext().req_name(presets[i]).trim().equals(name.trim()))
                    return false;
            }
            return true;
        } catch (RemoteDeviceDidNotRespondException e) {
            e.printStackTrace();
        } catch (RemoteMessagingException e) {
            e.printStackTrace();
        } catch (RemoteUnreachableException e) {
            e.printStackTrace();
        } finally {
            deviceLock.unlock();
        }
        return false;
    }

    private Integer[] defVerifySamples;

    {
        defVerifySamples = new Integer[VERIFY_COUNT + VERIFY_LEAD];
        for (int i = 0; i < VERIFY_LEAD; i++)
            defVerifySamples[i] = IntPool.get(i + 1);
        for (int i = VERIFY_LEAD; i < VERIFY_COUNT + VERIFY_LEAD; i++)
            defVerifySamples[i] = IntPool.get(i * VERIFY_INTERVAL + 1);
    }

    protected boolean verifyRemoteSampleNames() {
        return verifyRemoteSampleNames(defVerifySamples);
    }

    protected boolean verifyRemoteSampleNames(Integer[] samples) {
        deviceLock.immuneConfigure();
        try {
            String name;
            for (int i = 0; i < samples.length; i++) {
                try {
                    if (sampleDB.getRootContext().isPending(samples[i]))
                        continue;
                    name = sampleDB.getRootContext().getName(samples[i]);
                } catch (NoSuchContextException e) {
                    return false;
                } catch (DeviceException e) {
                    return false;
                } catch (ContentUnavailableException e) {
                    return false;
                } catch (EmptyException e) {
                    name = DeviceContext.EMPTY_SAMPLE;
                }
                if (!remote.getSampleContext().req_name(samples[i]).trim().equals(name.trim()))
                    return false;
            }
            return true;
        } catch (RemoteDeviceDidNotRespondException e) {
            e.printStackTrace();
        } catch (RemoteMessagingException e) {
            e.printStackTrace();
        } catch (RemoteUnreachableException e) {
            e.printStackTrace();
        } finally {
            deviceLock.unlock();
        }
        return false;
    }

    private void makeDeviceConfigReportHeader() {
        String ls = Zoeos.lineSeperator;
        deviceConfigReport =
                remote.getIdentityMessageReadable() + ls
                + "Inport:         " + remote.getInportIdentifier() + ls
                + "Outport:        " + remote.getOutportIdentifier() + ls;

        tabularDeviceConfigReport[0][0] = Zoeos.versionStr;
        tabularDeviceConfigReport[0][1] = new Date().toString();

        tabularDeviceConfigReport[1][0] = "";
        tabularDeviceConfigReport[1][1] = "";

        tabularDeviceConfigReport[2][0] = this.remote.getIdentityMessageReadable().toString();
        tabularDeviceConfigReport[2][1] = this.remote.getName();

        tabularDeviceConfigReport[3][0] = "Inport";
        tabularDeviceConfigReport[3][1] = this.remote.getInportIdentifier().toString();

        tabularDeviceConfigReport[4][0] = "Outport";
        tabularDeviceConfigReport[4][1] = this.remote.getOutportIdentifier().toString();

        tabularDeviceConfigReport[5][0] = "";
        tabularDeviceConfigReport[5][1] = "";
    }

    private void assertPresetDBStopped() {
        if (presetDB != null && presetDB.getState() == StdStates.STATE_STARTED)
            try {
                presetDB.stateStop();
            } catch (IllegalStateTransitionException e) {
                e.printStackTrace();
            }
    }

    private void assertSampleDBStopped() {
        if (sampleDB != null && sampleDB.getState() == StdStates.STATE_STARTED)
            try {
                sampleDB.stateStop();
            } catch (IllegalStateTransitionException e) {
                e.printStackTrace();
            }
    }

    private void assertRemoteStopped() {
        if (remote != null && remote.getState() == StdStates.STATE_STARTED)
            try {
                remote.stateStop();
            } catch (IllegalStateTransitionException e) {
                e.printStackTrace();
            }
    }

    public SyncTicket startDevice() throws ZDeviceStartupException, IllegalStateTransitionException {
        deviceLock.immuneConfigure(null);
        //  System.out.println("start_LOCKED");
        try {
            if (sts.testTransition(STATE_RUNNING) == STATE_RUNNING)
                return SyncTicket.uselessTicket;
            if (sts.getState() == ZExternalDevice.STATE_STOPPED)
                return startFromStopped();
            else
                return startFromPending();
        } finally {
            String condition = null;
            try {
                if (sts.getState() == STATE_PENDING) {
                    condition = PENDING_CONDITIONAL;
                    Zoeos.postZDeviceEvent(new ZDevicePendingEvent(this, this));
                    queues.stop(false);
                    assertSampleDBStopped();
                    assertPresetDBStopped();
                    assertRemoteStopped();
                }
                if (sts.getState() == STATE_STOPPED) {
                    condition = PENDING_CONDITIONAL;
                    Zoeos.postZDeviceEvent(new ZDeviceStoppedEvent(this, this, "Could not restart device"));
                    queues.stop(false);
                    assertSampleDBStopped();
                    assertPresetDBStopped();
                    assertRemoteStopped();
                }
            } finally {
                //    System.out.println("start_UNLOCKING");
                deviceLock.unlock(condition);
                //    System.out.println("start_UNLOCKED: conditional = " + condition);
            }
        }
    }

    private SyncTicket startFromPending() throws ZDeviceStartupException, IllegalStateTransitionException {
        try {
            remote.stateStart();
        } catch (IllegalStateTransitionException e) {
            throw new ZDeviceStartupException(this, "Could not start remote device");
        }

        try {
            deviceParameterContext = new Impl_DeviceParameterContext(this, remote);
        } catch (RemoteDeviceDidNotRespondException e) {
            throw new ZDeviceStartupException(this, "Device startup failed - remoting error - device did not respond");
        } catch (RemoteUnreachableException e) {
            throw new ZDeviceStartupException(this, "Device startup failed - remoting error - remote unreachable");
        } catch (RemoteMessagingException e) {
            throw new ZDeviceStartupException(this, "Device startup failed - remoting error - remote messaging exception");
        }

        // get device configuration here
        if (retrieveDeviceConfiguration(true) == false)
            throw new ZDeviceStartupException(this, "Failed to retrieve device config from remote device.");

        try {
            mmContext = new Impl_MultiModeContext();
        } catch (RemoteDeviceDidNotRespondException e) {
            throw new ZDeviceStartupException(this, "Device startup failed. Remoting error.");
        } catch (RemoteMessagingException e) {
            throw new ZDeviceStartupException(this, "Device startup failed. Remoting error.");
        } catch (RemoteUnreachableException e) {
            throw new ZDeviceStartupException(this, "Device startup failed. Remoting error.");
        } catch (IllegalParameterIdException e) {
            throw new ZDeviceStartupException(this, "Device startup failed. Parameter setup error.");
        }

        try {
            masterContext = new Impl_MasterContext();
        } catch (RemoteDeviceDidNotRespondException e) {
            throw new ZDeviceStartupException(this, "Device startup failed. Remoting error.");
        } catch (RemoteUnreachableException e) {
            throw new ZDeviceStartupException(this, "Device startup failed. Remoting error.");
        } catch (RemoteMessagingException e) {
            throw new ZDeviceStartupException(this, "Device startup failed. Remoting error.");
        }

        sampleDB = new SampleDatabase();
        presetDB = new PresetDatabase();
        presetDB.init(deviceParameterContext, this, sampleDB, "Default preset database", maxPreset);
        presetSync = new RemotePresetSynchronizer(this, presetDB, remote);
        presetDB.getEventHandler().addExternalHandler(presetSync);
        presetDB.getEventHandler().addRequestHandler(presetSync);
        sampleDB.init(this, presetDB, "Default sample database", maxSample);
        sampleSync = new RemoteSampleSynchronizer(this, remote);
        sampleDB.getEventHandler().addExternalHandler(sampleSync);
        sampleDB.getEventHandler().addRequestHandler(sampleSync);
        loadDatabases();
        presetDB.stateStart();
        sampleDB.stateStart();
        queues.start();

        ((Impl_MultiModeContext) mmContext).addChannelPresetListeners();

        sts.transition(STATE_RUNNING);
        presetDB.initializeAllPresetNames(false);
        sampleDB.initializeAllSampleData();
        viewManager.registerPresetInitializationMonitors();
        try {
            getAuditionManager();
        } catch (DeviceException e) {
            SystemErrors.internal(e);
        }
        try {
            internalQ.getPostableTicket(new TicketRunnable() {
                public void run() throws Exception {
                    viewManager.openDeviceViews().send(0);
                }
            }, "Open device views").post();
        } catch (ResourceUnavailableException e) {
            e.printStackTrace();
        }
        Zoeos.postZDeviceEvent(new ZDeviceStartedEvent(this, this));
        return internalQ.getSynchronizationTicket("Device start from pending synchronizer");
    }

    private SyncTicket startFromStopped() throws ZDeviceStartupException, IllegalStateTransitionException {
        try {
            remote.stateStart();
        } catch (IllegalStateTransitionException e) {
            Zoeos.postZDeviceEvent(new ZDeviceStoppedEvent(E4Device.this, E4Device.this, "Could not restart device"));
            throw new ZDeviceStartupException(this, "Could not restart remote device");
        }
        presetDB.stateStart();
        sampleDB.stateStart();
        presetDB.initializeAllPresetNames(false);
        sampleDB.initializeAllSampleData();
        queues.start();
        deviceExceptions.clear();
        try {
            internalQ.getPostableTicket(new TicketRunnable() {
                public void run() throws Exception {
                    viewManager.openDeviceViews().send(0);
                }
            }, "Open device views").post();
        } catch (ResourceUnavailableException e) {
            e.printStackTrace();
        }
        mmContext.task_refresh();
        masterContext.task_refresh();
        retrieveDeviceConfiguration(true);
        sts.transition(STATE_RUNNING);
        viewManager.registerPresetInitializationMonitors();
        try {
            internalQ.getPostableTicket(new TicketRunnable() {
                public void run() throws Exception {
                    boolean canRestore = viewManager.hasDesktopElementsForNextStart();
                    if (!verifyRemotePresetNames() || !verifyRemoteSampleNames())
                        if (UserMessaging.askYesNo("ZoeOS performed a quick check and the bank state for '" + E4Device.this.getName() + "' doesn't seem to match the bank state on the remote device. Refresh now?")) {
                            if (canRestore && !UserMessaging.askYesNo("Restore device workspace for '" + E4Device.this.getName() + "' after refresh?")) {
                                canRestore = false;
                                viewManager.invalidateDesktopElements();
                            }
                            try {
                                boolean clear = true;
                                try {
                                    clear = getViewManager().hasWorkspaceElements() && UserMessaging.askYesNo("Clear device workspace first (recommended)?");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (clear) {
                                    try {
                                        getViewManager().clearDeviceWorkspace().send(0);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                refreshBank(false).post();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    if (canRestore) {
                        try {
                            viewManager.restoreDesktopElements().send(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, "Restore desktop").post();
        } catch (ResourceUnavailableException e) {
            e.printStackTrace();
        }

        try {
            getAuditionManager();
        } catch (DeviceException e) {
            SystemErrors.internal(e);
        }

        Zoeos.postZDeviceEvent(new ZDeviceStartedEvent(this, this));
        return internalQ.getSynchronizationTicket("Device start from stopped synchronizer");
    }

    public void stopDevice(boolean waitForConfigurers, String reason) throws IllegalStateTransitionException {
        if (waitForConfigurers) {
            deviceLock.immuneConfigure(STOPPED_CONDITIONAL);
        } else {
            if (deviceLock.tryImmuneConfigure(STOPPED_CONDITIONAL) == false) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        new FlashMsg(ZoeosFrame.getInstance(), ZoeosFrame.getInstance(), 1000, 200, FlashMsg.colorWarning, "DEVICE BUSY");
                    }
                });
                return;
            }
        }
        // System.out.println("stop_LOCKED");
        try {
            if (sts.testTransition(STATE_STOPPED) == STATE_STOPPED)
                return;
            queues.stop(false);
            // System.out.println("Device " + this.getName() + " stopped queues");
            assertPresetDBStopped();
            // System.out.println("Device " + this.getName() + " stopped Preset DB");
            assertSampleDBStopped();
            // System.out.println("Device " + this.getName() + " stopped Sample DB");
            assertRemoteStopped();
            //  System.out.println("Device " + this.getName() + " stopped Remote");
            //  System.out.println("Device " + this.getName() + " stopped done");
            sts.transition(STATE_STOPPED, reason);
            Zoeos.postZDeviceEvent(new ZDeviceStoppedEvent(E4Device.this, E4Device.this, reason));
        } finally {
            //  System.out.println("stop_UNLOCKING");
            deviceLock.unlock((sts.getState() == STATE_STOPPED ? STOPPED_CONDITIONAL : null));
            //   System.out.println("stop_UNLOCKED: conditional = " + (sts.getState() == STATE_STOPPED ? STOPPED_CONDITIONAL : null));
        }
    }

    public void removeDevice(boolean saveState) throws ZDeviceCannotBeRemovedException, IllegalStateTransitionException {
        if (saveState)
            viewManager.retrieveDeviceDesktopElementsForNextStart();

        deviceLock.immuneConfigure();
        try {
            if (sts.testTransition(STATE_REMOVED) == STATE_REMOVED)
                return;
            assertPresetDBStopped();
            assertSampleDBStopped();
            assertRemoteStopped();
            if (saveState)
                taskSaveDeviceState();

            sts.transition(STATE_REMOVED);
            try {
                viewManager.closeDeviceViews().post(new Callback() {
                    public void result(Exception e, boolean wasCancelled) {
                        if (e != null)
                            e.printStackTrace();
                        zDispose();
                    }
                });
            } catch (ResourceUnavailableException e) {
                e.printStackTrace();
                zDispose();
            }
        } finally {
            deviceLock.unlock();
        }
        Zoeos.postZDeviceEvent(new ZDeviceRemovedEvent(this, this));
    }

    public String makeDeviceProgressTitle(String str) {
        return remote.makeDeviceProgressTitle(str);
    }

    private void loadDatabases() {
        try {
            if (getDevicePreferences().ZPREF_alwaysReloadROMSamples.getValue()) {
                ProgressSession ps = Zoeos.getInstance().getProgressSession(makeDeviceProgressTitle("Loading backup ROM data from previous session"), DeviceContext.MAX_USER_SAMPLE);
                ps.setIndeterminate(true);
                try {
                    sampleDB.loadRomSnapshot(SessionExternalization.loadRom(this));
                } catch (SessionExternalization.ExternalizationException e) {
                    e.printStackTrace();
                } finally {
                    ps.end();
                }
            }
            if (getDevicePreferences().ZPREF_alwaysReloadFlashPresets.getValue()) {
                ProgressSession ps = Zoeos.getInstance().getProgressSession(makeDeviceProgressTitle("Loading backup FLASH data from previous session"), DeviceContext.MAX_USER_SAMPLE);
                ps.setIndeterminate(true);
                try {
                    presetDB.loadFlashSnapshot(SessionExternalization.loadFlash(this));
                } catch (SessionExternalization.ExternalizationException e) {
                    e.printStackTrace();
                } finally {
                    ps.end();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PostableTicket saveDeviceState() {
        return internalQ.getPostableTicket(new TicketRunnable() {
            public void run() throws Exception {
                ProgressSession sess = Zoeos.getInstance().getProgressSession(E4Device.this.makeDeviceProgressTitle("saving state"), 100);
                sess.setIndeterminate(true);
                try {
                    viewManager.retrieveDeviceDesktopElementsForNextStart();
                    deviceLock.configure();
                    try {
                        taskSaveDeviceState();
                    } finally {
                        deviceLock.unlock();
                    }
                } finally {
                    sess.end();
                }
            }
        }, "saveDeviceState");
    }

    private void taskSaveDeviceState() {
        try {
            int st = this.getState();
            switch (st) {
                case STATE_RUNNING:
                    sts.transition(STATE_STOPPED, "Saving device state (session)");
                    queues.stop(false);
                    presetDB.stateStop();
                    sampleDB.stateStop();
                    try {
                        SessionExternalization.saveAsLastSession(SessionExternalization.makeDeviceSession(this));
                        SessionExternalization.saveFlash(this);
                        SessionExternalization.saveRom(this);
                    } finally {
                        presetDB.stateStart();
                        sampleDB.stateStart();
                        queues.start();
                        sts.transition(STATE_RUNNING, "");
                    }
                    break;
                case STATE_STOPPED:
                    try {
                        SessionExternalization.saveAsLastSession(SessionExternalization.makeDeviceSession(this));
                        SessionExternalization.saveFlash(this);
                        SessionExternalization.saveRom(this);
                    } finally {
                    }
                    break;
                case STATE_REMOVED:
                case STATE_PENDING:
                default:
                    return;
            }
            return;
        } catch (IllegalStateTransitionException e) {
            SystemErrors.internal(e);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public String getLicenseProduct() {
        return LicenseKeyManager.zoeosProduct;
    }

    public String getLicenseType() {
        return LicenseKeyManager.fullType;
    }

// PRIVATE HELPERS
    private boolean retrieveDeviceConfiguration(final boolean showProgress) {
        Zoeos z = Zoeos.getInstance();
        ProgressSession ps = null;
        if (showProgress)
            ps = z.getProgressSession(this.makeDeviceProgressTitle("Retrieving device configuration"), 7);
        try {
            MinMaxDefault mmd;

            try {
                mmd = remote.getParameterContext().req_prmMMD(IntPool.get(23)); // E4_LINK_PRESET
                maxPreset = mmd.getMax().intValue();
                if (showProgress)
                    ps.updateStatus();

                mmd = remote.getParameterContext().req_prmMMD(ID.sample); // E4_GEN_SAMPLE
                maxSample = mmd.getMax().intValue();
                if (showProgress)
                    ps.updateStatus();

                deviceConfig = remote.getMasterContext().req_deviceConfig();
                if (showProgress)
                    ps.updateStatus();

                exDeviceConfig = remote.getMasterContext().req_deviceExConfig();
                if (showProgress)
                    ps.updateStatus();

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                presetMemory = remote.getMasterContext().req_presetMemory();
                if (showProgress)
                    ps.updateStatus();

                sampleMemory = remote.getMasterContext().req_sampleMemory();
                if (showProgress)
                    ps.updateStatus();

            } catch (RemoteDeviceDidNotRespondException e) {
                return false;
            } catch (RemoteMessagingException e) {
                return false;
            } catch (com.pcmsolutions.device.EMU.E4.RemoteUnreachableException e) {
                return false;
            }

            String ls = Zoeos.getLineSeperator();

            makeDeviceConfigReportHeader();

            deviceConfigReport +=
                    exDeviceConfig + ls
                    + presetMemory + ls
                    + sampleMemory + ls;

            if (maxPreset > 999) {
                DecimalFormat df = new DecimalFormat("0000");
                deviceConfigReport += "Flash Presets   " + df.format(1000) + " ... " + df.format(maxPreset) + ls;
            } else
                deviceConfigReport += "No Flash Presets" + ls;

            if (maxSample > 999) {
                DecimalFormat df = new DecimalFormat("0000");
                deviceConfigReport += "ROM Samples     " + df.format(1000) + " ... " + df.format(maxSample) + ls;
            } else
                deviceConfigReport += "No ROM Samples  " + ls;

            deviceConfigReport += ls;


            tabularDeviceConfigReport[6][0] = "Voices";
            tabularDeviceConfigReport[6][1] = exDeviceConfig.getVoices().toString();

            tabularDeviceConfigReport[7][0] = "Legacy FX";
            tabularDeviceConfigReport[7][1] = (exDeviceConfig.hasFX() ? "Yes" : "No");

            tabularDeviceConfigReport[8][0] = "Midi Channels";
            tabularDeviceConfigReport[8][1] = (exDeviceConfig.hasMidi() ? "32" : "16");

            tabularDeviceConfigReport[9][0] = "ADAT";
            tabularDeviceConfigReport[9][1] = (exDeviceConfig.hasADAT() ? "Yes" : "No");

            tabularDeviceConfigReport[10][0] = "Digital I/O";
            tabularDeviceConfigReport[10][1] = (exDeviceConfig.hasDigitalIO() ? "Yes" : "No");

            tabularDeviceConfigReport[11][0] = "Octopus";
            tabularDeviceConfigReport[11][1] = (exDeviceConfig.hasOctopus() ? "Yes" : "No");

            tabularDeviceConfigReport[12][0] = "Preset Flash";
            tabularDeviceConfigReport[12][1] = (exDeviceConfig.hasPresetFlash() ? "Yes" : "No");

            tabularDeviceConfigReport[13][0] = "Sample RAM";
            tabularDeviceConfigReport[13][1] = (exDeviceConfig.getSampleRAM() + " MB");

            tabularDeviceConfigReport[14][0] = "Sample ROM";
            tabularDeviceConfigReport[14][1] = (exDeviceConfig.getSampleROM() + " MB");

            tabularDeviceConfigReport[15][0] = "Sample Flash";
            tabularDeviceConfigReport[15][1] = (exDeviceConfig.getSampleFlash() + " MB");

            tabularDeviceConfigReport[16][0] = "";
            tabularDeviceConfigReport[16][1] = "";

            tabularDeviceConfigReport[17][0] = "Flash Presets";
            if (maxPreset > 999) {
                DecimalFormat df = new DecimalFormat("0000");
                tabularDeviceConfigReport[17][1] = df.format(1000) + " ... " + df.format(maxPreset);
            } else
                tabularDeviceConfigReport[17][1] = "None";

            tabularDeviceConfigReport[18][0] = "ROM Samples";
            if (maxSample > 999) {
                DecimalFormat df = new DecimalFormat("0000");
                tabularDeviceConfigReport[18][1] = df.format(1000) + " ... " + df.format(maxSample);
            } else
                tabularDeviceConfigReport[18][1] = "None";

            tabularDeviceConfigReport[19][0] = "Preset Memory";
            tabularDeviceConfigReport[19][1] = presetMemory.getPresetMemory() + " Kb (free: " + presetMemory.getPresetFreeMemory() + " Kb)";

            tabularDeviceConfigReport[20][0] = "Sample Memory";
            tabularDeviceConfigReport[20][1] = (sampleMemory.getSampleMemory().intValue()) + " MB (free: " + (sampleMemory.getSampleFreeMemory().intValue() * 10) + " Kb)";

            tabularDeviceConfigReport[21][0] = "";
            tabularDeviceConfigReport[21][1] = "";

            tabularDeviceConfigReport[22][0] = "Identifying Message";
            tabularDeviceConfigReport[22][1] = remote.getIdentityMessage().toString();

            if (showProgress)
                ps.updateStatus();

            return true;
        } finally {
            if (showProgress)
                ps.end();
        }
    }

    public void logCommError(final Object error) {
        synchronized (deviceExceptions) {
            System.out.println("Comms error: " + error);
            if (error instanceof Exception)
                ((Exception) error).printStackTrace();
            deviceExceptions.add(error);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new FlashMsg(ZoeosFrame.getInstance(), ZoeosFrame.getInstance(), 2000, 250, FlashMsg.colorError, "Communications Error: " + remote.getName());
                }
            });
            if (deviceExceptions.size() >= remote.getRemotePreferences().ZPREF_commErrorThreshold.getValue()) {
                final StringBuffer buf = new StringBuffer();
                for (int i = 0; i < deviceExceptions.size(); i++) {
                    if (deviceExceptions.get(i) instanceof Exception)
                        buf.append(((Exception) deviceExceptions.get(i)).getMessage() + Zoeos.lineSeperator);
                    else
                        buf.append(deviceExceptions.get(i).toString() + Zoeos.lineSeperator);
                }
                try {
                    internalQ.getPostableTicket(new TicketRunnable() {
                        public void run() throws Exception {
                            try {
                                stopDevice(true, buf.toString());
                            } catch (IllegalStateTransitionException e) {
                                e.printStackTrace();
                            }
                        }
                    }, "stopDevice (comm/internal error threshold reached)").post();
                } catch (ResourceUnavailableException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void logInternalError(final Object error) {
        synchronized (deviceExceptions) {
            System.out.println("Internal error: " + error);
            if (error instanceof Exception)
                ((Exception) error).printStackTrace();
            deviceExceptions.add(error);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new FlashMsg(ZoeosFrame.getInstance(), ZoeosFrame.getInstance(), 1000, 500, FlashMsg.colorError, "Internal Error: " + remote.getName());
                }
            });
            if (deviceExceptions.size() >= remote.getRemotePreferences().ZPREF_commErrorThreshold.getValue()) {
                final StringBuffer buf = new StringBuffer();
                for (int i = 0; i < deviceExceptions.size(); i++) {
                    if (deviceExceptions.get(i) instanceof Exception)
                        buf.append(((Exception) deviceExceptions.get(i)).getMessage() + Zoeos.lineSeperator);
                    else
                        buf.append(deviceExceptions.get(i).toString() + Zoeos.lineSeperator);
                }
                try {
                    internalQ.getPostableTicket(new TicketRunnable() {
                        public void run() throws Exception {
                            try {
                                stopDevice(true, buf.toString());
                            } catch (IllegalStateTransitionException e) {
                                e.printStackTrace();
                            }
                        }
                    }, "stopDevice (comm/internal error threshold reached)").post();
                } catch (ResourceUnavailableException e) {
                    e.printStackTrace();
                }
            }
        }
    }

// UTILITY
    public PostableTicket reinitializePresetFlash() {
        return queues.generalQ().getPostableTicket(new TicketRunnable() {
            public void run() throws Exception {
                deviceLock.configure();
                try {
                    presetDB.uninitializeFlash();
                    presetDB.initializeFlashPresetNames(false);
                } finally {
                    deviceLock.unlock();
                }
            }
        }, "reinitializePresetFlash");
    }

// ZExternalDevice
    public String getDeviceCategory() {
        return "E-MU";
    }

    public String getDeviceConfigReport() {
        if (deviceLock.tryAccess())
            try {
                retrieveDeviceConfiguration(false);
            } finally {
                deviceLock.unlock();
            }

        return remote.getName() + Zoeos.getLineSeperator() + deviceConfigReport;
    }

    public TableModel getDeviceConfigTableModel() {
        if (deviceLock.tryAccess()) {
            try {
                retrieveDeviceConfiguration(false);
            } finally {
                deviceLock.unlock();
            }
        }
        return configTableModel;
    }

    public Ticket refreshDeviceConfiguration(final boolean showProgress) {
        return queues.generalQ().getTicket(new TicketRunnable() {
            public void run() throws Exception {
                deviceLock.configure();
                try {
                    retrieveDeviceConfiguration(showProgress);
                } finally {
                    deviceLock.unlock();
                }
            }
        }, "refreshDeviceConfiguration");
    }

    public String getStaticName() {
        return remote.getIdentityMessage().toString();
    }

    public String getName() {
        return remote.getName();
    }

    public void setName(final String name) throws DeviceException {
        deviceLock.configure();
        try {
            remote.setName(name);
            tplh.fireTitleProviderDataChanged();
        } finally {
            deviceLock.unlock();
        }
    }

    public String toString() {
        return getName();
    }

    public File getDeviceLocalDir() {
        return remote.getDeviceLocalDir();
    }

// CONTEXTS
    public DeviceParameterContext getDeviceParameterContext() throws DeviceException {
        deviceLock.access();
        try {
            return deviceParameterContext;
        } finally {
            deviceLock.unlock();
        }
    }

    public MultiModeContext getMultiModeContext() throws DeviceException {
        deviceLock.access();
        try {
            return mmContext;
        } finally {
            deviceLock.unlock();
        }
    }

    public PresetContext getDefaultPresetContext() throws DeviceException {
        deviceLock.access();
        try {
            return presetDB.getRootContext();
        } finally {
            deviceLock.unlock();
        }
    }

    public SampleContext getDefaultSampleContext() throws DeviceException {
        deviceLock.access();
        try {
            return sampleDB.getRootContext();
        } finally {
            deviceLock.unlock();
        }
    }

    public MasterContext getMasterContext() throws DeviceException {
        deviceLock.access();
        try {
            return masterContext;
        } finally {
            deviceLock.unlock();
        }
    }

    public SampleMemory getSampleMemory() throws DeviceException {
        deviceLock.access();
        try {
            try {
                return remote.getMasterContext().req_sampleMemory();
            } catch (RemoteDeviceDidNotRespondException e) {
                throw new RemoteUnreachableException(e.getMessage());
            } catch (RemoteMessagingException e) {
                throw new RemoteUnreachableException(e.getMessage());
            }
        } finally {
            deviceLock.unlock();
        }
    }

    public Remotable.PresetMemory getPresetMemory() throws DeviceException {
        deviceLock.access();
        try {
            try {
                return remote.getMasterContext().req_presetMemory();
            } catch (RemoteDeviceDidNotRespondException e) {
                throw new RemoteUnreachableException(e.getMessage());
            } catch (RemoteMessagingException e) {
                throw new RemoteUnreachableException(e.getMessage());
            }
        } finally {
            deviceLock.unlock();
        }
    }

    private static final int defragPause = 1500;

    public PostableTicket sampleMemoryDefrag(final boolean pause) throws DeviceException {
        return queues.generalQ().getPostableTicket(new TicketRunnable() {
            public void run() throws Exception {
                deviceLock.access();
                try {
                    try {
                        remote.getMasterContext().cmd_sampleDefrag();
                        if (pause) {
                            try {
                                Thread.sleep(defragPause);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (RemoteMessagingException e) {
                        throw new RemoteUnreachableException(e.getMessage());
                    }
                } finally {
                    deviceLock.unlock();
                }
            }
        }, "sampleMemoryDefrag");
    }

// SMDI
    public Object getSmdiCouplingObject() {
        return remote.getIdentityMessage();
    }

    public boolean isSmdiCoupled() throws DeviceException {
        deviceLock.access();
        try {
            return remote.isSmdiCoupled();
        } finally {
            deviceLock.unlock();
        }
    }

// AUDITION
    public AuditionManager getAuditionManager() throws DeviceException {
        // if (!getDevicePreferences().ZPREF_enableAuditioning.getValue())
        //    throw new AuditioningDisabledException();
        deviceLock.access();
        try {
            synchronized (this) {
                if (auditionManager == null)
                    auditionManager = new Impl_AuditionManager(this);
                return auditionManager;
            }
        } finally {
            deviceLock.unlock();
        }
    }

// DESKTOP
    public ViewManager getViewManager() {
        return viewManager;
    }

// BANK

    public PostableTicket eraseBank() {
        return internalQ.getPostableTicket(new TicketRunnable() {
            public void run() throws Exception {
                deviceLock.configure();
                try {
                    presetDB.stopWorkerQueues();
                    sampleDB.stopWorkerQueues();

                    presetDB.eraseUserForBankErase();
                    sampleDB.eraseUserForBankErase();

                    remote.getMasterContext().cmd_bankErase();

                    E4Device.this.mmContext.refresh().post();
                    E4Device.this.masterContext.refresh().post();

                    presetDB.initializeUserPresetData();
                    sampleDB.initializeAllSampleData();
                } catch (Exception e) {
                    logCommError(e);
                } finally {
                    deviceLock.unlock();
                }
            }
        }, "eraseBank");
    }

    public PostableTicket refreshBank(final boolean refreshData) {
        return internalQ.getPostableTicket(new TicketRunnable() {
            public void run() throws Exception {
                deviceLock.configure();
                try {
                    presetDB.stopWorkerQueues();
                    sampleDB.stopWorkerQueues();

                    presetDB.uninitializeUser();
                    sampleDB.uninitializeUser();

                    E4Device.this.mmContext.refresh().post();
                    E4Device.this.masterContext.refresh().post();

                    presetDB.initializeAllPresetNames(refreshData);
                    sampleDB.initializeAllSampleData();
                } catch (Exception e) {
                    logCommError(e);
                } finally {
                    deviceLock.unlock();
                }
            }
        }, "refreshBank");
    }

    public PostableTicket cancelAuditions() {
        return internalQ.getPostableTicket(new TicketRunnable() {
            public void run() throws Exception {
                queues.auditionQ().cancel();
//getAuditionManager().allNotesOff();
            }
        }, "cancelAuditions");
    }

// CONFIGURATION
    public DeviceConfig getDeviceConfig() throws DeviceException {
        deviceLock.access();
        try {
            retrieveDeviceConfiguration(true);
            return deviceConfig;
        } finally {
            deviceLock.unlock();
        }
    }

    public DeviceExConfig getDeviceExConfig() throws DeviceException {
        deviceLock.access();
        try {
            retrieveDeviceConfiguration(true);
            return exDeviceConfig;
        } finally {
            deviceLock.unlock();
        }
    }

    public double getDeviceVersion() {
        return remote.getDeviceVersion();
    }

    public DevicePreferences getDevicePreferences() {
        return devicePreferences;
    }

    public RemotePreferences getRemotePreferences() {
        return remote.getRemotePreferences();
    }

    public int getNumberOfInstalledSampleRoms() {
        return sampleDB.getNumberOfInstalledSampleRoms();
    }

    public ZCommand[] getZCommands(Class markerClass) {
        return cmdProviderHelper.getCommandObjects(markerClass, this);
    }

    public Class[] getZCommandMarkers() {
        return cmdProviderHelper.getSupportedMarkers();
    }

    public Remotable getRemote() {
        return remote;
    }

    public int getScsiId() throws DeviceException, ParameterException {
        try {
            return getMasterContext().getMasterParams(new Integer[]{IntPool.get(190)})[0].intValue();
        } catch (IllegalParameterIdException e) {
        }
        return -1;
    }

    public Icon getIcon() {
        if (remote != null)
            if (remote.getDeviceVersion() >= BASE_ULTRA_VERSION)
                return ultraIcon;
            else
                return classicIcon;
        return null;
    }

    public String getToolTipText() {
        if (identityMessage != null)
            return identityMessage.toString();
        return null;
    }

    public String getTitle() {
        return getName();
    }

    public String getReducedTitle() {
        return getTitle();
    }

    public void addTitleProviderListener(TitleProviderListener tpl) {
        tplh.addTitleProviderListener(tpl);
    }

    public void removeTitleProviderListener(TitleProviderListener tpl) {
        tplh.addTitleProviderListener(tpl);
    }

    private class Master extends Parameterized {
        public Master() {
            super.initNew(deviceParameterContext.getMasterContext(), true);
        }

        public void setValue(Integer id, Integer value) throws ParameterValueOutOfRangeException, IllegalParameterIdException {
            putValue(id, value);
        }
    }

    private class Impl_MultiModeContext implements MultiModeContext, ZCommandProvider, PresetListener, Serializable, ZDisposable {
        transient private Vector<MultiModeListener> listeners;
        private MultiModeMap mmMap = null;
        private final boolean has32;
        private final MultiModeDescriptor mmDescriptor = new Impl_MultiModeDescripor();
        private final GeneralParameterDescriptor presetPD;
        private final GeneralParameterDescriptor volPD;
        private final GeneralParameterDescriptor panPD;
        private final GeneralParameterDescriptor submixPD;

        private transient ZThread currThread;
        private transient ParameterEditLoader pel;
        transient protected ManageableTicketedQ multimodeQ;
        transient protected ManageableTaskQ<AbstractChannelTask> setEventQ;

        private void checkThread() {
            ZThread t = (ZThread) Thread.currentThread();
            if (t != currThread) {
                if (currThread != null)
                    flush();
                currThread = t;
                t.addOnCompletedAction(new ZThread.OnCompletedAction() {
                    public void completed(ZThread t) {
                        flush();
                    }
                });
            }
        }

        private ParameterEditLoader getPEL() {
            if (pel == null)
                pel = remote.getEditLoader();
            return pel;
        }

        private synchronized void flush() {
            currThread = null;
            try {
                getPEL().dispatch();
                return;
            } catch (RemoteUnreachableException e) {
                logCommError(e);
            } catch (RemoteMessagingException e) {
                logCommError(e);
            }
            try {
                refresh().post();
            } catch (ResourceUnavailableException e) {
                e.printStackTrace();
            }
        }

        public void zDispose() {
            setEventQ.stop(true);
            multimodeQ.stop(true);
            listeners.clear();
        }

        public Impl_MultiModeContext() throws RemoteDeviceDidNotRespondException, RemoteMessagingException, com.pcmsolutions.device.EMU.E4.RemoteUnreachableException, IllegalParameterIdException {
            mmMap = remote.getMasterContext().req_multimodeMap();
            has32 = mmMap.has32();
            presetPD = deviceParameterContext.getParameterDescriptor(IntPool.get(247));
            volPD = deviceParameterContext.getParameterDescriptor(IntPool.get(248));
            panPD = deviceParameterContext.getParameterDescriptor(IntPool.get(249));
            submixPD = deviceParameterContext.getParameterDescriptor(IntPool.get(250));
            buildTransients();
        }

        private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
            ois.defaultReadObject();
            buildTransients();
        }

        private void buildTransients() {
            listeners = new Vector<MultiModeListener>();
            multimodeQ = QueueFactory.createTicketedQueue(this, "multimodeQ", 6);
            multimodeQ.start();
            setEventQ = QueueFactory.createTaskQueue(this, "multiModeSetParameterQ", 6, new ThreadAllower() {
                public boolean allowThread() {
                    return !SwingUtilities.isEventDispatchThread();
                }

                public void zDispose() {
                }
            }, new TaskInserter<AbstractChannelTask>() {

                public void zDispose() {
                }

                public void insertTask(AbstractChannelTask task, List<AbstractChannelTask> postedTasks, String qName) throws QueueUnavailableException {
                    int i = postedTasks.indexOf(task);
                    if (i == -1)
                        postedTasks.add(task);
                    else
                        postedTasks.set(i, task);
                }
            }, new TaskFetcher<AbstractChannelTask>() {
                public List<AbstractChannelTask> fetch(List<AbstractChannelTask> eventTasks) {
                    ArrayList<AbstractChannelTask> outEventTasks = new ArrayList<AbstractChannelTask>();
                    outEventTasks.addAll(eventTasks);
                    eventTasks.clear();
                    final Set<Integer> chSet = new HashSet<Integer>();
                    for (AbstractChannelTask t : outEventTasks)
                        chSet.add(t.getCh());
                    outEventTasks.add(new AbstractChannelTask(IntPool.minus_one, "fireMultiModeChannelChangedEvent") {
                        public void run() throws Exception {
                            for (Integer ch : chSet)
                                fireMultiModeEvent(new MultiModeChannelChangedEvent(this, Impl_MultiModeContext.this, ch));
                        }
                    });
                    return outEventTasks;
                }

                public void zDispose() {

                }
            }, DelayFactory.createDelay(75));
            setEventQ.start();
        }

        abstract class AbstractChannelTask implements Task {
            private Integer ch;
            private String name;

            protected AbstractChannelTask(Integer ch, String name) {
                this.ch = ch;
                this.name = name;
            }

            public Integer getCh() {
                return ch;
            }

            public String getName() {
                return name;
            }

            public void cbCancelled() {
            }

            public void cbFinished(Exception e) {

            }

            public boolean equals(Object obj) {
                return this.getClass().equals(obj.getClass()) && getCh().equals(((AbstractChannelTask) obj).getCh());
            }
        }

        abstract class AbstractSetTask extends AbstractChannelTask implements Task {
            private Integer value;

            public AbstractSetTask(Integer ch, Integer value, String name) {
                super(ch, name);
                this.value = value;
            }

            public Integer getValue() {
                return value;
            }
        }

        class SetPreset extends AbstractSetTask {
            public SetPreset(Integer ch, Integer value) {
                super(ch, value, "setPreset");
            }

            public void run() throws Exception {
                checkThread();
                getPEL().add(new Integer[]{IntPool.get(246), IntPool.get(247)}, new Integer[]{getCh(), getValue()});
            }
        }

        class SetVolume extends AbstractSetTask {
            public SetVolume(Integer ch, Integer value) {
                super(ch, value, "setVolume");
            }

            public void run() throws Exception {

                checkThread();
                getPEL().add(new Integer[]{IntPool.get(246), IntPool.get(248)}, new Integer[]{getCh(), getValue()});
            }
        }

        class SetPan extends AbstractSetTask {
            public SetPan(Integer ch, Integer value) {
                super(ch, value, "setPan");
            }

            public void run() throws Exception {
                checkThread();
                getPEL().add(new Integer[]{IntPool.get(246), IntPool.get(249)}, new Integer[]{getCh(), getValue()});
            }
        }

        class SetSubmix extends AbstractSetTask {
            public SetSubmix(Integer ch, Integer value) {
                super(ch, value, "setSubmix");
            }

            public void run() throws Exception {
                checkThread();
                getPEL().add(new Integer[]{IntPool.get(246), IntPool.get(250)}, new Integer[]{getCh(), getValue()});
            }
        }

        public Ticket audition(final int ch) {
            return queues.auditionQ().getTicket(new TicketRunnable() {
                public void run() throws Exception {
                    deviceLock.access();
                    try {
                        AuditionManager am = E4Device.this.getAuditionManager();
                        int note = NoteUtilities.Note.getValueForString(getDevicePreferences().ZPREF_quickAuditionNote.getValue());
                        int gate = getDevicePreferences().ZPREF_quickAuditionGate.getValue();
                        int vel = getDevicePreferences().ZPREF_quickAuditionVel.getValue();
                        Ticket noteTicket = am.getNote(note, ch, vel, gate);
                        // if (getDevicePreferences().ZPREF_allNotesOffBetweenAuditions.getValue())
                        //   am.allNotesOff(ch);
                        noteTicket.send(0);
                    } finally {
                        deviceLock.unlock();
                    }
                }
            }, "audition");
        }

        public MultiModeMap getMultimodeMap() throws DeviceException {
            deviceLock.access();
            try {
                synchronized (this) {
                    return mmMap.getCopy();
                }
            } finally {
                deviceLock.unlock();
            }
        }

        public MultiModeChannel getMultiModeChannel(Integer channel) throws IllegalMultimodeChannelException {
            return new Impl_MultiModeChannel(channel);
        }

        public Ticket setMultimodeMap(final MultiModeMap mmMap) {
            return multimodeQ.getTicket(new TicketRunnable() {
                public void run() throws Exception {
                    deviceLock.access();
                    try {
                        synchronized (this) {
                            removeChannelPresetListeners();
                            for (int i = 1, j = mmMap.has32() ? 32 : 16; i <= j; i++) {
                                Integer ch = IntPool.get(i);
                                setPreset(ch, mmMap.getPreset(ch)).post();
                                setVolume(ch, mmMap.getVolume(ch)).post();
                                setPan(ch, mmMap.getPan(ch)).post();
                                setSubmix(ch, mmMap.getSubmix(ch)).post();
                            }
                            addChannelPresetListeners();

                            /*
                            remote.getMasterContext().edit_multimodeMap(mmMap);
                            removeChannelPresetListeners();
                            this.mmMap = mmMap.getCopy();
                            addChannelPresetListeners();
                            */
                        }
                    } /*catch (com.pcmsolutions.device.EMU.E4.RemoteUnreachableException e) {
                        logCommError(e);
                    } catch (RemoteMessagingException e) {
                        logCommError(e);
                    } */ finally {
                        deviceLock.unlock();
                    }
                    fireMultiModeEvent(new MultiModeRefreshedEvent(Impl_MultiModeContext.this, Impl_MultiModeContext.this));
                }
            }, "setMultimodeMap");
        }

        public Integer[] getDistinctMultimodePresetIndexes() throws DeviceException {
            deviceLock.access();
            HashSet<Integer> presets = new HashSet<Integer>();
            try {
                for (int i = 1, j = mmMap.has32() ? 32 : 16; i <= j; i++) {
                    Integer p = this.getPreset(IntPool.get(i));
                    if (p.intValue() >= 0)
                        presets.add(p);
                }
            } catch (IllegalMultimodeChannelException e) {
                SystemErrors.internal(e);
            } finally {
                deviceLock.unlock();
            }
            return (Integer[]) presets.toArray(new Integer[presets.size()]);
        }

        private void removeChannelPresetListeners() {
            int chnls;
            if (has32)
                chnls = 32;
            else
                chnls = 16;
            Integer ch;
            PresetContext pc = null;
            pc = presetDB.getRootContext();
            for (int n = 1; n <= chnls; n++) {
                ch = IntPool.get(n);
                try {
                    Integer preset = mmMap.getPreset(ch);
                    if (preset.intValue() < 0)
                        continue;
                    pc.removeContentListener(this, new Integer[]{mmMap.getPreset(ch)});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        protected void addChannelPresetListeners() {
            int chnls;
            if (has32)
                chnls = 32;
            else
                chnls = 16;
            Integer ch;
            PresetContext pc = null;
            pc = presetDB.getRootContext();
            for (int n = 1; n <= chnls; n++) {
                ch = IntPool.get(n);
                try {
                    Integer preset = mmMap.getPreset(ch);
                    if (preset.intValue() < 0)
                        continue;
                    pc.addContentListener(this, new Integer[]{preset});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void addMultiModeListener(MultiModeListener mml) {
            listeners.add(mml);
        }

        public void removeMultiModeListener(MultiModeListener mml) {
            listeners.remove(mml);
        }

        public MultiModeDescriptor getMultiModeDescriptor() {
            return mmDescriptor;
        }

        public boolean has32Channels() {
            return has32;
        }

        public Integer getPreset(Integer ch) throws DeviceException {
            deviceLock.access();
            try {
                synchronized (this) {
                    return mmMap.getPreset(ch);
                }
            } finally {
                deviceLock.unlock();
            }
        }

        public Integer getVolume(Integer ch) throws IllegalMultimodeChannelException, DeviceException {
            deviceLock.access();
            try {
                synchronized (this) {
                    return mmMap.getVolume(ch);
                }
            } finally {
                deviceLock.unlock();
            }
        }

        public Integer getPan(Integer ch) throws DeviceException {
            deviceLock.access();
            try {
                synchronized (this) {
                    return mmMap.getPan(ch);
                }
            } finally {
                deviceLock.unlock();
            }
        }

        public Integer getSubmix(Integer ch) throws DeviceException {
            deviceLock.access();
            try {
                synchronized (this) {
                    return mmMap.getSubmix(ch);
                }
            } finally {
                deviceLock.unlock();
            }
        }

        public Ticket setPreset(final Integer ch, final Integer preset) {
            return queues.parameterQ().getTicket(new TicketRunnable() {
                public void run() throws Exception {
                    task_setPreset(ch, preset);
                }
            }, "setPreset");
        }

        private void task_setPreset(final Integer ch, final Integer preset) throws DeviceException {
            if (!getMultiModeDescriptor().getPresetParameterDescriptor().isValidValue(preset))
                return;
            deviceLock.access();
            try {
                synchronized (this) {
                    Integer lastPreset = getPreset(ch);
                    mmMap.setPreset(ch, preset);
                    PresetContext pc = getDefaultPresetContext();
                    pc.addContentListener(Impl_MultiModeContext.this, new Integer[]{preset});
                    pc.removeContentListener(Impl_MultiModeContext.this, new Integer[]{lastPreset});
                    try {
                        setEventQ.postTask(new SetPreset(ch, preset));
                    } catch (QueueUnavailableException e) {
                        throw new DeviceException(e.getMessage());
                    }
                }
            } finally {
                deviceLock.unlock();
            }
        }

        public Ticket setVolume(final Integer ch, final Integer volume) {
            return queues.parameterQ().getTicket(new TicketRunnable() {
                public void run() throws Exception {
                    task_setVolume(ch, volume);
                }
            }, "setVolume");
        }

        private void task_setVolume(final Integer ch, final Integer volume) throws DeviceException {
            deviceLock.access();
            try {
                synchronized (this) {
                    mmMap.setVolume(ch, volume);
                    try {
                        setEventQ.postTask(new SetVolume(ch, volume));
                    } catch (QueueUnavailableException e) {
                        throw new DeviceException(e.getMessage());
                    }
                }
            } finally {
                deviceLock.unlock();
            }
        }

        public Ticket setPan(final Integer ch, final Integer pan) {
            return queues.parameterQ().getTicket(new TicketRunnable() {
                public void run() throws Exception {
                    task_setPan(ch, pan);
                }
            }, "setPan");
        }

        private void task_setPan(final Integer ch, final Integer pan) throws DeviceException {
            deviceLock.access();
            try {
                synchronized (this) {
                    mmMap.setPan(ch, pan);
                    try {
                        setEventQ.postTask(new SetPan(ch, pan));
                    } catch (QueueUnavailableException e) {
                        throw new DeviceException(e.getMessage());
                    }
                }
            } finally {
                deviceLock.unlock();
            }
        }

        public Ticket setSubmix(final Integer ch, final Integer submix) {
            return queues.parameterQ().getTicket(new TicketRunnable() {
                public void run() throws Exception {
                    task_setSubmix(ch, submix);
                }
            }, "setSubmix");
        }

        private void task_setSubmix(final Integer ch, final Integer submix) throws DeviceException {
            deviceLock.access();
            try {
                synchronized (this) {
                    checkThread();
                    if (/*submix.intValue() == -2 ||*/ submix.intValue() > 7) { // RFX bus
                        flush();
                        AuditionManager am = getAuditionManager();
                        if (am.isMidiChannelReachable(ch.intValue())) {
                            mmMap.setSubmix(ch, submix);
                            int smv = submix.intValue();
                            if (smv == -2)
                                smv = 127;
                            else
                                smv++; // adjust to 0..15
                            am.sendCC(79, ch.intValue(), smv);
                            fireMultiModeEvent(new MultiModeChannelChangedEvent(this, Impl_MultiModeContext.this, ch));
                        }
                    } else {
                        mmMap.setSubmix(ch, submix);
                        try {
                            setEventQ.postTask(new SetSubmix(ch, submix));
                        } catch (QueueUnavailableException e) {
                            throw new DeviceException(e.getMessage());
                        }
                    }
                }
            } catch (AuditionManager.MultimodeChannelUnreachableException e) {
                e.printStackTrace();
            } finally {
                deviceLock.unlock();
            }
        }

        public Ticket offsetPreset(final Integer ch, final Integer offset) {
            return queues.parameterQ().getTicket(new TicketRunnable() {
                public void run() throws Exception {
                    deviceLock.access();
                    try {
                        task_setPreset(ch, presetPD.constrainValue(IntPool.get(getPreset(ch).intValue() + offset.intValue())));
                    } finally {
                        deviceLock.unlock();
                    }
                }
            }, "offsetPreset");
        }

        public Ticket offsetVolume(final Integer ch, final Integer offset) {
            return queues.parameterQ().getTicket(new TicketRunnable() {
                public void run() throws Exception {
                    deviceLock.access();
                    try {
                        task_setVolume(ch, volPD.constrainValue(IntPool.get(getVolume(ch).intValue() + offset.intValue())));
                    } finally {
                        deviceLock.unlock();
                    }
                }
            }, "offsetVolume");
        }

        public Ticket offsetPan(final Integer ch, final Integer offset) {
            return queues.parameterQ().getTicket(new TicketRunnable() {
                public void run() throws Exception {
                    deviceLock.access();
                    try {
                        task_setPan(ch, panPD.constrainValue(IntPool.get(getPan(ch).intValue() + offset.intValue())));
                    } finally {
                        deviceLock.unlock();
                    }
                }
            }, "offsetPan");
        }

        public Ticket offsetSubmix(final Integer ch, final Integer offset) {
            return queues.parameterQ().getTicket(new TicketRunnable() {
                public void run() throws Exception {
                    deviceLock.access();
                    try {
                        task_setSubmix(ch, submixPD.constrainValue(IntPool.get(getSubmix(ch).intValue() + offset.intValue())));
                    } finally {
                        deviceLock.unlock();
                    }
                }
            }, "offsetSubmix");
        }

        public Ticket offsetPreset(final Integer ch, final Double offsetAsFOR) {
            return queues.parameterQ().getTicket(new TicketRunnable() {
                public void run() throws Exception {
                    deviceLock.access();
                    try {
                        task_setPreset(ch, presetPD.constrainValue(IntPool.get(getPreset(ch).intValue() + ParameterModelUtilities.calcIntegerOffset(presetPD, offsetAsFOR).intValue())));
                    } finally {
                        deviceLock.unlock();
                    }
                }
            }, "offsetPreset");
        }

        public Ticket offsetVolume(final Integer ch, final Double offsetAsFOR) {
            return queues.parameterQ().getTicket(new TicketRunnable() {
                public void run() throws Exception {
                    deviceLock.access();
                    try {
                        task_setVolume(ch, volPD.constrainValue(IntPool.get(getVolume(ch).intValue() + ParameterModelUtilities.calcIntegerOffset(volPD, offsetAsFOR).intValue())));
                    } finally {
                        deviceLock.unlock();
                    }
                }
            }, "offsetVolume");
        }

        public Ticket offsetPan(final Integer ch, final Double offsetAsFOR) {
            return queues.parameterQ().getTicket(new TicketRunnable() {
                public void run() throws Exception {
                    deviceLock.access();
                    try {
                        task_setPan(ch, panPD.constrainValue(IntPool.get(getPan(ch).intValue() + ParameterModelUtilities.calcIntegerOffset(panPD, offsetAsFOR).intValue())));
                    } finally {
                        deviceLock.unlock();
                    }
                }
            }, "offsetPan");
        }

        public Ticket offsetSubmix(final Integer ch, final Double offsetAsFOR) {
            return queues.parameterQ().getTicket(new TicketRunnable() {
                public void run() throws Exception {
                    deviceLock.access();
                    try {
                        task_setVolume(ch, submixPD.constrainValue(IntPool.get(getSubmix(ch).intValue() + ParameterModelUtilities.calcIntegerOffset(submixPD, offsetAsFOR).intValue())));
                    } finally {
                        deviceLock.unlock();
                    }
                }
            }, "offsetSubmix");
        }

        // to help with RFX detection
        private void applyMapToSubmixPD() {
            int chnls = mmMap.has32() ? 32 : 16;
            for (int i = 1; i <= chnls; i++) {
                try {
                    submixPD.getStringForValue(mmMap.getSubmix(IntPool.get(i)));
                } catch (Exception e) {
                }
            }
        }

        public void sendMidiMessage(MidiMessage m) throws RemoteUnreachableException, DeviceException {
            deviceLock.access();
            try {
                remote.getMasterContext().sendMidiMessage(m);
            } finally {
                deviceLock.unlock();
            }
        }

        public Ticket refresh() {
            return multimodeQ.getTicket(new TicketRunnable() {
                public void run() throws Exception {
                    deviceLock.access();
                    try {
                        task_refresh();
                    } finally {
                        deviceLock.unlock();
                    }
                }
            }, "refreshMultiMode");
        }

        public void syncRefresh() throws DeviceException {
            deviceLock.access();
            try {
                task_refresh();
            } finally {
                deviceLock.unlock();
            }
        }


        public void syncToEdits() {
            setEventQ.waitUntilEmpty();
        }

        void task_refresh() {
            try {
                synchronized (this) {
                    flush();
                    mmMap = remote.getMasterContext().req_multimodeMap();
                    applyMapToSubmixPD();
                }
                fireMultiModeEvent(new MultiModeRefreshedEvent(this, this));
            } catch (RemoteDeviceDidNotRespondException e) {
                e.printStackTrace();
            } catch (RemoteMessagingException e) {
                e.printStackTrace();
            } catch (com.pcmsolutions.device.EMU.E4.RemoteUnreachableException e) {
                e.printStackTrace();
            }
        }

        private void fireMultiModeEvent(final MultiModeEvent ev) {
            final Object[] la = listeners.toArray();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    for (Object o : la)
                        try {
                            ev.fire((MultiModeListener) o);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }
            });
        }

        public ZCommand[] getZCommands(Class markerClass) {
            return multiModeCmdProviderHelper.getCommandObjects(markerClass, this);
        }

        public Class[] getZCommandMarkers() {
            return multiModeCmdProviderHelper.getSupportedMarkers();
        }

        private void updateChannelsOnPresetChange(Integer preset) {
            try {
                deviceLock.access();
                try {
                    int chnls;
                    if (has32)
                        chnls = 32;
                    else
                        chnls = 16;
                    Integer ch;
                    for (int n = 1; n <= chnls; n++) {
                        ch = IntPool.get(n);
                        try {
                            if (preset.equals(mmMap.getPreset(ch)))
                                this.fireMultiModeEvent(new MultiModeChannelChangedEvent(this, this, ch));

                        } catch (IllegalMultimodeChannelException e) {
                            e.printStackTrace();
                        }
                    }
                } finally {
                    deviceLock.unlock();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void presetInitializationStatusChanged(PresetInitializationStatusChangedEvent ev) {
            updateChannelsOnPresetChange(ev.getIndex());
        }

        public void presetRefreshed(PresetInitializeEvent ev) {
            updateChannelsOnPresetChange(ev.getIndex());
        }

        public void presetChanged(PresetChangeEvent ev) {
        }

        public void presetNameChanged(PresetNameChangeEvent ev) {
            updateChannelsOnPresetChange(ev.getIndex());
        }

        public void voiceAdded(VoiceAddEvent ev) {
        }

        public void voiceRemoved(VoiceRemoveEvent ev) {
        }

        public void voiceChanged(VoiceChangeEvent ev) {
        }

        public void linkAdded(LinkAddEvent ev) {
        }

        public void linkRemoved(LinkRemoveEvent ev) {
        }

        public void linkChanged(LinkChangeEvent ev) {
        }

        public void zoneAdded(ZoneAddEvent ev) {
        }

        public void zoneRemoved(ZoneRemoveEvent ev) {
        }

        public void zoneChanged(ZoneChangeEvent ev) {
        }

        private class Impl_MultiModeDescripor implements MultiModeDescriptor {

            public Integer getMaxChannel() {
                // TODO! get max channel for multimode
                synchronized (Impl_MultiModeContext.this) {
                    if (has32)
                        return IntPool.get(32);
                    return IntPool.get(16);
                }
            }

            public Integer getMaxPreset() {
                synchronized (Impl_MultiModeContext.this) {
                    return IntPool.get(maxPreset);
                }
            }

            public GeneralParameterDescriptor getPresetParameterDescriptor() {
                return presetPD;
            }

            public GeneralParameterDescriptor getVolumeParameterDescriptor() {
                return volPD;
            }

            public GeneralParameterDescriptor getPanParameterDescriptor() {
                return panPD;
            }

            public GeneralParameterDescriptor getSubmixParameterDescriptor() {
                return submixPD;
            }
        }

        private class Impl_MultiModeChannel implements MultiModeChannel, ZCommandProvider {
            private Integer ch;

            public Impl_MultiModeChannel(Integer ch) throws IllegalMultimodeChannelException {
                this.ch = ch;
                int chi = ch.intValue();
                if (has32) {
                    if (chi > 32 || chi < 1)
                        throw new IllegalMultimodeChannelException(ch);
                } else if (chi > 16 || chi < 1)
                    throw new IllegalMultimodeChannelException(ch);
            }

            public MultiModeDescriptor getMultiModeDescriptor() {
                return getMultiModeDescriptor();
            }

            public Ticket audition() {
                return Impl_MultiModeContext.this.audition(ch.intValue());
            }

            public Integer getChannel() {
                return ch;
            }

            public Integer getPreset() throws ParameterException {
                try {
                    return Impl_MultiModeContext.this.getPreset(ch);
                } catch (DeviceException e) {
                    throw new ParameterException(e.getMessage());
                }
            }

            public Integer getVolume() throws ParameterException {
                try {
                    return Impl_MultiModeContext.this.getVolume(ch);
                } catch (DeviceException e) {
                    throw new ParameterException(e.getMessage());
                }
            }

            public Integer getPan() throws ParameterException {
                try {
                    return Impl_MultiModeContext.this.getPan(ch);
                } catch (DeviceException e) {
                    throw new ParameterException(e.getMessage());
                }
            }

            public Integer getSubmix() throws ParameterException {
                try {
                    return Impl_MultiModeContext.this.getSubmix(ch);
                } catch (DeviceException e) {
                    throw new ParameterException(e.getMessage());
                }
            }

            public void setPreset(Integer preset) throws ParameterException {
                try {
                    Impl_MultiModeContext.this.setPreset(ch, preset).post();
                } catch (ResourceUnavailableException e) {
                    throw new ParameterException(e.getMessage());
                }
            }

            public void setVolume(Integer volume) throws ParameterException {
                try {
                    Impl_MultiModeContext.this.setVolume(ch, volume).post();
                } catch (ResourceUnavailableException e) {
                    throw new ParameterException(e.getMessage());
                }
            }

            public void setPan(Integer pan) throws ParameterException {
                try {
                    Impl_MultiModeContext.this.setPan(ch, pan).post();
                } catch (ResourceUnavailableException e) {
                    throw new ParameterException(e.getMessage());
                }
            }

            public void setSubmix(Integer submix) throws ParameterException {
                try {
                    Impl_MultiModeContext.this.setSubmix(ch, submix).post();
                } catch (ResourceUnavailableException e) {
                    throw new ParameterException(e.getMessage());
                }
            }

            public void offsetPreset(Integer offset) throws ParameterException {
                try {
                    Impl_MultiModeContext.this.offsetPreset(ch, offset).post();
                } catch (ResourceUnavailableException e) {
                    throw new ParameterException(e.getMessage());
                }
            }

            public void offsetVolume(Integer offset) throws ParameterException {
                try {
                    Impl_MultiModeContext.this.offsetVolume(ch, offset).post();
                } catch (ResourceUnavailableException e) {
                    throw new ParameterException(e.getMessage());
                }
            }

            public void offsetPan(Integer offset) throws ParameterException {
                try {
                    Impl_MultiModeContext.this.offsetPan(ch, offset).post();
                } catch (ResourceUnavailableException e) {
                    throw new ParameterException(e.getMessage());
                }
            }

            public void offsetSubmix(Integer offset) throws ParameterException {
                try {
                    Impl_MultiModeContext.this.offsetSubmix(ch, offset).post();
                } catch (ResourceUnavailableException e) {
                    throw new ParameterException(e.getMessage());
                }
            }

            public void offsetPreset(Double offsetAsFOR) throws ParameterException {
                try {
                    Impl_MultiModeContext.this.offsetPreset(ch, offsetAsFOR).post();
                } catch (ResourceUnavailableException e) {
                    throw new ParameterException(e.getMessage());
                }
            }

            public void offsetVolume(Double offsetAsFOR) throws ParameterException {
                try {
                    Impl_MultiModeContext.this.offsetVolume(ch, offsetAsFOR).post();
                } catch (ResourceUnavailableException e) {
                    throw new ParameterException(e.getMessage());
                }
            }

            public void offsetPan(Double offsetAsFOR) throws ParameterException {
                try {
                    Impl_MultiModeContext.this.offsetPan(ch, offsetAsFOR).post();
                } catch (ResourceUnavailableException e) {
                    throw new ParameterException(e.getMessage());
                }
            }

            public void offsetSubmix(Double offsetAsFOR) throws ParameterException {
                try {
                    Impl_MultiModeContext.this.offsetSubmix(ch, offsetAsFOR).post();
                } catch (ResourceUnavailableException e) {
                    throw new ParameterException(e.getMessage());
                }
            }

            public void addMultiModeListener(MultiModeListener mml) {
                Impl_MultiModeContext.this.addMultiModeListener(mml);
            }

            public void removeMultiModeListener(MultiModeListener mml) {
                Impl_MultiModeContext.this.removeMultiModeListener(mml);
            }

            public EditableParameterModel getPanEditableParameterModel() throws IllegalParameterIdException {
                return new Impl_MultiModePanParameterModel();
            }

            public EditableParameterModel getPresetEditableParameterModel() throws IllegalParameterIdException {
                return new Impl_MultiModePresetParameterModel();
            }

            public EditableParameterModel getVolumeEditableParameterModel() throws IllegalParameterIdException {
                return new Impl_MultiModeVolumeParameterModel();
            }

            public EditableParameterModel getSubmixEditableParameterModel() throws IllegalParameterIdException {
                return new Impl_MultiModeSubmixParameterModel();
            }

            public ZCommand[] getZCommands(Class markerClass) {
                return multiModeChannelCmdProviderHelper.getCommandObjects(markerClass, this);
            }

            public Class[] getZCommandMarkers() {
                return multiModeChannelCmdProviderHelper.getSupportedMarkers();
            }

            private class Impl_MultiModePresetParameterModel extends AbstractEditableParameterModel implements IconAndTipCarrier {
                public Impl_MultiModePresetParameterModel() {
                    super(presetPD);
                }

                public Icon getIcon() {
                    try {
                        return E4Device.this.getDefaultPresetContext().getReadablePreset(getPreset()).getIcon();
                    } catch (ParameterException e) {
                    } catch (DeviceException e) {
                    }
                    return null;
                }

                public String getToolTipText() {
                    try {
                        return E4Device.this.getDefaultPresetContext().getReadablePreset(getPreset()).getToolTipText();
                    } catch (DeviceException e) {
                    } catch (ParameterException e) {
                    }
                    return null;
                }

                public void setValue(Integer value) throws ParameterException {
                    setPreset(value);
                }

                public void offsetValue(Integer offset) throws ParameterException {
                    offsetPreset(offset);
                }

                public void offsetValue(Double offsetAsFOR) throws ParameterException {
                    offsetPreset(offsetAsFOR);
                }

                public Integer getValue() throws ParameterException {
                    return getPreset();
                }

                public String getValueString() throws ParameterException {
                    Integer i = getValue();
                    if (i.intValue() == -1)
                        return "Disabled";
                    else {
                        try {
                            return PresetContextMacros.getPresetDisplayName(E4Device.this.getDefaultPresetContext(), i);
                        } catch (DeviceException e) {
                            e.printStackTrace();
                        }
                    }
                    return "";
                }

                public String getValueUnitlessString() throws ParameterException {
                    return getValueString();
                }

                public String toString() {
                    try {
                        return getValueString();
                    } catch (ParameterException e) {
                    }
                    return "";
                }

                public ZCommand[] getZCommands(Class markerClass) {
                    return EditableParameterModel.cmdProviderHelper.getCommandObjects(markerClass, this);
                }

                // most capable/super first
                public Class[] getZCommandMarkers() {
                    return EditableParameterModel.cmdProviderHelper.getSupportedMarkers();
                }
            }


            private class Impl_MultiModeVolumeParameterModel extends AbstractEditableParameterModel {
                public Impl_MultiModeVolumeParameterModel() {
                    super(volPD);
                }

                public void setValue(Integer value) throws ParameterException {
                    setVolume(value);
                }

                public void offsetValue(Integer offset) throws ParameterException {
                    offsetVolume(offset);
                }

                public void offsetValue(Double offsetAsFOR) throws ParameterException {
                    offsetVolume(offsetAsFOR);
                }

                public Integer getValue() throws ParameterException {
                    return getVolume();
                }

                public ZCommand[] getZCommands(Class markerClass) {
                    return EditableParameterModel.cmdProviderHelper.getCommandObjects(markerClass, this);
                }

                // most capable/super first
                public Class[] getZCommandMarkers() {
                    return EditableParameterModel.cmdProviderHelper.getSupportedMarkers();
                }
            }

            private class Impl_MultiModePanParameterModel extends AbstractEditableParameterModel {
                public Impl_MultiModePanParameterModel() {
                    super(panPD);
                }

                public void setValue(Integer value) throws ParameterException {
                    setPan(value);
                }

                public void offsetValue(Integer offset) throws ParameterException {
                    offsetPan(offset);
                }

                public void offsetValue(Double offsetAsFOR) throws ParameterException {
                    offsetPan(offsetAsFOR);
                }

                public Integer getValue() throws ParameterException {
                    return getPan();
                }

                public ZCommand[] getZCommands(Class markerClass) {
                    return EditableParameterModel.cmdProviderHelper.getCommandObjects(markerClass, this);
                }

                // most capable/super first
                public Class[] getZCommandMarkers() {
                    return EditableParameterModel.cmdProviderHelper.getSupportedMarkers();
                }
            }

            private class Impl_MultiModeSubmixParameterModel extends AbstractEditableParameterModel {
                public Impl_MultiModeSubmixParameterModel() {
                    super(submixPD);
                }

                public void setValue(Integer value) throws ParameterException {
                    setSubmix(value);
                }

                public void offsetValue(Integer offset) throws ParameterException {
                    offsetSubmix(offset);
                }

                public void offsetValue(Double offsetAsFOR) throws ParameterException {
                    offsetSubmix(offsetAsFOR);
                }

                public Integer getValue() throws ParameterException {
                    return getSubmix();
                }
            }
        }
    }

    private class Impl_MasterContext implements MasterContext, ZCommandProvider, ZDisposable {
        private Master master = new Master();
        private final Integer[] ids;
        transient private Vector listeners = new Vector();

        public void zDispose() {
            listeners.clear();
            master = null;
        }

        public Impl_MasterContext() throws RemoteDeviceDidNotRespondException, com.pcmsolutions.device.EMU.E4.RemoteUnreachableException, RemoteMessagingException {
            Set setIds = deviceParameterContext.getMasterContext().getIds();
            ids = new Integer[setIds.size()];
            setIds.toArray(ids);
            Integer[] masterIdVals;
            masterIdVals = remote.getParameterContext().req_prmValues(ids);
            master.initValues(masterIdVals);
        }

        private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
            ois.defaultReadObject();
            listeners = new Vector();
        }

        private void fireMasterEvent(final MasterEvent ev) {
            synchronized (listeners) {
                int size = listeners.size();
                for (int n = 0; n < size; n++)
                    try {
                        ev.fire((MasterListener) listeners.get(n));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        }

        public List getEditableParameterModels(Integer[] ids) {
            ArrayList models = new ArrayList();
            for (int i = 0, n = ids.length; i < n; i++) {
                try {
                    final GeneralParameterDescriptor pd = deviceParameterContext.getMasterContext().getParameterDescriptor(ids[i]);
                    models.add(new Impl_MasterEditableParameterModel(pd));
                } catch (IllegalParameterIdException e) {
                    e.printStackTrace();
                }
            }
            return models;
        }

        private class Impl_MasterEditableParameterModel extends AbstractEditableParameterModel {
            private MasterListener ml = new MasterListener() {
                public void masterChanged(MasterChangedEvent ev) {
                    Integer[] parameters = ev.getParameters();
                    int num = parameters.length;
                    for (int n = 0; n < num; n++)
                        if (parameters[n].equals(pd.getId())) {
                            fireChanged();
                        }
                }

                public void masterRefreshed(MasterRefreshedEvent ev) {
                    fireChanged();
                }

            };

            public Impl_MasterEditableParameterModel(GeneralParameterDescriptor pd) {
                super(pd);
                addMasterListener(ml);
            }

            public void setValue(Integer value) throws ParameterException {
                try {
                    setMasterParam(pd.getId(), value).post();
                } catch (ResourceUnavailableException e) {
                    throw new ParameterUnavailableException(pd.getId());
                }
            }

            public void offsetValue(Integer offset) throws ParameterException {
                try {
                    offsetMasterParam(pd.getId(), offset).post();
                } catch (ResourceUnavailableException e) {
                    throw new ParameterUnavailableException(pd.getId());
                }
            }

            public void offsetValue(Double offsetAsFOR) throws ParameterException {
                try {
                    offsetMasterParam(pd.getId(), offsetAsFOR).post();
                } catch (ResourceUnavailableException e) {
                    throw new ParameterUnavailableException(pd.getId());
                }
            }

            public Integer getValue() throws ParameterException, IllegalParameterIdException {
                Integer[] vals;
                vals = getMasterParams(new Integer[]{pd.getId()});
                return vals[0];
            }

            public String getToolTipText() {
                try {
                    return getValueString();
                } catch (ParameterException e) {
                }
                return super.getToolTipText();
            }

            public void zDispose() {
                removeMasterListener(ml);
                super.zDispose();
            }
        }

        public List getAllEditableParameterModels() {
            return getEditableParameterModels(ids);
        }

        public DeviceContext getDeviceContext() {
            return E4Device.this;
        }

        public Integer[] getMasterParams(Integer[] ids) throws ParameterException {
            Integer[] rv;
            try {
                deviceLock.access();
            } catch (DeviceException e) {
                throw new ParameterException(e.getMessage());
            }
            try {
                rv = master.getValues(ids);
            } finally {
                deviceLock.unlock();
            }
            return rv;
        }

        private Integer[] master_fxaIds = new Integer[]{IntPool.get(229), IntPool.get(230), IntPool.get(231), IntPool.get(232), IntPool.get(233), IntPool.get(234), IntPool.get(235)};
        private Integer[] master_fxbIds = new Integer[]{IntPool.get(237), IntPool.get(238), IntPool.get(239), IntPool.get(240), IntPool.get(241), IntPool.get(242), IntPool.get(243)};

        public Ticket offsetMasterParam(final Integer id, final Integer offset) {
            return queues.parameterQ().getTicket(new TicketRunnable() {
                public void run() throws Exception {
                    GeneralParameterDescriptor pd = deviceParameterContext.getParameterDescriptor(id);
                    deviceLock.access();
                    try {
                        task_setMasterParam(id, pd.constrainValue(IntPool.get(getMasterParams(new Integer[]{id})[0].intValue() + offset.intValue())));
                    } finally {
                        deviceLock.unlock();
                    }
                }
            }, "offsetMasterParam");
        }

        public Ticket offsetMasterParam(Integer id, double offsetAsFOR) throws IllegalParameterIdException {
            GeneralParameterDescriptor pd = deviceParameterContext.getParameterDescriptor(id);
            return offsetMasterParam(id, IntPool.get((int) Math.round((pd.getMaxValue() - pd.getMinValue() + 1) * offsetAsFOR)));
        }

        public Ticket setMasterParam(final Integer id, final Integer value) {
            return queues.parameterQ().getTicket(new TicketRunnable() {
                public void run() throws Exception {
                    task_setMasterParam(id, value);
                }
            }, "setMasterParam");
        }

        private void task_setMasterParam(final Integer id, final Integer value) throws DeviceException, ParameterValueOutOfRangeException, IllegalParameterIdException {
            boolean fxaChanged = false;
            boolean fxbChanged = false;

            if (id.equals(IntPool.get(228)))
                fxaChanged = true;
            else if (id.equals(IntPool.get(236)))
                fxbChanged = true;
            deviceLock.access();
            try {
                master.setValues(new Integer[]{id}, new Integer[]{value});
                try {
                    remote.getParameterContext().edit_prmValues(new Integer[]{id, value});
                    if (fxaChanged) {
                        //Integer[] vals = remote.getParameterContext().req_prmValues(master_fxaIds);
                        //master.setValues(master_fxaIds, ZUtilities.extractOneOfIntegerPairs(vals, false));
                        Integer val1 = FXDefaults.getFXA_Decay(value);
                        Integer val2 = FXDefaults.getFXA_HFDamping(value);
                        master.putValue(IntPool.get(229), val1);
                        master.putValue(IntPool.get(230), val2);
                        fireMasterEvent(new MasterChangedEvent(this, E4Device.this, master_fxaIds));
                    }
                    if (fxbChanged) {
                        //Integer[] vals = remote.getParameterContext().req_prmValues(master_fxbIds);
                        //master.setValues(master_fxbIds, ZUtilities.extractOneOfIntegerPairs(vals, false));
                        Integer val1 = FXDefaults.getFXB_Feedback(value);
                        Integer val2 = FXDefaults.getFXB_LFORate(value);
                        Integer val3 = FXDefaults.getFXB_DelayTime(value);
                        master.putValue(IntPool.get(237), val1);
                        master.putValue(IntPool.get(238), val2);
                        master.putValue(IntPool.get(239), val3);
                        fireMasterEvent(new MasterChangedEvent(this, E4Device.this, master_fxbIds));
                    }
                } catch (IllegalStateException e) {
                    logCommError(e);
                } catch (Exception e) {
                    logCommError(e);
                }
            } finally {
                deviceLock.unlock();
            }
            fireMasterEvent(new MasterChangedEvent(this, E4Device.this, new Integer[]{id}));
        }

        public Ticket refresh() {
            return queues.parameterQ().getTicket(new TicketRunnable() {
                public void run() throws Exception {
                    deviceLock.access();
                    try {
                        task_refresh();
                    } finally {
                        deviceLock.unlock();
                    }
                }
            }, "refresh(master)");
        }

        void task_refresh() {
            Integer[] masterIdVals;
            try {
                masterIdVals = remote.getParameterContext().req_prmValues(ids);
                master.initValues(masterIdVals);
                fireMasterEvent(new MasterRefreshedEvent(this, E4Device.this));
            } catch (RemoteDeviceDidNotRespondException e) {
                logCommError(e);
            } catch (RemoteMessagingException e) {
                logCommError(e);
            } catch (com.pcmsolutions.device.EMU.E4.RemoteUnreachableException e) {
                logCommError(e);
            }
        }

        public void addMasterListener(MasterListener ml) {
            listeners.add(ml);
        }

        public void removeMasterListener(MasterListener ml) {
            listeners.remove(ml);
        }

        public ZCommand[] getZCommands(Class markerClass) {
            return masterCmdProviderHelper.getCommandObjects(markerClass, this);
        }

        public Class[] getZCommandMarkers() {
            return masterCmdProviderHelper.getSupportedMarkers();
        }
    }
}


