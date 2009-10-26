package com.pcmsolutions.gui.desktop;

import com.pcmsolutions.gui.ComponentGenerationException;
import com.pcmsolutions.system.paths.DesktopName;
import com.pcmsolutions.system.ZoeosPreferences;
import com.jidesoft.document.DocumentPane;
import com.jidesoft.swing.JideTabbedPane;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.MutableTreeNode;
import java.util.ArrayList;

/**
 * User: paulmeehan
 * Date: 26-Jan-2004
 * Time: 18:20:32
 */
public class ZDocumentTreeModel implements TreeModel {
    protected EventListenerList listenerList = new EventListenerList();

    protected final ZDocumentPane rootDocumentPane;

    public ZDocumentTreeModel(ZDocumentPane root) {
        this.rootDocumentPane = root;
    }

    public ZDocumentPane getRootDocumentPane() {
        return rootDocumentPane;
    }

    public boolean addDesktopElement(DesktopElement e, boolean activate) throws ComponentGenerationException, ChildViewNotAllowedException, LogicalHierarchyException {
        try {
            DesktopName[] path = e.getViewPath().getDesktopNamePath();

            Object node = getChild(rootDocumentPane, path, 0, path.length - 1, activate);

            if (node instanceof ZDocumentPane) {
                return ((ZDocumentPane) node).openDesktopElement(e, activate);
            } else if (node instanceof ZDocumentComponent) {
                ZDocumentComponent zdc = (ZDocumentComponent) node;
                return zdc.addDesktopElement(e, activate);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return false;
    }

    private Object getChild(Object node, DesktopName[] path, int start, int end, boolean activate) {
        for (int i = start; i < end; i++) {
            if (activate)
                activateChild(node, path[i].toString());
            node = getChild(node, path[i].toString());
        }
        return node;
    }

    private void activateChild(Object node, String child) {
        if (node instanceof ZDocumentPane) {
            ((ZDocumentPane) node).setActiveDocument(child);
        } else if (node instanceof ZDocumentComponent) {
            ZDocumentComponent zdc = (ZDocumentComponent) node;
            if (zdc.isContainer())
                ((ZDocumentPane) zdc.getRealComponent()).setActiveDocument(child);
        }
    }

    private Object getChild(Object parent, String name) {
        if (parent instanceof ZDocumentPane) {
            return ((ZDocumentPane) parent).getDocument(name);
        } else if (parent instanceof ZDocumentComponent) {
            Object comp = ((ZDocumentComponent) parent).getRealComponent();
            if (comp instanceof ZDocumentPane)
                return ((ZDocumentPane) comp).getDocument(name);
            else
                return null;
        }
        throw new IllegalArgumentException("parent invalid");
    }

    public boolean removeDesktopElement(DesktopElement e) {
        return removeDesktopElement(e.getViewPath().getDesktopNamePath());
    }

    public boolean removeDesktopElement(DesktopName[] path) {
        try {
            Object node = getChild(rootDocumentPane, path, 0, path.length - 1, false);
            if (node instanceof ZDocumentPane && ((ZDocumentPane) node).isDocumentOpened(path[path.length - 1].toString())) {
                ((ZDocumentPane) node).closeDocument(path[path.length - 1].toString());
                return true;
            } else if (node instanceof ZDocumentComponent) {
                ZDocumentComponent zdc = (ZDocumentComponent) node;
                return zdc.removeElement(path[path.length - 1].toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // in-order
    public DesktopElement[] getDesktopElementTree(DesktopName[] path, boolean originals) {
        ArrayList elems = new ArrayList();

        Object node = getChild(rootDocumentPane, path, 0, path.length, false);

        if (node instanceof ZDocumentPane)
            elems.addAll(((ZDocumentPane) node).getDesktopElementTree(originals));
        else if (node instanceof ZDocumentComponent)
            elems.addAll(((ZDocumentComponent) node).getDesktopElementTree(originals));

        return (DesktopElement[]) elems.toArray(new DesktopElement[elems.size()]);
    }

    public Object getRoot() {
        return rootDocumentPane;
    }

    public Object getChild(Object parent, int index) {
        if (parent instanceof ZDocumentPane)
            return ((ZDocumentPane) parent).getDocument(((ZDocumentPane) parent).getDocumentNameAt(index));
        else if (parent instanceof ZDocumentComponent) {
            ZDocumentComponent zdc = ((ZDocumentComponent) parent);
            if (zdc.isContainer()) {
                ZDocumentPane zdp = (ZDocumentPane) zdc.getRealComponent();
                return zdp.getDocument(zdp.getDocumentNameAt(index));
            }
        }
        throw new IllegalArgumentException("parent invalid");
    }

    public int getChildCount(Object parent) {
        if (parent instanceof ZDocumentPane)
            return ((ZDocumentPane) parent).getDocumentCount();

        return 0;
    }

    public boolean isLeaf(Object node) {
        return node instanceof ZDocumentComponent && !((ZDocumentComponent) node).isContainer();
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    public int getIndexOfChild(Object parent, Object child) {
        if (parent instanceof ZDocumentPane) {
            ZDocumentPane zdp = ((ZDocumentPane) parent);
            for (int i = 0,j = zdp.getDocumentCount(); i < j; i++)
                if (zdp.getDocument(zdp.getDocumentNameAt(i)).equals(child))
                    return i;
        }
        return -1;
    }

    public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(TreeModelListener.class, l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        listenerList.remove(TreeModelListener.class, l);
    }
}
