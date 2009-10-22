package com.pcmsolutions.device.EMU.E4.gui.master;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.gui.Impl_TableExclusiveSelectionContext;
import com.pcmsolutions.device.EMU.E4.gui.TitleProvider;
import com.pcmsolutions.device.EMU.E4.gui.TitleProviderListener;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTablePanel;
import com.pcmsolutions.device.EMU.E4.gui.table.PopupTable;
import com.pcmsolutions.device.EMU.E4.master.MasterContext;
import com.pcmsolutions.device.EMU.E4.parameter.DeviceParameterContext;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterCategories;
import com.pcmsolutions.device.EMU.E4.selections.MasterParameterSelection;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.gui.GriddedPanel;
import com.pcmsolutions.gui.ZCommandFactory;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;
import com.pcmsolutions.system.threads.Impl_ZThread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.tritonus.sampled.file.WaveTool;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 04-Jul-2003
 * Time: 21:45:43
 * To change this template use Options | File Templates.
 */
public class MasterPanel extends GriddedPanel implements TitleProvider, MouseListener, MasterParameterSelectionAcceptor, ZDisposable {
    protected DeviceContext device;
    protected ArrayList panels = new ArrayList();
    protected final Impl_TableExclusiveSelectionContext tsc = new Impl_TableExclusiveSelectionContext(new Impl_TableExclusiveSelectionContext.SelectionAction(){
        public void newSelection(PopupTable t) {

        }

        public void clearedSelection(PopupTable t) {

        }
    });

    public void zDispose() {
        removeAll();
        device = null;
        ZUtilities.zDisposeCollection(panels);
        panels = null;
        removeMouseListener(this);
        setTransferHandler(null);
        setDropTarget(null);
    }

    public Color getBackground() {
        return UIColors.getDefaultBG();
    }

    public Color getForeground() {
        return UIColors.getDefaultFG();
    }

    public MasterPanel init(DeviceContext dc) throws IllegalParameterIdException, DeviceException {
        this.device = dc;
        DeviceParameterContext dpc = dc.getDeviceParameterContext();
        List categoryList = dpc.getMasterContext().getCategories();
        Collections.sort(categoryList);

        int otherColIndex = 0;
        for (int i = 0, j = categoryList.size(); i < j; i++) {
            String cat = (String) categoryList.get(i);
            List ids = dpc.getMasterContext().getIdsForCategory(cat);
            List modelList = (dc.getMasterContext().getEditableParameterModels((Integer[]) ids.toArray(new Integer[ids.size()])));
            EditableParameterModel[] models = (EditableParameterModel[]) modelList.toArray(new EditableParameterModel[modelList.size()]);
            Action ref = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        MasterPanel.this.device.getMasterContext().refresh().post();
                    } catch (DeviceException e1) {
                        e1.printStackTrace();
                    } catch (ResourceUnavailableException e1) {
                        e1.printStackTrace();
                    }
                }
            };
            ref.putValue("tip", "Refresh " + cat.toUpperCase());
            RowHeaderedAndSectionedTablePanel panel;
            MasterParameterTable mpt;
            mpt = new MasterParameterTable(dc, models, cat.toUpperCase(), cat);
            tsc.addTableToContext(mpt);
            panel = new RowHeaderedAndSectionedTablePanel().init(mpt, "Show " + cat.toUpperCase(), UIColors.getTableBorder(), ref);
            panels.add(panel);
            /*
            if (cat.indexOf(ParameterCategories.MASTER_FX_SETUP) != -1)
                this.addAnchoredComponent(panel, 0, 0, 2, 1, GridBagConstraints.CENTER);
            else if (cat.indexOf(ParameterCategories.MASTER_FX_A) != -1)
                this.addAnchoredComponent(panel, 1, 0, 1, 1, GridBagConstraints.CENTER);
            else if (cat.indexOf(ParameterCategories.MASTER_FX_B) != -1)
                this.addAnchoredComponent(panel, 1, 1, 1, 1, GridBagConstraints.CENTER);
            else if (cat.indexOf(ParameterCategories.MASTER_MIDI_CONTROLLERS) != -1)
                this.addAnchoredComponent(panel, 2, 0, 1, 2, GridBagConstraints.CENTER);
            else if (cat.indexOf(ParameterCategories.MASTER_MIDI_MODE) != -1)
                this.addAnchoredComponent(panel, 2, 1, 1, 1, GridBagConstraints.CENTER);
            else if (cat.indexOf(ParameterCategories.MASTER_MIDI_PREFERENCES) != -1)
                this.addAnchoredComponent(panel, 3, 1, 1, 1, GridBagConstraints.CENTER);
            else if (cat.indexOf(ParameterCategories.MASTER_IO) != -1)
                this.addAnchoredComponent(panel, 4, 0, 1, 1, GridBagConstraints.CENTER);
            else if (cat.indexOf(ParameterCategories.MASTER_TUNING) != -1)
                this.addAnchoredComponent(panel, 4, 1, 1, 1, GridBagConstraints.CENTER);
            else if (cat.indexOf(ParameterCategories.MASTER_IMPORT_OPTIONS) != -1)
                this.addAnchoredComponent(panel, 5, 0, 1, 1, GridBagConstraints.CENTER);
            else if (cat.indexOf(ParameterCategories.MASTER_SCSI_DISK) != -1)
                this.addAnchoredComponent(panel, 5, 1, 1, 1, GridBagConstraints.CENTER);
            else
                this.addAnchoredComponent(panel, 6, otherColIndex++, 1, 1, GridBagConstraints.NONE);
                */

            if (cat.indexOf(ParameterCategories.MASTER_FX_SETUP) != -1)
                this.addAnchoredComponent(panel, 0, 0, 1, 1, GridBagConstraints.CENTER);
            else if (cat.indexOf(ParameterCategories.MASTER_FX_A) != -1)
                this.addAnchoredComponent(panel, 1, 0, 1, 1, GridBagConstraints.CENTER);
            else if (cat.indexOf(ParameterCategories.MASTER_FX_B) != -1)
                this.addAnchoredComponent(panel, 2, 0, 1, 1, GridBagConstraints.CENTER);
            else if (cat.indexOf(ParameterCategories.MASTER_MIDI_CONTROLLERS) != -1)
                this.addAnchoredComponent(panel, 4, 0, 1, 1, GridBagConstraints.CENTER);
            else if (cat.indexOf(ParameterCategories.MASTER_MIDI_MODE) != -1)
                this.addAnchoredComponent(panel, 3, 0, 1, 1, GridBagConstraints.CENTER);
            else if (cat.indexOf(ParameterCategories.MASTER_MIDI_PREFERENCES) != -1)
                this.addAnchoredComponent(panel, 5, 0, 1, 1, GridBagConstraints.CENTER);
            else if (cat.indexOf(ParameterCategories.MASTER_IO) != -1)
                this.addAnchoredComponent(panel, 6, 0, 1, 1, GridBagConstraints.CENTER);
            else if (cat.indexOf(ParameterCategories.MASTER_TUNING) != -1)
                this.addAnchoredComponent(panel, 7, 0, 1, 1, GridBagConstraints.CENTER);
            else if (cat.indexOf(ParameterCategories.MASTER_IMPORT_OPTIONS) != -1)
                this.addAnchoredComponent(panel, 8, 0, 1, 1, GridBagConstraints.CENTER);
            else if (cat.indexOf(ParameterCategories.MASTER_SCSI_DISK) != -1)
                this.addAnchoredComponent(panel, 9, 0, 1, 1, GridBagConstraints.CENTER);
            else
                this.addAnchoredComponent(panel, 10 + otherColIndex++, 0, 1, 1, GridBagConstraints.NONE);

        }
        addMouseListener(this);
        this.setTransferHandler(MasterTransferHandler.getInstance());
        return this;
    }

    public String getTitle() {
        return device.getTitle();
    }

    public String getReducedTitle() {
        return device.getReducedTitle();
    }

    public void setSelection(final MasterParameterSelection sel) {
      //  Impl_ZThread.ddTQ.postTask(new Impl_ZThread.Task(){
      //      public void doTask() {
                try {
                    sel.render(device.getMasterContext());
                } catch (DeviceException e) {
                    e.printStackTrace();
                }
     //       }
     //   });
    }

    public boolean willAcceptCategory(int category) {
        return true;
    }

    public DeviceContext getDevice() {
        return device;
    }

    public void addTitleProviderListener(TitleProviderListener tpl) {
        device.addTitleProviderListener(tpl);
    }

    public void removeTitleProviderListener(TitleProviderListener tpl) {
        device.removeTitleProviderListener(tpl);
    }

    public Icon getIcon() {
        //return hat;
        return null;
    }

    public void mouseDragged(java.awt.event.MouseEvent mouseEvent) {
    }

    public void mouseClicked(java.awt.event.MouseEvent e) {
        checkPopup(e);
    }

    public void mouseEntered(java.awt.event.MouseEvent e) {
    }

    public void mouseExited(java.awt.event.MouseEvent e) {
    }

    public void mousePressed(java.awt.event.MouseEvent e) {
        checkPopup(e);
    }

    public void mouseReleased(java.awt.event.MouseEvent e) {
        checkPopup(e);
    }

    public boolean checkPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            MasterContext mc;
            try {
                mc = device.getMasterContext();
                JMenu mm = ZCommandFactory.getMenu(new Object[]{mc}, "Master");
                JMenu dm = ZCommandFactory.getMenu(new Object[]{device}, "Device");
                JPopupMenu popup = new JPopupMenu("Master >");
                popup.add(mm);
                popup.add(dm);
                ZCommandFactory.showPopup(popup, this, e);
                return true;
            } catch (DeviceException e1) {
                e1.printStackTrace();
            }
        }
        return false;
    }
}
