package com.pcmsolutions.device.EMU.E4.gui;

import com.pcmsolutions.device.EMU.E4.gui.table.PopupTable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Map;

/**
 * User: paulmeehan
 * Date: 02-May-2004
 * Time: 10:07:49
 */
public interface TableExclusiveSelectionContext {
    void addTableToContext(PopupTable t);
    public ArrayList getTables();
}
