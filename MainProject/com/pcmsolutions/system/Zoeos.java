/*
 * SeedSystem.java
 *
 * Created on November 29, 2002, 11:12 AM
 */

package com.pcmsolutions.system;

import com.pcmsolutions.comms.ZMidiSystem;
import com.pcmsolutions.device.EMU.E4.gui.TitleProvider;
import com.pcmsolutions.device.EMU.E4.gui.TitleProviderListener;
import com.pcmsolutions.gui.*;
import com.pcmsolutions.license.LicenseKeyManager;
import com.pcmsolutions.system.tasking.ManageableTicketedQ;
import com.pcmsolutions.system.tasking.QueueFactory;
import com.pcmsolutions.system.tasking.TicketRunnable;
import com.pcmsolutions.system.threads.ZDefaultThread;
import sun.misc.Perf;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Date;
import java.util.Vector;
import java.util.prefs.Preferences;


/**
 * @author pmeehan
 */

public class Zoeos implements ZDisposable, TitleProvider {
    protected static final Impl_ZDeviceEventHandler zde_handler;
    private static final String ZOEOS_LOCAL_USER_DIRECTORY = "ZoeOS Local";
    private static final ZoeosPreferences zoeosPrefs = new ZoeosPreferences();
    private final ManageableTicketedQ systemQ = QueueFactory.createTicketedQueue(this, "ZoeOS system queue", 6);

    {
        systemQ.start();
    }

    static {
        ZoeosPreferences.ZPREF_zoeosUses.offsetValue(1);
        ZoeosPreferences.ZPREF_zoeosVersionUses.offsetValue(1);
    }

    static {
        zde_handler = new Impl_ZDeviceEventHandler();
        zde_handler.start();
    }

    public static final boolean useAntiAlias = false;
    public static final boolean isMuon = true;
    private static volatile boolean isUnlicensed = false;
    private static volatile boolean isEvaluation = false;

    private static synchronized void setUnlicensed(boolean isUnlicensed) {
        if (isUnlicensed) {
            if (unlicensedThread == null) {
                unlicensedThread = new UnlicensedThread();
                unlicensedThread.start();
            }
        } else {
            if (unlicensedThread != null) {
                unlicensedThread.kill();
                unlicensedThread = null;
            }
        }
        Zoeos.isUnlicensed = isUnlicensed;
        makeVersionStr();
    }

    public static boolean isUnlicensed() {
        return isUnlicensed;// || isEvaluation;
    }

    public static boolean isEvaluation() {
        return isEvaluation;
    }

    private static UnlicensedThread unlicensedThread;
    private static final long nagInterval = 480000;

    private static class UnlicensedThread extends ZDefaultThread {
        public UnlicensedThread() {
            super("Unlicensed thread");
        }

        private volatile boolean run = true;

        public void runBody() {
            while (run) {
                double r = Math.random();
                if (r < 0.1)
                    r = 0.1;  // not too referencedClose to each other
                try {
                    Thread.sleep(((long) (r * nagInterval)));
                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {
                            public void run() {
                                JOptionPane.showMessageDialog(ZoeosFrame.getInstance(), "This is an UNLICENSED product. Please visit  http://www.zuonics.com  to purchase ZoeOS");
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

    public static final double version = 1.4;
    public static String versionStr;
    public static String aboutMessage;

    private final static boolean isBeta = false;
    private final static int betaVersion = 1;

    static {
        makeVersionStr();
    }

    private static void makeVersionStr() {
        versionStr = "ZoeOS" + "  v" + version + (isEvaluation() ? "e " : "") + (isBeta ? " BETA " + betaVersion : "") + (isUnlicensed() ? "(Unlicensed)" : "");
        aboutMessage = versionStr + lineSeperator +
                "(c) 2009 Open source version by Zuonics Ltd and Paul Meehan" + lineSeperator +
                "with help from Gareth Pidgeon";
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

        System.out.println("Date: " + new Date());
        System.out.println("ZoeOS start time: " + System.currentTimeMillis());
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
                if (LicenseKeyManager.getLoadForType(LicenseKeyManager.zoeosProduct, LicenseKeyManager.fullType, version) == 0 || isEvaluation()) {
                    if (!isUnlicensed()) {
                        setUnlicensed(true);
                        new FlashMsg(ZoeosFrame.getInstance(), 10000, 500, FlashMsg.colorInfo, "DEMO PRODUCT ACTIVE");
                    }
                } else {
                    if (isUnlicensed()) {
                        setUnlicensed(false);
                        new FlashMsg(ZoeosFrame.getInstance(), 10000, 500, FlashMsg.colorInfo, "FULL PRODUCT ACTIVE");
                    }
                }
            }
        });

        if (LicenseKeyManager.getLoadForType(LicenseKeyManager.zoeosProduct, LicenseKeyManager.fullType, version) == 0 || isEvaluation()) {
            setUnlicensed(true);
        }
        //KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(new FocusChangeListener() {
        //});

        /*KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(new KeyEventPostProcessor(){
            public boolean postProcessKeyEvent(KeyEvent e) {
                return false;
            }
        });*/
        //KeyboardFocusManager.getCurrentKeyboardFocusManager().
    }

    private Zoeos() {
        zdm = new Impl_ZDeviceManager();
    }

    public ManageableTicketedQ getSystemQ() {
        return systemQ;
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

    public ProgressSession getProgressSession(String title, int max, boolean allowCancel) {
        return ZoeosFrame.getInstance().getProgressSession(title, max, allowCancel);
    }

    public ProgressSession getProgressSession(String title, int max) {
        return ZoeosFrame.getInstance().getProgressSession(title, max, false);
    }

    public static long getZoeosTime() {
        long t = ((perf.highResCounter() * 1000) / zoeosTickFreq) - zoeosStartTime;
        return t;
    }

    public static long zoeosTicks2Time(long ticks) {
        long t = ((ticks * 1000) / zoeosTickFreq);
        return t;
    }

    public ZDeviceManager getDeviceManager() {
        return zdm;
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

    public static String getLineSeperator() {
        return lineSeperator;
    }

    private static final long disposalWaitTime = 30000;

    public void zDispose() {
        try {
            Preferences.systemRoot().flush();
            Preferences.userRoot().flush();
            TempFileManager.cleanTempDirectory(false);

            if (unlicensedThread != null)
                unlicensedThread.kill();
            zdm.revokeDevices("ZoeOS shutdown").post();

            systemQ.getSendableTicket(new TicketRunnable() {
                public void run() throws Exception {
                    zdm.zDispose();
                    ZMidiSystem.getInstance().zDispose();
                }
            }, "ZoeOS disposal").send(disposalWaitTime);

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
            zoeosIcon = new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
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
                        for (int i = 0, j = zDeviceListeners.size(); i < j; i++)
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
