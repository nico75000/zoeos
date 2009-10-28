package com.pcmsolutions.device.EMU.E4;

import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.system.IntPool;

import java.util.*;

/**
 * User: paulmeehan
 * Date: 02-Feb-2004
 * Time: 23:51:34
 */
class Impl_FixedGeneralParameterDescriptor extends AbstractParameterDescriptor {
    public Impl_FixedGeneralParameterDescriptor() {
    }

    public void init(Integer id, MinMaxDefault mmd, int loc) {
        super.init(id, mmd, loc);
    }
}
