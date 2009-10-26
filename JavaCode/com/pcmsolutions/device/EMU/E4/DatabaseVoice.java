package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.preset.IsolatedPreset;
import com.pcmsolutions.util.IntegerUseMap;

import java.util.Map;

/**
 * User: paulmeehan
 * Date: 24-Mar-2004
 * Time: 12:28:02
 */
public interface DatabaseVoice extends DatabaseParameterized {
    Integer getSample();

    void applySingleSample(Integer sample) throws ParameterValueOutOfRangeException;

    void refreshParameters(Integer[] ids) throws IllegalParameterIdException;

    IntegerUseMap getReferencedSampleUsage();

    Integer getPreset();

    Integer getVoice();

    Map offsetSampleIndexes(Integer sampleOffset, boolean user);

    // returned map format is voice(Integer)->new sample value(Integer)
    Map remapSampleIndexes(Map translationMap, Integer defaultSampleTranslation);

    void setValue(Integer id, Integer val) throws IllegalParameterIdException, ParameterValueOutOfRangeException;

    //void sortZones(Integer[] ids, boolean post);

    IsolatedPreset.IsolatedVoice.IsolatedZone getIsolatedZone(Integer z) throws NoSuchZoneException;

    DatabaseZone getZone(Integer z) throws NoSuchZoneException;

    Integer newZone() throws TooManyZonesException;

    Integer dropZone(IsolatedPreset.IsolatedVoice.IsolatedZone iz) throws TooManyZonesException;

    void rmvZone(Integer zone) throws NoSuchZoneException;

    int numZones();
}
