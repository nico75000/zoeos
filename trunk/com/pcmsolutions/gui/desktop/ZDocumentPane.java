package com.pcmsolutions.gui.desktop;

import com.jidesoft.document.*;
import com.jidesoft.swing.JideTabbedPane;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.gui.ComponentGenerationException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.ZoeosPreferences;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.*;

/**
 * User: paulmeehan
 * Date: 20-Jan-2004
 * Time: 05:04:49
 */
public class ZDocumentPane extends DocumentPane implements DocumentComponentListener, ZDisposable, ChangeListener {
    public ZDocumentPane() {
        this.setReorderAllowed(false);
        this.setTabPlacement(DocumentPane.TOP);
        setTabbedPaneCustomizer(new DocumentPane.TabbedPaneCustomizer() {
            public void customize(JideTabbedPane jideTabbedPane) {
                jideTabbedPane.setRightClickSelect(true);
                jideTabbedPane.setBoxStyleTab(ZoeosPreferences.ZPREF_boxStyleTabs.getValue());
            }
        });
        this.setPopupMenuCustomizer(new PopupMenuCustomizer() {
            public void customizePopupMenu(JPopupMenu jPopupMenu, IDocumentPane iDocumentPane, String s, IDocumentGroup iDocumentGroup, boolean b) {
                DocumentComponent dc = iDocumentPane.getDocument(s);
                if (dc instanceof ZDocumentComponent)
                    try {
                        ((ZDocumentComponent) dc).getDesktopElement().getNodalDescriptor().customizePopupMenu(jPopupMenu);
                        /*if (((ZDocumentComponent) dc).isContainer())
                            jPopupMenu.add("Container");
                        else
                            jPopupMenu.add("Not a Container");
                        */
                        jPopupMenu.add("index = " + String.valueOf(iDocumentPane.indexOfDocument(s)));
                        jPopupMenu.add("groupIndex = " + String.valueOf(iDocumentPane.groupIndexOfDocument(s)));
                        jPopupMenu.add("inGroupIndex = " + String.valueOf(iDocumentGroup.indexOfDocument(iDocumentPane.getDocument(s).getComponent())));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        });
    }

    private Vector listeners = new Vector();

    public void updateSessionStrings() {
        String[] names = getDocumentNames();
        for (int i = 0; i < names.length; i++) {
            ZDocumentComponent ezdc = (ZDocumentComponent) this.getDocument(names[i]);
            ezdc.getDesktopElement().setSessionString(makeSessionString(names[i]));
        }
    }

    private String makeSessionString(String name) {
        StringBuffer sb = new StringBuffer();
        sb.append(groupIndexOfDocument(name));
        sb.append(",");
        sb.append(indexOfDocument(name));
        sb.append(",");
        sb.append(this.getOrientation());
        return sb.toString();
    }

    public void addZDocumentPaneListener(ZDocumentPaneListener zdpl) {
        listeners.add(zdpl);
    }

    public void removeZDocumentPaneListener(ZDocumentPaneListener zdpl) {
        listeners.remove(zdpl);
    }

    protected void fireDocumentClosed() {
        for (int i = 0, j = listeners.size(); i < j; i++)
            try {
                ((ZDocumentPaneListener) listeners.get(i)).ZDocumentComponentClosed(this);
            } catch (Exception e) {

            }
    }

    public boolean openDesktopElement(DesktopElement d) throws ComponentGenerationException {
        return openDesktopElement(d, false);
    }

    public boolean openDesktopElement(DesktopElement d, boolean activate) throws ComponentGenerationException {
        if (!this.isDocumentOpened(d.getName().toString())) {
            openDocument(new ZDocumentComponent(d), activate);
            return true;
        } else {
            if (activate && !this.getActiveDocument().getName().equals(d.getName().toString()))
                this.setActiveDocument(d.getName().toString());

            //d.zDispose();
        }
        return false;
    }

    public void openDocument(DocumentComponent documentComponent) {
        openDocument(documentComponent, false);
    }

    private final Comparator documentCurrentPositionComparator = new Comparator() {
        public int compare(Object o1, Object o2) {
            int c = IntPool.get(groupIndexOfDocument(o1.toString())).compareTo(IntPool.get(groupIndexOfDocument(o2.toString())));
            if (c == 0)
                c = IntPool.get(indexOfDocument(o1.toString())).compareTo(IntPool.get(indexOfDocument(o2.toString())));
            return c;
        }
    };

    public void openDocument(DocumentComponent documentComponent, boolean activate) {
        if (!(documentComponent instanceof ZDocumentComponent))
            throw new IllegalArgumentException("DocumentComponent must be a ZDocumentComponent");
        ZDocumentComponent zdc = (ZDocumentComponent) documentComponent;
        if (!this.isDocumentOpened(zdc.getDesktopElement().getName().toString())) {
            documentComponent.addDocumentComponentListener(this);
            super.openDocument(zdc);
            adjustPosition(zdc, activate);
        } else {
            if (activate && !this.getActiveDocument().getName().equals(zdc.getDesktopElement().getName().toString()))
                this.setActiveDocument(zdc.getDesktopElement().getName().toString());
        }
    }

    protected IDocumentGroup createDocumentGroup() {
        return super.createDocumentGroup();
    }

    private int numGroups() {
        String[] names = this.getDocumentNames();
        int mg = 0;
        int g;
        for (int i = 0; i < names.length; i++) {
            g = groupIndexOfDocument(names[i]);
            if (g > mg)
                mg = g;
        }
        return mg + 1;
    }

    private void adjustPosition(ZDocumentComponent zdc, boolean activate) {
        try {
            String ss = zdc.getDesktopElement().getSessionString();
            if (ss != null) {
                StringTokenizer st = new StringTokenizer(ss, ",");
                if (st.countTokens() < 3)
                    return;
                int group = Integer.valueOf(st.nextToken()).intValue();
                int index = Integer.valueOf(st.nextToken()).intValue();
                int orient = Integer.valueOf(st.nextToken()).intValue();
                int ng = numGroups();
                //System.out.println("Incoming session: group = " + group + " index = " + index + " numGroups = " + ng);

                if (group >= ng) {
                    if (group - ng > 0) {
                        // we're not receiving groups consecutively - this could be because we are adding something from a different workspace snapshot
                        // either way we will have to trim it to the last group and reset the index in the group to zero as a default strategy
                        //System.out.println("Non-consecutive groups: group = " + group + " numGroups = " + ng);
                        group = ng;
                    }
                    this.moveDocument(zdc.getName(), group - 1, false);
                    this.newDocumentGroup(zdc.getName(), group - 1, orient);
                 //   System.out.println("Created new group: group = " + group);
                }
                this.moveDocument(zdc.getName(), group, activate);
            } else
                moveDocument(zdc.getName(), groupIndexOfDocument(zdc.getName()), activate);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getSortedPosition(ZDocumentComponent zdc) {
        int mi = -1;
        String[] names = getSortedNamesForGroup(groupIndexOfDocument(zdc.getName()));
        for (int i = 0; i < names.length; i++) {
            ZDocumentComponent ezdc = (ZDocumentComponent) this.getDocument(names[i]);
            if (ezdc.getDesktopElement() instanceof ZDocumentComponent.ContainerDesktopElement || ezdc == zdc)
                continue;
            if (ezdc.getDesktopElement().compareTo(zdc.getDesktopElement()) > 0) {
                mi = i;
                break;
            }
        }
        return mi;
    }

    // catching calls from by user interface - ensure good ordering
    public void moveDocument(String s, int i, int i1) {
        super.moveDocument(s, i, i1);
        int mi = getSortedPosition((ZDocumentComponent) getDocument(s));
        if (mi >= 0)
            super.moveDocument(s, groupIndexOfDocument(s), mi);
    }

    public void moveDocument(String s, int group, boolean activate) {
        DocumentComponent adc = getActiveDocument();
        moveDocument(s, group, -1);
        if (!activate && adc != null)
            setActiveDocument(adc.getName());
    }

    private String[] getSortedNames() {
        String[] names = getDocumentNames();
        Arrays.sort(names, documentCurrentPositionComparator);
        return names;
    }

    private String[] getSortedNamesForGroup(int group) {
        ArrayList groupNames = new ArrayList();
        String[] names = getDocumentNames();
        for (int i = 0; i < names.length; i++)
            if (groupIndexOfDocument(names[i]) == group)
                groupNames.add(names[i]);
        Collections.sort(groupNames, documentCurrentPositionComparator);
        return (String[]) groupNames.toArray(new String[groupNames.size()]);
    }

    public void documentComponentOpened(DocumentComponentEvent event) {
    }

    public void documentComponentClosing(DocumentComponentEvent event) {
        if (event.getDocumentComponent() instanceof ZDocumentComponent) {
            ZDocumentComponent zdc = (ZDocumentComponent) event.getDocumentComponent();
            try {
                boolean close = zdc.getDesktopElement().getActivityContext().tryClosing();
                zdc.setAllowClosing(close || zdc.getDesktopElement().hasExpired());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void documentComponentClosed(DocumentComponentEvent event) {
        if (event.getDocumentComponent() instanceof ZDocumentComponent)
            try {
                ((ZDocumentComponent) event.getDocumentComponent()).getDesktopElement().getActivityContext().closed();
                ((ZDocumentComponent) event.getDocumentComponent()).zDispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        fireDocumentClosed();
    }

    public void documentComponentActivated(DocumentComponentEvent event) {
        ZDocumentComponent zdc = (ZDocumentComponent) event.getDocumentComponent();
        zdc.getRealComponent().grabFocus();
        try {
            zdc.getDesktopElement().getActivityContext().activated();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void documentComponentDeactivated(DocumentComponentEvent event) {
        ZDocumentComponent zdc = (ZDocumentComponent) event.getDocumentComponent();
        try {
            zdc.getDesktopElement().getActivityContext().deactivated();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList getDesktopElementTree(boolean originals) {
        updateSessionStrings();
        ArrayList elems = new ArrayList();
        String[] names = getSortedNames();

        for (int i = 0; i < names.length; i++)
            elems.addAll(((ZDocumentComponent) getDocument(names[i])).getDesktopElementTree(originals));
        return elems;
    }

    public Color getBackground() {
        return UIColors.getDefaultBG();
    }

    public Color getForeground() {
        return UIColors.getDefaultFG();
    }

    public void zDispose() {
        String[] docs = this.getDocumentNames();
        for (int i = 0; i < docs.length; i++)
            ((ZDocumentComponent) this.getDocument(docs[i])).zDispose();
    }
}
