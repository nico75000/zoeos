package com.pcmsolutions.device.EMU.E4.multimode;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.RemoteUnreachableException;
import com.pcmsolutions.system.tasking.Ticket;

import javax.sound.midi.MidiMessage;
import java.io.Serializable;


/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 23-Mar-2003
 * Time: 15:33:53
 * To change this template use Options | File Templates.
 */
public interface MultiModeContext extends Serializable {
    public Ticket audition(int ch);

    public MultiModeMap getMultimodeMap() throws DeviceException;

    public MultiModeChannel getMultiModeChannel(Integer channel) throws DeviceException;

    public Ticket setMultimodeMap(MultiModeMap mmMap);

    public Integer[] getDistinctMultimodePresetIndexes() throws DeviceException;

    public void addMultiModeListener(MultiModeListener mml);

    public void removeMultiModeListener(MultiModeListener mml);

    public MultiModeDescriptor getMultiModeDescriptor();

    public boolean has32Channels();

    public Integer getPreset(Integer ch) throws DeviceException;

    public Integer getVolume(Integer ch) throws DeviceException, DeviceException;

    public Integer getPan(Integer ch) throws DeviceException;

    public Integer getSubmix(Integer ch) throws DeviceException;

    public Ticket setPreset(Integer ch, Integer preset);

    public Ticket setVolume(Integer ch, Integer volume);

    public Ticket setPan(Integer ch, Integer pan);

    public Ticket setSubmix(Integer ch, Integer submix);

    public Ticket offsetPreset(Integer ch, Integer offset);

    public Ticket offsetVolume(Integer ch, Integer offset);

    public Ticket offsetPan(Integer ch, Integer offset) ;

    public Ticket offsetSubmix(Integer ch, Integer offset) ;

    public Ticket offsetPreset(Integer ch, Double offsetAsFOR) ;

    public Ticket offsetVolume(Integer ch, Double offsetAsFOR) ;

    public Ticket offsetPan(Integer ch, Double offsetAsFOR) ;

    public Ticket offsetSubmix(Integer ch, Double offsetAsFOR) ;

    public void sendMidiMessage(MidiMessage m) throws RemoteUnreachableException, DeviceException;

    public Ticket refresh();

    public void syncRefresh() throws DeviceException;
    
    public void syncToEdits();
}
