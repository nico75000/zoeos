/*
 * IdentityRequest.java
 *
 * Created on November 21, 2002, 12:22 PM
 */

package com.pcmsolutions.comms;

import javax.sound.midi.SysexMessage;

/**
 *
 * @author  pmeehan
 */
public final class IdentityRequest extends SysexMessage {
    private byte id;

    /** Creates a new instance of IdentityRequest */
    public IdentityRequest(byte id) {
        super(new byte[]{(byte) 0xF0, (byte) 0x7E, id, (byte) 0x06, (byte) 0x01, (byte) 0xF7});
        this.id = id;
    }

    public Object clone() {
        return new IdentityRequest(id);
    }
}
