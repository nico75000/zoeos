package com.pcmsolutions.system;

import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.preset.SampleDescriptor;
import com.pcmsolutions.device.EMU.E4.sample.ReadableSample;
import com.pcmsolutions.device.EMU.database.ContextLocation;
import com.pcmsolutions.gui.HideIcon;
import com.pcmsolutions.system.audio.AudioUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.GeneralPath;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-May-2003
 * Time: 07:17:58
 * To change this template use Options | File Templates.
 */
public class ZUtilities {
    public static final String DOT_POSTFIX = "...";
    public static final String FILE_EXTENSION = ".";
    public static final String STRING_FIELD_SEPERATOR = "_";

    private static final DecimalFormat indexFormat = new DecimalFormat("0000");

    public static Color invert(Color c){
        return new Color((c.getRed()+128)%256, (c.getGreen()+128)%256, (c.getBlue()+128)%256);
    }
    public static String makeCopyingString(ContextLocation src, Integer dest) {
        return makeCopyingString(src.toString(), dest);
    }

    public static void zdispose(Object o) {
        try {
            if (o instanceof ZDisposable)
                ((ZDisposable) o).zDispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getExternalName(String name) {
        StringBuffer sb = new StringBuffer(name);
        int i;
        while ((i = sb.indexOf("\\")) != -1)
            sb = sb.deleteCharAt(i);
        while ((i = sb.indexOf("/")) != -1)
            sb = sb.deleteCharAt(i);
        while ((i = sb.indexOf(":")) != -1)
            sb = sb.deleteCharAt(i);
        while ((i = sb.indexOf("*")) != -1)
            sb = sb.deleteCharAt(i);
        while ((i = sb.indexOf("?")) != -1)
            sb = sb.deleteCharAt(i);
        while ((i = sb.indexOf("\"")) != -1)
            sb = sb.deleteCharAt(i);
        while ((i = sb.indexOf("<")) != -1)
            sb = sb.deleteCharAt(i);
        while ((i = sb.indexOf(">")) != -1)
            sb = sb.deleteCharAt(i);
        while ((i = sb.indexOf("|")) != -1)
            sb = sb.deleteCharAt(i);
        return sb.toString();
    }

    public static String quantify(int len, String type) {
        StringBuffer sb = new StringBuffer();
        sb.append(len);
        sb.append(" ");
        sb.append(type);
        if (len != 1)
            sb.append("s");
        return sb.toString();
    }

    public static String[] makeStringArray(String str, int copies) {
        String[] sa = new String[copies];
        Arrays.fill(sa, str);
        return sa;
    }

    public static int constrain(int val, int low, int high) {
        if (val < low)
            return low;
        if (val > high)
            return high;
        return val;
    }

    public static boolean inRange(int val, int low, int high) {
        return val >= low && val <= high;
    }

    public static boolean inRange(Integer val, Integer low, Integer high) {
        return inRange(val.intValue(), low.intValue(), high.intValue());
    }

    public static String getCommonPrefix(String[] strs) {
        if (strs.length == 0)
            return "";
        int min = getShortestLength(strs);
        if (min < 1)
            return "";
        StringBuffer common = new StringBuffer();
        char c;
        for (int i = 0; i < min; i++) {
            c = strs[0].charAt(i);
            for (int j = 1; j < strs.length; j++)
                if (strs[j].charAt(i) != c)
                    return common.toString();
            common.append(c);
        }
        return common.toString();
    }

    public static <T> Map<T,T> createIdentityMap(T[] objs) {
        HashMap<T,T> m = new HashMap<T,T>();
        for (int i = 0; i < objs.length; i++)
            m.put(objs[i], objs[i]);
        return m;
    }

    public static void assertSameLength(Object[] arr1, Object[] arr2) {
        if (arr1.length != arr2.length)
            throw new IllegalArgumentException("both arrays must be same length");
    }

    public static String extractTaggedField(String s, String tagIdentifier) {
        if (s != null && tagIdentifier != null) {
            String tag = makeTag(tagIdentifier);
            int i = s.indexOf(tag);
            if (i != -1) {
                int i2 = s.indexOf("<", i + 1);
                if (i2 == -1)
                    return s.substring(i + tag.length());
                else
                    return s.substring(i + tag.length(), i2);
            }
        }
        return null;
    };
    public static String makeTag(String tagIdentifier) {
        StringBuffer sb = new StringBuffer("<");
        sb.append(tagIdentifier);
        sb.append(">");
        return sb.toString();
    }

    public static String makeTaggedField(String tagIdentifier, String field) {
        StringBuffer sb = new StringBuffer(makeTag(tagIdentifier));
        sb.append(field);
        return sb.toString();
    }

    public static String makeCopyingString(String name, Integer dest) {
        StringBuffer s = new StringBuffer();
        s.append("Copying ");
        s.append(name);
        s.append(" to ");
        s.append(indexFormat.format(dest));
        return s.toString();
    }

    /* public static int extractUnsignedInt(char[] arr, int pos) {
         return extractUnsignedInt(arr, pos, 4);
     }
     */

    public static String tokenizeIntegers(Integer[] presets) {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < presets.length; i++) {
            if (i != 0)
                s.append(",");
            s.append(presets[i]);
        }
        return s.toString();
    }

    public static Integer[] detokenizeIntegers(String sess) {
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        StringTokenizer tok = new StringTokenizer(sess, ",");
        try {
            while (tok.hasMoreTokens()) {
                indexes.add(IntPool.get(Integer.parseInt(tok.nextToken())));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return indexes.toArray(new Integer[indexes.size()]);
    }

    public static double[] parseDoubles(String[] valueStrings) {
        try {
            double[] numericValueStrings = new double[valueStrings.length];
            for (int i = 0; i < valueStrings.length; i++)
                numericValueStrings[i] = Double.parseDouble(valueStrings[i]);
            return numericValueStrings;
        } catch (Exception e) {
        }
        return null;
    }

    // assumes doubles are ordered
    // rounds down
    // return array index of nearest
    public static int getNearestDoubleIndex(double[] doubles, double val) {
        if (val <= doubles[0])
            return 0;
        if (val >= doubles[doubles.length - 1])
            return doubles.length - 1;
        double lastVal = doubles[0];
        for (int i = 1; i < doubles.length; i++)
            if (val >= lastVal && val < doubles[i]) {
                double lowDiff = val - lastVal;
                double highDiff = doubles[i] - val;
                if (highDiff < lowDiff)
                    return i;
                else
                    return i - 1;
            } else
                lastVal = doubles[i];
        return 0;
    }

    public static java.util.List intersection(java.util.List l1, java.util.List l2) {
        ArrayList l3 = new ArrayList();
        for (int i = 0, j = l1.size(); i < j; i++) {
            Object o = l1.get(i);
            if (l2.contains(o))
                l3.add(o);
        }
        return l3;
    }

    public static String formatIndex(Integer index) {
        return indexFormat.format(index);
    }

    public static Object[] getRealObjects(Object[] objs) {
        Object[] outObjs = new Object[objs.length];
        for (int i = 0; i < objs.length; i++)
            if (objs[i] instanceof ObjectProxy)
                outObjs[i] = ((ObjectProxy) objs[i]).getRealObject();
            else
                outObjs[i] = objs[i];
        return outObjs;
    }

    // unsigned byte
    private static int ubyte(byte b) {
        int i = b;
        if (i < 0)
            i += 256;
        return i;
    }

    public static int extractUnsignedInt(byte[] arr, int pos) {
        return extractUnsignedInt(arr, pos, 4);
    }

    public static int extractUnsignedInt(byte[] arr, int pos, int bytes) {
        if (bytes < 1 || bytes > 4)
            throw new IllegalArgumentException("illegal number of bytes specified for int");
        if (pos + bytes > arr.length)
            throw new IllegalArgumentException("array too small to extract an int at specified position");
        int v = 0;
        if (bytes > 3)
            v += ubyte(arr[pos++]) << 24;
        if (bytes > 2)
            v += ubyte(arr[pos++]) << 16;
        if (bytes > 1)
            v += ubyte(arr[pos++]) << 8;
        v += ubyte(arr[pos]);
        return v;
    }

    public static void applyBytesAsChar(char[] arr, int v, int msbi, int bc) {
        if (msbi + bc > arr.length)
            throw new IllegalArgumentException("array too small to apply bytes at specified position");

        if (bc > 0)
            arr[msbi + bc - 1] = (char) (v & 0x000000FF);
        if (bc > 1)
            arr[msbi + bc - 2] = (char) ((v & 0x0000FF00) >> 8);
        if (bc > 2)
            arr[msbi + bc - 3] = (char) ((v & 0x00FF0000) >> 16);
        if (bc > 3)
            arr[msbi + bc - 4] = (char) ((v & 0xFF000000) >> 24);
    }

    public static void applyBytes(byte[] arr, int v, int msbi, int bc) {
        if (msbi + bc > arr.length)
            throw new IllegalArgumentException("array too small to apply bytes at specified position");

        if (bc > 0)
            arr[msbi + bc - 1] = (byte) (v & 0x000000FF);
        if (bc > 1)
            arr[msbi + bc - 2] = (byte) ((v & 0x0000FF00) >> 8);
        if (bc > 2)
            arr[msbi + bc - 3] = (byte) ((v & 0x00FF0000) >> 16);
        if (bc > 3)
            arr[msbi + bc - 4] = (byte) ((v & 0xFF000000) >> 24);
    }

    public static Integer[] joinIntegerArrays(Integer[] arr1, Integer[] arr2) {
        Integer[] arr3 = new Integer[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, arr3, 0, arr1.length);
        System.arraycopy(arr2, 0, arr3, arr1.length, arr2.length);
        return arr3;
    }

    public static Object[] appendArray(Object[] arr1, Object obj2, boolean end) {
        return appendArray(arr1, new Object[]{obj2}, end);
    }

    public static Object[] appendArray(Object[] arr1, Object[] arr2, boolean end) {
        Object[] arr3 = new Object[arr1.length + arr2.length];
        if (end) {
            System.arraycopy(arr1, 0, arr3, 0, arr1.length);
            System.arraycopy(arr2, 0, arr3, arr1.length, arr2.length);
        } else {
            System.arraycopy(arr2, 0, arr3, 0, arr2.length);
            System.arraycopy(arr1, 0, arr3, arr2.length, arr1.length);
        }
        return arr3;
    }

    public static char[] extractChars(byte[] arr, int pos, int len) {
        if (pos + len > arr.length)
            throw new IllegalArgumentException("invalid segment of byte array specified");
        char[] chars = new char[len];
        for (int i = 0; i < len; i++)
            chars[i] = (char) arr[pos + i];
        return chars;
    }

    public static void applyToCharArray(char[] arr, byte[] barr, int pos) {
        if (pos + barr.length > arr.length)
            throw new IllegalArgumentException("array too small to apply bytes at specified position");
        for (int i = pos, j = barr.length + pos, b = 0; i < j; i++)
            arr[i] = (char) barr[b++];
    }

    public static byte[] applyToByteArray(char[] arr, int pos, int len) {
        if (pos + len > arr.length)
            throw new IllegalArgumentException("array too small to extract bytes from specified position");
        byte[] outArr = new byte[len];
        for (int i = pos, j = pos + len, b = 0; i < j; i++)
            outArr[b++] = (byte) arr[i];
        return outArr;
    }

    public static byte[] applyToByteArray(int[] arr, int pos, int len) {
        if (pos + len > arr.length)
            throw new IllegalArgumentException("array too small to extract bytes from specified position");
        byte[] outArr = new byte[len];
        for (int i = pos, j = pos + len, b = 0; i < j; i++)
            outArr[b++] = (byte) arr[i];
        return outArr;
    }

    public static byte[] castToByteArray(int[] arr, int pos, int len) {
        if (pos + len > arr.length)
            throw new IllegalArgumentException("array too small to extract bytes from specified position");
        byte[] outArr = new byte[len * 4];
        for (int i = pos, j = pos + len, b = 0; i < j; i++, b += 4) {
            outArr[b + 0] = (byte) ((arr[i] & 0xFF000000) >> 24);
            outArr[b + 1] = (byte) ((arr[i] & 0xFF0000) >> 16);
            outArr[b + 2] = (byte) ((arr[i] & 0xFF00) >> 8);
            outArr[b + 3] = (byte) ((arr[i] & 0xFF));
        }
        return outArr;
    }

    public static char[] castToCharArray(int[] arr, int pos, int len) {
        if (pos + len > arr.length)
            throw new IllegalArgumentException("array too small to extract bytes from specified position");
        char[] outArr = new char[len * 2];
        for (int i = pos, j = pos + len, b = 0; i < j; i++, b += 2) {
            outArr[b + 0] = (char) ((arr[i] & 0xFFFF0000) >> 16);
            outArr[b + 1] = (char) ((arr[i] & 0xFFFF));
        }
        return outArr;
    }

    public static int[] castToIntArray(char[] arr, int pos, int len, boolean bigEndian) {
        if (pos + len > arr.length || arr.length % 2 != 0)
            throw new IllegalArgumentException("array too small to extract bytes from specified position or array length no aligned correctly");
        int[] outArr = new int[len / 2];
        int i2;
        for (int i = 0; i < outArr.length; i++) {
            if (bigEndian)
                i2 = ((int) arr[i * 2]) >> 16 + ((int) arr[i * 2 + 1]);
            else
                i2 = (int) arr[i * 2] + (((int) arr[i * 2 + 1]) >> 16);

            outArr[i] = i2;
        }
        return outArr;
    }

    public static int[] castToIntArray(byte[] arr, boolean bigEndian) {
        if (arr.length % 4 != 0)
            throw new IllegalArgumentException("array length must be divisible by 4");
        int[] outArr = new int[arr.length / 4];
        int i2;
        int off;
        for (int i = 0; i < outArr.length; i++) {
            off = i * 2;
            if (bigEndian) {
                i2 = ((int) arr[off]) << 24;
                i2 += ((int) arr[off + 1]) << 16;
                i2 += ((int) arr[off] + 2) << 8;
                i2 += ((int) arr[off] + 3);
            } else {
                i2 = ((int) arr[off]);
                i2 += ((int) arr[off + 1]) << 8;
                i2 += ((int) arr[off] + 2) << 16;
                i2 += ((int) arr[off] + 3) << 24;
            }
            outArr[i] = i2;
        }
        return outArr;
    }

    public static byte lobyte(short b) {
        return (byte) (b & 0x00FF);
    }

    public static byte hibyte(short b) {
        return (byte) (b & 0xFF00);
    }

    public static short loword(int b) {
        return (short) (b & 0x0000FFFF);
    }

    public static short hiword(int b) {
        return (short) (((b & 0xFFFF0000) >>> 16));
    }

    public static boolean isMapContentsSerializable(Map m) {
        Map.Entry e;
        for (Iterator i = m.entrySet().iterator(); i.hasNext();) {
            e = (Map.Entry) i.next();
            if (!(e.getKey() instanceof Serializable) || !(e.getValue() instanceof Serializable))
                return false;
        }
        return true;
    }

    public static void prefixPath(File[] files, String path) {
        for (int i = 0; i < files.length; i++)
            if (files[i] != null)
                files[i] = new File(path, files[i].getPath());
    }

    public static File prefixPath(File file, String path) {
        return new File(path, file.getPath());
    }

    public static boolean filesExist(File[] files) {
        return (howManyFilesExist(files) == files.length);
    }

    public static int howManyFilesExist(File[] files) {
        int ec = 0;
        for (int i = 0; i < files.length; i++)
            if (files[i].exists())
                ec++;
        return ec;
    }

    public static File[] filesThatExist(File[] files) {
        ArrayList exist = new ArrayList();
        for (int i = 0; i < files.length; i++)
            if (files[i].exists())
                exist.add(files[i]);
        return (File[]) exist.toArray(new File[exist.size()]);
    }

    public static int getIndexForString(Object[] objs, String s) {
        for (int i = 0; i < objs.length; i++)
            if (s.equals(objs[i].toString()))
                return i;
        return -1;
    }

    public static List filterWaveFiles(final List files) {
        final List wavFiles = new ArrayList();
        File f;
        for (Iterator i = files.iterator(); i.hasNext();) {
            f = (File) i.next();
            if (ZUtilities.getExtension(f.getName()).toLowerCase().equals(AudioUtilities.WAV_EXTENSION.toLowerCase()))
                wavFiles.add(f);
        }
        return wavFiles;
    }

    public static String quote(String s) {
        return "\"" + s + "\"";
    }

    public static String getLastToken(String str) {
        return getLastToken(str, null);
    }

    public static String getLastToken(String str, String delim) {
        StringTokenizer t;
        if (delim != null)
            t = new StringTokenizer(str, delim);
        else
            t = new StringTokenizer(str);
        String tok = "";
        while (t.hasMoreTokens())
            tok = t.nextToken();
        return tok;
    }

    public static String removeDelimiters(String str) {
        return removeDelimiters(str, null);
    }

    public static String removeDelimiters(String str, String delim) {
        StringTokenizer t;
        String outStr = "";
        if (delim == null)
            t = new StringTokenizer(str);
        else
            t = new StringTokenizer(str, delim);
        while (t.hasMoreTokens())
            outStr += t.nextToken();
        return outStr;
    }

    public static Object[] eliminateDuplicates(Object[] objs) {
        ArrayList s = new ArrayList();
        for (int i = 0; i < objs.length; i++) {
            if (!s.contains(objs[i]))
                s.add(objs[i]);
        }
        return s.toArray();
    }

    public static Object[] eliminateInstances(Object[] objs, Class c) {
        ArrayList s = new ArrayList();
        for (int i = 0; i < objs.length; i++) {
            if (!(c.isInstance(objs[i])))
                s.add(objs[i]);
        }
        return s.toArray();
    }

    public static int numNulls(final Object[] objs) {
        int c = 0;
        for (int i = 0; i < objs.length; i++)
            if (objs[i] == null)
                c++;
        return c;
    }

    public static Object[] extractClassOfObjects(Class c, final Object[] objs) {
        ArrayList eobjs = new ArrayList();

        for (int i = 0; i < objs.length; i++)
            if (c.isInstance(objs[i]))
                eobjs.add(objs[i]);

        return eobjs.toArray();
    }

    public static void clearSubtree(Preferences prefs) throws BackingStoreException {
        String[] kids = prefs.childrenNames();
        for (int i = 0; i < kids.length; i++) {
            clearSubtree(prefs.node(kids[i]));
        }
        prefs.clear();
    }

    public static Integer[] extractIndexes(ReadablePreset[] presets) {
        Integer[] indexes = new Integer[presets.length];
        for (int i = 0; i < presets.length; i++)
            indexes[i] = presets[i].getIndex();
        return indexes;
    }

    public static Integer[] extractIndexes(ReadableSample[] samples) {
        Integer[] indexes = new Integer[samples.length];
        for (int i = 0; i < samples.length; i++)
            indexes[i] = samples[i].getIndex();
        return indexes;
    }

    public static JPanel applyHideButton(final JInternalFrame iFrame) {
        JPanel np = new JPanel(new BorderLayout());
        np.add(iFrame.getContentPane(), BorderLayout.CENTER);
        JButton hb = new JButton(new AbstractAction("", HideIcon.getInstance()) {
            public void actionPerformed(ActionEvent e) {
                try {
                    iFrame.setIcon(true);
                } catch (PropertyVetoException e1) {
                }
            }
        });
        np.add(hb, BorderLayout.SOUTH);
        iFrame.setContentPane(np);
        return np;
    }

    public static Integer[] fillIncrementally(Integer[] arr, int base) {
        for (int i = 0; i < arr.length; i++)
            arr[i] = IntPool.get(base + i);
        return arr;
    }

    //on 128x128 grid
    private static GeneralPath keyWinAxes;
    private static GeneralPath velWinAxes;
    private static GeneralPath rtWinAxes;

    static {
        keyWinAxes = new GeneralPath();
        for (int i = 0; i < 128; i += 12) {
            keyWinAxes.moveTo(i, 0);
            keyWinAxes.lineTo(i, 127);
        }
        velWinAxes = new GeneralPath();
        for (int i = 0; i < 128; i += 8) {
            velWinAxes.moveTo(i, 0);
            velWinAxes.lineTo(i, 127);
        }
        rtWinAxes = velWinAxes;
    }

    public static GeneralPath getKeyWinAxes() {
        return (GeneralPath) keyWinAxes.clone();
    }

    public static GeneralPath getVelWinAxes() {
        return (GeneralPath) velWinAxes.clone();
    }

    public static GeneralPath getRTWinAxes() {
        return (GeneralPath) rtWinAxes.clone();
    }

    public static GeneralPath getWindowMiddle(int low, int lowFade, int high, int highFade) {
        GeneralPath p = new GeneralPath();
        p.moveTo(high, 127);
        p.lineTo(high - lowFade, 0);
        p.lineTo(low, 0);
        p.lineTo(low, 127);
        p.closePath();
        return p;
    }

    public static GeneralPath getWindowFadeIn(int low, int lowFade) {
        GeneralPath p = new GeneralPath();
        p.moveTo(low, 127);
        p.lineTo(low + lowFade, 0);
        p.lineTo(low + lowFade, 127);
        p.closePath();
        return p;
    }

    public static GeneralPath getWindowFadeOut(int high, int highFade) {
        GeneralPath p = new GeneralPath();
        p.moveTo(high, 127);
        p.lineTo(high - highFade, 0);
        p.lineTo(high - highFade, 127);
        p.closePath();
        return p;
    }

    public static Dimension constrainDimension(Dimension d, int w, int h) {
        int nw = d.width;
        int nh = d.height;
        if (nw > w)
            nw = w;
        if (nh > h) {
            nh = h;
        }
        return new Dimension(nw, nh);
    }

    public static GeneralPath getWindowShape(int low, int lowFade, int high, int highFade) {
        GeneralPath p = new GeneralPath();

        int lhc = (low + lowFade) - (high - highFade);

        p.moveTo(low, 127);
        if (lhc > 0 && lowFade != 0 && highFade != 0) {
            int hfx = high - highFade;
            int lfx = low + lowFade;
            int hfy = (127 * (hfx - low)) / lowFade;
            p.lineTo(hfx, 127 - hfy);
            int midx = hfx + (lfx - hfx) / 2;
            int midy = (127 * (high - midx) / highFade);
            p.lineTo(midx, 127 - midy);
            int lfy = (127 * (high - lfx)) / highFade;
            p.lineTo(lfx, 127 - lfy);
        } else {
            p.lineTo(low + lowFade, 0);
            p.lineTo(high - highFade, 0);
        }

        p.lineTo(high, 127);
        p.closePath();

        return p;
    }

    // assumes low, high and lowFade have range 0...127
    // returns shape in 128x128 space
    public static GeneralPath getLowWindowShape(int low, int lowFade, int high) {
        GeneralPath p = new GeneralPath();
        p.moveTo(low, 127);
        p.lineTo(low + lowFade, 0);
        p.lineTo(high, 0);
        p.lineTo(high, 127);
        p.closePath();
        return p;
    }

    // assumes low, high and highFade have range 0...127
    // returns shape in 128x128 space
    public static GeneralPath getHighWindowShape(int low, int high, int highFade) {
        GeneralPath p = new GeneralPath();
        p.moveTo(high, 127);
        p.lineTo(high - highFade, 0);
        p.lineTo(low, 0);
        p.lineTo(low, 127);
        p.closePath();
        return p;
    }

    public static void applyHideButton(final JDialog dlg, final boolean disposeOnHide, boolean show) {
        JPanel ncp = new JPanel(new BorderLayout());
        ncp.add(dlg.getContentPane(), BorderLayout.CENTER);
        JButton hb = new JButton(new AbstractAction("", HideIcon.getInstance()) {
            public void actionPerformed(ActionEvent e) {
                if (disposeOnHide)
                    dlg.dispose();
                else
                    dlg.setVisible(false);
            }
        });
        ncp.add(hb, BorderLayout.SOUTH);
        dlg.getRootPane().setDefaultButton(hb);
        dlg.setContentPane(ncp);
        dlg.pack();
        dlg.setResizable(false);
        hb.grabFocus();

        if (disposeOnHide)
            dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        else
            dlg.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

        if (show)
            dlg.show();
    }

    public static String getByteString(byte[] data) {
        StringBuffer sb = new StringBuffer();
        for (int n = 0; n < data.length - 1; n++) {
            sb.append(data[n]);
            sb.append("_");
        }
        sb.append(data[data.length - 1]);
        return sb.toString();
    }

    public static String getByteString(byte[] data, int max) {
        StringBuffer sb = new StringBuffer();
        for (int n = 0; n < data.length - 1; n++) {
            if (n > max)
                break;
            else if (n == max) {
                sb.append("..._");
            } else {
                sb.append(data[n]);
                sb.append("_");
            }
        }
        sb.append(data[data.length - 1]);
        return sb.toString();
    }

    // specify extension without the "."
    public static boolean hasExtension(String name, String ext) {
        int i = name.lastIndexOf(FILE_EXTENSION);
        if (i != -1 && name.substring(i).equals(FILE_EXTENSION + ext))
            return true;
        return false;
    }

    public static File replaceExtension(File f, String ext) {
        return new File(f.getParentFile(), replaceExtension(f.getName(), ext));
    }

    public static String replaceExtension(String name, String ext) {
        return stripExtension(name) + FILE_EXTENSION + ext;
    }

    public static String stripExtension(String name) {
        int i = name.indexOf(FILE_EXTENSION);
        if (i != -1)
            return name.substring(0, i);
        return name;
    }

    public static String makeSampleSummary(SampleDescriptor sampleDescriptor) {
        if (sampleDescriptor != null) {
            StringBuffer s = new StringBuffer("<html><sp>");
            s.append(sampleDescriptor.getFormattedSampleRateInKhz());
            s.append("<br><sp>");
            s.append(sampleDescriptor.getChannelDescription());
            s.append("<br><sp>");
            s.append(sampleDescriptor.getLengthInSampleFrames() + " samples");
            s.append("<br><sp>");
            s.append(sampleDescriptor.getFormattedDurationInSeconds());
            s.append("<br><sp>");
            s.append(sampleDescriptor.getFormattedSize());
            s.append("</html>");
            return s.toString();
        } else
            return null;
    }

    // specify extension without the "."
    public static String getExtension(String name) {
        return getExtension(name, FILE_EXTENSION);
    }

    public static String getExtension(String name, String sep) {
        int i = name.lastIndexOf(sep);
        if (i != -1)
            try {
                return name.substring(i + 1);
            } catch (Exception e) {
            }
        return "";
    }

    public static ZCommand[] concatZCommands(ZCommand[] superCmdObjects, ZCommand[] cmdObjects) {

        ZCommand[] allCmds = new ZCommand[superCmdObjects.length + cmdObjects.length];

        System.arraycopy(superCmdObjects, 0, allCmds, 0, superCmdObjects.length);

        for (int n = 0; n < cmdObjects.length; n++) {
            allCmds[superCmdObjects.length + n] = cmdObjects[n];
        }
        return allCmds;
    }

    /*  public static ZView[] concatZViews(ZView[] superViewObjects, ZView[] viewObjects) {
          ZView[] allViews = new ZView[superViewObjects.length + viewObjects.length];

          System.arraycopy(superViewObjects, 0, allViews, 0, superViewObjects.length);

          for (int n = 0; n < viewObjects.length; n++) {
              allViews[superViewObjects.length + n] = viewObjects[n];
          }
          return allViews;
      }
      */

    public static void zDisposeCollection(Collection c) {
        if (c == null)
            return;
        Object o;
        for (Iterator i = c.iterator(); i.hasNext();) {
            o = i.next();
            if (o instanceof ZDisposable)
                try {
                    ((ZDisposable) o).zDispose();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }

    /*public static void zDisposeList(List l) {
        if (l == null)
            return;
        Object o;
        for (int i = 0, n = l.size(); i < n; i++) {
            o = l.get(i);
            if (o instanceof ZDisposable)
                ((ZDisposable) o).zDispose();
        }
    } */


    public static Integer[] extractOneOfIntegerPairs(Integer[] integerPairs, boolean extractFirst) {
        if (integerPairs.length % 2 != 0)
            throw new IllegalArgumentException("extractOneOfIntegerPairs: elementCount % 2 != 0");
        Integer[] firstOfPairs = new Integer[integerPairs.length / 2];
        for (int i = 0, j = integerPairs.length; i < j; i += 2)
            if (extractFirst)
                firstOfPairs[i / 2] = integerPairs[i];
            else
                firstOfPairs[i / 2] = integerPairs[i + 1];
        return firstOfPairs;
    }

    /*  public byte[][] splitByteArray(byte[] bytes, int segmentSize) {
          int segs = (bytes.length - 1) / segmentSize + 1;
          byte[][] segBytes = new byte[segs][];

          Arrays.
          for (int i = 0; i < segs; i++) {
              if (i * segmentSize > bytes.length) {

              } else {

              }
          }
          return segBytes;
      } */

    public static JMenu sortSubMenus(JMenu menu, boolean addSeperator) {
        Component[] comps = menu.getMenuComponents();
        menu.removeAll();
        ArrayList subMenus = new ArrayList();
        for (int i = 0; i < comps.length; i++) {
            if (comps[i] instanceof JMenu)
                subMenus.add(comps[i]);
            else
                menu.add(comps[i]);
        }

        if (subMenus.size() > 0) {
            if (addSeperator)
                menu.addSeparator();

            Collections.sort(subMenus, new Comparator() {
                public int compare(Object o1, Object o2) {
                    return ((JMenu) o1).getText().compareTo(((JMenu) o2).getText());
                }
            });

            for (int i = 0, j = subMenus.size(); i < j; i++)
                menu.add(sortSubMenus((JMenu) subMenus.get(i), addSeperator));
        }
        return menu;
    }

    public static String postfixString(String s, String postFix, int maxLength) {
        if (postFix.length() >= maxLength)
            return postFix.substring(0, maxLength);
        if (s.length() + postFix.length() > maxLength)
            return s.substring(0, maxLength - postFix.length()) + postFix;
        return s + postFix;
    }

    public static String makeExactLengthString(String s, int len) {
        return makeExactLengthString(s, len, ' ', true);
    }

    public static String makeExactLengthString(String s, int len, boolean postFix) {
        return makeExactLengthString(s, len, ' ', postFix);
    }

    public static String makeExactLengthString(String s, int len, char filler, boolean postFix) {
        if (s != null) {
            if (s.length() > len)
                return s.substring(0, len);
            char[] fill = new char[len - s.length()];
            Arrays.fill(fill, filler);
            if (postFix)
                return s + new String(fill);
            else
                return new String(fill) + s;
        } else {
            char[] fill = new char[len];
            Arrays.fill(fill, filler);
            return new String(fill);
        }
    }

    public static List expandList(List l, Object o, int count) {
        for (int i = 0; i < count; i++)
            l.add(o);
        return l;
    }

    public static String makeMinimumLengthString(String s, int len) {
        return makeMinimumLengthString(s, len, ' ', true);
    }

    public static String makeMinimumLengthString(String s, int len, boolean postFix) {
        return makeMinimumLengthString(s, len, ' ', postFix);
    }

    public static String makeMinimumLengthString(String s, int len, char filler, boolean postFix) {
        if (s != null) {
            if (s.length() >= len)
            //return s.substring(0, len);
                return s;
            char[] fill = new char[len - s.length()];
            Arrays.fill(fill, filler);
            if (postFix)
                return s + new String(fill);
            else
                return new String(fill) + s;
        } else {
            char[] fill = new char[len];
            Arrays.fill(fill, filler);
            return new String(fill);
        }
    }

    public static int getFileCountForPattern(File[] files, Pattern p) {
        int mc = 0;
        for (int i = 0; i < files.length; i++) {
            Matcher m = p.matcher(files[i].getName().subSequence(0, files[i].getName().length()));
            if (m.find())
                mc++;
        }
        return mc;
    }

    public static File[] stripPatternFromFiles(File[] files, Pattern p) {
        File[] outFiles = new File[files.length];
        for (int i = 0; i < files.length; i++)
            outFiles[i] = new File(files[i].getParent(), removeFirstPattern(files[i].getName(), p));
        return outFiles;
    }

    public static String removeAllPattern(String s, Pattern p) {
        Matcher m = p.matcher(s.subSequence(0, s.length()));
        return m.replaceAll("");
    }

    public static String removeFirstPattern(String s, Pattern p) {
        Matcher m = p.matcher(s.subSequence(0, s.length()));
        return m.replaceFirst("");
    }

     public static int getShortestLength(String[] strs){
        int min = Integer.MAX_VALUE;
        for ( String s: strs)
            if ( s.length() < min)
                min = s.length();
        return (min != Integer.MAX_VALUE?min:0);
    }
}
