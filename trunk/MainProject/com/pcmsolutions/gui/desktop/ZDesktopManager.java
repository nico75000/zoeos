package com.pcmsolutions.gui.desktop;

import com.pcmsolutions.gui.ComponentGenerationException;
import com.pcmsolutions.system.Linkable;
import com.pcmsolutions.system.paths.ViewPath;

/**
 * User: paulmeehan
 * Date: 18-Jan-2004
 * Time: 02:36:46
 */
public interface ZDesktopManager {
    String LAYOUT_LAST = "previous";
    String LAYOUT_DEFAULT = "default";
    String LAYOUT_CUSTOM_1 = "custom_1";
    String LAYOUT_CUSTOM_2 = "custom_2";
    String LAYOUT_CUSTOM_3 = "custom_3";

    String dockWORKSPACE = "Workspace";
    String dockDEVICES = "Devices";
    String dockPROPERTIES = "Properties";
    String dockMULTI = "Multi";
    String dockPRESETS = "Presets";
    String dockSAMPLES = "Samples";
    String dockMASTER = "Master";
    String dockPIANO = "Piano";

    public boolean addDesktopElement(DesktopElement e, boolean activate) throws ComponentGenerationException, ChildViewNotAllowedException, LogicalHierarchyException;

    public boolean addDesktopElement(DesktopElement e) throws ComponentGenerationException, ChildViewNotAllowedException, LogicalHierarchyException;

    public boolean removeDesktopElement(DesktopElement e);

    public boolean removeElement(ViewPath vp);

    public void sendMessage(DesktopElement e, String msg);

    public void sendMessage(ViewPath vp, String msg);

    public DesktopElement[] evaluateCondition(ViewPath vp, String condition);

    public DesktopElement[] getDesktopElementTree(ViewPath vp);

    public DesktopElement[] getDesktopElementTree(ViewPath vp, boolean originals);

    public boolean mutuallyLinkComponents(ViewPath v1, ViewPath v2) throws ComponentGenerationException, Linkable.InvalidLinkException;

    public void modifyBranch(DesktopBranch branch, boolean activate, int clipIndex) throws LogicalHierarchyException, ChildViewNotAllowedException, ComponentGenerationException;

    public void modifyBranch(DesktopBranch branch, boolean activate) throws LogicalHierarchyException, ChildViewNotAllowedException, ComponentGenerationException;
}
