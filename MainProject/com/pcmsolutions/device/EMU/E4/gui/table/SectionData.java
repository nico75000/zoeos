package com.pcmsolutions.device.EMU.E4.gui.table;

import java.awt.*;
import java.awt.event.MouseListener;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 06-Jun-2003
 * Time: 22:09:32
 * To change this template use Options | File Templates.
 */
public class SectionData {
    public String sectionName;
    public int sectionWidth;
    public Color sectionHeaderBG;
    public Color sectionBG;
    public Color sectionFG;
    public MouseListener ml;

    public SectionData(Color sectionBG,Color sectionHeaderBG, Color sectionFG, int sectionLength, String sectionName) {
        this(sectionBG, sectionHeaderBG,sectionFG, sectionLength, sectionName, null);
    }

    public SectionData(Color sectionBG, Color sectionHeaderBG,Color sectionFG, int sectionLength, String sectionName, MouseListener ml) {
        this.sectionBG = sectionBG;
        this.sectionFG = sectionFG;
        this.sectionWidth = sectionLength;
        this.sectionName = sectionName;
        this.sectionHeaderBG = sectionHeaderBG;
        this.ml = ml;
    }
}
