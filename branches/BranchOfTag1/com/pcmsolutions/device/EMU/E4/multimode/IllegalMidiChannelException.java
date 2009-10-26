/*
 * IllegalParameterId.java
 *
 * Created on January 2, 2003, 7:41 PM
 */

package com.pcmsolutions.device.EMU.E4.multimode;

/**
 *
 * @author  pmeehan
 */
public class IllegalMidiChannelException extends Exception {
    private Integer mc;

    /**
     * Creates a new instance of <code>IllegalParameterId</code> without detail message.
     */
    public IllegalMidiChannelException(Integer mc) {
        this.mc = mc;
    }


    /**
     * Constructs an instance of <code>IllegalParameterId</code> with the specified detail message.
     * @param msg the detail message.
     */
    public IllegalMidiChannelException(Integer mc, String msg) {
        super(msg);
        this.mc = mc;
    }
}
