/*
 * ZTreeNodeComparator.java
 *
 * Created on February 7, 2003, 8:49 AM
 */

package com.pcmsolutions.system;

import javax.swing.tree.MutableTreeNode;
import java.util.Comparator;

/**
 *
 * @author  pmeehan
 */
public class MutableTreeNodeComparator implements Comparator {

    /** Creates a new instance of ZTreeNodeComparator */
    public MutableTreeNodeComparator() {
    }

    public int compare(Object obj, Object obj1) {
        int rv = 0;
        if (obj instanceof MutableTreeNode && obj1 instanceof MutableTreeNode) {
            rv = ((MutableTreeNode) obj).toString().compareTo(((MutableTreeNode) obj1).toString());
        }
        return rv;
    }
}
