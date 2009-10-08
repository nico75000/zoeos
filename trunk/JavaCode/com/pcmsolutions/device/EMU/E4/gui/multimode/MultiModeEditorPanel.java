package com.pcmsolutions.device.EMU.E4.gui.multimode;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTablePanel;
import com.pcmsolutions.device.EMU.E4.multimode.MultiModeContext;
import com.pcmsolutions.gui.ZCommandInvocationHelper;
import com.pcmsolutions.system.ZDeviceNotRunningException;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.threads.ZDBModifyThread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


public class MultiModeEditorPanel extends JPanel implements MouseListener , ZDisposable{
    private DeviceContext device;

    private RowHeaderedAndSectionedTablePanel mmp;

    public void zDispose() {
        removeAll();
        device = null;
        mmp.zDispose();
        //ZUtilities.zDisposeCollection(panels);
        //panels = null;
        removeMouseListener(this);
        setTransferHandler(null);
        setDropTarget(null);
    }

    public MultiModeEditorPanel init(final DeviceContext device, boolean just16, Action utilityAction) throws ZDeviceNotRunningException {
        AbstractAction rmm = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                new ZDBModifyThread("Refresh Multimode") {
                    public void run() {
                        try {
                            device.getMultiModeContext().refresh();
                        } catch (ZDeviceNotRunningException e1) {
                            e1.printStackTrace();
                        }
                    }
                }.start();
            }
        };
        rmm.putValue("tip", "Refresh Multimode");
        this.device = device;
        mmp = new RowHeaderedAndSectionedTablePanel();
        mmp.init(new MultiModeTable(device, just16), "Show MultiMode", UIColors.getTableBorder(), rmm);
        //mmp.getHideButton().setAction(null);
        if (utilityAction != null) {
            mmp.getHideButton().setAction(utilityAction);
            mmp.getHideButton().setToolTipText(utilityAction.getValue("tip").toString());
        }

        //this.setLayout(new GridLayout(1, 1, 0, 0));
        this.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        add(mmp);
        addMouseListener(this);
        return this;
    }

    public Color getBackground() {
        return UIColors.getDefaultBG();
    }

    public Color getForeground() {
        return UIColors.getDefaultFG();
    }

    public DeviceContext getDevice() {
        return device;
    }

    public void mouseDragged(MouseEvent mouseEvent) {
    }

    public void mouseClicked(MouseEvent e) {
        checkPopup(e);
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        checkPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        checkPopup(e);
    }

    public boolean checkPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            Object[] sels = new Object[0];
            MultiModeContext mmc;
            try {
                mmc = this.device.getMultiModeContext();
            } catch (ZDeviceNotRunningException e1) {
                return false;
            }
            sels = new Object[]{mmc};
            ZCommandInvocationHelper.showPopup("Multimode >", this, sels, e);
            return true;
        }
        return false;
    }
}
