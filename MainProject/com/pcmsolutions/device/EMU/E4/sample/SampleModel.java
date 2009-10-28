package com.pcmsolutions.device.EMU.E4.sample;

import com.pcmsolutions.device.EMU.E4.gui.sample.SampleEditingMediator;


/**
 * Created by IntelliJ IDEA.
 * User: pmeehan                impl_
 * Date: 24-May-2003
 * Time: 13:30:15
 * To change this template use Options | File Templates.
 */
public interface SampleModel {
    public void setSampleContext(SampleContext pc);

    public void setSample(Integer p);

    public SampleContext getSampleContext();

    public Integer getSample();

}