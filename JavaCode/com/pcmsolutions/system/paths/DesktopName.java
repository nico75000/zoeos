package com.pcmsolutions.system.paths;

import java.io.Serializable;

/**
 * User: paulmeehan
 * Date: 21-Jan-2004
 * Time: 11:26:12
 */
public class DesktopName implements Serializable {
    protected Class viewClass;
    protected LogicalPath[] logicalPaths;

    public DesktopName(Class viewClass, LogicalPath logicalPath) {
        this(viewClass, new LogicalPath[]{logicalPath});
    }

    public DesktopName(Class viewClass, LogicalPath[] logicalPaths) {
        if (logicalPaths.length < 1)
            throw new IllegalArgumentException("must provide at least one LogicalPath for DesktopName");

        this.viewClass = viewClass;
        this.logicalPaths = logicalPaths;
    }

    public Class getViewClass() {
        return viewClass;
    }

    // guaranteed to contain at least 1 element
    public LogicalPath[] getLogicalPaths() {
        return logicalPaths;
    }

    public String toString() {
        String s = viewClass.toString();
        for (int i = 0; i < logicalPaths.length; i++)
            s += logicalPaths[i].toString();
        return s;
    }

    public boolean isLogicalDescendant(DesktopName name) {
        /*
         LogicalPath[] paths = name.getLogicalPaths();
         for (int i = 0; i < logicalPaths.length; i++)
             for (int j = 0; j < paths.length; j++)
                 if (!(logicalPaths[i].isDescendant(paths[j])))
                     return false;
         return true;
         */
        LogicalPath[] paths = name.getLogicalPaths();
        int comapreLength = Math.min(logicalPaths.length, paths.length);
        for (int i = 0; i < comapreLength; i++)
            if (!(logicalPaths[i].isDescendant(paths[i])))
                return false;
        return true;
    }

   /* public boolean equals(Object obj) {
        if (obj instanceof DesktopName) {
            DesktopName de = (DesktopName) obj;
            if (de.viewClass == viewClass && de.logicalPaths.length == logicalPaths.length) {
                for (int i = 0; i < logicalPaths.length; i++)
                    if (!de.logicalPaths[i].equals(logicalPaths[i]))
                        return false;
                return true;
            }
        }
        return false;
    }*/
    public boolean equals(Object obj) {
        return toString().equals(obj.toString());
    }
}
