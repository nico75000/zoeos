package com.pcmsolutions.system.paths;

import com.pcmsolutions.system.ZUtilities;

import javax.swing.tree.TreePath;

/**
 * User: paulmeehan
 * Date: 21-Jan-2004
 * Time: 11:22:13
 */
public class ViewPath extends TreePath {
    protected String viewEntryPoint;
    protected DesktopName[] desktopPath;

    public ViewPath(String vep, DesktopName path) {
        this(vep, new DesktopName[]{path});
    }

    public ViewPath(String vep, DesktopName[] path) {
        super(ZUtilities.appendArray(path, vep, false));
        this.viewEntryPoint = vep;
        this.desktopPath = (DesktopName[]) path.clone();
    }

    public String getViewEntryPoint() {
        return viewEntryPoint;
    }

    public DesktopName[] getDesktopNamePath() {
        return (DesktopName[]) desktopPath.clone();
    }
}
