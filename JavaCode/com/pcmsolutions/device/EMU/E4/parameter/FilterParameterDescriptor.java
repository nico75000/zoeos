package com.pcmsolutions.device.EMU.E4.parameter;


public interface FilterParameterDescriptor extends GeneralParameterDescriptor {

    public Integer setFilterType(Integer filterType); // defaults to 0 ( 2 pole lowpass)

    public boolean isCurrentlyActive();

    public FilterParameterDescriptor duplicate();
}

