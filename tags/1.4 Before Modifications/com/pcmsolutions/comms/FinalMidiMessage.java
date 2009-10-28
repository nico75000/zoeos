/*
 * ExMidiMessage.java
 *
 * Created on November 21, 2002, 8:21 AM
 */

package com.pcmsolutions.comms;

import com.pcmsolutions.system.Zoeos;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import java.io.ByteArrayInputStream;
import java.io.Serializable;

/**
 *
 * @author  pmeehan
 */

public abstract class FinalMidiMessage implements ByteStreamable, Serializable {
    protected byte[] data;
    private long timeStamp;
    private MidiDevice.Info source;
    private int matchRef = 0;

    {
        timeStamp = Zoeos.getZoeosTicks();
    }

    protected FinalMidiMessage() {
        this(new byte[0], null);
    }

    public void addMatchRef() {
        matchRef++;
    }

    public int getMatchRef() {
        return matchRef;
    }

    protected FinalMidiMessage(MidiMessage msg, MidiDevice.Info source) {
        this(msg.getMessage(), source);
    }

    public MidiDevice.Info getSource() {
        return source;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    //protected FinalMidiMessage() {
    //     data = new byte[0];
    // }

    protected FinalMidiMessage(byte[] data, MidiDevice.Info source) {
        this.data = new byte[data.length];
        System.arraycopy(data, 0, this.data, 0, data.length);
        this.source = source;
    }

    public final byte[] getMessage() {
        byte[] retArr = new byte[data.length];
        System.arraycopy(data, 0, retArr, 0, data.length);
        return retArr;
    }

    public final ByteArrayInputStream getByteStream() {
        return new ByteArrayInputStream(data);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("MidiMessage --> len = ");
        int len = data.length;
        sb.append(len);
        sb.append(", bytes = ");
        if (len > 0) {
            for (int n = 0; n < len - 1; n++) {
                sb.append(data[n]);
                sb.append(", ");
                if (n > 20) {
                    sb.append(" ... ");
                    break;
                }
            }
            sb.append(data[len - 1]);
        }
        return sb.toString();
    }
}
