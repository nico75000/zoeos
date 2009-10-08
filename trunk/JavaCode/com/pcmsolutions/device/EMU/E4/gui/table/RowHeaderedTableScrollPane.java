package com.pcmsolutions.device.EMU.E4.gui.table;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 05-May-2003
 * Time: 09:47:57
 * To change this template use Options | File Templates.
 */
public class RowHeaderedTableScrollPane extends JScrollPane {
    private RowHeaderedTable pvt;

    public RowHeaderedTableScrollPane(RowHeaderedTable rht, Component topLeftComponent) {
        super(rht.getTable());
        this.pvt = rht;
        this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.setWheelScrollingEnabled(false);
        this.setBackground(UIColors.getDefaultBG());
        this.setBorder(null);
        JViewport jv = new JViewport();
        jv.setView(rht.getRowHeader());
        jv.setPreferredSize(rht.getRowHeader().getPreferredSize());
        jv.setBackground(UIColors.getDefaultBG());
        this.add(topLeftComponent, JScrollPane.UPPER_LEFT_CORNER);
        setRowHeader(jv);
        getViewport().setBorder(null);
        setAlignmentX(Component.CENTER_ALIGNMENT);
        setAlignmentY(Component.CENTER_ALIGNMENT);
        setFocusable(false);
        getViewport().setBackground(UIColors.getDefaultBG());

        pvt.getTable().getModel().addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                if (e.getType() != TableModelEvent.UPDATE || (e.getType() == TableModelEvent.UPDATE && (e.getLastRow() == Integer.MAX_VALUE || e.getLastRow() == TableModelEvent.HEADER_ROW)))
                    adjustViewport();
            }
        });

        adjustViewport();
    }

    private void adjustViewport() {
        getViewport().setPreferredSize(pvt.getTable().getPreferredSize());
        setViewport(getViewport());
    }
}
