package com.pcmsolutions.gui.midi;

import com.pcmsolutions.comms.ZMidiSystem;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.AbstractRowHeaderedAndSectionedTableModel;
import com.pcmsolutions.device.EMU.E4.gui.table.ColumnData;
import com.pcmsolutions.device.EMU.E4.gui.table.SectionData;
import com.pcmsolutions.system.IntPool;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.*;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 02-Sep-2003
 * Time: 15:01:26
 * To change this template use Options | File Templates.
 */
public class MidiManagerTableModel extends AbstractRowHeaderedAndSectionedTableModel implements ZMidiSystem.MidiSystemListener {


    protected void buildColumnAndSectionData() {
        rowHeaderColumnData = new ColumnData("", 150, JLabel.LEFT, 0, Object.class);
        columnData = new ColumnData[5];
        columnData[0] = new ColumnData("Permitted", 60, JLabel.LEFT, 0, String.class, null, null);
        //columnData[1] = new ColumnData("Never referencedClose", 60, JLabel.LEFT, 0, String.class, null, null);
        columnData[1] = new ColumnData("References", 60, JLabel.LEFT, 0, String.class, null, null);
        columnData[2] = new ColumnData("Description", 200, JLabel.LEFT, 0, String.class, null, null);
        columnData[3] = new ColumnData("Vendor", 150, JLabel.LEFT, 0, String.class, null, null);
        columnData[4] = new ColumnData("Version", 80, JLabel.LEFT, 0, String.class, null, null);
        sectionData = new SectionData[]{new SectionData(UIColors.getTableFirstSectionBG(),UIColors.getTableFirstSectionHeaderBG(), UIColors.getTableFirstSectionFG(), 550, "")};
    }

    protected void doRefresh() {
        final ZMidiSystem midi = ZMidiSystem.getInstance();
        if (midi != null) {
            midi.removeMidiSystemListener(this);
            midi.addMidiSystemListener(this);
            MidiDevice.Info[] devices = midi.getAllDevices();
            Arrays.sort(devices, new Comparator() {
                public int compare(Object o1, Object o2) {
                    return o1.toString().compareTo(o2.toString());
                }
            });
            for (int i = 0, n = devices.length; i < n; i++) {
                final MidiDevice.Info di = devices[i];
                tableRowObjects.add(new ColumnValueProvider() {
                    public Object getValueAt(int col) {
                        switch (col) {
                            case 0:
                                return di;
                            case 1:
                                try {
                                    return (midi.isPortPermitted(di) ? "Yes" : "no");
                                } catch (MidiUnavailableException e) {
                                    return "unknown";
                                }
                            /*case 2:
                                try {
                                    return (midi.isPortNeverToBeClosed(di) ? "Yes" : "no");
                                } catch (MidiUnavailableException e) {
                                    return "unknown";
                                }
                                */
                            case 2:
                                try {
                                    return IntPool.get(midi.getRefCount(di)).toString();
                                } catch (MidiUnavailableException e) {
                                    e.printStackTrace();
                                }
                            case 3:
                                return di.getDescription();
                            case 4:
                                return di.getVendor();
                            case 5:
                                return di.getVersion();
                        }
                        return "";
                    }

                    public void zDispose() {
                    }
                });
            }
        }
    }

    protected void doPreRefresh() {
    }

    protected void doPostRefresh() {
    }

    public void midiSystemChanged(ZMidiSystem msf) {
        refresh(false);
    }

    public void zDispose() {
        super.zDispose();
        if (ZMidiSystem.getInstance() != null)
            ZMidiSystem.getInstance().removeMidiSystemListener(this);
    }
}
