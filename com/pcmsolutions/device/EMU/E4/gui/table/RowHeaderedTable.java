package com.pcmsolutions.device.EMU.E4.gui.table;

import com.pcmsolutions.system.ZDisposable;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 06-Jun-2003
 * Time: 21:32:59
 * To change this template use Options | File Templates.
 */
public interface RowHeaderedTable extends ZDisposable {
    public JTable getRowHeader();

    public JTable getTable();
}
