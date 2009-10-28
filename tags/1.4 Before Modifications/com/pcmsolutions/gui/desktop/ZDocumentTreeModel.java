package com.pcmsolutions.gui.desktop;

import com.pcmsolutions.gui.ComponentGenerationException;
import com.pcmsolutions.system.paths.DesktopName;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

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
            if (path.length == 1)
                return rootDocumentPane.openDesktopElement(e, activate);
            else {
                ZDocumentComponent zdc = getDocumentNode(path, path.length - 1, activate);
                return zdc.addDesktopElement(e, activate);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return false;
    }

    ZDocumentComponent getDocumentNode(DesktopName[] path, int depth, boolean activate) {
        if (activate)
            activateChild(rootDocumentPane, path[0].toString());
        ZDocumentComponent zdc = getChild(rootDocumentPane, path[0].toString());

        for (int i = 1; i < depth; i++) {
            if (activate)
                activateChild(zdc, path[i].toString());
            zdc = getChild(zdc, path[i].toString());
        }
        return zdc;
    }

    ZDocumentComponent getChild(ZDocumentComponent parent, String name) {
        if (!parent.isContainer())
            return null;
        ZDocumentPane zdp = (ZDocumentPane) ((ZDocumentComponent) parent).getRealComponent();
        return (ZDocumentComponent) zdp.getDocument(name);
    }

    ZDocumentComponent getChild(ZDocumentPane parent, String name) {
        return (ZDocumentComponent) parent.getDocument(name);
    }

    void activateChild(ZDocumentComponent zdc, String child) {
        if (zdc.isContainer())
            ((ZDocumentPane) zdc.getRealComponent()).setActiveDocument(child);
    }

    void activateChild(ZDocumentPane zdp, String child) {
        zdp.setActiveDocument(child);
    }

    public boolean removeDesktopElement(DesktopElement e) {
        return removeDesktopElement(e.getViewPath().getDesktopNamePath());
    }

    public boolean removeDesktopElement(DesktopName[] path) {
        try {
            if (path.length == 1) {
                if (rootDocumentPane.isDocumentOpened(path[0].toString())) {
                    rootDocumentPane.closeDocument(path[0].toString());
                    return true;
                }
            } else {
                ZDocumentComponent zdc = getDocumentNode(path, path.length - 1, false);
                return zdc.removeElement(path[path.length - 1].toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void sendMessage(DesktopElement e, String msg) {
        sendMessage(e.getViewPath().getDesktopNamePath(), msg);
    }

    public void sendMessage(DesktopName[] path, String msg) {
        try {
            ZDocumentComponent zdc = getDocumentNode(path, path.length, false);
            zdc.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DesktopElement[] evaluateCondition(DesktopName[] path, String condition) {
        try {
            ZDocumentComponent zdc = getDocumentNode(path, path.length, false);
            List<DesktopElement> positive =  zdc.evaluateCondition(condition);
            return positive.toArray(new DesktopElement[positive.size()]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new DesktopElement[0];
    }
    // in-order
    public DesktopElement[] getDesktopElementTree(DesktopName[] path, boolean originals) {
        ArrayList elems = new ArrayList();
        ZDocumentComponent node = getDocumentNode(path, path.length, false);
        ((ZDocumentPane)node.getDocumentPane()).updateSessionStrings();
        elems.addAll(node.getDesktopElementTree(originals));
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
            for (int i = 0, j = zdp.getDocumentCount(); i < j; i++)
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
