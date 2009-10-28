/*
 * DeviceInterface.java
 *
 * Created on December 23, 2002, 7:09 AM
 */

package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.remote.Remotable.DeviceConfig;
import com.pcmsolutions.device.EMU.E4.remote.Remotable.DeviceExConfig;
import com.pcmsolutions.device.EMU.E4.remote.Remotable.PresetMemory;
import com.pcmsolutions.device.EMU.E4.remote.Remotable.SampleMemory;
import com.pcmsolutions.device.EMU.E4.desktop.ViewManager;
import com.pcmsolutions.device.EMU.E4.gui.TitleProvider;
import com.pcmsolutions.device.EMU.E4.master.MasterContext;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeContext;
import com.pcmsolutions.device.EMU.E4.parameter.DeviceParameterContext;
import com.pcmsolutions.device.EMU.E4.preset.PresetContext;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.gui.IconAndTipCarrier;
import com.pcmsolutions.smdi.SmdiTarget;
import com.pcmsolutions.system.ScsiIdProvider;
import com.pcmsolutions.system.ZCommandProvider;
import com.pcmsolutions.system.ZExternalDevice;
import com.pcmsolutions.system.tasking.Ticket;
import com.pcmsolutions.system.tasking.PostableTicket;

import javax.swing.table.TableModel;
import java.io.File;

/**
 * @author pmeehan
 */
public interface DeviceContext extends ZExternalDevice, TitleProvider, ZCommandProvider, ScsiIdProvider, IconAndTipCarrier {

    // PREFERENCES
    public DevicePreferences getDevicePreferences();

    public RemotePreferences getRemotePreferences();

    public File getDeviceLocalDir();

    // DESKTOP
    public ViewManager getViewManager();

    // QUEUES
    public DeviceQueues getQueues();

    // CONTEXTS
    public PresetContext getDefaultPresetContext() throws DeviceException;

    public MultiModeContext getMultiModeContext() throws  DeviceException;

    public SampleContext getDefaultSampleContext() throws DeviceException;

    public MasterContext getMasterContext() throws DeviceException;

    public SampleMemory getSampleMemory() throws RemoteUnreachableException, DeviceException;

    public PresetMemory getPresetMemory() throws DeviceException;

    public PostableTicket sampleMemoryDefrag(boolean pause) throws DeviceException;

    public TableModel getDeviceConfigTableModel();

    public Ticket refreshDeviceConfiguration(boolean showProgress) ;

    public String makeDeviceProgressTitle(String str);

    public DeviceParameterContext getDeviceParameterContext() throws DeviceException;

    // UTILITY
    public PostableTicket reinitializePresetFlash();

    // UTILITY
    public PostableTicket saveDeviceState();

    // SMDI

    public interface SMDIProfile {
        public SmdiTarget getSmdiTarget() throws NotSMDICoupledException;

        public File getLocalDirectory();
    }

    public Object getSmdiCouplingObject();

    public boolean isSmdiCoupled() throws DeviceException;

    // AUDITION
    AuditionManager getAuditionManager() throws DeviceException;

    // BANK
    public PostableTicket eraseBank() ;

    public PostableTicket refreshBank(boolean refreshObjects) ;

    public PostableTicket cancelAuditions();

    // CONFIGURATION
    public DeviceConfig getDeviceConfig() throws DeviceException;

    public DeviceExConfig getDeviceExConfig() throws DeviceException;

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
