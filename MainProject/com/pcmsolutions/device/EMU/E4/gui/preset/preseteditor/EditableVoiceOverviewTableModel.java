package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor;

import com.pcmsolutions.device.EMU.E4.*;
import com.pcmsolutions.device.EMU.E4.gui.parameter2.ParameterModelTableCellEditor;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.AbstractPresetTableModel;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.VoiceOverviewTableModel;
import com.pcmsolutions.device.EMU.E4.gui.sample.AbstractSampleTableCellEditor;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.E4.sample.ReadableSample;
import com.pcmsolutions.device.EMU.E4.sample.SampleContext;
import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZCommand;
import com.pcmsolutions.system.threads.Impl_ZThread;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class EditableVoiceOverviewTableModel extends VoiceOverviewTableModel {

    public EditableVoiceOverviewTableModel(ContextEditablePreset p, DeviceParameterContext dpc, int mode) {
        super(p, dpc, mode);
    }

    /*public boolean isCellEditable(int rowIndex, int columnIndex) {

        if (columnIndex == 0)
            return false;
        if (tableRowObjects.get(rowIndex) instanceof AbstractPresetTableModel.ColumnEditableTester)
            return ((AbstractPresetTableModel.ColumnEditableTester) tableRowObjects.get(rowIndex)).isColumnEditable(columnIndex);
        return false;
    } */

    protected void buildColumnAndSectionData() {
        super.buildColumnAndSectionData();
        for (int i = 0, n = columnData.length; i < n; i++) {
            if (columnData[i].columnClass == ReadableParameterModel.class) {
                if (i == 1)
                    try {
                        columnData[i].editor = new VoiceSampleTableCellEditor(preset.getPresetContext().getDeviceContext(), sectionData[columnData[i].sectionIndex].sectionBG, sectionData[columnData[i].sectionIndex].sectionFG);
                    } catch (Exception e) {
                        columnData[i].editor = new ParameterModelTableCellEditor(sectionData[columnData[i].sectionIndex].sectionBG, sectionData[columnData[i].sectionIndex].sectionFG);
                    }
                else
                    columnData[i].editor = new ParameterModelTableCellEditor(sectionData[columnData[i].sectionIndex].sectionBG, sectionData[columnData[i].sectionIndex].sectionFG);
                columnData[i].columnClass = EditableParameterModel.class;
            }
        }
    }

    protected static class VoiceSampleEditableParameterModel extends VoiceOverviewTableModel.VoiceSampleReadableParameterModel implements EditableParameterModel, ParameterModelWrapper {
        public VoiceSampleEditableParameterModel(EditableParameterModel pm, SampleContext sc) {
            super(pm, sc);
        }

        public void setValue(Integer value) throws ParameterException {
            ((EditableParameterModel) pm).setValue(value);
        }

        public void offsetValue(Integer offset) throws ParameterException {
            ((EditableParameterModel) pm).offsetValue(offset);
        }

        public void offsetValue(Double offsetAsFOR) throws ParameterException {
            ((EditableParameterModel) pm).offsetValue(offsetAsFOR);
        }

        public void setValueString(String value) throws ParameterException {
            ((EditableParameterModel) pm).setValueString(value);
        }

        public void setValueUnitlessString(String value) throws ParameterException {
            ((EditableParameterModel) pm).setValueUnitlessString(value);
        }

        public void defaultValue() throws ParameterException {
            ((EditableParameterModel) pm).defaultValue();
        }

        public boolean getShowUnits() {
            return pm.getShowUnits();
        }

        public Object[] getWrappedObjects() {
            return new Object[]{pm};
        }
    }

    class VoiceSampleTableCellEditor extends AbstractSampleTableCellEditor {
        private EditableParameterModel pm;

        public VoiceSampleTableCellEditor(DeviceContext d, Color bg, Color fg) throws DeviceException {
            super(d, bg, fg);
        }

        protected Integer getSelectedIndex() {
            try {
                Integer rv = pm.getValue();
                return IntPool.get(rv.intValue() + negativeSampleCount);
            } catch (ParameterException e) {
            }
            return IntPool.get(0);
        }

        protected void buildSampleItemsList(DeviceContext d) throws DeviceException {
            try {
                sampleItems = new ArrayList();
                try {
                    if (d.getDeviceParameterContext().getVoiceContext().getParameterDescriptor(ID.sample).getMMD().getMin().intValue() < -1) {
                        sampleItems.add(ADAT_7_8);
                        sampleItems.add(ADAT_5_6);
                        sampleItems.add(ADAT_3_4);
                        sampleItems.add(ADAT_1_2);

                        sampleItems.add(EXTERNAL_ADC2);
                        sampleItems.add(EXTERNAL_ADC1);
                        sampleItems.add(AES_EBU_INPUT);
                        sampleItems.add(SAMPLING_ADC);

                        negativeSampleCount = 8;
                    }
                } catch (IllegalParameterIdException e) {
                    e.printStackTrace();
                }
                sampleItems.addAll(d.getDefaultSampleContext().getDatabaseSamples());
            } catch (NoSuchContextException e) {
            }
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected,
                                                     int row, int column) {
            pm = (EditableParameterModel) value;
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }

        protected void doAction(Object selection) {
            Integer sn;
            if (selection instanceof ReadableSample)
                sn = ((ReadableSample) selection).getIndex();
            else
                sn = IntPool.get(subZeroStringToIndex(selection.toString()));

           final Integer f_sn = sn;
        //    Impl_ZThread.parameterTQ.postTask(new Impl_ZThread.Task(){
          //      public void doTask() {
                    try {
                        pm.setValue(f_sn);
                    } catch (ParameterException e) {
                        e.printStackTrace();
                    }
          //      }
          //  });
        }

        public void zDispose() {
            pm = null;
        }
    }

    protected void newVoice(boolean expanded) {
        new Voice((ContextEditablePreset.EditableVoice) getVoiceInterface(numVoices)).init(expanded);
    }

    protected ReadablePreset.ReadableVoice getVoiceInterface(int index) {
        return ((ContextEditablePreset) preset).getEditableVoice(IntPool.get(index));
    }

    protected class Voice extends VoiceOverviewTableModel.Voice implements AbstractPresetTableModel.ColumnEditableTester, ContextEditablePreset.EditableVoice {
        public Voice(ContextEditablePreset.EditableVoice v) {
            super(v);
        }

        protected ReadableParameterModel getAppropiateParameterModelInterface(int i) throws ParameterException  {
            EditableParameterModel pm = ((ContextEditablePreset.EditableVoice) voice).getEditableParameterModel(((GeneralParameterDescriptor) parameterObjects.get(i)).getId());
            if (((GeneralParameterDescriptor) parameterObjects.get(i)).getId().equals(ID.sample))
                try {
                    return new VoiceSampleEditableParameterModel(pm, preset.getDeviceContext().getDefaultSampleContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            pm.setTipShowingOwner(true);
            return pm;
        }

        protected ReadablePreset.ReadableVoice.ReadableZone getAppropiateZoneInterface(int i) {
            return ((ContextEditablePreset.EditableVoice) voice).getEditableZone(IntPool.get(i));
        }

        protected VoiceOverviewTableModel.Voice.Zone newZone() {
            return new Zone(((ContextEditablePreset.EditableVoice) voice).getEditableZone(IntPool.get(numZones)));
        }

        public boolean isColumnEditable(int column) {
            try {
                if ((column == sampleCol || column == origKeyCol) && hasZones()) // sample number or orig key
                    return false;
            } catch (Exception e) {
                return false;
            }
            return true;
        }

        public ContextEditablePreset.EditableVoice duplicate() {
            return ((ContextEditablePreset.EditableVoice) voice).duplicate();
        }

        public void setGroupMode(boolean groupMode) {
            ((ContextEditablePreset.EditableVoice) voice).setGroupMode(groupMode);
        }

        public boolean getGroupMode() {
            return ((ContextEditablePreset.EditableVoice) voice).getGroupMode();
        }

        public void trySetOriginalKeyFromSampleName() throws ParameterException, PresetException {
            ((ContextEditablePreset.EditableVoice) voice).trySetOriginalKeyFromSampleName();
        }

        public void setVoiceParam(Integer id, Integer value) throws PresetException {
            ((ContextEditablePreset.EditableVoice) voice).setVoiceParam(id, value);
        }

        public void offsetVoiceParam(Integer id, Integer offset) throws PresetException {
            ((ContextEditablePreset.EditableVoice) voice).offsetVoiceParam(id, offset);
        }

        public void offsetVoiceParam(Integer id, Double offsetAsFOR) throws PresetException {
            ((ContextEditablePreset.EditableVoice) voice).offsetVoiceParam(id, offsetAsFOR);
        }

        public EditableParameterModel getEditableParameterModel(Integer id) throws ParameterException {
            return ((ContextEditablePreset.EditableVoice) voice).getEditableParameterModel(id);
        }

        public void removeVoice() throws PresetException {
            ((ContextEditablePreset.EditableVoice) voice).removeVoice();
        }

        public void splitVoice(int splitKey) throws PresetException {
            ((ContextEditablePreset.EditableVoice) voice).splitVoice(splitKey);
        }

        public void expandVoice() throws PresetException {
            ((ContextEditablePreset.EditableVoice) voice).expandVoice();
        }

        public void combineVoiceGroup() throws ParameterException, PresetException, EmptyException {
            ((ContextEditablePreset.EditableVoice) voice).combineVoiceGroup();
        }

        public void copyVoice() throws ParameterException, PresetException, EmptyException {
            ((ContextEditablePreset.EditableVoice) voice).copyVoice();
        }

        public void newZones(Integer num) throws PresetException {
            ((ContextEditablePreset.EditableVoice) voice).newZones(num);
        }

        public PresetContext getPresetContext() {
            return ((ContextEditablePreset.EditableVoice) voice).getPresetContext();
        }

        public ContextEditablePreset.EditableVoice.EditableZone getEditableZone(Integer zone) {
            return ((ContextEditablePreset.EditableVoice) voice).getEditableZone(zone);
        }

        public IsolatedPreset.IsolatedVoice.IsolatedZone getIsolatedZone(Integer zone) throws PresetException, EmptyException {
            return ((ContextEditablePreset.EditableVoice) voice).getIsolatedZone(zone);
        }

        public Integer[] getVoiceParams(Integer[] ids) throws ParameterException, PresetException, EmptyException {
            return voice.getVoiceParams(ids);
        }

        public Integer getVoiceNumber() {
            return voice.getVoiceNumber();
        }

        public void setVoiceNumber(Integer voice) {
            this.voice.setVoiceNumber(voice);
        }

        public IsolatedPreset.IsolatedVoice getIsolated() throws PresetException, EmptyException {
            return voice.getIsolated();
        }

        public ReadablePreset.ReadableVoice.ReadableZone getReadableZone(Integer zone) {
            return null;
        }

        public ReadableParameterModel getParameterModel(Integer id) throws ParameterException {
            return (voice).getParameterModel(id);
        }

        public ZCommand[] getZCommands(Class markerClass) {
            return ContextEditablePreset.EditableVoice.cmdProviderHelper.getCommandObjects(null, this);
        }

        protected class Zone extends VoiceOverviewTableModel.Voice.Zone implements AbstractPresetTableModel.ColumnEditableTester, ContextEditablePreset.EditableVoice.EditableZone {
            public Zone(ContextEditablePreset.EditableVoice.EditableZone z) {
                super(z);
            }

            protected ReadableParameterModel getAppropiateParameterModelInterface(GeneralParameterDescriptor pd) throws ParameterException {
                EditableParameterModel pm = ((ContextEditablePreset.EditableVoice.EditableZone) zone).getEditableParameterModel(pd.getId());
                if (pd.getId().equals(ID.sample))
                    try {
                        return new VoiceSampleEditableParameterModel(pm, ((ContextEditablePreset.EditableVoice.EditableZone) zone).getPresetContext().getDeviceContext().getDefaultSampleContext());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                pm.setTipShowingOwner(true);
                return pm;
            }

            public boolean isColumnEditable(int column) {
                if (!getValueAt(column).equals(""))
                    return true;
                return false;
            }

            public void setZoneParam(Integer id, Integer value) throws ParameterException, PresetException, EmptyException {
                ((ContextEditablePreset.EditableVoice.EditableZone) zone).setZoneParam(id, value);
            }

            public void offsetZoneParam(Integer id, Integer offset) throws PresetException {
                ((ContextEditablePreset.EditableVoice.EditableZone) zone).offsetZoneParam(id, offset);
            }

            public void offsetZoneParam(Integer id, Double offsetAsFOR) throws PresetException {
                ((ContextEditablePreset.EditableVoice.EditableZone) zone).offsetZoneParam(id, offsetAsFOR);
            }

            public EditableParameterModel getEditableParameterModel(Integer id) throws ParameterException {
                return ((ContextEditablePreset.EditableVoice.EditableZone) zone).getEditableParameterModel(id);
            }

            public void trySetOriginalKeyFromSampleName() throws ParameterException, PresetException, EmptyException {
                ((ContextEditablePreset.EditableVoice.EditableZone) zone).trySetOriginalKeyFromSampleName();
            }

            public void removeZone() throws PresetException {
                ((ContextEditablePreset.EditableVoice.EditableZone) zone).removeZone();
            }

            public PresetContext getPresetContext() {
                return ((ContextEditablePreset.EditableVoice.EditableZone) zone).getPresetContext();
            }

            public Integer[] getZoneParams(Integer[] ids) throws ParameterException, PresetException, EmptyException {
                return (zone).getZoneParams(ids);
            }

            public Integer getVoiceNumber() {
                return (zone).getVoiceNumber();
            }

            public Integer getPresetNumber() {
                return preset.getIndex();
            }

            public Integer getZoneNumber() {
                return zone.getZoneNumber();
            }

            public ReadablePreset getPreset() {
                return zone.getPreset();
            }

            public IsolatedPreset.IsolatedVoice.IsolatedZone getIsolated() throws PresetException, EmptyException {
                return zone.getIsolated();
            }

            public void setZoneNumber(Integer zone) {
                this.zone.setZoneNumber(zone);
            }

            public ReadableParameterModel getParameterModel(Integer id) throws ParameterException {
                return zone.getParameterModel(id);
            }

            public ZCommand[] getZCommands(Class markerClass) {
                return EditableZone.cmdProviderHelper.getCommandObjects(markerClass, this);
            }

            // most capable/super first
            public Class[] getZCommandMarkers() {
                return EditableZone.cmdProviderHelper.getSupportedMarkers();
            }
        }
    }
}
