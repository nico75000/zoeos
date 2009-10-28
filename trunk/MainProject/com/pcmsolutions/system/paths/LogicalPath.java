package com.pcmsolutions.system.paths;

import com.pcmsolutions.system.SystemEntryPoint;
import com.pcmsolutions.system.ZUtilities;

import javax.swing.tree.TreePath;
import java.io.Serializable;

/**
 * User: paulmeehan
 * Date: 21-Jan-2004
 * Time: 11:11:04
 */
public class LogicalPath extends TreePath implements Serializable, Comparable {
    protected SystemEntryPoint systemEntryPoint;
    protected Object[] contextualPath;

    public LogicalPath(SystemEntryPoint sep, Object[] contextualPath) {
        super(ZUtilities.appendArray(contextualPath, sep, false));
        this.systemEntryPoint = sep;
        if (contextualPath == null)
            this.contextualPath = new Object[0];
        else
            this.contextualPath = (Object[]) contextualPath.clone();
    }

    public SystemEntryPoint getSystemEntryPoint() {
        return systemEntryPoint;
    }

    public LogicalPath append(Object obj) {
        return append(new Object[]{obj});
    }

    public LogicalPath append(Object[] objs) {
        return new LogicalPath(systemEntryPoint, ZUtilities.appendArray(contextualPath, objs, true));
    }

    public Object[] getContextualPath() {
        return (Object[]) contextualPath.clone();
    }

    public int compareTo(Object o) {
        if (o instanceof LogicalPath) {
            LogicalPath lp = (LogicalPath) o;
            // compare system enrty point first
            int c = systemEntryPoint.compareTo(lp.systemEntryPoint);
            if (c == 0) {
                // ok now compare common length
                int min = Math.min(contextualPath.length, lp.contextualPath.length);
                for (int i = 0; i < min; i++) {
                    if ((contextualPath[i].getClass() == lp.contextualPath[i].getClass()) && contextualPath[i] instanceof Comparable)
                        c = ((Comparable) contextualPath[i]).compareTo(lp.contextualPath[i]);
                    else
                        c = contextualPath[i].toString().compareTo(lp.contextualPath[i].toString());
                    if (c != 0)
                        return c;
                }
                // ok common length is identical, now look to see if depths differ
                if (contextualPath.length > lp.contextualPath.length)
                    return -1;
                else if (contextualPath.length < lp.contextualPath.length)
                    return 1;
                // ok pretty much the same, so fall out with a compare value of 0
            }
            return c;
        }
        return toString().compareTo(o.toString());
    }
}
