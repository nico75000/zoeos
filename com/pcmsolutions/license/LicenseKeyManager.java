package com.pcmsolutions.license;

import javax.swing.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 03-Nov-2003
 * Time: 12:30:38
 * To change this template use Options | File Templates.
 */
public class LicenseKeyManager {
    private static final Vector listeners = new Vector();

    private static String FIELD_SEPERATOR = "-";
    private static final Vector keys = new Vector();
    private final static String zuonicsPassword ="6H7D CD92 KL92 HAPH 10GN XKG9 57D5 KDPW 5S58 8GJE SP2G KS58 GKSI RJFU W2KG GH67";

    public static final String zoeosProduct = "ZoeOS";
    public static final String fullType = "Full";
    //public static final String eosDeviceType = "EosDevice";
    public static final String demoPrefix = "DEMO_";

    public static final int MAX_MINOR_VERSION = 999;

    public static final Preferences prefs = Preferences.userNodeForPackage(LicenseKeyManager.class).node("LicenseKeyManager");
    public static final String PREF_keys = "installedLicenseKeys";
    private static final String keyFieldDelimiter = "$";

    static {
        // load keys from reg
        refreshLicenseKeys();
    }

    public synchronized static void refreshLicenseKeys() {
        keys.clear();
        StringTokenizer st = new StringTokenizer(prefs.get(PREF_keys, ""), keyFieldDelimiter);
        while (st.hasMoreTokens())
            try {
                addLicenseKey(parseKey((String) st.nextToken()));
            } catch (InvalidLicenseKeyException e) {
            }
        fireLicenseKeysChanged();
    }

    public synchronized static void dumpLicenseKeys() {
        String s = "";
        for (Iterator i = keys.iterator(); i.hasNext();)
            s += i.next().toString() + keyFieldDelimiter;
        prefs.put(PREF_keys, s);
    }

    public synchronized static void removeLicenseKey(LicenseKey key) {
        keys.remove(key);
        dumpLicenseKeys();
        fireLicenseKeysChanged();
    }

    public synchronized static void removeLicenseKey(String keyString) {
        for (int i = 0; i < keys.size(); i++) {
            if (keys.get(i).toString().equals(keyString)) {
                keys.remove(i);
                dumpLicenseKeys();
                fireLicenseKeysChanged();
                break;
            }
        }
    }

    public static void addLicenseKey(LicenseKey key) {
        if (key == null)
            return;
        if (!keys.contains(key)) {
            keys.add(key);
            dumpLicenseKeys();
            fireLicenseKeysChanged();
        }
    }

    public static void clearLicenseKeys() {
        keys.clear();
        dumpLicenseKeys();
        fireLicenseKeysChanged();
    }

    public synchronized static void removeKeysByExcludedVersion(double version) {
        LicenseKey k;
        List kc = (Vector) keys.clone();
        for (Iterator i = kc.iterator(); i.hasNext();) {
            k = ((LicenseKey) i);
            if (version < k.getlowVersion() || version > k.getHighVersion())
                keys.remove(k);
        }
        dumpLicenseKeys();
        fireLicenseKeysChanged();
    }

    public synchronized static void removeKeysByType(String type) {
        LicenseKey k;
        List kc = (Vector) keys.clone();
        for (Iterator i = kc.iterator(); i.hasNext();) {
            k = ((LicenseKey) i);
            if (k.getType().equals(type))
                keys.remove(k);
        }
        dumpLicenseKeys();
        fireLicenseKeysChanged();
    }

    public synchronized static void removeKeysByVersion(double version) {
        LicenseKey k;
        List kc = (Vector) keys.clone();
        for (Iterator i = kc.iterator(); i.hasNext();) {
            k = ((LicenseKey) i);
            if (version >= k.getlowVersion() && version <= k.getHighVersion())
                keys.remove(k);
        }
        dumpLicenseKeys();
        fireLicenseKeysChanged();
    }

    public synchronized static int getLoadForKeys(LicenseKey[] keys) {
        int q = 0;
        for (int i = 0; i < keys.length; i++)
            q += keys[i].getLoad();
        return q;
    }

    public static int getLoadForType(String type, double version) {
        return getLoadForKeys(getKeys(zoeosProduct, type, version));
    }

    public static int getLoadForType(String product, String type, double version) {
        return getLoadForKeys(getKeys(product, type, version));
    }

    public synchronized static LicenseKey[] getAllKeys() {
        return (LicenseKey[]) keys.toArray(new LicenseKey[keys.size()]);
    }

    public synchronized static LicenseKey[] getKeys(String type, double version) {
        return getKeys(zoeosProduct, type, version);
    }

    public synchronized static LicenseKey[] getKeys(String product, String type, double version) {
        LicenseKey k;
        List kc = (Vector) keys.clone();
        List tl = new ArrayList();
        for (Iterator i = kc.iterator(); i.hasNext();) {
            k = ((LicenseKey) i.next());
            if (k.getProduct().equals(product) && k.getType().equals(type) && checkVersion(k, version))
                tl.add(k);
        }
        return (LicenseKey[]) tl.toArray(new LicenseKey[tl.size()]);
    }

    /*public static LicenseKey[] getKeys(String product, String type) {
        LicenseKey k;
        List kc = (Vector) keys.clone();
        List tl = new ArrayList();
        for (Iterator i = kc.iterator(); i.hasNext();) {
            k = ((LicenseKey) i);
            if (k.getType().equals(product) && k.getType().equals(type))
                tl.addDesktopElement(k);
        }
        return (LicenseKey[]) tl.toArray(new LicenseKey[tl.size()]);
    }
      */
    public static boolean checkVersion(LicenseKey k, double version) {
        if (version >= k.getlowVersion() && version <= k.getHighVersion())
            return true;
        return false;
    }

    public static LicenseKey parseKey(String key) throws InvalidLicenseKeyException {
        final String f_key = key.trim();
        StringTokenizer st = new StringTokenizer(f_key, FIELD_SEPERATOR);
        try {
            final String product = st.nextToken();
            final String type = st.nextToken();

            final String loadStr = st.nextToken();
            final int load = Integer.parseInt(loadStr);

            final String lowMajorVersionStr = st.nextToken();
            final int lowMajorVersion = Integer.parseInt(lowMajorVersionStr);

            final String lowMinorVersionStr = st.nextToken();
            final double lowMinorVersion = Double.parseDouble("0." + lowMinorVersionStr);

            final String highMajorVersionStr = st.nextToken();
            final int highMajorVersion = Integer.parseInt(highMajorVersionStr);

            final String highMinorVersionStr = st.nextToken();
            final double highMinorVersion = Double.parseDouble("0." + highMinorVersionStr);

            final String regName = st.nextToken();

            final String randomHexStr = st.nextToken();

            final String md5Str = st.nextToken();

            String subKey = f_key.substring(0, f_key.indexOf(md5Str) - 1);

            byte[] md5;
            MessageDigest md = null;
            md = MessageDigest.getInstance("MD5");
            md.update(subKey.getBytes());
            md.update(FIELD_SEPERATOR.getBytes());
            md.update(zuonicsPassword.getBytes());
            md5 = md.digest();
            String testKey = subKey + FIELD_SEPERATOR;
            for (int i = 0; i < md5.length; i++)
                testKey += Integer.toHexString(md5[i]).toUpperCase();

            if (!testKey.equals(f_key))
                throw new InvalidLicenseKeyException("doesn't hash");

            return new LicenseKey() {

                public String getProduct() {
                    return product;
                }

                public String getType() {
                    return type;
                }

                public int getLoad() {
                    return load;
                }

                public String getRegName() {
                    return regName;
                }

                public double getlowVersion() {
                    return lowMajorVersion + lowMinorVersion;
                }

                public double getHighVersion() {
                    return highMajorVersion + highMinorVersion;
                }

                public String getRandomHexStr() {
                    return randomHexStr;
                }

                public String getMD5HexStr() {
                    return md5Str;
                }

                public String toString() {
                    return f_key;
                }

                public boolean equals(Object obj) {
                    if (obj.toString().equals(toString()))
                        return true;
                    return false;
                }
            };
        } catch (Exception e) {
            throw new InvalidLicenseKeyException(e.getMessage());
        }
    }

    public static interface LicenseKey {
        public String getProduct();

        public String getType();

        public int getLoad();

        public String getRegName();

        public double getlowVersion();

        public double getHighVersion();

        public String getRandomHexStr();

        public String getMD5HexStr();
    }

    private static void fireLicenseKeysChanged() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Vector lc = (Vector) listeners.clone();
                for (Iterator i = lc.iterator(); i.hasNext();)
                    try {
                        ((LicenseKeyListener) i.next()).licenseKeysChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        });
    }

    public static void addLicenseKeyListener(LicenseKeyListener lkl) {
        listeners.add(lkl);
    }

    public static void removeLicenseKeyListener(LicenseKeyListener lkl) {
        listeners.remove(lkl);
    }

    public static interface LicenseKeyListener {
        public void licenseKeysChanged();
    }
}