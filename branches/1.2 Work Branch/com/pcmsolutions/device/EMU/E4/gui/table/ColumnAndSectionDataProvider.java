package com.pcmsolutions.device.EMU.E4.gui.table;


/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 08-Jun-2003
 * Time: 00:40:21
 * To change this template use Options | File Templates.
 */
public interface ColumnAndSectionDataProvider {
    public ColumnData[] getColumnData();

    public SectionData[] getSectionData();

    public ColumnData getRowHeaderColumnData();

    public void addColumnAndSectionDataListener(ColumnAndSectionDataListener cdsl);

    public void removeColumnAndSectionDataListener(ColumnAndSectionDataListener cdsl);
}
