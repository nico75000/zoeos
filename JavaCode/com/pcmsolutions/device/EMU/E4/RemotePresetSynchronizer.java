package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.comms.RemoteDeviceDidNotRespondException;
import com.pcmsolutions.comms.RemoteMessagingException;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.events.preset.*;
import com.pcmsolutions.device.EMU.E4.events.preset.requests.*;
import com.pcmsolutions.device.EMU.E4.preset.PresetListener;
import com.pcmsolutions.device.EMU.E4.remote.ParameterEditLoader;
import com.pcmsolutions.device.EMU.E4.remote.Remotable;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.device.EMU.database.NoSuchContextIndexException;
import com.pcmsolutions.device.EMU.database.events.content.ManageableContentEventHandler;
import com.pcmsolutions.system.SystemErrors;
import com.pcmsolutions.system.threads.Impl_ZThread;
import com.pcmsolutions.system.threads.ZThread;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;

/**
 * User: paulmeehan
 * Date: 18-Mar-2004
 * Time: 09:07:57
 */
public class RemotePresetSynchronizer implements Serializable, ManageableContentEventHandler.ExternalHandler<PresetEvent, PresetListener>, ManageableContentEventHandler.RequestHandler<PresetRequestEvent>, RemoteAssignable {
    private transient HashMap<Integer, ZThread> presetMap;
    private transient HashMap<ZThread, EditProfile> threadMap;
    private PresetDatabase pdb;
    private transient Remotable remote;
    private E4Device device;
    private transient Impl_ZThread.OnCompletedAction flushAction;

    private transient Hashtable presetInitializationMonitors;
    private ManageableContentEventHandler ceh;

    static class EditProfile {
        private ParameterEditLoader pel;
        private ArrayList<Integer> presets = new ArrayList<Integer>();
        private final Impl_ZThread thread = (Impl_ZThread) Thread.currentThread();

        private RemotePresetSynchronizer sync;

        public EditProfile(RemotePresetSynchronizer sync) {
            this.sync = sync;
            pel = sync.remote.getEditLoader();
        }

        void add(PresetChangeEvent ev) {
            refPreset(ev.getIndex());
            pel.selPreset(ev.getIndex());
            pel.add(ev.getIds(), ev.getValues());
        }

        void add(VoiceChangeEvent ev) {
            refPreset(ev.getIndex());
            pel.selVoice(ev.getIndex(), ev.getVoice());
            pel.add(ev.getIds(), ev.getValues());
        }

        void add(ZoneChangeEvent ev) {
            refPreset(ev.getIndex());
            pel.selZone(ev.getIndex(), ev.getVoice(), ev.getZone());
            pel.add(ev.getIds(), ev.getValues());
        }

        void add(GroupChangeEvent ev) {
            refPreset(ev.getIndex());
            pel.selGroup(ev.getIndex(), ev.getGroup());
            pel.add(ev.getParameters(), ev.getValues());
        }

        void add(LinkChangeEvent ev) {
            refPreset(ev.getIndex());
            pel.selLink(ev.getIndex(), ev.getLink());
            pel.add(ev.getIds(), ev.getValues());
        }

        private void refPreset(Integer preset) {
            if (presets.contains(preset))
                return;
            if (sync.presetMap.get(preset) != thread) {
                sync.updatePreset(preset);
                sync.presetMap.put(preset, thread);
            }
            presets.add(preset);
        }

        void flush() {
            try {
                //System.out.println("dispatching " + pel.getBytes().length);
                pel.dispatch();
                return;
            } catch (RemoteUnreachableException e) {
                sync.device.logCommError(e);
            } catch (RemoteMessagingException e) {
                sync.device.logCommError(e);
            } catch (Exception e) {
                sync.device.logCommError(e);
            } finally {
                pel.reset();
                for (Iterator i = presets.iterator(); i.hasNext();)
                    sync.presetMap.remove((Integer) i.next());
                presets.clear();
            }
            uninitializePresets();
        }

        void uninitializePresets() {
            new Impl_ZThread() {
                public void runBody() {
                    try {
                        sync.pdb.access();
                    } catch (DeviceException e) {
                        e.printStackTrace();
                        return;
                    }
                    try {
                        for (Iterator<Integer> i = presets.iterator(); i.hasNext();)
                            try {
                                sync.pdb.uninitialize(i.next());
                            } catch (NoSuchContextIndexException e) {
                                e.printStackTrace();
                            } catch (NoSuchContextException e) {
                                e.printStackTrace();
                            } finally {
                            }
                    } finally {
                        sync.pdb.release();
                    }
                }
            }.start();
        }
    }

    public RemotePresetSynchronizer(E4Device device, PresetDatabase pdb, Remotable remote) {
        this.device = device;
        this.pdb = pdb;
        this.remote = remote;
        buildTransients();
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        buildTransients();
    }

    private void buildTransients() {
        presetMap = new HashMap<Integer, ZThread>();
        threadMap = new HashMap<ZThread, EditProfile>();
        flushAction = new Impl_ZThread.OnCompletedAction() {
            public void completed(ZThread t) {
                synchronized (RemotePresetSynchronizer.this) {
                    EditProfile eacc = threadMap.remove(t);
                    if (eacc != null)
                        eacc.flush();
                }
            }
        };
        presetInitializationMonitors = new Hashtable();
    }

    public void setRemote(Remotable r) {
        this.remote = r;
    }

    EditProfile getEditProfile() {
        Impl_ZThread t = (Impl_ZThread) Thread.currentThread();
        EditProfile ep = threadMap.get(t);
        if (ep == null) {
            ep = new EditProfile(this);
            threadMap.put(t, ep);
            t.addOnCompletedAction(flushAction);
            return ep;
        }
        return ep;
    }

    void updatePreset(Integer preset) {
        ZThread t = presetMap.get(preset);
        if (t != null) {
            EditProfile eacc = threadMap.get(t);
            if (eacc != null)
                eacc.flush();
            presetMap.remove(preset);
        }
    }

    void handleRemotingException(final Exception e, final Integer preset) {
        device.logCommError(e);
        uninitializePreset(preset);
    }

    void uninitializePreset(final Integer preset) {
        new Impl_ZThread() {
            public void runBody() {
                try {
                    pdb.access();
                } catch (DeviceException e) {
                    e.printStackTrace();
                    return;
                }
                try {
                    pdb.uninitialize(preset);
                    // pdb.assertNamed(preset);
                } catch (NoSuchContextIndexException e) {
                    e.printStackTrace();
                } catch (NoSuchContextException e) {
                    e.printStackTrace();
                } /*catch (ContentUnavailableException e) {
                    e.printStackTrace();
                } */ finally {
                    pdb.release();
                }
            }
        }.start();
    }

    void verifyDestinationName(Integer preset, String name, int retries, String failMsg) throws RemoteDeviceDidNotRespondException, RemoteUnreachableException, RemoteMessagingException {
        for (int i = 0; i < retries + 1; i++) {
            try {
                String destName = remote.getPresetContext().req_name(preset);
                if (!destName.trim().equals(name.trim())) {
                    // if it is the first failed test, wait a second to give remote time to hasCompleted copy or finish compacting
                    if (i == 0) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                    uninitializePreset(preset);
                    SystemErrors.flashWarning(failMsg);
                }
                break;
            } catch (RemoteDeviceDidNotRespondException e) {
                if (i == retries)
                    throw e;
                // System.out.println("retry");
            } catch (RemoteMessagingException e) {
                if (i == retries)
                    throw e;
                //  System.out.println("retry");
            } catch (RemoteUnreachableException e) {
                if (i == retries)
                    throw e;
                // System.out.println("retry");
            }
        }
    }

    private static final int POST_COPY_NAME_VERIFICATION_RETRIES = 2;

    public boolean handleEvent(final PresetEvent pe) {
        synchronized (this) {
            // PRESET CHANGE
            if (pe.getClass().equals(PresetChangeEvent.class)) {
                getEditProfile().add((PresetChangeEvent) pe);
            }
            // VOICE CHANGE
            else if (pe.getClass().equals(VoiceChangeEvent.class)) {
                getEditProfile().add((VoiceChangeEvent) pe);
            }
            // GROUP CHANGE
            else if (pe.getClass().equals(GroupChangeEvent.class)) {
                getEditProfile().add((GroupChangeEvent) pe);
            }
            // ZONE CHANGED
            else if (pe.getClass().equals(ZoneChangeEvent.class)) {
                getEditProfile().add((ZoneChangeEvent) pe);
            }
            // PRESET COPY
            else if (pe.getClass().equals(PresetCopyEvent.class)) {
                final PresetCopyEvent pce = (PresetCopyEvent) pe;
                updatePreset(pce.getSourcePreset());

                if (pce.getIndex().intValue() >= DeviceContext.BASE_FLASH_PRESET) {
                    try {
                        final String srcName = remote.getPresetContext().req_name(pce.getSourcePreset());
                        remote.getPresetContext().cmd_copy(pce.getSourcePreset(), pce.getIndex());
                        verifyDestinationName(pce.getIndex(), srcName, POST_COPY_NAME_VERIFICATION_RETRIES, "Copy to preset flash failed");
                    } catch (RemoteDeviceDidNotRespondException e) {
                        handleRemotingException(e, pe.getIndex());
                    } catch (RemoteMessagingException e) {
                        handleRemotingException(e, pe.getIndex());
                    } catch (RemoteUnreachableException e) {
                        handleRemotingException(e, pe.getIndex());
                    }
                } else {
                    try {
                        String srcName = remote.getPresetContext().req_name(pce.getSourcePreset());
                        remote.getPresetContext().cmd_copy(pce.getSourcePreset(), pce.getIndex());
                        verifyDestinationName(pce.getIndex(), srcName, POST_COPY_NAME_VERIFICATION_RETRIES, "Copy to user preset failed");
                    } catch (RemoteDeviceDidNotRespondException e) {
                        handleRemotingException(e, pe.getIndex());
                    } catch (RemoteMessagingException e) {
                        handleRemotingException(e, pe.getIndex());
                    } catch (RemoteUnreachableException e) {
                        handleRemotingException(e, pe.getIndex());
                    }
                }
            }
            // PRESET NEW
            else if (pe.getClass().equals(PresetNewEvent.class)) {
                final PresetNewEvent pne = (PresetNewEvent) pe;
                try {
                    updatePreset(pne.getIndex());
                    PresetInitializationMonitor mon = new PresetInitializationMonitor(pne.getIndex(), ceh) {
                        public void setStatus(double status) {
                            super.setStatus(status);
                            pne.getProgressCallback().updateProgress(Math.min(Math.abs(status), 1));
                        }
                    };
                    presetInitializationMonitors.put(pe.getIndex(), mon);
                    try {
                        remote.getPresetContext().edit_dump(((PresetNewEvent) pe).getInputStream(), mon);
                    } catch (RemoteUnreachableException e) {
                        handleRemotingException(e, pe.getIndex());
                    } catch (RemoteMessagingException e) {
                        handleRemotingException(e, pe.getIndex());
                        e.printStackTrace();
                    } catch (RemoteDeviceDidNotRespondException e) {
                        handleRemotingException(e, pe.getIndex());
                        e.printStackTrace();
                    } catch (IOException e) {
                        handleRemotingException(e, pe.getIndex());
                        e.printStackTrace();
                    } finally {
                        presetInitializationMonitors.remove(pe.getIndex());
                    }
                } finally {
                    pne.getProgressCallback().updateProgress(1);
                }
            }
            // PRESET NAME CHANGE
            else if (pe.getClass().equals(PresetNameChangeEvent.class)) {
                PresetNameChangeEvent pnce = (PresetNameChangeEvent) pe;
                try {
                    remote.getPresetContext().edit_name(pnce.getIndex(), pnce.getName());
                } catch (RemoteUnreachableException e) {
                    handleRemotingException(e, pe.getIndex());
                } catch (RemoteMessagingException e) {
                    handleRemotingException(e, pe.getIndex());
                }
            }
            // PRESET REFRESH
            else if (pe.getClass().equals(PresetInitializeEvent.class)) {
                // internal event
            }
            // PRESET ERASE
            else if (pe.getClass().equals(PresetEraseEvent.class)) {
                updatePreset(pe.getIndex());
                try {
                    remote.getPresetContext().cmd_erase(pe.getIndex());
                } catch (RemoteUnreachableException e) {
                    handleRemotingException(e, pe.getIndex());
                } catch (RemoteMessagingException e) {
                    handleRemotingException(e, pe.getIndex());
                }
            }

            // VOICE COPY
            else if (pe.getClass().equals(VoiceCopyEvent.class)) {
                VoiceCopyEvent vce = (VoiceCopyEvent) pe;
                try {
                    remote.getVoiceContext().cmd_copy(vce.getSourcePreset(), vce.getSourceVoice(), vce.getIndex(), vce.getGroup());
                } catch (RemoteUnreachableException e) {
                    handleRemotingException(e, pe.getIndex());
                } catch (RemoteMessagingException e) {
                    handleRemotingException(e, pe.getIndex());
                }
            }
            // VOICE ADD
            else if (pe.getClass().equals(VoiceAddEvent.class)) {
                VoiceAddEvent vae = (VoiceAddEvent) pe;
                try {
                    remote.getPresetContext().cmd_newVoice(vae.getIndex());
                } catch (RemoteUnreachableException e) {
                    handleRemotingException(e, pe.getIndex());
                } catch (RemoteMessagingException e) {
                    handleRemotingException(e, pe.getIndex());
                }
            }

            // VOICE REMOVE
            else if (pe.getClass().equals(VoiceRemoveEvent.class)) {
                VoiceRemoveEvent vre = (VoiceRemoveEvent) pe;
                updatePreset(vre.getIndex());
                try {
                    remote.getVoiceContext().cmd_delete(vre.getIndex(), vre.getVoice());
                } catch (RemoteUnreachableException e) {
                    handleRemotingException(e, pe.getIndex());
                } catch (RemoteMessagingException e) {
                    handleRemotingException(e, pe.getIndex());
                }
            }  // VOICE EXPAND
            else if (pe.getClass().equals(VoiceExpandEvent.class)) {
                VoiceExpandEvent vre = (VoiceExpandEvent) pe;
                updatePreset(vre.getIndex());
                try {
                    remote.getVoiceContext().cmd_expandVoice(vre.getIndex(), vre.getVoice());
                } catch (RemoteUnreachableException e) {
                    handleRemotingException(e, pe.getIndex());
                } catch (RemoteMessagingException e) {
                    handleRemotingException(e, pe.getIndex());
                }
            }

            // COMBINE GROUP VOICES
            else if (pe.getClass().equals(GroupCombineEvent.class)) {
                GroupCombineEvent gce = (GroupCombineEvent) pe;
                updatePreset(gce.getIndex());
                try {
                    remote.getPresetContext().cmd_combineVoices(gce.getIndex(), gce.getGroup());
                } catch (RemoteUnreachableException e) {
                    handleRemotingException(e, pe.getIndex());
                } catch (RemoteMessagingException e) {
                    handleRemotingException(e, pe.getIndex());
                }
            }
            // LINK COPY
            else if (pe.getClass().equals(LinkCopyEvent.class)) {
                LinkCopyEvent lce = (LinkCopyEvent) pe;
                try {
                    remote.getLinkContext().cmd_copy(lce.getSrcPreset(), lce.getSrcLink(), lce.getIndex());
                } catch (RemoteUnreachableException e) {
                    handleRemotingException(e, pe.getIndex());
                } catch (RemoteMessagingException e) {
                    handleRemotingException(e, pe.getIndex());
                }
            }
            // LINK ADD
            else if (pe.getClass().equals(LinkAddEvent.class)) {
                LinkAddEvent lae = (LinkAddEvent) pe;
                try {
                    remote.getPresetContext().cmd_newLink(lae.getIndex());
                } catch (RemoteUnreachableException e) {
                    handleRemotingException(e, pe.getIndex());
                } catch (RemoteMessagingException e) {
                    handleRemotingException(e, pe.getIndex());
                }
            }
            // LINK REMOVE
            else if (pe.getClass().equals(LinkRemoveEvent.class)) {
                LinkRemoveEvent lre = (LinkRemoveEvent) pe;
                updatePreset(lre.getIndex());
                try {
                    remote.getLinkContext().cmd_delete(lre.getIndex(), lre.getLink());
                } catch (RemoteUnreachableException e) {
                    handleRemotingException(e, pe.getIndex());
                } catch (RemoteMessagingException e) {
                    handleRemotingException(e, pe.getIndex());
                }
            }
            // LINK CHANGE
            else if (pe.getClass().equals(LinkChangeEvent.class)) {
                getEditProfile().add((LinkChangeEvent) pe);
            }
            // ZONE ADD
            else if (pe.getClass().equals(ZoneAddEvent.class)) {
                ZoneAddEvent zae = (ZoneAddEvent) pe;
                if (zae.getZone().intValue() != 1) // only create remotely if it is not after the default zone of the voice
                    try {
                        remote.getVoiceContext().cmd_newZone(zae.getIndex(), zae.getVoice());
                    } catch (RemoteUnreachableException e) {
                        handleRemotingException(e, pe.getIndex());
                    } catch (RemoteMessagingException e) {
                        handleRemotingException(e, pe.getIndex());
                    }
            }
            // ZONE REMOVE
            else if (pe.getClass().equals(ZoneRemoveEvent.class)) {
                ZoneRemoveEvent zre = (ZoneRemoveEvent) pe;
                updatePreset(zre.getIndex());
                try {
                    remote.getZoneContext().cmd_delete(zre.getIndex(), zre.getVoice(), zre.getZone());
                } catch (RemoteUnreachableException e) {
                    handleRemotingException(e, pe.getIndex());
                } catch (RemoteMessagingException e) {
                    handleRemotingException(e, pe.getIndex());
                }
            }
        }
        return false;
    }

    public Object handleRequest(PresetRequestEvent pe) {
        // PRESET INITIALIZATION STATUS REQUEST
        if (pe.getClass().equals(PresetRequestInitializationStatusEvent.class)) {
            PresetRequestInitializationStatusEvent pris = (PresetRequestInitializationStatusEvent) pe;
            PresetInitializationMonitor pim = (PresetInitializationMonitor) presetInitializationMonitors.get(pe.getIndex());
            if (pim != null)
                return new Double(pim.getStatus());
            else
                //return new Double(PresetDatabase.STATUS_INITIALIZED);
                return new Double(0);
        }
        // PRESET NAME REQUEST
        else if (pe.getClass().equals(PresetNameRequestEvent.class)) {
            try {
                try {
                    return remote.getPresetContext().req_name(pe.getIndex());
                } catch (RemoteUnreachableException e) {
                    handleRemotingException(e, pe.getIndex());
                } catch (RemoteMessagingException e) {
                    handleRemotingException(e, pe.getIndex());
                    e.printStackTrace();
                } catch (RemoteDeviceDidNotRespondException e) {
                    handleRemotingException(e, pe.getIndex());
                    e.printStackTrace();
                }
            } finally {
            }
        }
        // VOICE PARAMETERS REQUEST
        else if (pe.getClass().equals(VoiceParametersRequestEvent.class)) {
            VoiceParametersRequestEvent vpre = (VoiceParametersRequestEvent) pe;
            try {
                synchronized (remote) {
                    remote.getEditLoader().selVoice(vpre.getIndex(), vpre.getVoice()).dispatch();
                    return Arrays.asList(remote.getParameterContext().req_prmValues(vpre.getParameters(), false));
                }
            } catch (RemoteUnreachableException e) {
                handleRemotingException(e, pe.getIndex());
            } catch (RemoteMessagingException e) {
                handleRemotingException(e, pe.getIndex());
            } catch (RemoteDeviceDidNotRespondException e) {
                handleRemotingException(e, pe.getIndex());
            }
        }
        // PRESET PARAMETERS REQUEST
        else if (pe.getClass().equals(PresetParametersRequestEvent.class)) {
            PresetParametersRequestEvent ppre = (PresetParametersRequestEvent) pe;
            try {
                synchronized (remote) {
                    remote.getEditLoader().selPreset(ppre.getIndex()).dispatch();
                    return Arrays.asList(remote.getParameterContext().req_prmValues(ppre.getParameters(), false));
                }
            } catch (RemoteUnreachableException e) {
                handleRemotingException(e, pe.getIndex());
            } catch (RemoteMessagingException e) {
                handleRemotingException(e, pe.getIndex());
            } catch (RemoteDeviceDidNotRespondException e) {
                handleRemotingException(e, pe.getIndex());
            }
        }
        // PRESET DUMP REQUEST
        else if (pe.getClass().equals(PresetDumpRequestEvent.class)) {
            PresetInitializationMonitor mon = new PresetInitializationMonitor(pe.getIndex(), ceh);
            presetInitializationMonitors.put(pe.getIndex(), mon);
            try {
                updatePreset(pe.getIndex());
                try {
                    final ByteArrayInputStream bais = remote.getPresetContext().req_dump(pe.getIndex(), mon);
                    return new PresetDumpResult() {
                        public ByteArrayInputStream getDump() {
                            return bais;
                        }

                        public boolean isEmpty() {
                            return false;
                        }
                    };
                } catch (RemoteUnreachableException e) {
                    handleRemotingException(e, pe.getIndex());
                } catch (RemoteMessagingException e) {
                    handleRemotingException(e, pe.getIndex());
                    e.printStackTrace();
                } catch (RemoteDeviceDidNotRespondException e) {
                    handleRemotingException(e, pe.getIndex());
                    e.printStackTrace();
                } catch (EmptyException e) {
                    return new PresetDumpResult() {
                        public ByteArrayInputStream getDump() {
                            return null;
                        }

                        public boolean isEmpty() {
                            return true;
                        }
                    };
                }
            } finally {
                presetInitializationMonitors.remove(pe.getIndex());
            }
        }
        return null;
    }

    public void setEventHandler(ManageableContentEventHandler ceh) {
        this.ceh = ceh;
    }
}
