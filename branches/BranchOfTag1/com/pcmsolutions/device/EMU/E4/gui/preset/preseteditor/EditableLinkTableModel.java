package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.gui.parameter2.ParameterModelTableCellEditor;
import com.pcmsolutions.device.EMU.E4.gui.preset.AbstractPresetTableCellEditor;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.AbstractPresetTableModel;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.LinkTableModel;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZCommand;
import com.pcmsolutions.system.ZDeviceNotRunningException;

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

    public EditableLinkTableModel(ContextEditablePreset p, DeviceParameterContext dpc) throws ZDeviceNotRunningException {
        super(p, dpc);
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

        public void setValue(Integer value) throws ParameterUnavailableException, ParameterValueOutOfRangeException {
            ((EditableParameterModel) pm).setValue(value);
        }

        public void setValueString(String value) throws ParameterUnavailableException, ParameterValueOutOfRangeException {
            ((EditableParameterModel) pm).setValueString(value);
        }

        public void setValueUnitlessString(String value) throws ParameterUnavailableException, ParameterValueOutOfRangeException {
            ((EditableParameterModel) pm).setValueUnitlessString(value);
        }

        public void defaultValue() throws ParameterUnavailableException, ParameterValueOutOfRangeException {
            ((EditableParameterModel) pm).defaultValue();
        }

        public void setValue(EditChainValueProvider ecvp, EditableParameterModel[] chained) throws ParameterUnavailableException, ParameterValueOutOfRangeException {
            ((EditableParameterModel) pm).setValue(ecvp, chained);
        }

        public boolean isEditChainableWith(Object o) {
            return ((EditableParameterModel) pm).isEditChainableWith(o);
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

        public LinkPresetTableCellEditor(DeviceContext d, Color bg, Color fg) throws ZDeviceNotRunningException {
            super(d, bg, fg);
        }

        protected Integer getSelectedIndex() {
            try {
                return pm.getValue();
            } catch (ParameterUnavailableException e) {
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

        protected void doAction(Object selection) {
            try {
                if (selection instanceof ReadablePreset)
                    pm.setValue(((ReadablePreset) selection).getPresetNumber());
            } catch (ParameterUnavailableException e) {
                e.printStackTrace();
            } catch (ParameterValueOutOfRangeException e) {
                e.printStackTrace();
            }
        }

        public void zDispose() {
        }
    }

    protected void buildColumnAndSectionData() {
        super.buildColumnAndSectionData();
        for (int i = 0, n = columnData.length; i < n; i++) {
            if (columnData[i].columnClass == ReadableParameterModel.class) {
                if (i == 0)
                    try {
                        columnData[i].editor = new LinkPresetTableCellEditor(preset.getPresetContext().getDeviceContext(), sectionData[columnData[i].sectionIndex].sectionBG, sectionData[columnData[i].sectionIndex].sectionFG);
                    } catch (ZDeviceNotRunningException e) {
                        columnData[i].editor = new ParameterModelTableCellEditor(sectionData[columnData[i].sectionIndex].sectionBG, sectionData[columnData[i].sectionIndex].sectionFG);
                    }
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

        protected ReadableParameterModel getAppropiateParameterModelInterface(int i) throws IllegalParameterIdException {
            EditableParameterModel pm = ((ContextEditablePreset.EditableLink) link).getEditableParameterModel(((GeneralParameterDescriptor) parameterObjects.get(i)).getId());
            if (((GeneralParameterDescriptor) parameterObjects.get(i)).getId().equals(IntPool.get(23)))
                return new LinkPresetEditableParameterModel(pm, preset.getPresetContext());
            pm.setTipShowingOwner(true);
            return pm;
        }

        public boolean isColumnEditable(int column) {
            return true;
        }

        public ZCommand[] getZCommands() {
            return cmdProviderHelper.getCommandObjects(this);
        }

        public void setLinksParam(Integer id, Integer value) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchLinkException, ParameterValueOutOfRangeException {
            ((ContextEditablePreset.EditableLink) link).setLinksParam(id, value);
        }

        public EditableParameterModel getEditableParameterModel(Integer id) throws IllegalParameterIdException {
            return ((ContextEditablePreset.EditableLink) link).getEditableParameterModel(id);
        }

        public void removeLink() throws NoSuchPresetException, PresetEmptyException, NoSuchLinkException {
            ((ContextEditablePreset.EditableLink) link).removeLink();
        }

        public void copyLink() throws NoSuchPresetException, PresetEmptyException, NoSuchLinkException, TooManyVoicesException {
            ((ContextEditablePreset.EditableLink) link).copyLink();
        }

        public PresetContext getPresetContext() {
            return ((ContextEditablePreset.EditableLink) link).getPresetContext();
        }

        public Integer[] getLinkParams(Integer[] ids) throws NoSuchPresetException, PresetEmptyException, IllegalParameterIdException, NoSuchLinkException {
            return (link).getLinkParams(ids);
        }

        public Integer getLinkNumber() {
            return link.getLinkNumber();
        }

        public void setLinkNumber(Integer link) {
            this.link.setLinkNumber(link);
        }

        public IsolatedPreset.IsolatedLink getIsolated() throws PresetEmptyException, NoSuchPresetException, NoSuchLinkException {
            return link.getIsolated();
        }

        public ReadablePreset getPreset() {
            return link.getPreset();
        }

        public Integer getPresetNumber() {
            return preset.getPresetNumber();
        }

        public ReadableParameterModel getParameterModel(Integer id) throws IllegalParameterIdException {
            return link.getParameterModel(id);
        }

        public int compareTo(Object o) {
            return link.compareTo(o);
        }
    }
}