package com.pcmsolutions.device.EMU.E4.events;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 30-Apr-2003
 * Time: 06:54:00
 * To change this template use Options | File Templates.
 */
public interface RemoteEvent {
    public static int STATUS_ERROR_CONDITION = 0;
    public static int STATUS_OUTGOING_MESSAGE = 1;
    public static int STATUS_INCOMING_MESSAGE = 2;
    public static int STATUS_TRANSACTION_COMPLETE = 3;
    public static int STATUS_TRANSACTION_FAILED = 4;

    public Element[] getElements();

    public int getNumElements();

    public interface Element {

        // in relative ZoeOS time
        public long getTimestamp();

        // formatted string describing the element
        public String getString();

        // status one of above
        public int getStatus();
    }
}
