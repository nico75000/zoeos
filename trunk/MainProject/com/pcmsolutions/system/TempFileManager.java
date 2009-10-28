package com.pcmsolutions.system;

import com.pcmsolutions.comms.ZMidiSystem;
import com.pcmsolutions.gui.ProgressSession;
import com.pcmsolutions.gui.UserMessaging;

import javax.swing.*;
import java.io.File;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Sep-2003
 * Time: 21:36:19
 * To change this template use Options | File Templates.
 */
public class TempFileManager {
    private static final String PREF_lastTempIndex = "lastIndex";
    private static Preferences pref = Preferences.systemNodeForPackage(TempFileManager.class);
    private static final int firstTempIndex = 1024;
    private static final String progressStr = "Cleaning TEMP directory";
    private static final String tempDirectoryStr = "TEMP";
    private static final String tempExt = ".tmp";
    private static final File tempDir = new File(Zoeos.getZoeosLocalDir(), tempDirectoryStr);

    static {
        assertTempDirectory();
        cleanTempDirectory(true);
    }

    public static int numTmpExtensions(final File[] files) {
        int c = 0;
        for (int i = 0; i < files.length; i++)
            if (files[i] != null && files[i].getName().endsWith(tempExt))
                c++;
        return c;
    }

    private static void assertTempDirectory() {
        if (!tempDir.exists() && !tempDir.mkdir()) {
            UserMessaging.showError( "Could not create ZoeOS temp directory. Exiting.");
            ZMidiSystem.getInstance().zDispose();
            Zoeos.getInstance().zDispose();
            System.exit(0);
        }
    }

    public static File getTempDirectory() {
        return tempDir.getAbsoluteFile();
    }

    public static void cleanTempDirectory(final boolean showProgress) {
        File[] files = tempDir.listFiles();
        if (files == null)
            return;
        Zoeos z = Zoeos.getInstance();
        ProgressSession ps = null;
        if (showProgress)
            ps = z.getProgressSession(progressStr, files.length + 1);
        try {
            for (int i = 0; i < files.length; i++) {
                files[i].delete();
                if (showProgress)
                    ps.updateStatus();
            }
        } finally {
            if (showProgress)
                ps.end();
        }
    }

    public static File getNewTempFile() {
        int index = pref.getInt(PREF_lastTempIndex, firstTempIndex);

        if (index == Integer.MAX_VALUE)
            index = firstTempIndex;

        File tf;
        do {
            tf = new File(tempDir, Integer.toHexString(++index) + tempExt);
        } while (tf.exists());

        pref.putInt(PREF_lastTempIndex, index);

        return tf;
    }
}
