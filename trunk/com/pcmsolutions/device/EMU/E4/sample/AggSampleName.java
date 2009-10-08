package com.pcmsolutions.device.EMU.E4.sample;

import java.text.DecimalFormat;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 14-Mar-2003
 * Time: 09:07:05
 * To change this template use Options | File Templates.
 */
public class AggSampleName {
    private final DecimalFormat df = new DecimalFormat("0000");
    private String name;
    private Integer sample;

    public String toString() {
        return df.format(sample) + " " + name;
    }

    public AggSampleName(Integer sample, String name) {
        this.name = name;
        this.sample = sample;
    }

    public String getName() {
        return name;
    }

    public Integer getSample() {
        return sample;
    }
}
