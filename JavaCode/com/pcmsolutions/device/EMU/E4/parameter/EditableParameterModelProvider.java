package com.pcmsolutions.device.EMU.E4.parameter;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 04-Jul-2003
 * Time: 04:55:36
 * To change this template use Options | File Templates.
 */
public interface EditableParameterModelProvider extends ParameterModelProvider {
    public EditableParameterModel getEditableParameterModel(Integer id) throws IllegalParameterIdException;
}
