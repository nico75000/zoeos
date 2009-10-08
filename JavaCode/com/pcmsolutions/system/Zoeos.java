/*
 * SeedSystem.java
 *
 * Created on November 29, 2002, 11:12 AM
 */

package com.pcmsolutions.system;

import com.pcmsolutions.comms.MidiSystemFacade;
import com.pcmsolutions.comms.SysexTransactionRecord;
import com.pcmsolutions.device.EMU.E4.gui.TitleProvider;
import com.pcmsolutions.device.EMU.E4.gui.TitleProviderListener;
import com.pcmsolutions.device.MidiDeviceMarshall;
import com.pcmsolutions.gui.*;
import com.pcmsolutions.license.LicenseKeyManager;
import com.pcmsolutions.system.threads.ZDefaultThread;
import sun.misc.Perf;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

//import sunex.javakernel.common.SecurityPreferences;


/**
 *
 * @author  pmeehan
 */

public class Zoeos implements ZDisposable, TitleProvider {
    protected static final Impl_ZDeviceEventHandler zde_handler;
    public static final ThreadGroup defaultTG = new ThreadGroup("Default Group");
    public static final ThreadGroup DBModifyTG = new ThreadGroup("Database Modifier Group");
    public static final ThreadGroup BackgroundRemoterTG = new ThreadGroup("Background Remoting Group");
    public static final ThreadGroup RemoteDumpTG = new ThreadGroup("Remote Dump Group");

    private static final String ZOEOS_LOCAL_USER_DIRECTORY = "ZoeOS Local";

    private static final ZoeosPreferences zoeosPrefs = new ZoeosPreferences();

    static {
        ZoeosPreferences.ZPREF_zoeosUses.offsetValue(1);
    }

    static {
        zde_handler = new Impl_ZDeviceEventHandler();
        zde_handler.start();
    }

    public static final boolean isMuon = true;

    private static volatile boolean isDemo;

    static {
        setDemo(false);
    }

    private static synchronized void setDemo(boolean isDemo) {
        if (isDemo) {
            if (demoThread == null) {
                demoThread = new DemoThread();
                demoThread.start();
            }
        } else {
            if (demoThread != null) {
                demoThread.kill();
                demoThread = null;
            }
        }
        Zoeos.isDemo = isDemo;
        makeVersionStr();
    }

    public static boolean isDemo() {
        return isDemo;
    }

    private static DemoThread demoThread;
    private static long nagInterval = 100000;//10000000;

    private static class DemoThread extends ZDefaultThread {
        public DemoThread() {
            super("Demo Thread");
        }

        private volatile boolean run = true;

        public void run() {
            while (run) {
                double r = Math.random();
                if (r < 0.1)
                    r = 0.1;  // not too closed to each other
                try {
                    Thread.sleep(((long) (r * nagInterval)));
                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {
                            public void run() {
                                JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), "This is a DEMO. Please visit  http://www.zuonics.com  to purchase ZoeOS");
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                }
            }
        }

        public void kill() {
            run = false;
            this.interrupt();
        }
    }

    // LOGO
    public final static Color logoColor = new Color(50, 50, 50, 50);
    public final static Font logoFont = new Font("Superpoint", Font.ITALIC, 20);
    public final static Font logoSmallFont = new Font("Superpoint", Font.ITALIC, 10);
    public final static String logoStr = "Zuonics";

    // SYSTEM OBJECTS
    private final static Zoeos instance;

    private final static Perf perf = sun.misc.Perf.getPerf();
    public final static long zoeosTickFreq = sun.misc.Perf.getPerf().highResFrequency();
    private final static long zoeosStartTime;
    private final static long zoeosStartTick;

    static {
        zoeosStartTick = perf.highResCounter();
        zoeosStartTime = (zoeosStartTick * 1000) / zoeosTickFreq;
    }

    // private final static String lineSeperator = System.getProperty("lineStroke.seperator");
    public final static String lineSeperator = "\n";
    private static ZoeosFrame zoeosFrame;

    public static final double version = 1.2;
    public static String versionStr;
    public static String aboutMessage;

    static {
        makeVersionStr();
    }

    private static void makeVersionStr() {
        versionStr = "ZoeOS" + "  v" + version + (isDemo ? " Demo" : "");
        aboutMessage = versionStr + lineSeperator +
                "(c) Zuonics Ltd and Paul Muon" + lineSeperator + "2003";
    }

    private static String homeDir;

    // DEVICE MANAGER
    private final ZDeviceManager zdm;

    // EVENTS
    private final Vector listeners = new Vector();
    //  private final ZoeosEventHandler zeh = new Impl_ZoeosEventHandler(listeners);

    // PROPERTIES
    public final static String preferenceFieldSeperator = ";";

    static {
        homeDir = System.getProperty("user.home");
        getZoeosLocalDir().mkdir();

        System.out.println("ZoeOS stateStart time: " + System.currentTimeMillis());
        System.out.println("ZoeOS tick freq: " + zoeosTickFreq);
        instance = new Zoeos();
        /* if (isDemo) {
             LicenseKeyManager.clearLicenseKeys();
             try {
                 if (isDemo)
                     LicenseKeyManager.addLicenseKey(LicenseKeyManager.parseKey(P200083.generateInternalLicense(LicenseKeyManager.zoeosProduct, LicenseKeyManager.demoPrefix + LicenseKeyManager.fullType, "DEMO_USER")));
                 else
                     LicenseKeyManager.addLicenseKey(LicenseKeyManager.parseKey(P200083.generateInternalLicense(LicenseKeyManager.zoeosProduct, LicenseKeyManager.fullType, "DEMO_USER")));
             } catch (InvalidLicenseKeyException e) {
                 e.printStackTrace();
             } catch (NoSuchAlgorithmException e) {
                 e.printStackTrace();
             }
         }*/

        LicenseKeyManager.addLicenseKeyListener(new LicenseKeyManager.LicenseKeyListener() {
            public void licenseKeysChanged() {
                if (LicenseKeyManager.getLoadForType(LicenseKeyManager.zoeosProduct, LicenseKeyManager.fullType, version) == 0) {
                    if (!isDemo()) {
                        setDemo(true);
                        new FlashMsg(ZoeosFrame.getInstance(), 3000, 300, FlashMsg.colorInfo, "DEMO PRODUCT ACTIVE");
                    }
                } else {
                    if (isDemo()) {
                        setDemo(false);
                        new FlashMsg(ZoeosFrame.getInstance(), 3000, 300, FlashMsg.colorInfo, "FULL PRODUCT ACTIVE");
                    }
                }
            }
        });

        if (LicenseKeyManager.getLoadForType(LicenseKeyManager.zoeosProduct, LicenseKeyManager.fullType, version) == 0)
            setDemo(true);

        //KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(new FocusChangeListener() {
        //});

        /*KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(new KeyEventPostProcessor(){
            public boolean postProcessKeyEvent(KeyEvent e) {
                return false;
            }
        });*/
        //KeyboardFocusManager.getCurrentKeyboardFocusManager().
    }

    static class FocusChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            Component oldComp = (Component) evt.getOldValue();
            Component newComp = (Component) evt.getNewValue();
            if ("focusOwner".equals(evt.getPropertyName())) {
                if (oldComp == null) {
                    // the newComp component gained the focus
                    System.out.println(newComp.getClass() + " gained focus");
                } else {
                    // the oldComp component lost the  mousefocus
                    System.out.println(oldComp.getClass() + " lost focus");
                }
            } else if ("focusedWindow".equals(evt.getPropertyName())) {
                if (oldComp == null) {
                    // the newComp window gained the focus
                    System.out.println(newComp.getClass() + " gained focus");
                } else {
                    // the oldComp window lost the focus
                    System.out.println(oldComp.getClass() + " lost focus");
                }
            }
        }
    }

    private Zoeos() {
        zdm = new Impl_ZDeviceManager();
    }

    public static void addZDeviceListener(ZDeviceListener zdl) {
        zde_handler.addZDeviceListener(zdl);
    }

    public static java.io.File getHomeDir() {
        return new java.io.File(homeDir);
    }

    public static java.io.File getZoeosLocalDir() {
        return new java.io.File(Zoeos.getHomeDir(), ZOEOS_LOCAL_USER_DIRECTORY);
    }

    public static void removeZDeviceListener(ZDeviceListener zdl) {
        zde_handler.removeZDeviceListener(zdl);
    }

    public static void postZDeviceEvent(ZDeviceEvent ev) {
        zde_handler.postZDeviceEvent(ev);
    }

    public static JLabel getStoppedLabel() {
        return new DeviceStoppedLabel();
    }

    public static JLabel getRemovedLabel() {
        return new DeviceRemovedLabel();
    }

    public static JLabel getPendingLabel() {
        return new DevicePendingLabel();
    }

    public static long getZoeosStartTime() {
        return zoeosStartTime;
    }

    public static long getZoeosTicks() {
        return perf.highResCounter() - zoeosStartTick;
    }

    /* public static long getZoeosTicksForMs() {
         long t = ((perf.highResCounter() * 1000) / zoeosTickFreq) - zoeosStartTime;
         return t;
     } */

    public static long getZoeosTime() {
        long t = ((perf.highResCounter() * 1000) / zoeosTickFreq) - zoeosStartTime;
        return t;
    }

    public static long getZoeosTimeForZoeosTicks(long ticks) {
        long t = ((ticks * 1000) / zoeosTickFreq);
        return t;
    }

    public ZDeviceManager getDeviceManager() {
        return zdm;
    }

    public void setZoeosFrame(ZoeosFrame mainFrame) {
        this.zoeosFrame = mainFrame;
    }

    public String toString() {
        return "==ZoeOS==";
    }

    public static Zoeos getInstance() {
        return instance;
    }

    public static ZoeosPreferences getZoeosPrefs() {
        return zoeosPrefs;
    }

    public void addZoeosListener(ZoeosListener zl) {
        listeners.add(zl);
    }

    public void removeZoeosListener(ZoeosListener zl) {
        listeners.remove(zl);
    }

    public void beginProgressElement(Object e, String title, int maximum) {
        if (zoeosFrame != null)
            zoeosFrame.beginProgressElement(e, title, maximum);
    }

    public void endProgressElement(Object e) {
        if (zoeosFrame != null)
            zoeosFrame.endProgressElement(e);
    }

    public void updateProgressElement(Object e, int status) {
        if (zoeosFrame != null)
            zoeosFrame.updateProgressElement(e, status);
    }

    public void updateProgressElement(Object e) {
        if (zoeosFrame != null)
            zoeosFrame.updateProgressElement(e);
    }

    public void updateProgressElement(Object e, String title) {
        if (zoeosFrame != null)
            zoeosFrame.updateProgressElement(e, title);
    }

    public void setProgressElementIndeterminate(Object e, boolean b) {
        if (zoeosFrame != null)
            zoeosFrame.setProgressElementIndeterminate(e, b);
    }

    public void updateProgressElementTitle(Object e, String title) {
        if (zoeosFrame != null)
            zoeosFrame.updateProgressElementTitle(e, title);
    }

    public ProgressMultiBox getCustomProgress(String title) {
        if (zoeosFrame == null)
            return null;
        return zoeosFrame.getCustomProgress(title, null);
    }

    public static String getLineSeperator() {
        return lineSeperator;
    }

    public void zDispose() {
        try {
            if (demoThread != null)
                demoThread.kill();
            zdm.revokeDevicesNonThreaded("Shutdown");
            zdm.zDispose();
            Preferences.systemRoot().flush();
            Preferences.userRoot().flush();
            TempFileManager.cleanTempDirectory(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getTitle() {
        return "ZoeOS";
    }

    public String getReducedTitle() {
        return getTitle();
    }

    public void addTitleProviderListener(TitleProviderListener tpl) {
    }

    public void removeTitleProviderListener(TitleProviderListener tpl) {
    }

    private static Icon zoeosIcon;

    static {
        URL url = ZoeosFrame.class.getResource("/zoeosFrameIcon.gif");
        if (url != null)
            zoeosIcon = new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(16,16, Image.SCALE_DEFAULT));
    }

    public Icon getIcon() {
        return zoeosIcon;
    }

    public String getToolTipText() {
        return "ZoeOS";
    }

    public class Impl_ZoeosEventHandler implements ZoeosEventHandler {
        private Vector listeners;

        public Impl_ZoeosEventHandler(Vector listeners) {
            this.listeners = listeners;
        }

        public void addZoeosListener(ZoeosListener zl) {
            listeners.add(zl);
        }

        public void removeZoeosListener(ZoeosListener zl) {
            listeners.remove(zl);
        }

        public void postZoeosEvent(final ZoeosEvent ev) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    synchronized (listeners) {
                        for (int i = 0, j = listeners.size(); i < j; i++)
                            try {
                                ev.fire((ZoeosListener) listeners.get(i));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                }
            });
        }

        public void stop() {
        }
    } // end event handler

    private interface ZoeosEventHandler {
        public void postZoeosEvent(ZoeosEvent ev);
    }

    private class Impl_ZDeviceManager implements ZDeviceManager, ZDeviceListener {
        // DEVICE HANDLING
        private final Vector unidentifiedMessages = new Vector();
        private final Hashtable duplicateDevices = new Hashtable();
        private final Hashtable devices = new Hashtable();
        //private volatile boolean startBarrier = false;
        private final Vector revokedDevices = new Vector();
        private final Vector listeners = new Vector();

        public Impl_ZDeviceManager() {
            Zoeos.addZDeviceListener(this);
        }

        public Impl_ZDeviceManager(List startupHuntReplies) {
            processDeviceHunt(startupHuntReplies);
        }

        public void performHunt() {
            //Thread t = new Thread(){
            // public void run(){
            MidiSystemFacade.DeviceHunter hunter = MidiSystemFacade.getDeviceHunter();
            List replies = hunter.hunt(127); // passing all devices ID
            processDeviceHunt(replies);
            //        }
            //        };
            //      t.stateStart();
        }

        public ZExternalDevice getDeviceMatchingIdentityMessageString(String imt) {
            synchronized (devices) {
                Map.Entry me;
                for (Iterator i = devices.entrySet().iterator(); i.hasNext();) {
                    me = (Map.Entry) i.next();
                    if (me.getValue().toString().equals(imt) && me.getKey() instanceof ZExternalDevice)
                        return (ZExternalDevice) me.getKey();
                }
            }
            return null;
        }

        private boolean checkLicensed(LicensedEntity le) {
            Map m = (Map) devices.clone();
            int count = 0;
            // how many others of this type?
            for (Iterator i = m.keySet().iterator(); i.hasNext();) {
                Object d = i.next();
                if (d instanceof LicensedEntity) {
                    if (((LicensedEntity) d).getLicenseProduct().equals(le.getLicenseProduct()) && ((LicensedEntity) d).getLicenseType().equals(le.getLicenseType()))
                        count++;
                }
            }
            // get max quantity for this type
            //int q = LicenseKeyManager.getLoadForType(LicenseKeyManager.demoPrefix + le.getLicenseProduct(), LicenseKeyManager.demoPrefix + le.getLicenseType(), Zoeos.version);
            int q = LicenseKeyManager.getLoadForType(le.getLicenseProduct(), le.getLicenseType(), Zoeos.version);

            if (q == 0 || count >= q)
            /*if (isDemo) {
                try {
                    LicenseKeyManager.addLicenseKey(LicenseKeyManager.parseKey(P200083.generateInternalLicense(le.getLicenseProduct(), LicenseKeyManager.demoPrefix + le.getLicenseType(), "DEMO_USER")));
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidLicenseKeyException e) {
                    e.printStackTrace();
                }
            } else */
                return false;

            return true;
        }

        private void processDeviceHunt(final List replies) {
            /* if (isDemo) {
                 if (LicenseKeyManager.getLoadForType(LicenseKeyManager.zoeosProduct, LicenseKeyManager.demoPrefix + LicenseKeyManager.fullType, Zoeos.version) == 0) {
                     JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), "Could not process results of device hunt as there is no ZoeOS Seat license present on the system");
                     return;
                 }
             } else if (LicenseKeyManager.getLoadForType(LicenseKeyManager.zoeosProduct, LicenseKeyManager.fullType, Zoeos.version) == 0) {
                 JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), "Could not process results of device hunt as there is no ZoeOS license present on the system");
                 return;
             }
              */

            if (replies == null)
                return;
            final MidiDeviceMarshall m = MidiDeviceMarshall.getInstance();

            System.out.println(Zoeos.getZoeosTime() + ": " + "DEVICE HUNT REPLIES: " + replies.size());
            Thread t = new ZDefaultThread("Device Hunt Results") {
                public void run() {
                    int num = replies.size();
                    int duped = 0;
                    if (num == 0)
                        new FlashMsg(ZoeosFrame.getInstance(), 2500, 500, FlashMsg.colorWarning, "NO DEVICES FOUND DURING HUNT");
                    else {
                        List unlicensed = new ArrayList();
                        for (Iterator i = replies.iterator(); i.hasNext();) {
                            final SysexTransactionRecord str = (SysexTransactionRecord) i.next();
                            ZExternalDevice d = m.tryIdentify(str);
                            if (d != null) {
                                ZExternalDevice dup = getDuplicate(d.getDeviceIdentityMessage());
                                if (dup == null) {
                                    if (!isDemo() && d instanceof LicensedEntity && !checkLicensed((LicensedEntity) d)) {
                                        unlicensed.add(d);
                                        continue;
                                    } else if (isDemo() && /*SecurityPreferences.expired()*/false) {
                                        unlicensed.add(d);
                                        continue;
                                    }

                                    devices.put(d, d.getDeviceIdentityMessage());
                                    //serializeDeviceMarshalling.setSelected(sun.getBoolean(Zoeos.PREF_serializeDeviceMarshalling, true));

                                    if (!ZoeosPreferences.ZPREF_stopHuntAtPending.getValue())
                                    //if (startBarrier == false)
                                        startDevice(d);
                                    else {
                                        Impl_ZDeviceManager.this.firePendingListChanged();
                                        Impl_ZDeviceManager.this.fireStoppedListChanged();
                                        Impl_ZDeviceManager.this.fireStartedListChanged();
                                        ZoeosFrame.getInstance().showDeviceManager();
                                    }
                                } else {
                                    duped++;
                                    duplicateDevices.put(d, dup);
                                    Impl_ZDeviceManager.this.fireDuplicateListChanged();
                                    System.out.println(Zoeos.getZoeosTime() + ": " + "DUPLICATE MIDI DEVICE: " + str.getReply());
                                }
                            } else {
                                unidentifiedMessages.add(str.getReply());
                                Impl_ZDeviceManager.this.fireUnidentifiedListChanged();
                                System.out.println(Zoeos.getZoeosTime() + ": " + "UNRESOLVED MIDI DEVICE: " + str.getReply());
                            }
                        }
                        if (unlicensed.size() > 0) {
                            UserMessaging.showError(unlicensed.size() + (unlicensed.size() == 1 ? " device was" : " devices were") + " not marshalled due to insufficient licensing");
                            ZUtilities.zDisposeCollection(unlicensed);
                            unlicensed.clear();
                        }

                        if (duped == num)
                            new FlashMsg(ZoeosFrame.getInstance(), 2500, 500, FlashMsg.colorWarning, "NO DEVICES MARSHALLED FROM HUNT");
                    }
                }
            };
            t.start();
        }

         public boolean isDuplicate(Object deviceIndentityMessage) {
            assertDevices();
            synchronized (devices) {
                Enumeration e = devices.keys();
                for (; e.hasMoreElements();) {
                    Object next = e.nextElement();
                    if (deviceIndentityMessage.equals(devices.get(next)))
                        return true;
                }

                return false;
            }
        }
        private ZExternalDevice getDuplicate(Object deviceIndentityMessage) {
            assertDevices();
            synchronized (devices) {
                Enumeration e = devices.keys();
                for (; e.hasMoreElements();) {
                    Object next = e.nextElement();
                    if (deviceIndentityMessage.equals(devices.get(next)))
                        return (ZExternalDevice) next;
                }

                return null;
            }
        }

        private void assertDevices() {
            synchronized (devices) {
                Enumeration e = devices.keys();
                for (; e.hasMoreElements();) {
                    Object next = e.nextElement();
                    if (((ZExternalDevice) next).getState() == ZExternalDevice.STATE_REMOVED)
                        devices.remove(next);
                }
            }
        }

        public void startDevice(final ZExternalDevice d) {
            Thread t = new ZDefaultThread() {
                public void run() {
                    setName("StartDeviceThread");
                    taskStartDevice(d);
                }
            };
            t.start();
        }

        private void taskStartDevice(ZExternalDevice d) {
            try {
                if (ZoeosPreferences.ZPREF_serializeDeviceMarshalling.getValue()) {
                    synchronized (Impl_ZDeviceManager.this) {
                        d.startDevice();
                    }
                } else
                    d.startDevice();
            } catch (ZDeviceStartupException e) {
                ZoeosFrame.getInstance().showDeviceManager();
                e.printStackTrace();
            } catch (IllegalStateTransitionException e) {
                ZoeosFrame.getInstance().showDeviceManager();
                e.printStackTrace();
            }
        }

        public void stopDevice(final ZExternalDevice d, final String reason) {
            Thread t = new ZDefaultThread() {
                public void run() {
                    setName("StopDeviceThread");
                    taskStopDevice(d, reason);
                }
            };
            t.start();
        }

        private void taskStopDevice(ZExternalDevice d, String reason) {
            try {
                d.stopDevice(false, reason);
            } catch (IllegalStateTransitionException e) {
                e.printStackTrace();
            }
        }

        public void removeDevice(final ZExternalDevice d, final boolean saveState) {
            Thread t = new ZDefaultThread() {
                public void run() {
                    setName("RemoveDeviceThread");
                    taskRemoveDevice(d, saveState);
                }
            };
            t.start();
        }

        private void taskRemoveDevice(ZExternalDevice d, boolean saveState) {
            try {
                d.removeDevice(saveState);
            } catch (ZDeviceCannotBeRemovedException e) {
                e.printStackTrace();
            } catch (IllegalStateTransitionException e) {
                e.printStackTrace();
            }
        }

        public void revokeDevices() {
            Thread t = new ZDefaultThread() {
                public void run() {
                    setName("RevokeDevicesThread");
                    taskRevokeDevices(null);
                }
            };
            t.start();
        }

        public void revokeDevicesNonThreaded() {
            taskRevokeDevices(null);
        }

        public void revokeDevicesNonThreaded(String reason) {
            taskRevokeDevices(reason);
        }

        private void taskRevokeDevices(String reason) {
            synchronized (revokedDevices) {
                List l = Impl_ZDeviceManager.this.getRunningList();
                int num = l.size();
                ZExternalDevice dev;
                for (int n = 0; n < num; n++) {
                    dev = (ZExternalDevice) l.get(n);
                    revokedDevices.add(dev);
                    try {
                        dev.stopDevice(true, (reason == null ? "Midi Revocation" : reason));
                    } catch (IllegalStateTransitionException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void unrevokeDevices() {
            Thread t = new ZDefaultThread() {
                public void run() {
                    setName("unrevokeDevicesThread");
                    taskUnrevokeDevices();
                }
            };
            t.start();
        }

        private void taskUnrevokeDevices() {
            synchronized (revokedDevices) {
                int num = revokedDevices.size();

                for (int n = 0; n < num; n++)
                    try {
                        ((ZExternalDevice) revokedDevices.get(n)).startDevice();
                    } catch (ZDeviceStartupException e) {
                        e.printStackTrace();
                    } catch (IllegalStateTransitionException e) {
                        e.printStackTrace();
                    }

                revokedDevices.clear();
            }
        }

        /*     public boolean getStartBarrier() {
                 return startBarrier;
             }

             public void setStartBarrier(boolean v) {
                 startBarrier = v;
             }
          */
        public void clearUnidentified() {
            Thread t = new ZDefaultThread("Clear unidentified device messages") {
                public void run() {
                    unidentifiedMessages.clear();
                    fireUnidentifiedListChanged();
                }
            };
            t.start();
        }

        public Map getDuplicateMap() {
            return ((Hashtable) duplicateDevices.clone());
        }

        public void clearDuplicates() {
            Thread t = new ZDefaultThread() {
                public void run() {
                    synchronized (duplicateDevices) {
                        ZUtilities.zDisposeCollection(duplicateDevices.keySet());
                        duplicateDevices.clear();
                    }
                    fireDuplicateListChanged();
                }
            };
            t.start();
        }

        public List getPendingList() {
            return getDeviceList(ZExternalDevice.STATE_PENDING);
        }

        public List getDeviceList(int state) {
            Hashtable devs = (Hashtable) devices.clone();
            ArrayList outList = new ArrayList();
            Enumeration e = devs.keys();
            ZExternalDevice zd;
            for (; e.hasMoreElements();) {
                zd = ((ZExternalDevice) e.nextElement());
                if (zd.getState() == state)
                    outList.add(zd);
            }
            return outList;
        }

        public List getRunningList() {
            return getDeviceList(ZExternalDevice.STATE_RUNNING);
        }

        public List getStoppedList() {
            return getDeviceList(ZExternalDevice.STATE_STOPPED);
        }

        public List getUnidentifiedList() {
            return (List) unidentifiedMessages.clone();

        }

        public void addDeviceManagerListener(ZDeviceManagerListener zdml) {
            listeners.add(zdml);
        }

        public void removeDeviceManagerListener(ZDeviceManagerListener zdml) {
            listeners.remove(zdml);
        }

        public void firePendingListChanged() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    synchronized (listeners) {
                        int size = listeners.size();
                        for (int n = 0; n < size; n++)
                            try {
                                ((ZDeviceManagerListener) listeners.get(n)).pendingListChanged();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                }
            });
        }

        public void fireStartedListChanged() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    synchronized (listeners) {
                        int size = listeners.size();
                        for (int n = 0; n < size; n++)
                            try {
                                ((ZDeviceManagerListener) listeners.get(n)).startedListChanged();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                }
            });
        }

        public void fireStoppedListChanged() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    synchronized (listeners) {
                        int size = listeners.size();
                        for (int n = 0; n < size; n++)
                            try {
                                ((ZDeviceManagerListener) listeners.get(n)).stoppedListChanged();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                }
            });
        }

        public void fireUnidentifiedListChanged() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    synchronized (listeners) {
                        int size = listeners.size();
                        for (int n = 0; n < size; n++)
                            try {
                                ((ZDeviceManagerListener) listeners.get(n)).unidentifiedListChanged();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                }
            });
        }

        public void fireDuplicateListChanged() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    synchronized (listeners) {
                        int size = listeners.size();
                        for (int n = 0; n < size; n++)
                            try {
                                ((ZDeviceManagerListener) listeners.get(n)).duplicateListChanged();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                }
            });
        }

        public void deviceStarted(ZDeviceStartedEvent ev) {
            firePendingListChanged();
            fireStoppedListChanged();
            fireStartedListChanged();
        }

        public void deviceStopped(ZDeviceStoppedEvent ev) {
            fireStartedListChanged();
            fireStoppedListChanged();
        }

        public void devicePending(ZDevicePendingEvent ev) {
            firePendingListChanged();
        }

        public void deviceRemoved(ZDeviceRemovedEvent ev) {
            devices.remove(ev.getDevice());
            System.out.println("Device Manager removed " + ev.getDevice());
            //ev.getDeviceLock().removeZDeviceListener(this);
            fireStartedListChanged();
            fireStoppedListChanged();
            firePendingListChanged();
        }

        public void zDispose() {
            Hashtable devs = (Hashtable) devices.clone();
            Enumeration e = devs.keys();
            ZExternalDevice zd;
            for (; e.hasMoreElements();) {
                zd = ((ZExternalDevice) e.nextElement());
                try {
                    if (zd.getState() != StdStates.STATE_PENDING) {
                        zd.stopDevice(true, "ZoeOS shutdown");
                        zd.removeDevice(true);
                    } else
                        zd.removeDevice(false);
                } catch (IllegalStateTransitionException e1) {
                    e1.printStackTrace();
                } catch (ZDeviceCannotBeRemovedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static class Impl_ZDeviceEventHandler implements ZDeviceEventHandler {
        private final Vector zDeviceListeners = new Vector();
        private final Vector events = new Vector();

        public Impl_ZDeviceEventHandler() {
        }

        public void start() {
            synchronized (events) {
                events.clear();
            }
        }

        public void stop() {
        }

        public void postZDeviceEvent(final ZDeviceEvent ev) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    synchronized (zDeviceListeners) {
                        for (int i = 0,j = zDeviceListeners.size(); i < j; i++)
                            try {
                                ev.fire((ZDeviceListener) zDeviceListeners.get(i));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                }
            });
        }

        public void addZDeviceListener(ZDeviceListener zdl) {
            //System.out.println("ZDE HANDLER added listener " + zdl.toString());
            zDeviceListeners.add(zdl);
        }

        public void removeZDeviceListener(ZDeviceListener zdl) {
            zDeviceListeners.remove(zdl);
        }
    }
}
