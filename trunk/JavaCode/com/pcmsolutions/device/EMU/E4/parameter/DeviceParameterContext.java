/*
 * ParameterContext.java
 *
 * Created on February 9, 2003, 10:54 AM
 */

package com.pcmsolutions.device.EMU.E4.parameter;

import com.pcmsolutions.device.EMU.E4.DeviceContext;


/**
 *
 * @author  pmeehan
 */
public interface DeviceParameterContext {
    public static final int MULTIMODE = 0;
    public static final int MASTER = 1;
    public static final int PRESET = 2;
    public static final int VOICE = 3;
    public static final int LINK = 4;
    public static final int ZONE = 5;

    public String getGenerator();

    public DeviceContext getDeviceContext();

    public ParameterContext getMasterContext();

    public ParameterContext getPresetContext();

    public ParameterContext getVoiceContext();

    public ParameterContext getLinkContext();

    public ParameterContext getZoneContext();

    public boolean paramExists(Integer id);

    public boolean paramExists(String refName);

    public GeneralParameterDescriptor getParameterDescriptor(String refName) throws IllegalParameterReferenceException;

    public GeneralParameterDescriptor getParameterDescriptor(Integer id) throws IllegalParameterIdException;

    //public String getPresentationString(Integer id) throws IllegalParameterIdException;
    public String getRefName(Integer id) throws IllegalParameterIdException;

    public Integer getId(String refName) throws IllegalParameterReferenceException;

    public Integer getNearestCordSrcValue(Integer value);

    public Integer getNearestCordDestValue(Integer value);

    // EOS 3.2 amd 3.00 (<EOS 4.0?) seem to only have 28 link words and no word for number of voices in dump
    public boolean isWeirdPresetDumping();
}
