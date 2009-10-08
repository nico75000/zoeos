/*
 * DeviceInterface.java
 *
 * Created on December 23, 2002, 7:09 AM
 */

package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.Remotable.DeviceConfig;
import com.pcmsolutions.device.EMU.E4.Remotable.DeviceExConfig;
import com.pcmsolutions.device.EMU.E4.Remotable.PresetMemory;
import com.pcmsolutions.device.EMU.E4.Remotable.SampleMemory;
import com.pcmsolutions.device.EMU.E4.desktop.ViewManager;
import com.pcmsolutions.device.EMU.E4.gui.TitleProvider;
import com.pcmsolutions.device.EMU.E4.master.MasterContext;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeContext;
import com.pcmsolutions.device.EMU.E4.parameter.DeviceParameterContext;
import com.pcmsolutions.device.EMU.E4.preset.PresetContext;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.gui.IconAndTipCarrier;
import com.pcmsolutions.smdi.SmdiTarget;
import com.pcmsolutions.system.ScsiIdProvider;
import com.pcmsolutions.system.ZCommandProvider;
import com.pcmsolutions.system.ZDeviceNotRunningException;
import com.pcmsolutions.system.ZExternalDevice;

import javax.swing.table.TableModel;
import java.io.File;

/**
 *
 * @author  pmeehan
 */
public interface DeviceContext extends ZExternalDevice, TitleProvider, ZCommandProvider, ScsiIdProvider, IconAndTipCarrier {

    // PREFERENCES
    public DevicePreferences getDevicePreferences();

    public RemotePreferences getRemotePreferences();

    public File getDeviceLocalDir();

    // EVENTS
    public void addDeviceListener(DeviceListener dl);

    public void removeDeviceListener(DeviceListener dl);

    // DESKTOP
    public ViewManager getViewManager();

    // SYNCHRONIZATION
    public void lockConfigure() throws IllegalArgumentException;

    public void lockAccess();

    public void unlock() throws IllegalArgumentException;

    // CONTEXTS
    public PresetContext getDefaultPresetContext() throws ZDeviceNotRunningException;

    public DeviceParameterContext getDeviceParameterContext();

    public MultiModeContext getMultiModeContext() throws ZDeviceNotRunningException;

    public SampleContext getDefaultSampleContext() throws ZDeviceNotRunningException;

    public MasterContext getMasterContext() throws ZDeviceNotRunningException;

    public SampleMemory getSampleMemory() throws RemoteUnreachableException, ZDeviceNotRunningException;

    public PresetMemory getPresetMemory() throws RemoteUnreachableException, ZDeviceNotRunningException;

    public void sampleMemoryDefrag(boolean pause) throws ZDeviceNotRunningException, RemoteUnreachableException;

    public TableModel getDeviceConfigTableModel();

    public void refreshDeviceConfiguration(boolean showProgress);

    public String makeDeviceProgressTitle(String str);

    public DeviceParameterContext getDpc();

    public void logCommError(Object error);

    public void logInternalError(Object error);

    // SMDI

    public interface SMDIProfile {
        public SmdiTarget getSmdiTarget() throws NotSMDICoupledException;

        public File getLocalDirectory();
    }

    public Object getSmdiCouplingObject();

    public boolean isSmdiCoupled();

    public SmdiTarget getSmdiTarget() throws NotSMDICoupledException;

    public void setSmdiTarget(SmdiTarget target);

    // BANK
    public void eraseBank() throws ZDeviceNotRunningException;

    public void refreshBank(boolean refreshObjects) throws ZDeviceNotRunningException;

    // CONFIGURATION
    public DeviceConfig getDeviceConfig() throws ZDeviceNotRunningException;

    public DeviceExConfig getDeviceExConfig() throws ZDeviceNotRunningException;

    public boolean isUltra();

    public double getDeviceVersion();

    public int getNumberOfInstalledSampleRoms();

    // CONSTANTS
    String UNTITLED_PRESET = "Untitled Preset";
    String EMPTY_PRESET = "Empty Preset";
    String EMPTY_SAMPLE = "Empty Sample";
    int MAX_USER_PRESET = 999;
    int BASE_FLASH_PRESET = 1000;
    int FIRST_USER_SAMPLE = 1;
    int MAX_USER_SAMPLE = 999;
    int BASE_ROM_SAMPLE = 1000;
    int SAMPLE_ROM_SIZE = 1000;
    int MAX_NAME_LENGTH = 16;
    double BASE_ULTRA_VERSION = 4.5;

    String PRESET_PACK_EXT = "zoeos_ppkg";
    String PRESET_PACK_SAMPLE_DIR_EXT = "zoeos_ppkg_samples";

    String MULTISAMPLE = "multisample..";
}
