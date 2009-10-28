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
        StringBuffer s = new StringBuffer(viewClass.toString());
        for (int i = 0; i < logicalPaths.length; i++)
            s.append(logicalPaths[i].toString());
        return s.toString();
    }

    // can't put multiple logical paths under multiple logical paths
    public boolean isLogicalDescendant(DesktopName name) {
        LogicalPath[] inPaths = name.getLogicalPaths();
        if (logicalPaths.length == 1) {
            for (int i = 0; i < inPaths.length; i++)
                if (!(logicalPaths[0].isDescendant(inPaths[i])))
                    return false;
        } else {
            if (inPaths.length == 1) {
                for (int i = 0; i < logicalPaths.length; i++)
                    if (!(logicalPaths[i].isDescendant(inPaths[0])))
                        return false;
            } //else
                //return false;
        }
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
