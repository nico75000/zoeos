package com.pcmsolutions.smdi;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 10-Sep-2003
 * Time: 15:51:26
 * To change this template use Options | File Templates.
 */
public class SmdiTargetNotCoupledException extends Exception {
    public SmdiTargetNotCoupledException(String message) {
        super(message);
    }

    public SmdiTargetNotCoupledException() {
        super("SMDI target is not coupled");
    }
}
