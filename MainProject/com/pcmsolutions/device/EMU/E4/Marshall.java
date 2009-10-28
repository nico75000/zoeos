package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.comms.FinalMidiMessage;
import com.pcmsolutions.comms.SysexTransactionRecord;
import com.pcmsolutions.gui.ProgressSession;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.system.ZExternalDevice;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.AbstractZDevice;
import com.pcmsolutions.device.EMU.E4.remote.Remotable;
import com.pcmsolutions.device.EMU.E4.remote.RemoteFactory;

import java.io.File;
import java.util.prefs.Preferences;

/**
 * @author  pmeehan
 */
public class Marshall implements com.pcmsolutions.device.DeviceMarshall {
    private static final com.pcmsolutions.device.EMU.E4.remote.EMU_E4_IRM filter = new com.pcmsolutions.device.EMU.E4.remote.EMU_E4_IRM();

    /** Creates a new instance of ZDeviceManager  */
    public Marshall() {

    }

    public boolean understandsClass(Class c) {
        if (c == SysexTransactionRecord.class)
            return true;

        return false;
    }

    public ZExternalDevice tryIdentify(Object o) throws IllegalArgumentException {
        if (o instanceof SysexTransactionRecord) {
            SysexTransactionRecord str = (SysexTransactionRecord) o;
            FinalMidiMessage msg = str.getReply();
            com.pcmsolutions.device.EMU.E4.remote.EMU_E4_IRM e = (com.pcmsolutions.device.EMU.E4.remote.EMU_E4_IRM) filter.filter(msg);
            if (e != null) {
                Preferences devicePrefs = Preferences.userNodeForPackage(E4Device.class.getClass()).node("device").node(e.toString());
                Preferences remotePrefs = Preferences.userNodeForPackage(Remotable.class.getClass()).node("remote").node(e.toString());
                RemotePreferences rprefs = new RemotePreferences(remotePrefs, new Double(e.getVersion()).doubleValue() >= DeviceContext.BASE_ULTRA_VERSION);
                DevicePreferences dprefs = new DevicePreferences(devicePrefs);

                com.pcmsolutions.device.EMU.E4.remote.Remotable remote = RemoteFactory.createRemotable(e, str.getInDeviceInfo(), str.getOutDeviceInfo(), rprefs);

                File lastBank = SessionExternalization.getLastSessionFileForDevice(remote);
                boolean reloadFailed = false;
                if (!Zoeos.getInstance().getDeviceManager().isDuplicate(e) && lastBank.exists()) {
                    boolean reload = false;
                    if (dprefs.ZPREF_sessionRestoreMode.getValue().equals(dprefs.SESSION_RESTORE_MODE_ASK)) {
                        reload = UserMessaging.askYesNo("Reload previous session on " + remote.getName() + "?");
                    } else if (dprefs.ZPREF_sessionRestoreMode.getValue().equals(dprefs.SESSION_RESTORE_MODE_ALWAYS))
                        reload = true;
                    if (reload) {
                        ProgressSession ps = Zoeos.getInstance().getProgressSession(remote.makeDeviceProgressTitle("Loading bank from previous session"), 100);
                        ps.setIndeterminate(true);
                        try {
                            SessionExternalization.DeviceSession snap = SessionExternalization.loadDeviceSession(lastBank);
                            E4Device device = snap.getDevice();
                            device.setRemote(remote);
                            device.setPreferences(dprefs);
                            device.getStateMachineHelper().setReason("Restoring previous session on device");
                            return device;
                        } catch (SessionExternalization.ExternalizationException e1) {
                            e1.printStackTrace();
                            reloadFailed = true;
                        } finally {
                            ps.end();
                        }
                    }
                }
                return new E4Device(remote, dprefs);
            } else
                throw new IllegalArgumentException("Illegal Class passed to tryIdentify.");
        }
        return null;
    }
}
