package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.gui.desktop.DesktopElement;

import java.io.*;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.*;

/**
 * User: paulmeehan
 * Date: 28-Jan-2004
 * Time: 01:57:14
 */
public class SessionExternalization {
    public static final String LAST_SESSION_FILENAME = "last";
    public static final String SESSION_EXT = "zsession_e4";
    public static final String SESSION_CONTENT_ENTRY = "e4_session_contents";

    public static DeviceSession makeDeviceSession(E4Device device) {
        return new Impl_DeviceSession(device);
    }

    protected interface DeviceSession {
        public E4Device getDevice();

        public RomAndFlash getRomAndFlash();

        public interface RomAndFlash {
            public Map getRomMap();

            public Map getFlashMap();
        }
    }

    private static class Impl_DeviceSession implements DeviceSession, Serializable {
        private E4Device device;
        private Map romMap = new HashMap();
        private Map flashMap = new HashMap();

        public Impl_DeviceSession(E4Device device) {
            this.device = device;

            for (Iterator i = device.presetDB.p2pobj.entrySet().iterator(); i.hasNext();) {
                Map.Entry me = (Map.Entry) i.next();
                if (((Integer) me.getKey()).intValue() > DeviceContext.MAX_USER_PRESET)
                    flashMap.put(me.getKey(), me.getValue());
            }
            for (Iterator i = device.sampleDB.s2sobj.entrySet().iterator(); i.hasNext();) {
                Map.Entry me = (Map.Entry) i.next();
                if (((Integer) me.getKey()).intValue() > DeviceContext.MAX_USER_SAMPLE)
                    romMap.put(me.getKey(), me.getValue());
            }
        }

        public E4Device getDevice() {
            return device;
        }

        private static class Impl_RomAndFlash implements RomAndFlash, Serializable {
            private Map romMap;
            private Map flashMap;

            public Impl_RomAndFlash(Map romMap, Map flashMap) {
                this.romMap = romMap;
                this.flashMap = flashMap;
            }

            public Map getRomMap() {
                return romMap;
            }

            public Map getFlashMap() {
                return flashMap;
            }
        }

        public DeviceSession.RomAndFlash getRomAndFlash() {
            return new Impl_RomAndFlash(romMap, flashMap);
        }
    }

    public static class ExternalizationException extends Exception {
        public ExternalizationException(String message) {
            super(message);
        }
    }

    protected static void saveAsLastSession(DeviceSession snap) throws IOException {
        try {
            saveSession(snap, getLastSessionFileForDevice(snap.getDevice()));
        } catch (IOException e) {
            getLastSessionFileForDevice(snap.getDevice()).delete();
            throw e;
        }
    }

    protected static File getLastSessionFileForDevice(DeviceContext device) {
        return new File(device.getDeviceLocalDir(), LAST_SESSION_FILENAME + "." + SESSION_EXT);
    }
     protected static File getLastSessionFileForDevice(Remotable r) {
        return new File(r.getDeviceLocalDir(), LAST_SESSION_FILENAME + "." + SESSION_EXT);
    }
    protected static void saveSession(DeviceSession session, File file) throws IOException {
        if (!(session instanceof Impl_DeviceSession))
            session = new Impl_DeviceSession(session.getDevice());

        ZipOutputStream zos = null;
        ObjectOutputStream oos = null;
        zos = new ZipOutputStream(new FileOutputStream(file));
        zos.setMethod(ZipOutputStream.DEFLATED);
        zos.setLevel(Deflater.BEST_SPEED);
        zos.putNextEntry(new ZipEntry(SESSION_CONTENT_ENTRY));
        oos = new ObjectOutputStream(zos);
        oos.writeObject(session.getRomAndFlash());
        oos.writeObject(session);
        oos.close();
    }

     public static DeviceSession.RomAndFlash loadDeviceRomAndFlash(File f) throws ExternalizationException {
        ZipInputStream zis;
        ZipEntry ze;
        ObjectInputStream ois;
        try {
            zis = new ZipInputStream(new FileInputStream(f));
            ze = zis.getNextEntry();
            if (ze == null)
                throw new ExternalizationException("Invalid device session file");
            ois = new ObjectInputStream(zis);

            // read just RomAndFlash
            Object o = ois.readObject();

            ois.close();
            if (!(o instanceof DeviceSession.RomAndFlash))
                throw new ExternalizationException("Invalid device session file");
            return (DeviceSession.RomAndFlash) o;
        } catch (FileNotFoundException e) {
            throw new ExternalizationException(e.getMessage());
        } catch (IOException e) {
            throw new ExternalizationException(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new ExternalizationException(e.getMessage());
        }
    }
    public static DeviceSession loadDeviceSession(File f) throws ExternalizationException {
        ZipInputStream zis;
        ZipEntry ze;
        ObjectInputStream ois;
        try {
            zis = new ZipInputStream(new FileInputStream(f));
            ze = zis.getNextEntry();
            if (ze == null)
                throw new ExternalizationException("Invalid device session file");
            ois = new ObjectInputStream(zis);
            // read RomAndFlash
            Object o = ois.readObject();
            // read DeviceSession
            o = ois.readObject();
            ois.close();
            if (!(o instanceof Impl_DeviceSession))
                throw new ExternalizationException("Invalid device session file");
            return (Impl_DeviceSession) o;
        } catch (FileNotFoundException e) {
            throw new ExternalizationException(e.getMessage());
        } catch (IOException e) {
            throw new ExternalizationException(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new ExternalizationException(e.getMessage());
        }
    }
}
