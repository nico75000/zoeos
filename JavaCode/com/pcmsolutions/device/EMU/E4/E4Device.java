/*
 * Device.java
 *
 * Created on December 14, 2002, 12:28 AM
 */

package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.comms.RemoteDeviceDidNotRespondException;
import com.pcmsolutions.comms.RemoteMessagingException;
import com.pcmsolutions.device.EMU.E4.Remotable.DeviceConfig;
import com.pcmsolutions.device.EMU.E4.Remotable.DeviceExConfig;
import com.pcmsolutions.device.EMU.E4.Remotable.PresetMemory;
import com.pcmsolutions.device.EMU.E4.Remotable.SampleMemory;
import com.pcmsolutions.device.EMU.E4.desktop.ViewManager;
import com.pcmsolutions.device.EMU.E4.events.*;
import com.pcmsolutions.device.EMU.E4.gui.TitleProvider;
import com.pcmsolutions.device.EMU.E4.gui.TitleProviderListener;
import com.pcmsolutions.device.EMU.E4.gui.TitleProviderListenerHelper;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.device.DeviceIcon;
import com.pcmsolutions.device.EMU.E4.master.MasterContext;
import com.pcmsolutions.device.EMU.E4.master.MasterListener;
import com.pcmsolutions.device.EMU.E4.multimode.*;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.E4.sample.Impl_SampleRetrievalInfo;
import com.pcmsolutions.device.EMU.E4.sample.IsolatedSampleUnavailableException;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.E4.sample.SampleRetrievalInfo;
import com.pcmsolutions.device.EMU.E4.zcommands.E4DeviceZCommandMarker;
import com.pcmsolutions.device.EMU.E4.zcommands.E4MasterContextZCommandMarker;
import com.pcmsolutions.device.EMU.E4.zcommands.E4MultiModeChannelZCommandMarker;
import com.pcmsolutions.device.EMU.E4.zcommands.E4MultiModeContextZCommandMarker;
import com.pcmsolutions.gui.FlashMsg;
import com.pcmsolutions.gui.IconAndTipCarrier;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.gui.desktop.DesktopElement;
import com.pcmsolutions.license.LicenseKeyManager;
import com.pcmsolutions.smdi.*;
import com.pcmsolutions.system.*;
import com.pcmsolutions.system.threads.ZDBModifyThread;
import com.pcmsolutions.system.threads.ZDefaultThread;
import com.pcmsolutions.util.CALock;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 *
 * @author  pmeehan
 */
class E4Device extends AbstractZDevice implements DeviceContext, RemoteAssignable, ZDisposable, SMDIAgent.SmdiListener, Serializable, TitleProvider {
    private static final int iconWidth = 18;
    private static final int iconHeight = 12;
    private static final Icon ultraIcon = new DeviceIcon(iconWidth, iconHeight, Color.white, UIColors.getUltraDeviceIcon());
    private static final Icon classicIcon = new DeviceIcon(iconWidth, iconHeight, Color.white, UIColors.getClassicDeviceIcon());

    // PARAMETERS
    protected DeviceParameterContext dpc;

    // ZCOMMANDS
    private static final ZCommandProviderHelper cmdProviderHelper;
    private static final ZCommandProviderHelper masterCmdProviderHelper;
    private static final ZCommandProviderHelper multiModeCmdProviderHelper;
    private static final ZCommandProviderHelper mutliModeChannelCmdProviderHelper;

    // EVENTS
    transient private Vector listeners;
    private TitleProviderListenerHelper tplh = new TitleProviderListenerHelper(this);

    // DATABASES
    protected PresetDatabase presetDB;
    protected SampleDatabase sampleDB;

    // CONTEXTS
    protected Impl_MultiModeContext mmContext;
    protected Impl_MasterContext masterContext;

    // DEVICE SYNCHRONIZATION
    protected final CALock device = new CALock();

    // REMOTING
    transient protected com.pcmsolutions.device.EMU.E4.Remotable remote;
    transient protected Vector deviceExceptions;

    // PREFERENCES
    transient private DevicePreferences devicePreferences;

    // SMDI
    protected SmdiTarget smdiTarget;
    private SampleMediator deviceSampleMediator;

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
            return String.class;
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
        listeners = new Vector();
    }

    public void zDispose() {
        SMDIAgent.removeSmdiListener(this);
        deviceExceptions.clear();
        listeners.clear();
        presetDB.zDispose();
        sampleDB.zDispose();
        masterContext.zDispose();
        mmContext.zDispose();
        if (remote instanceof ZDisposable)
            ((ZDisposable) remote).zDispose();
        if (dpc instanceof ZDisposable)
            ((ZDisposable) dpc).zDispose();
        devicePreferences.zDispose();
        viewManager.zDispose();
        tplh.clearListeners();
        smdiTarget = null;
        deviceSampleMediator = null;

        // Don't null remote, tplh, listeners, or devicePreferences - they may be required during the device's exit from the system
        deviceConfig = null;
        exDeviceConfig = null;
        sampleMemory = null;
        presetMemory = null;
        presetDB = null;
        sampleDB = null;
        dpc = null;
        deviceExceptions = null;
        configTableModel = null;
       // viewManager = null;
        mmContext = null;
        masterContext = null;
    }

    static {
        cmdProviderHelper = new ZCommandProviderHelper(E4DeviceZCommandMarker.class, "com.pcmsolutions.device.EMU.E4.zcommands.DeviceShowConfigurationZC;com.pcmsolutions.device.EMU.E4.zcommands.RenameE4DeviceZC;com.pcmsolutions.device.EMU.E4.zcommands.EraseBankZC;com.pcmsolutions.device.EMU.E4.zcommands.RefreshBankZC;com.pcmsolutions.device.EMU.E4.zcommands.TakeDeviceWorkspaceSnapshotZC");
        masterCmdProviderHelper = new ZCommandProviderHelper(E4MasterContextZCommandMarker.class, "com.pcmsolutions.device.EMU.E4.zcommands.RefreshMasterContextZC;");
        multiModeCmdProviderHelper = new ZCommandProviderHelper(E4MultiModeContextZCommandMarker.class, "com.pcmsolutions.device.EMU.E4.zcommands.RefreshMultiModeContextZC;");
        mutliModeChannelCmdProviderHelper = new ZCommandProviderHelper(E4MultiModeChannelZCommandMarker.class, "");
    }


    // CONSTRUCTORS
    public E4Device(com.pcmsolutions.device.EMU.E4.Remotable remote, DevicePreferences prefs) {
        super(remote.getIdentityMessage());
        this.remote = remote;
        makeTransients();
        makeDeviceConfigReportHeader();
        SMDIAgent.addSmdiListener(this);
        deviceSampleMediator = new Impl_SampleMediator();
        this.devicePreferences = prefs;
    }

    public void setRemote(Remotable r) {
        this.remote = r;
        presetDB.setRemote(r);
        sampleDB.setRemote(r);
    }

    protected void setPreferences(DevicePreferences dprefs) {
        devicePreferences = dprefs;
        sampleDB.setDevicePreferences(dprefs);
    }

    private class Impl_SampleMediator implements SampleMediator {
        public File retrieveSample(SampleRetrievalInfo sri) throws SampleMediator.SampleMediationException {
            device.access();
            try {
                String excStr = "Device not SMDI coupled";
                File f_fn = sri.getFile();
                //f_fn = ZUtilities.replaceExtension(fn, format.getExtension());
                if (f_fn.exists() && !sri.isOverwriting())
                    return null;
                if (smdiTarget != null)
                    try {
                        if (sri.getFormat().equals(AudioFileFormat.Type.WAVE)) {
                            System.out.println(f_fn.getAbsolutePath() + " <-- " + sri.getSample().intValue());
                            synchronized (remote) {
                                smdiTarget.recvSync(f_fn.getAbsolutePath(), sri.getSample().intValue());
                                /* if (sri.isEndOfAProcedure())
                                     try {
                                         f_remote.getMasterContext().cmd_sampleDefrag();   // this unhangs the UI on the Emulator - Don't ask me why!!!!!!!!!
                                     } catch (RemoteUnreachableException e) {
                                     } catch (RemoteMessagingException e) {
                                     }
                                     */
                            }
                        } else {
                            File temp = TempFileManager.getNewTempFile();
                            synchronized (remote) {
                                smdiTarget.recvSync(temp.getAbsolutePath(), sri.getSample().intValue());
                                /* if (sri.isEndOfAProcedure())
                                     try {
                                         f_remote.getMasterContext().cmd_sampleDefrag();   // this unhangs the UI on the Emulator - Don't ask me why!!!!!!!!!
                                     } catch (RemoteUnreachableException e) {
                                     } catch (RemoteMessagingException e) {
                                     }
                                     */
                            }
                            try {
                                AudioSystem.write(AudioSystem.getAudioInputStream(temp), sri.getFormat(), f_fn);
                            } catch (IOException e) {
                                throw new SampleMediator.SampleMediationException("error writing sample File");
                            } catch (UnsupportedAudioFileException e) {
                                throw new SampleMediator.SampleMediationException("error writing sample File");
                            }
                            temp.delete();
                            System.out.println(f_fn.getAbsolutePath() + " <-- " + sri.getSample().intValue());
                        }
                        return f_fn;
                    } catch (SmdiFileOpenException e) {
                        excStr = e.getMessage();
                    } catch (SmdiNoSampleException e) {
                        excStr = e.getMessage();
                    } catch (SmdiOutOfRangeException e) {
                        excStr = e.getMessage();
                    } catch (SmdiGeneralException e) {
                        excStr = e.getMessage();
                    } catch (TargetNotSMDIException e) {
                        excStr = e.getMessage();
                    }
                throw new SampleMediator.SampleMediationException(excStr);
            } finally {
                device.unlock();
            }
        }

        public void sendSampleMulti(File fn, Integer[] destSamples, String[] destNames) throws SampleMediator.SampleMediationException {
            for (int i = 0; i < destSamples.length; i++)
                sendSample(destSamples[i], fn, destNames[i]);
        }

        public void copySample(IsolatedSample is, Integer[] destSamples, String[] destNames) throws SampleMediator.SampleMediationException, IsolatedSampleUnavailableException {
            is.ZoeAssert();
            for (int i = 0; i < destSamples.length; i++)
                sendSample(destSamples[i], is.getLocalFile(), destNames[i]);
        }

        public void copySample(Integer srcSample, Integer[] destSamples, String[] destNames) throws SampleMediator.SampleMediationException {
            SampleRetrievalInfo sri = new Impl_SampleRetrievalInfo(srcSample);
            retrieveSample(sri);
            for (int i = 0; i < destSamples.length; i++)
                if (srcSample.intValue() != destSamples[i].intValue())
                    sendSample(destSamples[i], sri.getFile(), destNames[i]);
        }

        public void sendSample(Integer sample, IsolatedSample is, String sampleName) throws SampleMediator.SampleMediationException, IsolatedSampleUnavailableException {
            sendSample(sample, is.getLocalFile(), sampleName);
        }

        public void sendSample(Integer sample, File fn, String sampleName) throws SampleMediator.SampleMediationException {
            device.access();
            try {
                if (fn != null) {
                    if (smdiTarget != null) {
                        boolean usingTemp = false;
                        AudioFileFormat format = null;
                        try {
                            format = AudioSystem.getAudioFileFormat(fn);
                            File sendFile;
                            if (format.getType().equals(AudioFileFormat.Type.WAVE))
                                sendFile = fn;
                            else {
                                sendFile = TempFileManager.getNewTempFile();
                                usingTemp = true;
                                AudioSystem.write(AudioSystem.getAudioInputStream(fn), AudioFileFormat.Type.WAVE, sendFile);
                            }
                            try {
                                synchronized (remote) {
                                    smdiTarget.sendSync(sendFile.getAbsolutePath(), sample.intValue(), sampleName);
                                }
                                System.out.println(fn.getAbsolutePath() + " --> " + sample.intValue());
                                return;
                            } catch (SmdiFileOpenException e) {
                                throw new SampleMediator.SampleMediationException(e.getMessage());
                            } catch (SmdiOutOfRangeException e) {
                                throw new SampleMediator.SampleMediationException(e.getMessage());
                            } catch (SmdiGeneralException e) {
                                throw new SampleMediator.SampleMediationException(e.getMessage());
                            } catch (TargetNotSMDIException e) {
                                throw new SampleMediator.SampleMediationException(e.getMessage());
                            } catch (SmdiUnknownFileFormatException e) {
                                throw new SampleMediator.SampleMediationException(e.getMessage());
                            } catch (SmdiUnsupportedSampleBitsException e) {
                                throw new SampleMediator.SampleMediationException(e.getMessage());
                            } catch (SmdiNoMemoryException e) {
                                throw new SampleMediator.SampleMediationException(e.getMessage());
                            } finally {
                                if (usingTemp)
                                    sendFile.delete();
                            }
                        } catch (UnsupportedAudioFileException e) {
                            throw new SampleMediator.SampleMediationException(e.getMessage());
                        } catch (IOException e) {
                            throw new SampleMediator.SampleMediationException(e.getMessage());
                        }
                    } else
                        throw new SampleMediator.SampleMediationException("device not SMDI coupled");
                } else
                    throw new SampleMediator.SampleMediationException("no valid filename specified");
            } finally {
                device.unlock();
            }
        }

        public SampleDescriptor getSampleDescriptor(Integer sample) throws SampleMediator.SampleMediationException {
            device.access();
            String excStr = "Device not SMDI coupled";
            try {
                if (smdiTarget != null)
                    try {
                        synchronized (remote) {
                            return new Impl_SampleDescriptor(smdiTarget.getSampleHeader(sample.intValue()));
                        }
                    } catch (SmdiOutOfRangeException e) {
                        excStr = e.getMessage();
                    } catch (SmdiGeneralException e) {
                        excStr = e.getMessage();
                    } catch (TargetNotSMDIException e) {
                        excStr = e.getMessage();
                    } catch (SmdiNoSampleException e) {
                        excStr = e.getMessage();
                    }
                throw new SampleMediator.SampleMediationException(excStr);
            } finally {
                device.unlock();
            }
        }
    };

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

    private void stopDeviceThreaded(final String reason) {
        Thread t = new ZDefaultThread("Stop Device") {
            public void run() {
                try {
                    stopDevice(false, reason);
                } catch (IllegalStateTransitionException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    public void startDevice() throws ZDeviceStartupException, IllegalStateTransitionException {
        device.configure();
        try {
            if (sts.testTransition(STATE_RUNNING) == STATE_RUNNING)
                return;

            if (sts.getState() == ZExternalDevice.STATE_STOPPED) {
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
                updateSmdiCoupling();
                this.deviceExceptions.clear();
                viewManager.openDeviceViews().start();
                mmContext.refresh();
                masterContext.refresh();
                retrieveDeviceConfiguration(true);
                sts.transition(STATE_RUNNING);
                Zoeos.postZDeviceEvent(new ZDeviceStartedEvent(this, this));
                return;
            }

            try {
                remote.stateStart();
            } catch (IllegalStateTransitionException e) {
                throw new ZDeviceStartupException(this, "Could not start remote device");
            }

            try {
                dpc = new Impl_DeviceParameterContext(this, remote);
            } catch (RemoteDeviceDidNotRespondException e) {

                throw new ZDeviceStartupException(this, "Device startup failed - remoting error - device did not respond");
            } catch (com.pcmsolutions.device.EMU.E4.RemoteUnreachableException e) {
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
            } catch (com.pcmsolutions.device.EMU.E4.RemoteUnreachableException e) {
                throw new ZDeviceStartupException(this, "Device startup failed. Remoting error.");
            } catch (IllegalParameterIdException e) {
                throw new ZDeviceStartupException(this, "Device startup failed. Parameter setup error.");
            }

            try {
                masterContext = new Impl_MasterContext();
            } catch (RemoteDeviceDidNotRespondException e) {
                throw new ZDeviceStartupException(this, "Device startup failed. Remoting error.");
            } catch (com.pcmsolutions.device.EMU.E4.RemoteUnreachableException e) {
                throw new ZDeviceStartupException(this, "Device startup failed. Remoting error.");
            } catch (RemoteMessagingException e) {
                throw new ZDeviceStartupException(this, "Device startup failed. Remoting error.");
            }

            sampleDB = new SampleDatabase("Default Sample Database", maxSample);
            sampleDB.init(new Impl_OnlineSampleContextFactory(), device, devicePreferences);
            updateSmdiCoupling();
            presetDB = new PresetDatabase("Default Preset Database", maxPreset);
            presetDB.init(dpc, new Impl_OnlinePresetContextFactory(), device, sampleDB);
            sampleDB.setPresetDatabaseProxy(presetDB);

            if (presetDB.getState() == presetDB.STATE_PENDING)
                presetDB.stateInitial();
            if (sampleDB.getState() == sampleDB.STATE_PENDING)
                sampleDB.stateInitial();

            loadDatabases();

            presetDB.stateStart();
            sampleDB.stateStart();

            viewManager.openDeviceViews().start();

            ((Impl_MultiModeContext) mmContext).addChannelPresetListeners();

            sts.transition(STATE_RUNNING);
            presetDB.initializeAllPresetNames(false);
            sampleDB.initializeAllSampleData();
        } finally {
            try {
                if (sts.getState() == STATE_PENDING) {
                    Zoeos.postZDeviceEvent(new ZDevicePendingEvent(this, this));
                    if (remote.getState() == STATE_RUNNING)
                        remote.stateStop();
                }
            } finally {
                device.unlock();
            }
        }
        Zoeos.postZDeviceEvent(new ZDeviceStartedEvent(this, this));
    }

    public String makeDeviceProgressTitle(String str) {
        return remote.makeDeviceProgressTitle(str);
    }

    public DeviceParameterContext getDpc() {
        return dpc;
    }

    private void loadDatabases() {
        boolean reloadFlash = getDevicePreferences().ZPREF_alwaysReloadFlashPresets.getValue();
        boolean reloadRom = getDevicePreferences().ZPREF_alwaysReloadROMSamples.getValue();

        Zoeos z = Zoeos.getInstance();

        File lastSession = SessionExternalization.getLastSessionFileForDevice(this);
        if (reloadFlash || reloadRom) {
            int ps =
                    (reloadFlash ? maxPreset - DeviceContext.BASE_FLASH_PRESET : 0) +
                    (reloadRom ? maxSample - DeviceContext.BASE_ROM_SAMPLE : 0);
            z.beginProgressElement(this, makeDeviceProgressTitle("Loading Rom/Flash data from previous session"), ps);
            z.setProgressElementIndeterminate(this, true);
            try {
                SessionExternalization.DeviceSession.RomAndFlash raf = SessionExternalization.loadDeviceRomAndFlash(lastSession);

                Map map = raf.getFlashMap();
                if (map.size() == this.maxPreset - DeviceContext.MAX_USER_PRESET && reloadFlash) {
                    z.updateProgressElementTitle(this, makeDeviceProgressTitle("Reloading flash presets from previous session"));
                    Object po;
                    for (int i = DeviceContext.BASE_FLASH_PRESET; i <= maxPreset; i++) {
                        po = map.get(IntPool.get(i));
                        if (po == null) {
                            reloadFlash = false;
                            break;
                        }
                        presetDB.addObjectToDB(po, IntPool.get(i));
                        z.updateProgressElement(this);
                    }
                }
                map = raf.getRomMap();
                if (map.size() == this.maxSample - DeviceContext.MAX_USER_SAMPLE && reloadRom) {
                    z.updateProgressElementTitle(this, makeDeviceProgressTitle("Reloading ROM samples from previous session"));
                    Object so;
                    for (int i = DeviceContext.BASE_ROM_SAMPLE; i <= maxSample; i++) {
                        so = map.get(IntPool.get(i));
                        if (so == null) {
                            reloadRom = false;
                            break;
                        }
                        sampleDB.addObjectToDB(so, IntPool.get(i));
                        z.updateProgressElement(this);
                    }
                }
            } catch (SessionExternalization.ExternalizationException e) {
                reloadFlash = false;
                reloadRom = false;
            } finally {
                z.endProgressElement(this);
            }
        }


        z.beginProgressElement(this, makeDeviceProgressTitle("Loading User Sample Database"), DeviceContext.MAX_USER_SAMPLE);
        try {
            for (int n = 0; n <= DeviceContext.MAX_USER_SAMPLE; n++) {
                sampleDB.addObjectToDB(new UninitSampleObject(IntPool.get(n)), IntPool.get(n));
                z.updateProgressElement(this, n);
            }
        } finally {
            z.endProgressElement(this);
        }

        if (!reloadRom) {
            z.beginProgressElement(this, makeDeviceProgressTitle("Loading ROM Sample Database"), maxSample - DeviceContext.MAX_USER_SAMPLE);
            try {
                for (int n = DeviceContext.MAX_USER_SAMPLE + 1; n <= maxSample; n++) {
                    sampleDB.addObjectToDB(new UninitSampleObject(IntPool.get(n)), IntPool.get(n));
                    z.updateProgressElement(this, n);
                }
            } finally {
                z.endProgressElement(this);
            }
        }

        z.beginProgressElement(this, makeDeviceProgressTitle("Loading User Preset Database"), DeviceContext.MAX_USER_PRESET);
        try {
            for (int n = 0; n <= DeviceContext.MAX_USER_PRESET; n++) {
                presetDB.addObjectToDB(new UninitPresetObject(IntPool.get(n)), IntPool.get(n));
                z.updateProgressElement(this, n);
            }
        } finally {
            z.endProgressElement(this);
        }

        if (!reloadFlash) {
            z.beginProgressElement(this, makeDeviceProgressTitle("Loading Flash Preset Database"), maxPreset - DeviceContext.MAX_USER_PRESET);
            try {
                for (int n = DeviceContext.MAX_USER_PRESET + 1; n <= maxPreset; n++) {
                    presetDB.addObjectToDB(new UninitPresetObject(IntPool.get(n)), IntPool.get(n));
                    z.updateProgressElement(this, n);
                }
            } finally {
                z.endProgressElement(this);
            }
        }
    }

    private void updateSmdiCoupling() {
        try {
            smdiTarget = SMDIAgent.getSmdiTargetForIdentityMessage(remote.getIdentityMessage());
        } catch (DeviceNotCoupledToSmdiException e) {
            smdiTarget = null;
        } catch (SmdiUnavailableException e) {
            smdiTarget = null;
        }
    }

    public void stopDevice(boolean waitForConfigurers, String reason) throws IllegalStateTransitionException {
        if (waitForConfigurers) {
            device.configure();
        } else if (device.tryConfigure() == false) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new FlashMsg(ZoeosFrame.getInstance(), ZoeosFrame.getInstance(), 1000, 200, FlashMsg.colorWarning, "DEVICE BUSY");
                }
            });
            return;
        }

        try {
            if (sts.testTransition(STATE_STOPPED) == STATE_STOPPED)
                return;
            presetDB.stateStop();
            sampleDB.stateStop();
            remote.stateStop();
            sts.transition(STATE_STOPPED);
        } finally {
            device.unlock();
        }
        Zoeos.postZDeviceEvent(new ZDeviceStoppedEvent(E4Device.this, E4Device.this, reason));
    }

    public void removeDevice(boolean saveState) throws ZDeviceCannotBeRemovedException, IllegalStateTransitionException {
        if (saveState)
            viewManager.retrieveDeviceDesktopElementsForNextStart();
        device.configure();
        try {
            // TODO!! alternative logic required for remove on pending state

            int st = sts.testTransition(STATE_REMOVED);
            if (st == STATE_REMOVED)
                return;

            if (presetDB != null && presetDB.getState() == presetDB.STATE_STARTED)
                presetDB.stateStop();
            if (sampleDB != null && sampleDB.getState() == presetDB.STATE_STARTED)
                sampleDB.stateStop();
            if (remote != null && remote.getState() == remote.STATE_STARTED)
                remote.stateStop();

            if (saveState) {
                try {
                    SessionExternalization.saveAsLastSession(SessionExternalization.makeDeviceSession(this));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sts.transition(STATE_REMOVED);

            viewManager.closeDeviceViews().start();
            zDispose();
        } finally {
            device.unlock();
        }
        Zoeos.postZDeviceEvent(new ZDeviceRemovedEvent(this, this));
    }

    public void refreshDevice() throws ZDeviceRefreshException {
    }

    public String getLicenseProduct() {
        return LicenseKeyManager.zoeosProduct;
    }

    public String getLicenseType() {
        return LicenseKeyManager.fullType;
    }

    // PRIVATE HELPERS
    private boolean retrieveDeviceConfiguration(boolean showProgress) {
        Zoeos z = Zoeos.getInstance();

        if (showProgress)
            z.beginProgressElement(this, this.makeDeviceProgressTitle("Retrieving Device Configuration"), 7);
        try {
            MinMaxDefault mmd;

            try {
                mmd = remote.getParameterContext().req_prmMMD(IntPool.get(23)); // E4_LINK_PRESET
                maxPreset = mmd.getMax().intValue();
                if (showProgress)
                    z.updateProgressElement(this);

                mmd = remote.getParameterContext().req_prmMMD(ID.sample); // E4_GEN_SAMPLE
                maxSample = mmd.getMax().intValue();
                if (showProgress)
                    z.updateProgressElement(this);

                deviceConfig = remote.getMasterContext().req_deviceConfig();
                if (showProgress)
                    z.updateProgressElement(this);

                exDeviceConfig = remote.getMasterContext().req_deviceExConfig();
                if (showProgress)
                    z.updateProgressElement(this);

                presetMemory = remote.getMasterContext().req_presetMemory();
                if (showProgress)
                    z.updateProgressElement(this);

                sampleMemory = remote.getMasterContext().req_sampleMemory();
                if (showProgress)
                    z.updateProgressElement(this);

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
                z.updateProgressElement(this);

            return true;
        } finally {
            if (showProgress)
                z.endProgressElement(this);
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
                    new FlashMsg(ZoeosFrame.getInstance(), ZoeosFrame.getInstance(), 1000, 250, FlashMsg.colorError, "Communications Error: " + remote.getName());
                }
            });
            if (deviceExceptions.size() >= remote.getRemotePreferences().ZPREF_commErrorThreshold.getValue()) {
                StringBuffer buf = new StringBuffer();
                for (int i = 0; i < deviceExceptions.size(); i++) {
                    if (deviceExceptions.get(i) instanceof Exception)
                        buf.append(((Exception) deviceExceptions.get(i)).getMessage() + Zoeos.lineSeperator);
                    else
                        buf.append(deviceExceptions.get(i).toString() + Zoeos.lineSeperator);
                }
                stopDeviceThreaded(buf.toString());
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
                StringBuffer buf = new StringBuffer();
                for (int i = 0; i < deviceExceptions.size(); i++) {
                    if (deviceExceptions.get(i) instanceof Exception)
                        buf.append(((Exception) deviceExceptions.get(i)).getMessage() + Zoeos.lineSeperator);
                    else
                        buf.append(deviceExceptions.get(i).toString() + Zoeos.lineSeperator);
                }
                stopDeviceThreaded(buf.toString());
            }
        }
    }

    // ZExternalDevice
    public String getDeviceCategory() {
        return "E-MU";
    }

    public String getDeviceConfigReport() {
        device.access();
        try {
            int s = sts.getState();
            if (s == STATE_RUNNING)
                retrieveDeviceConfiguration(false);
        } finally {
            device.unlock();
        }
        return remote.getName() + Zoeos.getLineSeperator() + deviceConfigReport;
    }

    /* public String[][] getTabularDeviceConfigReport() {
         device.access();
         try {
             int s = sts.getState();
             if (s == STATE_RUNNING)
                 retrieveDeviceConfiguration(false);
         } finally {
             device.unlock();
         }
         return tabularDeviceConfigReport;
     }
      */
    public TableModel getDeviceConfigTableModel() {
        device.access();
        try {
            int s = sts.getState();
            if (s == STATE_RUNNING)
                retrieveDeviceConfiguration(false);
        } finally {
            device.unlock();
        }
        return configTableModel;
    }

    public void refreshDeviceConfiguration(boolean showProgress) {
        device.access();
        try {
            int s = sts.getState();
            if (s == STATE_RUNNING)
                retrieveDeviceConfiguration(showProgress);
        } finally {
            device.unlock();
        }
    }

    public int getStateSynchronized() {
        device.access();
        try {
            return sts.getState();
        } finally {
            device.unlock();
        }
    }

    public void markDuplicate() throws IllegalStateTransitionException {
        device.configure();
        try {
            sts.transition(STATE_MARKED_DUPLICATE);
        } finally {
            device.unlock();
        }
    }

    public String getStaticName() {
        return remote.getIdentityMessage().toString();
    }

    public String getName() {
        return remote.getName();
    }

    public void setName(final String name) {
        device.access();
        try {
            remote.setName(name);
            tplh.fireTitleProviderDataChanged();
        } finally {
            device.unlock();
        }
    }

    public void saveState() {
        taskSaveState();
    }

    private void taskSaveState() {
        device.configure();
        try {
            //Thread t = Thread.currentThread();
            viewManager.retrieveDeviceDesktopElementsForNextStart();
            SessionExternalization.saveAsLastSession(SessionExternalization.makeDeviceSession(this));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            device.unlock();
        }
    }

    public String toString() {
        return getName();
    }

    public File getDeviceLocalDir() {
        return remote.getDeviceLocalDir();
    }

    // CONTEXTS
    public DeviceParameterContext getDeviceParameterContext() {
        device.access();
        try {
            // if (sts.getState() != ZExternalDevice.STATE_RUNNING)
            //     throw new ZDeviceNotRunningException(this, null);
            return dpc;
        } finally {
            device.unlock();
        }
    }

    public MultiModeContext getMultiModeContext() throws ZDeviceNotRunningException {
        device.access();
        try {
            if (sts.getState() != ZExternalDevice.STATE_RUNNING)
                throw new ZDeviceNotRunningException(this, null);
            return mmContext;
        } finally {
            device.unlock();
        }
    }

    public PresetContext getDefaultPresetContext() throws ZDeviceNotRunningException {
        device.access();
        try {
            if (sts.getState() != ZExternalDevice.STATE_RUNNING)
                throw new ZDeviceNotRunningException(this, null);
            return presetDB.getRootContext();
        } finally {
            device.unlock();
        }
    }

    public SampleContext getDefaultSampleContext() throws ZDeviceNotRunningException {
        device.access();
        try {
            if (sts.getState() != ZExternalDevice.STATE_RUNNING)
                throw new ZDeviceNotRunningException(this, null);
            return sampleDB.getRootContext();
        } finally {
            device.unlock();
        }
    }

    public MasterContext getMasterContext() throws ZDeviceNotRunningException {
        device.access();
        try {
            if (sts.getState() != ZExternalDevice.STATE_RUNNING)
                throw new ZDeviceNotRunningException(this, null);
            return masterContext;
        } finally {
            device.unlock();
        }
    }

    public SampleMemory getSampleMemory() throws RemoteUnreachableException, ZDeviceNotRunningException {
        device.access();
        try {
            if (sts.getState() != ZExternalDevice.STATE_RUNNING)
                throw new ZDeviceNotRunningException(this, null);
            try {
                return remote.getMasterContext().req_sampleMemory();
            } catch (RemoteDeviceDidNotRespondException e) {
                throw new RemoteUnreachableException(e.getMessage());
            } catch (RemoteMessagingException e) {
                throw new RemoteUnreachableException(e.getMessage());
            }
        } finally {
            device.unlock();
        }
    }

    public Remotable.PresetMemory getPresetMemory() throws RemoteUnreachableException, ZDeviceNotRunningException {
        device.access();
        try {
            if (sts.getState() != ZExternalDevice.STATE_RUNNING)
                throw new ZDeviceNotRunningException(this, null);
            try {
                return remote.getMasterContext().req_presetMemory();
            } catch (RemoteDeviceDidNotRespondException e) {
                throw new RemoteUnreachableException(e.getMessage());
            } catch (RemoteMessagingException e) {
                throw new RemoteUnreachableException(e.getMessage());
            }
        } finally {
            device.unlock();
        }
    }

    private static final int defragPause = 1500;

    public void sampleMemoryDefrag(boolean pause) throws ZDeviceNotRunningException, RemoteUnreachableException {
        device.configure();
        try {
            if (sts.getState() != ZExternalDevice.STATE_RUNNING)
                throw new ZDeviceNotRunningException(this, null);
            try {
                remote.getMasterContext().cmd_sampleDefrag();
                if (pause) {
//  new FlashMsg(null, null, defragPause, defragPause / 8, FlashMsg.colorInfo, "WAIT: Sample defragmentation");
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
            device.unlock();
        }
    }

    // SMDI
    public Object getSmdiCouplingObject() {
        return remote.getIdentityMessage();
    }

    public boolean isSmdiCoupled() {
        device.access();
        try {
            if (smdiTarget != null)
                return true;
            return false;
        } finally {
            device.unlock();
        }
    }

    public SmdiTarget getSmdiTarget() throws NotSMDICoupledException {
        device.access();
        try {
            if (smdiTarget != null)
                return smdiTarget;
            throw new NotSMDICoupledException();
        } finally {
            device.unlock();
        }
    }

    public void setSmdiTarget(SmdiTarget target) {
        device.configure();
        try {
            smdiTarget = target;
        } finally {
            device.unlock();
        }
    }

    // EVENTS
    public void addDeviceListener(DeviceListener dl) {
        listeners.add(dl);
    }

    public void removeDeviceListener(DeviceListener dl) {
        listeners.remove(dl);
    }

    // DESKTOP
    public ViewManager getViewManager() {
        return viewManager;
    }

    // SYNCHRONIZATION
    public void lockAccess() {
        device.access();
    }

    public void lockConfigure() {
        device.configure();
    }

    public void unlock() {
        device.unlock();
    }

    // BANK

    public void eraseBank() throws ZDeviceNotRunningException {
        device.configure();
        try {
            if (sts.getState() != STATE_RUNNING)
                throw new ZDeviceNotRunningException(this, null);
            new ZDBModifyThread("Erase Bank") {
                public void run() {
                    device.configure();
                    try {
                        remote.getMasterContext().cmd_bankErase();

                        presetDB.stopWorkerThreads();
                        sampleDB.stopWorkerThreads();

                        presetDB.eraseUser();
                        sampleDB.eraseUser();

                        E4Device.this.mmContext.refresh();
                        E4Device.this.masterContext.refresh();

                        presetDB.initializeUserPresetData();
                        sampleDB.initializeAllSampleData();
                    } catch (Exception e) {
                        logCommError(e);
                    } finally {
                        device.unlock();
                    }
                }
            }.start();
        } catch (Exception e) {
        } finally {
            device.unlock();
        }
    }

    public void refreshBank(final boolean refreshData) throws ZDeviceNotRunningException {
        device.configure();
        try {
            if (sts.getState() != STATE_RUNNING)
                throw new ZDeviceNotRunningException(this, null);
            new ZDBModifyThread("Refresh Bank") {
                public void run() {
                    device.configure();
                    try {
                        presetDB.stopWorkerThreads();
                        sampleDB.stopWorkerThreads();

                        presetDB.uninitializeUser();
                        sampleDB.uninitializeUser();

                        E4Device.this.mmContext.refresh();
                        E4Device.this.masterContext.refresh();

                        presetDB.initializeAllPresetNames(refreshData);
                        sampleDB.initializeAllSampleData();
                    } catch (Exception e) {
                        logCommError(e);
                    } finally {
                        device.unlock();
                    }
                }
            }.start();
        } catch (Exception e) {
        } finally {
            device.unlock();
        }
    }

    // CONFIGURATION
    public DeviceConfig getDeviceConfig() throws ZDeviceNotRunningException {
        device.access();
        try {
            int st = sts.getState();
            if (st != ZExternalDevice.STATE_RUNNING)
                throw new ZDeviceNotRunningException(this, null);
            retrieveDeviceConfiguration(true);
            return deviceConfig;
        } finally {
            device.unlock();
        }
    }

    public DeviceExConfig getDeviceExConfig() throws ZDeviceNotRunningException {
        device.access();
        try {
            int st = sts.getState();
            if (st != ZExternalDevice.STATE_RUNNING)
                throw new ZDeviceNotRunningException(this, null);
            retrieveDeviceConfiguration(true);
            return exDeviceConfig;
        } finally {
            device.unlock();
        }
    }

    public boolean isUltra() {
        return this.getDeviceVersion() >= BASE_ULTRA_VERSION;
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

    public ZCommand[] getZCommands() {
        return cmdProviderHelper.getCommandObjects(this);
    }

    public void SmdiChanged() {
        new ZDefaultThread("Update SMDI") {
            public void run() {
                device.configure();
                try {
                    updateSmdiCoupling();
                } finally {
                    device.unlock();
                }
            }
        }.start();
    }

    public int getScsiId() throws ZDeviceNotRunningException {
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
            super(dpc.getMasterContext(), true);
        }
    }

    public class Impl_PresetContextFactory implements PresetContextFactory, Serializable {

        public Impl_PresetContextFactory() {
        }

        public PresetContext newPresetContext(String name, PresetDatabaseProxy pdbp) {
            return new Impl_PresetContext(E4Device.this, name, pdbp);
        }

        public Object initializePresetAtIndex(Integer index, PresetEventHandler peh) {
            device.access();
            try {
                return null;
            } finally {
                device.unlock();
            }
        }

        public boolean remoteInitializePresetAtIndex(Integer index, PresetEventHandler peh, ByteArrayInputStream is) {
            device.access();
            try {
                return false;
            } finally {
                device.unlock();
            }
        }

        public double getPresetInitializationStatus(Integer index) {
            device.access();
            try {
                return -1;
            } finally {
                device.unlock();
            }
        }

        /*   public Object initializePresetAtIndex(Integer index, PresetEventHandler peh, PresetInitializationMonitor mon) {
               device.access();
               try {
                   return null;
               } finally {
                   device.unlock();
               }
           }
          */
        // may return null
        public String initializePresetNameAtIndex(Integer index, PresetEventHandler peh) {
            device.access();
            try {
                return null;
            } finally {
                device.unlock();
            }
        }
    }

    public class Impl_OnlinePresetContextFactory implements PresetContextFactory, Serializable {
        private Hashtable presetInitializationMonitors = new Hashtable();

        public Impl_OnlinePresetContextFactory() {
        }

        public PresetContext newPresetContext(String name, PresetDatabaseProxy pdbp) {
            return new Impl_OnlinePresetContext(E4Device.this, name, pdbp, remote);
        }

        public double getPresetInitializationStatus(Integer index) {
            //  device.access();
            // try {
            PresetInitializationMonitor pim = (PresetInitializationMonitor) presetInitializationMonitors.get(index);
            if (pim != null)
                return pim.getStatus();
            return RemoteObjectStates.STATUS_INITIALIZED;
            //   } finally {
            //     device.unlock();
            // }
        }

        public Object initializePresetAtIndex(Integer index, PresetEventHandler peh) {
            //device.access();
            // try {
            ByteArrayInputStream dumpStream;
            PresetInitializationMonitor mon = new PresetInitializationMonitor(index, peh);
            presetInitializationMonitors.put(index, mon);
            try {
                dumpStream = remote.getPresetContext().req_dump(index, mon);
                if (dumpStream != null) {
                    PresetObject p = new PresetObject(dumpStream, peh, dpc);
                    return p;
                }
            } catch (InvalidPresetDumpException e) {
                e.printStackTrace();
                logCommError(e);
            } catch (RemoteDeviceDidNotRespondException e) {
                e.printStackTrace();
                logCommError(e);
            } catch (RemoteMessagingException e) {
                e.printStackTrace();
                logCommError(e);
            } catch (com.pcmsolutions.device.EMU.E4.RemoteUnreachableException e) {
                e.printStackTrace();
                logCommError(e);
            } catch (PresetEmptyException e) {
                //e.printStackTrace();
                return EmptyPreset.getInstance();
            } catch (TooManyVoicesException e) {
                e.printStackTrace();
            } catch (TooManyZonesException e) {
                e.printStackTrace();
            } finally {
                presetInitializationMonitors.remove(index);
            }
            return null;
            // } finally {
            //   device.unlock();
            // }
        }

        public boolean remoteInitializePresetAtIndex(Integer index, PresetEventHandler peh, ByteArrayInputStream is) {
            //  device.access();
            //   try {
            PresetInitializationMonitor mon = new PresetInitializationMonitor(index, peh);
            presetInitializationMonitors.put(index, mon);
            try {
                remote.getPresetContext().edit_dump(is, mon);
                return true;
            } catch (RemoteDeviceDidNotRespondException e) {
                e.printStackTrace();
                logCommError(e);
            } catch (RemoteMessagingException e) {
                e.printStackTrace();
                logCommError(e);
            } catch (com.pcmsolutions.device.EMU.E4.RemoteUnreachableException e) {
                e.printStackTrace();
                logCommError(e);
            } catch (IOException e) {
                e.printStackTrace();
                logCommError(e);
            } finally {
                presetInitializationMonitors.remove(index);
            }
            return false;
            // } finally {
            //   device.unlock();
            // }
        }

        public String initializePresetNameAtIndex(Integer index, PresetEventHandler peh) {
            //  device.access();
            // try {
            String name = null;
            try {
                name = remote.getPresetContext().req_name(index);
            } catch (RemoteDeviceDidNotRespondException e) {
                logCommError(e);
            } catch (RemoteMessagingException e) {
                logCommError(e);
            } catch (com.pcmsolutions.device.EMU.E4.RemoteUnreachableException e) {
                logCommError(e);
            }
            return name;
            //  } finally {
            //     device.unlock();
            //}
        }

        public boolean isPresetRefreshing(Integer preset) {
            return presetInitializationMonitors.containsKey(preset);
        }
    }

    public class Impl_SampleContextFactory implements SampleContextFactory, Serializable {

        public Impl_SampleContextFactory() {
        }

        public SampleContext newSampleContext(String name, SampleDatabaseProxy sdbp) {
            //return new Impl_SampleContext(name, sdbp);
            return null;
        }

        public Object initializeSampleAtIndex(Integer index, SampleEventHandler seh) {
            return null;
        }

        public Object initializeSampleAtIndex(Integer index, String name, SampleEventHandler peh) {
            return null;
        }

        public double getSampleInitializationStatus(Integer index) {
            return -1;
        }

        public Object initializeSampleAtIndex(Integer index, SampleEventHandler seh, SampleInitializationMonitor mon) {
            return null;
        }

        // may return null
        public String initializeSampleNameAtIndex(Integer index, SampleEventHandler seh) {
            return null;
        }
    }

    public class Impl_OnlineSampleContextFactory implements SampleContextFactory, Serializable {
        private Hashtable sampleInitializationMonitors = new Hashtable();

        public Impl_OnlineSampleContextFactory() {
        }

        public SampleContext newSampleContext(String name, SampleDatabaseProxy sdbp) {
            return new Impl_OnlineSampleContext(E4Device.this, name, sdbp, deviceSampleMediator, remote);
        }

        public double getSampleInitializationStatus(Integer index) {
            //   device.access();
            //   try {
            SampleInitializationMonitor sim = (SampleInitializationMonitor) sampleInitializationMonitors.get(index);
            if (sim != null)
                return sim.getStatus();
            return -1;
            //  } finally {
            //      device.unlock();
            //  }
        }

        public Object initializeSampleAtIndex(Integer index, SampleEventHandler seh) {
            //device.access();
            // try {
            if (smdiTarget != null && index.intValue() >= DeviceContext.FIRST_USER_SAMPLE && index.intValue() <= DeviceContext.MAX_USER_SAMPLE)
                try {
                    SmdiSampleHeader sh = smdiTarget.getSampleHeader(index.intValue());
                    return new SampleObject(index, sh.getName(), seh, new Impl_SampleDescriptor(sh));
                } catch (SmdiOutOfRangeException e) {
                    e.printStackTrace();
                } catch (SmdiNoSampleException e) {
                    return EmptySample.getInstance();
                } catch (SmdiGeneralException e) {
                    e.printStackTrace();
                } catch (TargetNotSMDIException e) {
                    updateSmdiCoupling();
                }
            String name = initializeSampleNameAtIndex(index, seh);
            return initializeSampleAtIndex(index, name, seh);
            // } finally {
            //      device.unlock();
            //  }
        }

        public Object initializeSampleAtIndex(Integer index, String name, SampleEventHandler seh) {
            //  device.access();
            //  try {
            if (name != null)
                if (name.trim().equals(DeviceContext.EMPTY_SAMPLE))
                    return EmptySample.getInstance();
                else {
                    if (smdiTarget != null && index.intValue() >= DeviceContext.FIRST_USER_SAMPLE && index.intValue() <= DeviceContext.MAX_USER_SAMPLE)
                        try {
                            SmdiSampleHeader sh = smdiTarget.getSampleHeader(index.intValue());
                            return new SampleObject(index, name, seh, new Impl_SampleDescriptor(sh));
                        } catch (SmdiOutOfRangeException e) {
                            e.printStackTrace();
                        } catch (SmdiNoSampleException e) {
                            return EmptySample.getInstance();
                        } catch (SmdiGeneralException e) {
                            e.printStackTrace();
                        } catch (TargetNotSMDIException e) {
                            updateSmdiCoupling();
                        }
                    return new SampleObject(index, name, seh, null);
                }
            return null;
            //  } finally {
            //      device.unlock();
            //  }
        }

        public String initializeSampleNameAtIndex(Integer index, SampleEventHandler seh) {
            //  device.access();
            //  try {
            String name = null;
            try {
                name = remote.getSampleContext().req_name(index);
            } catch (RemoteDeviceDidNotRespondException e) {
                logCommError(e);
            } catch (RemoteMessagingException e) {
                logCommError(e);
            } catch (com.pcmsolutions.device.EMU.E4.RemoteUnreachableException e) {
                logCommError(e);
            }
            return name;
            //   } finally {
            //       device.unlock();
            //  }
        }
    }

    private class Impl_MultiModeContext implements MultiModeContext, ZCommandProvider, PresetListener, Serializable, ZDisposable {
        transient private Vector listeners = new Vector();
        private MultiModeMap mmMap = null;
        private boolean has32;
        private MultiModeDescriptor mmDescriptor = new Impl_MultiModeDescripor();
        private GeneralParameterDescriptor presetPD;
        private GeneralParameterDescriptor volPD;
        private GeneralParameterDescriptor panPD;
        private GeneralParameterDescriptor submixPD;

        public void zDispose() {
            listeners.clear();
            mmMap = null;
            mmDescriptor = null;
        }

        public Impl_MultiModeContext() throws RemoteDeviceDidNotRespondException, RemoteMessagingException, com.pcmsolutions.device.EMU.E4.RemoteUnreachableException, IllegalParameterIdException {
            mmMap = remote.getMasterContext().req_multimodeMap();
            has32 = mmMap.has32();
            presetPD = dpc.getParameterDescriptor(IntPool.get(247));
            volPD = dpc.getParameterDescriptor(IntPool.get(248));
            panPD = dpc.getParameterDescriptor(IntPool.get(249));
            submixPD = dpc.getParameterDescriptor(IntPool.get(250));
        }

        private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
            ois.defaultReadObject();
            listeners = new Vector();
        }

        public MultiModeMap getMultimodeMap() {
            device.access();
            try {
                synchronized (this) {
                    return mmMap.getCopy();
                }
            } finally {
                device.unlock();
            }
        }

        public MultiModeChannel getMultiModeChannel(Integer channel) throws IllegalMidiChannelException {
            return new Impl_MultiModeChannel(channel);
        }

        public void setMultimodeMap(MultiModeMap mmMap) {
            device.access();
            try {
                synchronized (this) {
                    remote.getMasterContext().edit_multimodeMap(mmMap);
                    removeChannelPresetListeners();
                    this.mmMap = mmMap.getCopy();
                    addChannelPresetListeners();
                }
            } catch (com.pcmsolutions.device.EMU.E4.RemoteUnreachableException e) {
                logCommError(e);
            } catch (RemoteMessagingException e) {
                logCommError(e);
            } finally {
                device.unlock();
            }
            fireMultiModeEvent(new MultiModeRefreshedEvent(Impl_MultiModeContext.this, Impl_MultiModeContext.this));
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
                    pc.removePresetListener(this, new Integer[]{mmMap.getPreset(ch)});
                } catch (IllegalMidiChannelException e) {
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
                    pc.addPresetListener(this, new Integer[]{mmMap.getPreset(ch)});
                } catch (IllegalMidiChannelException e) {
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
            device.access();
            try {
                synchronized (this) {
                    return mmDescriptor;
                }
            } finally {
                device.unlock();
            }
        }

        public boolean has32Channels() {
            device.access();
            try {
                synchronized (this) {
                    return has32;
                }
            } finally {
                device.unlock();
            }
        }

        public Integer getPreset(Integer ch) throws IllegalMidiChannelException {
            device.access();
            try {
                synchronized (this) {
                    return mmMap.getPreset(ch);
                }
            } finally {
                device.unlock();
            }
        }

        public Integer getVolume(Integer ch) throws IllegalMidiChannelException {
            device.access();
            try {
                synchronized (this) {
                    return mmMap.getVolume(ch);
                }
            } finally {
                device.unlock();
            }
        }

        public Integer getPan(Integer ch) throws IllegalMidiChannelException {
            device.access();
            try {
                synchronized (this) {
                    return mmMap.getPan(ch);
                }
            } finally {
                device.unlock();
            }
        }

        public Integer getSubmix(Integer ch) throws IllegalMidiChannelException {
            device.access();
            try {
                synchronized (this) {
                    return mmMap.getSubmix(ch);
                }
            } finally {
                device.unlock();
            }
        }

        public void setPreset(Integer ch, Integer preset) throws IllegalMidiChannelException {
            if (!getMultiModeDescriptor().getPresetParameterDescriptor().isValidValue(preset))
                return;
            device.access();
            try {
                synchronized (this) {
                    Integer lastPreset = getPreset(ch);
                    mmMap.setPreset(ch, preset);
                    try {
                        remote.getParameterContext().edit_prmValues(new Integer[]{
                            IntPool.get(246), ch, IntPool.get(247), preset});
                    } catch (Exception e) {
                        logCommError(e);
                    }
                    PresetContext pc = getDefaultPresetContext();
                    pc.addPresetListener(this, new Integer[]{preset});
                    pc.removePresetListener(this, new Integer[]{lastPreset});
                    fireMultiModeEvent(new MultiModeChannelChangedEvent(Impl_MultiModeContext.this, Impl_MultiModeContext.this, ch));
                }
            } catch (ZDeviceNotRunningException e) {
                e.printStackTrace();
            } finally {
                device.unlock();
            }
        }

        public void setVolume(Integer ch, Integer volume) throws IllegalMidiChannelException {
            device.access();
            try {
                synchronized (this) {
                    mmMap.setVolume(ch, volume);
                    try {
                        remote.getParameterContext().edit_prmValues(new Integer[]{
                            IntPool.get(246), ch, IntPool.get(248), volume}
                        );
                    } catch (Exception e) {
                        logCommError(e);
                    }
                }
            } finally {
                device.unlock();
            }
            fireMultiModeEvent(new MultiModeChannelChangedEvent(Impl_MultiModeContext.this, Impl_MultiModeContext.this, ch));
        }

        public void setPan(Integer ch, Integer pan) throws IllegalMidiChannelException {
            device.access();
            try {
                synchronized (this) {
                    mmMap.setPan(ch, pan);
                    try {
                        remote.getParameterContext().edit_prmValues(new Integer[]{
                            IntPool.get(246), ch, IntPool.get(249), pan}
                        );
                    } catch (Exception e) {
                        logCommError(e);
                    }
                }

            } finally {
                device.unlock();
            }
            fireMultiModeEvent(new MultiModeChannelChangedEvent(Impl_MultiModeContext.this, Impl_MultiModeContext.this, ch));
        }

        public void setSubmix(Integer ch, Integer submix) throws IllegalMidiChannelException {
            device.access();
            try {
                synchronized (this) {
                    mmMap.setSubmix(ch, submix);
                    // TODO!! for EOS Ultra execute whole map here because of submix editing bug
                    try {
                        remote.getParameterContext().edit_prmValues(new Integer[]{
                            IntPool.get(246), ch, IntPool.get(250), submix}
                        );
                    } catch (Exception e) {
                        logCommError(e);
                    }
                }
            } finally {
                device.unlock();
            }
            fireMultiModeEvent(new MultiModeChannelChangedEvent(Impl_MultiModeContext.this, Impl_MultiModeContext.this, ch));
        }

        public void refresh() {
            device.access();
            try {
                synchronized (this) {
                    mmMap = remote.getMasterContext().req_multimodeMap();
                }
                fireMultiModeEvent(new MultiModeRefreshedEvent(this, this));
            } catch (RemoteDeviceDidNotRespondException e) {
                e.printStackTrace();
            } catch (RemoteMessagingException e) {
                e.printStackTrace();
            } catch (com.pcmsolutions.device.EMU.E4.RemoteUnreachableException e) {
                e.printStackTrace();
            } finally {
                device.unlock();
            }
        }

        private void fireMultiModeEvent(final MultiModeEvent ev) {
            final Vector f_listeners = (Vector) listeners.clone();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    for (int n = 0, o = f_listeners.size(); n < o; n++)
                        try {
                            ev.fire((MultiModeListener) f_listeners.get(n));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }
            });
        }

        public ZCommand[] getZCommands() {
            return multiModeCmdProviderHelper.getCommandObjects(this);
        }

        private void updateChannelsOnPresetChange(Integer preset) {
            device.access();
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

                    } catch (IllegalMidiChannelException e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                device.unlock();
            }
        }

        public void presetInitialized(PresetInitializeEvent ev) {
            updateChannelsOnPresetChange(ev.getPreset());
        }

        public void presetInitializationStatusChanged(PresetInitializationStatusChangedEvent ev) {
            updateChannelsOnPresetChange(ev.getPreset());
        }

        public void presetRefreshed(PresetRefreshEvent ev) {
            updateChannelsOnPresetChange(ev.getPreset());
        }

        public void presetChanged(PresetChangeEvent ev) {
        }

        public void presetNameChanged(PresetNameChangeEvent ev) {
            updateChannelsOnPresetChange(ev.getPreset());
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

            public Impl_MultiModeChannel(Integer ch) throws IllegalMidiChannelException {
                this.ch = ch;
                int chi = ch.intValue();
                if (has32Channels()) {
                    if (chi > 32 || chi < 1)
                        throw new IllegalMidiChannelException(ch, "Not a valid MultiMode channel");
                } else if (chi > 16 || chi < 1)
                    throw new IllegalMidiChannelException(ch, "Not a valid MultiMode channel");
            }

            public MultiModeDescriptor getMultiModeDescriptor() {
                return getMultiModeDescriptor();
            }

            public Integer getChannel() {
                return ch;
            }

            public Integer getPreset() {
                try {
                    return Impl_MultiModeContext.this.getPreset(ch);
                } catch (IllegalMidiChannelException e) {
                    throw new IllegalStateException("Configured midi channel no longer available");
                }
            }

            public Integer getVolume() {
                try {
                    return Impl_MultiModeContext.this.getVolume(ch);
                } catch (IllegalMidiChannelException e) {
                    throw new IllegalStateException("Configured midi channel no longer available");
                }
            }

            public Integer getPan() {
                try {
                    return Impl_MultiModeContext.this.getPan(ch);
                } catch (IllegalMidiChannelException e) {
                    throw new IllegalStateException("Configured midi channel no longer available");
                }
            }

            public Integer getSubmix() {
                try {
                    return Impl_MultiModeContext.this.getSubmix(ch);
                } catch (IllegalMidiChannelException e) {
                    throw new IllegalStateException("Configured midi channel no longer available");
                }
            }

            public void setPreset(Integer preset) {
                try {
                    Impl_MultiModeContext.this.setPreset(ch, preset);
                } catch (IllegalMidiChannelException e) {
                    throw new IllegalStateException("Configured midi channel no longer available");
                }
            }

            public void setVolume(Integer volume) {
                try {
                    Impl_MultiModeContext.this.setVolume(ch, volume);
                } catch (IllegalMidiChannelException e) {
                    throw new IllegalStateException("Configured midi channel no longer available");
                }
            }

            public void setPan(Integer pan) {
                try {
                    Impl_MultiModeContext.this.setPan(ch, pan);
                } catch (IllegalMidiChannelException e) {
                    throw new IllegalStateException("Configured midi channel no longer available");
                }
            }

            public void setSubmix(Integer submix) {
                try {
                    Impl_MultiModeContext.this.setSubmix(ch, submix);
                } catch (IllegalMidiChannelException e) {
                    throw new IllegalStateException("Configured midi channel no longer available");
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

            public ZCommand[] getZCommands() {
                return mutliModeChannelCmdProviderHelper.getCommandObjects(this);
            }


            private class Impl_MultiModePresetParameterModel extends AbstractEditableParameterModel implements IconAndTipCarrier {
                public Impl_MultiModePresetParameterModel() throws IllegalParameterIdException {
                    super(presetPD);
                }

                public Icon getIcon() {
                    try {
                        return E4Device.this.getDefaultPresetContext().getReadablePreset(getPreset()).getIcon();
                    } catch (NoSuchPresetException e) {
                    } catch (ZDeviceNotRunningException e) {
                    }
                    return null;
                }

                public String getToolTipText() {
                    try {
                        return E4Device.this.getDefaultPresetContext().getReadablePreset(getPreset()).getToolTipText();
                    } catch (NoSuchPresetException e) {
                    } catch (ZDeviceNotRunningException e) {
                    }
                    return null;
                }

                public void setValue(Integer value) throws ParameterUnavailableException, ParameterValueOutOfRangeException {
                    setPreset(value);
                }

                public Integer getValue() throws ParameterUnavailableException {
                    return getPreset();
                }

                public String getValueString() throws ParameterUnavailableException {
                    Integer i = getValue();
                    if (i.intValue() == -1)
                        return "Disabled";
                    else {
                        try {
                            return PresetContextMacros.getPresetDisplayName(E4Device.this.getDefaultPresetContext(), i);
                        } catch (ZDeviceNotRunningException e) {
                        }
                    }
                    return "";
                }

                public String getValueUnitlessString() throws ParameterUnavailableException {
                    return getValueString();
                }

                public String toString() {
                    try {
                        return getValueString();
                    } catch (ParameterUnavailableException e) {
                    }
                    return "";
                }
            }


            private class Impl_MultiModeVolumeParameterModel extends AbstractEditableParameterModel {
                public Impl_MultiModeVolumeParameterModel() throws IllegalParameterIdException {
                    super(volPD);
                }

                public void setValue(Integer value) throws ParameterUnavailableException, ParameterValueOutOfRangeException {
                    setVolume(value);
                }

                public Integer getValue() throws ParameterUnavailableException {
                    return getVolume();
                }
            }

            private class Impl_MultiModePanParameterModel extends AbstractEditableParameterModel {
                public Impl_MultiModePanParameterModel() {
                    super(panPD);
                }

                public void setValue(Integer value) throws ParameterUnavailableException, ParameterValueOutOfRangeException {
                    setPan(value);
                }

                public Integer getValue() throws ParameterUnavailableException {
                    return getPan();
                }
            }

            private class Impl_MultiModeSubmixParameterModel extends AbstractEditableParameterModel {
                public Impl_MultiModeSubmixParameterModel() throws IllegalParameterIdException {
                    super(submixPD);
                }

                public void setValue(Integer value) throws ParameterUnavailableException, ParameterValueOutOfRangeException {
                    setSubmix(value);
                }

                public Integer getValue() throws ParameterUnavailableException {
                    return getSubmix();
                }
            }

        }
    }

    private class Impl_MasterContext implements MasterContext, ZCommandProvider, ZDisposable {
        private Master master = new Master();
        private Integer[] ids;
        transient private Vector listeners = new Vector();

        public void zDispose() {
            listeners.clear();
            master = null;
        }

        public Impl_MasterContext() throws RemoteDeviceDidNotRespondException, com.pcmsolutions.device.EMU.E4.RemoteUnreachableException, RemoteMessagingException {
            Set setIds = dpc.getMasterContext().getIds();
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
            device.access();
            try {
                for (int i = 0, n = ids.length; i < n; i++) {
                    try {
                        final GeneralParameterDescriptor pd = dpc.getMasterContext().getParameterDescriptor(ids[i]);
                        models.add(new Impl_MasterEditableParameterModel(pd));
                    } catch (IllegalParameterIdException e) {
                        e.printStackTrace();
                    }
                }
                return models;
            } finally {
                device.unlock();
            }
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

            public void setValue(Integer value) throws ParameterUnavailableException, ParameterValueOutOfRangeException {
                try {
                    setMasterParam(pd.getId(), value);
                } catch (IllegalParameterIdException e) {
                    e.printStackTrace();
                    throw new ParameterUnavailableException();
                }
            }

            public Integer getValue() throws ParameterUnavailableException {
                Integer[] vals;
                try {
                    vals = getMasterParams(new Integer[]{pd.getId()});
                } catch (IllegalParameterIdException e) {
                    e.printStackTrace();
                    throw new ParameterUnavailableException();
                }
                return vals[0];
            }

            public String getToolTipText() {
                try {
                    return getValueString();
                } catch (ParameterUnavailableException e) {
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

        public Integer[] getMasterParams(Integer[] ids) throws IllegalParameterIdException {
            Integer[] rv;
            device.access();
            try {
                rv = master.getValues(ids);
            } finally {
                device.unlock();
            }
            return rv;
        }

        /*boolean fxaChanged = false;
          boolean fxbChanged = false;

          for (int i = 0,j = ids.elementCount; i < j; i++)
              if (ids[i].equals(IntPool.get(6)))
                  fxaChanged = true;
              else if (ids[i].equals(IntPool.get(14)))
                  fxbChanged = true;

          PDBReader reader = pdbp.getDBRead();
          try {
              PresetObject p = reader.getPresetWrite(this, preset);
              try {
                  p.setValues(ids, values);
                  ParameterEditLoader pl = remote.getEditLoader();
                  pl.selPreset(preset);
                  pl.addDesktopElement(ids, values);
                  pl.dispatch();

                  Integer[] vals;
                  if (fxaChanged) {
                      vals = remote.getParameterContext().req_prmValues(fxaIds);
                      p.setValues(fxaIds, ZUtilities.extractOneOfIntegerPairs(vals, false));
                  }
                  if (fxbChanged) {
                      vals = remote.getParameterContext().req_prmValues(fxbIds);
                      p.setValues(fxbIds, ZUtilities.extractOneOfIntegerPairs(vals, false));
                  }

                  228 236
          */

        private Integer[] master_fxaIds = new Integer[]{IntPool.get(229), IntPool.get(230), IntPool.get(231), IntPool.get(232), IntPool.get(233), IntPool.get(234), IntPool.get(235)};
        private Integer[] master_fxbIds = new Integer[]{IntPool.get(237), IntPool.get(238), IntPool.get(239), IntPool.get(240), IntPool.get(241), IntPool.get(242), IntPool.get(243)};

        public void setMasterParam(Integer id, Integer value) throws IllegalParameterIdException, ParameterValueOutOfRangeException {
            boolean fxaChanged = false;
            boolean fxbChanged = false;

            if (id.equals(IntPool.get(228)))
                fxaChanged = true;
            else if (id.equals(IntPool.get(236)))
                fxbChanged = true;

            device.access();
            try {
                master.setValues(new Integer[]{id}, new Integer[]{value});
                try {
                    remote.getParameterContext().edit_prmValues(new Integer[]{id, value});
                    if (fxaChanged) {
                        Integer[] vals = remote.getParameterContext().req_prmValues(master_fxaIds);
                        master.setValues(master_fxaIds, ZUtilities.extractOneOfIntegerPairs(vals, false));
                        fireMasterEvent(new MasterChangedEvent(this, E4Device.this, master_fxaIds));
                    }
                    if (fxbChanged) {
                        Integer[] vals = remote.getParameterContext().req_prmValues(master_fxbIds);
                        master.setValues(master_fxbIds, ZUtilities.extractOneOfIntegerPairs(vals, false));
                        fireMasterEvent(new MasterChangedEvent(this, E4Device.this, master_fxbIds));
                    }
                } catch (IllegalStateException e) {
                    logCommError(e);
                } catch (Exception e) {
                    logCommError(e);
                }
            } finally {
                device.unlock();
            }
            fireMasterEvent(new MasterChangedEvent(this, E4Device.this, new Integer[]{id}));
        }

        public void refresh() {
            device.access();
            Integer[] masterIdVals;
            try {
                masterIdVals = remote.getParameterContext().req_prmValues(ids);
                master.initValues(masterIdVals);
            } catch (RemoteDeviceDidNotRespondException e) {
                logCommError(e);
            } catch (RemoteMessagingException e) {
                logCommError(e);
            } catch (com.pcmsolutions.device.EMU.E4.RemoteUnreachableException e) {
                logCommError(e);
            } finally {
                device.unlock();
            }
            fireMasterEvent(new MasterRefreshedEvent(this, E4Device.this));
        }

        public void addMasterListener(MasterListener ml) {
            listeners.add(ml);
        }

        public void removeMasterListener(MasterListener ml) {
            listeners.remove(ml);
        }

        public ZCommand[] getZCommands() {
            return masterCmdProviderHelper.getCommandObjects(this);
        }
    }
}