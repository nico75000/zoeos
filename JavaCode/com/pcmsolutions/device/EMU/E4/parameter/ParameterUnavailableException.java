package com.pcmsolutions.device.EMU.E4.parameter;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 02-Mar-2003
 * Time: 01:08:07
 * To change this template use Options | File Templates.
 */
public class ParameterUnavailableException extends ParameterException {

    public ParameterUnavailableException(Integer id) {
        super("parameter unavailable", id);
    }
}
