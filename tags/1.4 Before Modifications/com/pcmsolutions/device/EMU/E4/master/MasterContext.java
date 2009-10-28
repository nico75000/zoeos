package com.pcmsolutions.device.EMU.E4.master;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterException;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.system.tasking.Ticket;

import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 24-Mar-2003
 * Time: 17:59:17
 * To change this template use Options | File Templates.
 */
public interface MasterContext extends Serializable {
    public Integer[] getMasterParams(Integer[] ids) throws ParameterException;

    public Ticket offsetMasterParam(Integer id, Integer offset);

    public Ticket offsetMasterParam(Integer id, double offsetAsFOR) throws IllegalParameterIdException;

    public Ticket setMasterParam(Integer id, Integer value);

    public Ticket refresh();

    public void addMasterListener(MasterListener ml);

    public void removeMasterListener(MasterListener ml);

    public List getEditableParameterModels(Integer[] ids) ;

    public List getAllEditableParameterModels();

    public DeviceContext getDeviceContext();
}
