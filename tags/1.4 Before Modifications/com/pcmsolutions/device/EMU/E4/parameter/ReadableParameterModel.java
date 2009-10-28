package com.pcmsolutions.device.EMU.E4.parameter;

import com.pcmsolutions.device.EMU.E4.zcommands.E4ReadableParameterModelZCommandMarker;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.gui.IconAndTipCarrier;
import com.pcmsolutions.system.ZCommandProvider;
import com.pcmsolutions.system.ZCommandProviderHelper;
import com.pcmsolutions.system.ZDisposable;

import javax.swing.event.ChangeListener;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 10-Mar-2003
 * Time: 17:07:04
 * To change this template use Options | File Templates.
 */
public interface ReadableParameterModel extends ZDisposable, ZCommandProvider, IconAndTipCarrier {
    public final static ZCommandProviderHelper cmdProviderHelper = new ZCommandProviderHelper(E4ReadableParameterModelZCommandMarker.class);

    public Integer getValue() throws ParameterException;

    public void setTipShowingOwner(boolean tipShowsOwner);

    public boolean isTipShowingOwner();

    public String getValueString() throws ParameterException;

    public String getValueUnitlessString() throws ParameterException;

    public GeneralParameterDescriptor getParameterDescriptor();

    public void addChangeListener(ChangeListener cl);

    public void removeChangeListener(ChangeListener cl);

    public void setShowUnits(boolean showUnits);

    public boolean getShowUnits();
}
