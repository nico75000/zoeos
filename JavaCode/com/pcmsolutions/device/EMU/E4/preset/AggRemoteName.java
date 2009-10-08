package com.pcmsolutions.device.EMU.E4.preset;

import java.text.DecimalFormat;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 14-Mar-2003
 * Time: 09:07:05
 * To change this template use Options | File Templates.
 */
public class AggRemoteName {
    private static final DecimalFormat df = new DecimalFormat("0000");
    private String name;
    private Integer index;
    private String formattedIndex;

    public String toString() {
        return formattedIndex + " " + name;
    }

    public AggRemoteName(Integer index, String name) {
        this.name = name;
        this.index = index;
        this.formattedIndex = df.format(index);
    }

    public static String makeName(Integer index, String name) {
        return df.format(index) + " " + name;
    }

    public String getName() {
        return name;
    }

    public Integer getIndex() {
        return index;
    }

    public String getFormattedIndex() {
        return formattedIndex;
    }
}
