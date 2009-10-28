package com.pcmsolutions.gui.desktop;

import com.jidesoft.docking.DockContext;
import com.jidesoft.docking.DockingManager;
import com.pcmsolutions.device.EMU.E4.gui.device.DeviceIcon;
import com.pcmsolutions.device.EMU.E4.gui.device.PropertiesIcon;
import com.pcmsolutions.device.EMU.E4.gui.multimode.MultimodeIcon;
import com.pcmsolutions.device.EMU.E4.gui.preset.icons.PresetContextIcon;
import com.pcmsolutions.device.EMU.E4.gui.sample.samplecontext.SampleContextIcon;
import com.pcmsolutions.gui.ComponentGenerationException;
import com.pcmsolutions.gui.ProgressSession;
import com.pcmsolutions.gui.piano.PianoIcon;
import com.pcmsolutions.system.Linkable;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.ZoeosPreferences;
import com.pcmsolutions.system.paths.ViewPath;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.MutableTreeNode;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: paulmeehan
 * Date: 23-Jan-2004
 * Time: 06:15:19
 */
public class Impl_ZDesktopManager implements ZDesktopManager, ZDisposable, ChangeListener {
    protected DockingManager dockingManager;
    protected final ZDocumentTreeModel workspaceViewTreeModel;

    public Impl_ZDesktopManager(DockingManager dockingManager) {
        this.dockingManager = dockingManager;
        workspaceViewTreeModel = new ZDocumentTreeModel(new ZDocumentPane(null));
        //dockingManager.setInitSplitPriority(DefaultDockingManager.SPLIT_SOUTH_NORTH_EAST_WEST);
        initDocks();
        //ZoeosPreferences.ZPREF_boxStyleTabs.addChangeListener(this);
    }

    public ZDocumentTreeModel getWorkspaceViewTreeModel() {
        return workspaceViewTreeModel;
    }

    public DockingManager getDockingManager() {
        return dockingManager;
    }

    public String toString() {
        return "views";
    }

    class ZCanonicalDock extends ZDockableFrame {
        private ZDocumentTreeModel documentTreeModel = new ZDocumentTreeModel(new ZDockDocumentPane(this, null));

        public ZCanonicalDock(String name, String title, String tabTitle) {
            super(name);
            setTitle(title);
            setTabTitle(tabTitle);
            getContentPane().add((documentTreeModel.getRootDocumentPane()));
            this.setPreferredSize(new Dimension(265, 500));
            this.setHidable(false);
            getContext().setInitMode(DockContext.STATE_AUTOHIDE);
            getContext().setInitSide(DockContext.DOCK_SIDE_EAST);
        }

        public ZDocumentTreeModel getDocumentTreeModel() {
            return documentTreeModel;
        }
    }

    class PresetsDock extends ZCanonicalDock {
        public PresetsDock() {
            super(dockPRESETS, dockPRESETS, dockPRESETS);
            setFrameIcon(new PresetContextIcon(14, 14));
            getContext().setInitIndex(0);
        }
    }

    class SamplesDock extends ZCanonicalDock {
        public SamplesDock() {
            super(dockSAMPLES, dockSAMPLES, dockSAMPLES);
            setFrameIcon(new SampleContextIcon(14, 14));
            getContext().setInitIndex(0);
        }
    }

    class MultiDock extends ZCanonicalDock {
        public MultiDock() {
            super(dockMULTI, dockMULTI, dockMULTI);
            setFrameIcon(new MultimodeIcon(15, 15));
            getContext().setInitIndex(0);
            this.setPreferredSize(new Dimension(425, 500));
        }
    }

    class MasterDock extends ZCanonicalDock {
        public MasterDock() {
            super(dockMASTER, dockMASTER, dockMASTER);
            URL url = MasterDock.class.getResource("/hat-icon.gif");
            if (url != null)
                setFrameIcon(new ImageIcon(url));
            getContext().setInitIndex(0);
        }
    }

    class DeviceDock extends ZCanonicalDock {
        public DeviceDock() {
            super(dockDEVICES, dockDEVICES, dockDEVICES);
            setFrameIcon(new DeviceIcon(20, 16));
            getContext().setInitIndex(0);
        }
    }

    class PropertiesDock extends ZCanonicalDock {
        public PropertiesDock() {
            super(dockPROPERTIES, dockPROPERTIES, dockPROPERTIES);
            setFrameIcon(new PropertiesIcon(20, 16));
            getContext().setInitIndex(0);
            this.setPreferredSize(new Dimension(400, 500));
        }
    }

    class PianoDock extends ZCanonicalDock {
        public PianoDock() {
            super(dockPIANO, dockPIANO, dockPIANO);
            setFrameIcon(new PianoIcon(20, 16));
            getContext().setInitIndex(0);
            getContext().setInitSide(DockContext.DOCK_SIDE_SOUTH);
            this.setPreferredSize(new Dimension(400, 500));
        }
    }

    private final ZCanonicalDock dockPresets = new PresetsDock();
    private final ZCanonicalDock dockSamples = new SamplesDock();
    private final ZCanonicalDock dockMulti = new MultiDock();
    private final ZCanonicalDock dockMaster = new MasterDock();
    private final ZCanonicalDock dockDevices = new DeviceDock();
    private final ZCanonicalDock dockProperties = new PropertiesDock();
    private final ZCanonicalDock dockPiano = new PianoDock();

    private void initDocks() {
        getDockingManager().beginLoadLayoutData();

        getDockingManager().addFrame(dockPresets);
        getDockingManager().addFrame(dockSamples);
        getDockingManager().addFrame(dockMulti);
        getDockingManager().addFrame(dockMaster);
        getDockingManager().addFrame(dockDevices);
        getDockingManager().addFrame(dockProperties);
        //getDockingManager().addFrame(dockPiano);

        getDockingManager().loadLayoutDataFrom(ZDesktopManager.LAYOUT_LAST);
        //getDockingManager().loadLayoutDataFromFile("ZoeOSLayout");
        //getDockingManager().loadLayoutDataFromFile("c://Documents and Settings//paulmeehan//.profile//default.layout");
    }

    public boolean addDesktopElement(DesktopElement e) throws ComponentGenerationException, ChildViewNotAllowedException, LogicalHierarchyException {
        return addDesktopElement(e, false);
    }

    public boolean addDesktopElement(DesktopElement e, boolean activate) throws ComponentGenerationException, ChildViewNotAllowedException, LogicalHierarchyException {
        String dock = e.getViewPath().getViewEntryPoint();
        if (dock.equals(dockWORKSPACE)) {
            return workspaceViewTreeModel.addDesktopElement(e, activate);
        } else if (dock.equals(dockDEVICES)) {
            //getDockingManager().setFrameAvailable(dockDEVICES);
            //dockDevices.setHidable(false);
            if (dockDevices.isHidden()) {
                getDockingManager().showFrame(dockDEVICES);
                dockDevices.setHidable(false);
            }
            //  return dockDevices.getZDocumentPane().openDesktopElement(e, activate);
            return dockDevices.getDocumentTreeModel().addDesktopElement(e, activate);
        } else if (dock.equals(dockMASTER)) {
            //   getDockingManager().setFrameAvailable(dockMASTER);
            // dockMaster.setHidable(false);
            if (dockMaster.isHidden()) {
                getDockingManager().showFrame(dockMASTER);
                dockMaster.setHidable(false);
            }
            // return dockMaster.getZDocumentPane().openDesktopElement(e, activate);
            return dockMaster.getDocumentTreeModel().addDesktopElement(e, activate);
            /*
            boolean active = dockMaster.isActive();
            try {
                try {
                    dockMaster.setActive(true);
                } catch (PropertyVetoException e1) {
                    e1.printStackTrace();
                }
                return dockMaster.getDocumentTreeModel().addDesktopElement(e, activate);
            } finally {
                try {
                    dockMaster.setActive(active);
                } catch (PropertyVetoException e1) {
                    e1.printStackTrace();
                }
            } */
        } else if (dock.equals(dockMULTI)) {
            //getDockingManager().setFrameAvailable(dockMULTI);
            //dockMulti.setHidable(false);
            if (dockMulti.isHidden()) {
                getDockingManager().showFrame(dockMULTI);
                dockMulti.setHidable(false);
            }
            //return dockMulti.getZDocumentPane().openDesktopElement(e, activate);
            return dockMulti.getDocumentTreeModel().addDesktopElement(e, activate);

        } else if (dock.equals(dockPRESETS)) {
            //getDockingManager().setFrameAvailable(dockPRESETS);
            //dockPresets.setHidable(false);
            if (dockPresets.isHidden()) {
                getDockingManager().showFrame(dockPRESETS);
                dockPresets.setHidable(false);
            }
            //return dockPresets.getZDocumentPane().openDesktopElement(e, activate);
            return dockPresets.getDocumentTreeModel().addDesktopElement(e, activate);

        } else if (dock.equals(dockSAMPLES)) {
            //getDockingManager().setFrameAvailable(dockSAMPLES);
            //dockSamples.setHidable(false);
            if (dockSamples.isHidden()) {
                getDockingManager().showFrame(dockSAMPLES);
                dockSamples.setHidable(false);
            }
            //return dockSamples.getZDocumentPane().openDesktopElement(e, activate);
            return dockSamples.getDocumentTreeModel().addDesktopElement(e, activate);
        } else if (dock.equals(dockPROPERTIES)) {
            //getDockingManager().setFrameAvailable(dockSAMPLES);
            //dockSamples.setHidable(false);
            if (dockProperties.isHidden()) {
                getDockingManager().showFrame(dockPROPERTIES);
                dockProperties.setHidable(false);
            }
            //return dockProperties.getZDocumentPane().openDesktopElement(e, activate);
            return dockProperties.getDocumentTreeModel().addDesktopElement(e, activate);
        } else if (dock.equals(dockPIANO)) {
            //getDockingManager().setFrameAvailable(dockSAMPLES);
            //dockSamples.setHidable(false);
            if (dockPiano.isHidden()) {
                getDockingManager().showFrame(dockPIANO);
                dockPiano.setHidable(false);
            }
            //return dockProperties.getZDocumentPane().openDesktopElement(e, activate);
            return dockPiano.getDocumentTreeModel().addDesktopElement(e, activate);
        } else {
            throw new IllegalArgumentException("no canonical dock specified");
        }
    }

    public boolean removeDesktopElement(DesktopElement e) {
        return removeElement(e.getViewPath());
    }

    public boolean removeElement(ViewPath vp) {
        MutableTreeNode mtr;
        String dock = vp.getViewEntryPoint();
        if (dock.equals(dockWORKSPACE)) {
            return workspaceViewTreeModel.removeDesktopElement(vp.getDesktopNamePath());
        } else if (dock.equals(dockDEVICES)) {
            if (getDockingManager().getFrame(dockDEVICES) != null) {
                dockDevices.getDocumentTreeModel().removeDesktopElement(vp.getDesktopNamePath());
                return true;
            }
        } else if (dock.equals(dockMASTER)) {
            if (getDockingManager().getFrame(dockMASTER) != null) {
                dockMaster.getDocumentTreeModel().removeDesktopElement(vp.getDesktopNamePath());
                return true;
            }
        } else if (dock.equals(dockMULTI)) {
            if (getDockingManager().getFrame(dockMULTI) != null) {
                dockMulti.getDocumentTreeModel().removeDesktopElement(vp.getDesktopNamePath());
                return true;
            }
        } else if (dock.equals(dockPRESETS)) {
            if (getDockingManager().getFrame(dockPRESETS) != null) {
                dockPresets.getDocumentTreeModel().removeDesktopElement(vp.getDesktopNamePath());
                return true;
            }
        } else if (dock.equals(dockSAMPLES)) {
            if (getDockingManager().getFrame(dockSAMPLES) != null) {
                dockSamples.getDocumentTreeModel().removeDesktopElement(vp.getDesktopNamePath());
                return true;
            }
        } else if (dock.equals(dockPROPERTIES)) {
            if (getDockingManager().getFrame(dockPROPERTIES) != null) {
                dockProperties.getDocumentTreeModel().removeDesktopElement(vp.getDesktopNamePath());
                return true;
            }
        } else if (dock.equals(dockPIANO)) {
            if (getDockingManager().getFrame(dockPIANO) != null) {
                dockPiano.getDocumentTreeModel().removeDesktopElement(vp.getDesktopNamePath());
                return true;
            }
        } else {
            throw new IllegalArgumentException("no canonical dock specified");
        }
        return false;
    }

    public void sendMessage(DesktopElement e, String msg) {
        sendMessage(e.getViewPath(), msg);
    }

    public void sendMessage(ViewPath vp, String msg) {
        String dock = vp.getViewEntryPoint();
        if (dock.equals(dockWORKSPACE)) {
            workspaceViewTreeModel.sendMessage(vp.getDesktopNamePath(), msg);
        } else if (dock.equals(dockDEVICES)) {
            if (getDockingManager().getFrame(dockDEVICES) != null) {
                dockDevices.getDocumentTreeModel().sendMessage(vp.getDesktopNamePath(), msg);
            }
        } else if (dock.equals(dockMASTER)) {
            if (getDockingManager().getFrame(dockMASTER) != null) {
                dockMaster.getDocumentTreeModel().sendMessage(vp.getDesktopNamePath(), msg);
            }
        } else if (dock.equals(dockMULTI)) {
            if (getDockingManager().getFrame(dockMULTI) != null) {
                dockMulti.getDocumentTreeModel().sendMessage(vp.getDesktopNamePath(), msg);
            }
        } else if (dock.equals(dockPRESETS)) {
            if (getDockingManager().getFrame(dockPRESETS) != null) {
                dockPresets.getDocumentTreeModel().sendMessage(vp.getDesktopNamePath(), msg);
            }
        } else if (dock.equals(dockSAMPLES)) {
            if (getDockingManager().getFrame(dockSAMPLES) != null) {
                dockSamples.getDocumentTreeModel().sendMessage(vp.getDesktopNamePath(), msg);
            }
        } else if (dock.equals(dockPROPERTIES)) {
            if (getDockingManager().getFrame(dockPROPERTIES) != null) {
                dockProperties.getDocumentTreeModel().sendMessage(vp.getDesktopNamePath(), msg);
            }
        } else if (dock.equals(dockPIANO)) {
            if (getDockingManager().getFrame(dockPIANO) != null) {
                dockPiano.getDocumentTreeModel().sendMessage(vp.getDesktopNamePath(), msg);
            }
        } else {
            throw new IllegalArgumentException("no canonical dock specified");
        }
    }

    public DesktopElement[] evaluateCondition(ViewPath vp, String condition) {
        String dock = vp.getViewEntryPoint();
        if (dock.equals(dockWORKSPACE)) {
            return workspaceViewTreeModel.evaluateCondition(vp.getDesktopNamePath(), condition);
        } else if (dock.equals(dockDEVICES)) {
            if (getDockingManager().getFrame(dockDEVICES) != null) {
                return dockDevices.getDocumentTreeModel().evaluateCondition(vp.getDesktopNamePath(), condition);
            }
        } else if (dock.equals(dockMASTER)) {
            if (getDockingManager().getFrame(dockMASTER) != null) {
                return dockMaster.getDocumentTreeModel().evaluateCondition(vp.getDesktopNamePath(), condition);
            }
        } else if (dock.equals(dockMULTI)) {
            if (getDockingManager().getFrame(dockMULTI) != null) {
                return dockMulti.getDocumentTreeModel().evaluateCondition(vp.getDesktopNamePath(), condition);
            }
        } else if (dock.equals(dockPRESETS)) {
            if (getDockingManager().getFrame(dockPRESETS) != null) {
                return dockPresets.getDocumentTreeModel().evaluateCondition(vp.getDesktopNamePath(), condition);
            }
        } else if (dock.equals(dockSAMPLES)) {
            if (getDockingManager().getFrame(dockSAMPLES) != null) {
                return dockSamples.getDocumentTreeModel().evaluateCondition(vp.getDesktopNamePath(), condition);
            }
        } else if (dock.equals(dockPROPERTIES)) {
            if (getDockingManager().getFrame(dockPROPERTIES) != null) {
                return dockProperties.getDocumentTreeModel().evaluateCondition(vp.getDesktopNamePath(), condition);
            }
        } else if (dock.equals(dockPIANO)) {
            if (getDockingManager().getFrame(dockPIANO) != null) {
                return dockPiano.getDocumentTreeModel().evaluateCondition(vp.getDesktopNamePath(), condition);
            }
        }
        throw new IllegalArgumentException("no canonical dock specified");
    }

    public DesktopElement[] getDesktopElementTree(ViewPath vp) {
        return getDesktopElementTree(vp, false);
    }

    // in-order
    public DesktopElement[] getDesktopElementTree(ViewPath vp, boolean originals) {
        String dock = vp.getViewEntryPoint();
        if (dock.equals(dockWORKSPACE)) {
            return workspaceViewTreeModel.getDesktopElementTree(vp.getDesktopNamePath(), originals);
        } else if (dock.equals(dockDEVICES)) {
            return dockDevices.getDocumentTreeModel().getDesktopElementTree(vp.getDesktopNamePath(), originals);
        } else if (dock.equals(dockMASTER)) {
            return dockMaster.getDocumentTreeModel().getDesktopElementTree(vp.getDesktopNamePath(), originals);
        } else if (dock.equals(dockMULTI)) {
            return dockMulti.getDocumentTreeModel().getDesktopElementTree(vp.getDesktopNamePath(), originals);
        } else if (dock.equals(dockPRESETS)) {
            return dockPresets.getDocumentTreeModel().getDesktopElementTree(vp.getDesktopNamePath(), originals);
        } else if (dock.equals(dockSAMPLES)) {
            return dockSamples.getDocumentTreeModel().getDesktopElementTree(vp.getDesktopNamePath(), originals);
        } else if (dock.equals(dockPROPERTIES)) {
            return dockProperties.getDocumentTreeModel().getDesktopElementTree(vp.getDesktopNamePath(), originals);
        } else {
            throw new IllegalArgumentException("no canonical dock specified");
        }
    }

    public boolean mutuallyLinkComponents(ViewPath v1, ViewPath v2) throws ComponentGenerationException, Linkable.InvalidLinkException {
        try {
            DesktopElement[] de1 = getDesktopElementTree(v1, true);
            DesktopElement[] de2 = getDesktopElementTree(v2, true);

            if (de1.length == 0 || de2.length == 0)
                return false;

            Component vc1 = de1[0].getComponent();
            Component vc2 = de2[0].getComponent();

            if (vc1 instanceof Linkable && vc2 instanceof Linkable) {
                ((Linkable) vc1).linkTo(vc2);
                ((Linkable) vc2).linkTo(vc1);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void modifyBranch(DesktopBranch branch, boolean activate) throws LogicalHierarchyException, ChildViewNotAllowedException, ComponentGenerationException {
        modifyBranch(branch, activate, -1);
    }

    // typically called to activate a snapshot i.e replace existing
    public void modifyBranch(DesktopBranch branch, boolean activate, int clipIndex) throws LogicalHierarchyException, ChildViewNotAllowedException, ComponentGenerationException {
        //FlashMsg m = new FlashMsg(ZoeosFrame.getInstance(), ZoeosFrame.getInstance(), Integer.MAX_VALUE, 1000, FlashMsg.colorInfo, "Loading views...");
        ProgressSession sess = Zoeos.getInstance().getProgressSession("Loading device views", 100);
        sess.setIndeterminate(true);
        try {
            List incoming = branch.asList();
            if (incoming.size() == 0)
                return;

            if (clipIndex > 0)
                for (int i = incoming.size() - 1; i >= clipIndex; i--)
                    removeDesktopElement((DesktopElement) incoming.get(clipIndex));

            List existing = Arrays.asList(getDesktopElementTree(branch.getRoot().getViewPath()));

            List adding = new ArrayList();
            List removing = new ArrayList();
            adding.addAll(incoming);
            removing.addAll(Arrays.asList(existing.toArray()));

            adding.removeAll(existing);
            removing.removeAll(incoming);
            for (int i = removing.size() - 1; i >= 0; i--)
                removeDesktopElement((DesktopElement) removing.get(i));

            if (adding.size() > 0)
                for (int i = 0, j = adding.size(); i < j; i++)
                    addDesktopElement((DesktopElement) adding.get(i), (i == 0 ? activate : false));
            else if (activate)
                addDesktopElement((DesktopElement) incoming.get(0), true); // re-add the first to get activation
        } finally {
            sess.end();
        }
    }

    /*
    public void recreate() {
        ArrayList elems = workspaceViewTreeModel.getRootDocumentPane().getDesktopElementTree();
        DesktopElement[] elems2 = (DesktopElement[]) elems.toArray(new DesktopElement[elems.size()]);
        workspaceViewTreeModel.getRootDocumentPane().closeAll();
        for (int i = 0; i < elems2.length; i++)
            try {
                workspaceViewTreeModel.addDesktopElement(elems2[i], false);
            } catch (ComponentGenerationException e) {
                e.printStackTrace();
            } catch (ChildViewNotAllowedException e) {
                e.printStackTrace();
            } catch (LogicalHierarchyException e) {
                e.printStackTrace();
            }
    }
    */
    public void zDispose() {
        ZoeosPreferences.ZPREF_boxStyleTabs.removeChangeListener(this);
    }

    public void stateChanged(ChangeEvent e) {
        //if (e.getSource() == ZoeosPreferences.ZPREF_boxStyleTabs)
        //   recreate();
    }
}
