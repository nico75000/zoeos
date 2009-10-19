package com.pcmsolutions.aspi;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 13-Jan-2004
 * Time: 21:07:08
 * To change this template use Options | File Templates.
 */
public interface Result {
    public int getReturnValue();

    public ASPIMsg.SRB getReturnedStruct() throws ASPIMsg.ASPIWrapperException;
}
