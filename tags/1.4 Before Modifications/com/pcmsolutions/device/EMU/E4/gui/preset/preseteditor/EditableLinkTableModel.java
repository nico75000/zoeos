package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.gui.parameter2.ParameterModelTableCellEditor;
import com.pcmsolutions.device.EMU.E4.gui.preset.AbstractPresetTableCellEditor;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.AbstractPresetTableModel;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.LinkTableModel;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZCommand;
import com.pcmsolutions.system.threads.Impl_ZThread;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 09-Jun-2003
 * Time: 10:00:08
 * To change this template use Options | File Templates.
 */
public class EditableLinkTableModel extends LinkTableModel {

    public EditableLinkTableModel(ContextEditablePreset p, DeviceParameterContext dpc, int mode) {
        super(p, dpc, mode);
    }

    /*public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 0)
            return false;
        if (tableRowObjects.get(rowIndex) instanceof AbstractPresetTableModel.ColumnEditableTester)
            return ((AbstractPresetTableModel.ColumnEditableTester) tableRowObjects.get(rowIndex)).isColumnEditable(columnIndex);
        return false;
    } */

    protected static class LinkPresetEditableParameterModel extends LinkTableModel.LinkPresetReadableParameterModel implements ParameterModelWrapper, EditableParameterModel {
        public LinkPresetEditableParameterModel(EditableParameterModel pm, PresetContext pc) {
            super(pm, pc);
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

    private class LinkPresetTableCellEditor extends AbstractPresetTableCellEditor {
        private EditableParameterModel pm;

        public LinkPresetTableCellEditor(DeviceContext d, Color bg, Color fg) {
            super(d, bg, fg);
        }

        protected Integer getSelectedIndex() {
            try {
                return pm.getValue();
            } catch (ParameterException e) {
                e.printStackTrace();
            }
            return IntPool.get(0);
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected,
                                                     int row, int column) {
            pm = (EditableParameterModel) value;
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }

        protected void doAction(final Object selection) {
            if (selection instanceof ReadablePreset)
             //   Impl_ZThread.parameterTQ.postTask(new Impl_ZThread.Task(){
              //      public void doTask() {
                        try {
                            pm.setValue(((ReadablePreset) selection).getIndex());
                        } catch (ParameterException e) {
                            e.printStackTrace();
                        }
            //        }
             //   });
        }

        public void zDispose() {
        }
    }

    protected void buildColumnAndSectionData() {
        super.buildColumnAndSectionData();
        for (int i = 0, n = columnData.length; i < n; i++) {
            if (columnData[i].columnClass == ReadableParameterModel.class) {
                if (i == 0)
                   // try {
                        columnData[i].editor = new LinkPresetTableCellEditor(preset.getPresetContext().getDeviceContext(), sectionData[columnData[i].sectionIndex].sectionBG, sectionData[columnData[i].sectionIndex].sectionFG);
                   // } catch (ZDeviceNotRunningException e) {
                  //      columnData[i].editor = new ParameterModelTableCellEditor(sectionData[columnData[i].sectionIndex].sectionBG, sectionData[columnData[i].sectionIndex].sectionFG);
                //    }
                else
                    columnData[i].editor = new ParameterModelTableCellEditor(sectionData[columnData[i].sectionIndex].sectionBG, sectionData[columnData[i].sectionIndex].sectionFG);
                columnData[i].columnClass = EditableParameterModel.class;
            }
        }
    }

    protected void newLink() {
        new Link(((ContextEditablePreset) preset).getEditableLink(IntPool.get(numLinks))).init();
    }

    protected class Link extends LinkTableModel.Link implements AbstractPresetTableModel.ColumnEditableTester, ContextEditablePreset.EditableLink {
        public Link(ContextEditablePreset.EditableLink l) {
            super(l);
        }

        protected ReadableParameterModel getAppropiateParameterModelInterface(int i) throws ParameterException {
            EditableParameterModel pm = ((ContextEditablePreset.EditableLink) link).getEditableParameterModel(((GeneralParameterDescriptor) parameterObjects.get(i)).getId());
            if (((GeneralParameterDescriptor) parameterObjects.get(i)).getId().equals(IntPool.get(23)))
                return new LinkPresetEditableParameterModel(pm, preset.getPresetContext());
            pm.setTipShowingOwner(true);
            return pm;
        }

        public boolean isColumnEditable(int column) {
            return true;
        }

        public ZCommand[] getZCommands(Class markerClass) {
            return ContextEditablePreset.EditableLink.cmdProviderHelper.getCommandObjects(markerClass, this);
        }

        // most capable/super first
        public Class[] getZCommandMarkers() {
            return ContextEditablePreset.EditableLink.cmdProviderHelper.getSupportedMarkers();
        }

        public void setLinkParam(Integer id, Integer value) throws PresetException {
            ((ContextEditablePreset.EditableLink) link).setLinkParam(id, value);
        }

        public void offsetLinkParam(Integer id, Integer offset) throws PresetException {
            ((ContextEditablePreset.EditableLink) link).offsetLinkParam(id, offset);
        }

        public void offsetLinkParam(Integer id, Double offsetAsFOR) throws PresetException {
            ((ContextEditablePreset.EditableLink) link).offsetLinkParam(id, offsetAsFOR);
        }

        public EditableParameterModel getEditableParameterModel(Integer id) throws ParameterException {
            return ((ContextEditablePreset.EditableLink) link).getEditableParameterModel(id);
        }

        public void removeLink() throws PresetException {
            ((ContextEditablePreset.EditableLink) link).removeLink();
        }

        public void copyLink() throws PresetException, EmptyException {
            ((ContextEditablePreset.EditableLink) link).copyLink();
        }

        public PresetContext getPresetContext() {
            return ((ContextEditablePreset.EditableLink) link).getPresetContext();
        }

        public Integer[] getLinkParams(Integer[] ids) throws ParameterException, PresetException, EmptyException {
            return (link).getLinkParams(ids);
        }

        public Integer getLinkNumber() {
            return link.getLinkNumber();
        }

        public void setLinkNumber(Integer link) {
            this.link.setLinkNumber(link);
        }

        public IsolatedPreset.IsolatedLink getIsolated() throws PresetException, EmptyException {
            return link.getIsolated();
        }

        public ReadablePreset getPreset() {
            return link.getPreset();
        }

        public Integer getPresetNumber() {
            return preset.getIndex();
        }

        public ReadableParameterModel getParameterModel(Integer id) throws ParameterException {
            return link.getParameterModel(id);
        }

        public int compareTo(Object o) {
            return link.compareTo(o);
        }
    }
}