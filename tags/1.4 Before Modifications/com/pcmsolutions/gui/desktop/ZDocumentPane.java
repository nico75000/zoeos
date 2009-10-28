package com.pcmsolutions.gui.desktop;

import com.jidesoft.document.*;
import com.jidesoft.swing.JideSplitPane;
import com.jidesoft.swing.JideTabbedPane;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.gui.ComponentGenerationException;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.ZoeosPreferences;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * User: paulmeehan
 * Date: 20-Jan-2004
 * Time: 05:04:49
 */
public class ZDocumentPane extends DocumentPane implements DocumentComponentListener, ZDisposable, ChangeListener {
    public TreeNode parent;
    String containerSession;
    String containerAsChildDesktopName;

    public ZDocumentPane(TreeNode parent) {
        this.parent = parent;
        this.setReorderAllowed(false);
        this.setTabPlacement(DocumentPane.TOP);
        this.setUpdateTitle(false);
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
                        /*jPopupMenu.add("index = " + String.valueOf(iDocumentPane.indexOfDocument(s)));
                        jPopupMenu.add("groupIndex = " + String.valueOf(iDocumentPane.groupIndexOfDocument(s)));
                        jPopupMenu.add("inGroupIndex = " + String.valueOf(iDocumentGroup.indexOfDocument(iDocumentPane.getDocument(s).getComponent())));
                        */
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        });
    }

    private Vector listeners = new Vector();
    private final String containerAsChildPrefix = "CAS_";

    private final Comparator<String> documentPositionComparator = new Comparator<String>() {

        public int compare(String s, String s1) {
            int c = IntPool.get(groupIndexOfDocument(s)).compareTo(IntPool.get(groupIndexOfDocument(s1)));
            if (c == 0)
                c = IntPool.get(indexOfDocument(s)).compareTo(IntPool.get(indexOfDocument(s1)));
            return c;
        }
    };

    // puts any container view showing in child at first position
    private final Comparator<String> documentPositionComparatorForExternalization = new Comparator<String>() {
        public int compare(String s, String s1) {
            ZDocumentComponent zdc = (ZDocumentComponent) getDocument(s);
            ZDocumentComponent zdc1 = (ZDocumentComponent) getDocument(s1);
            if (zdc.isContainerViewAsChild())
                return -1;
            if (zdc1.isContainerViewAsChild())
                return 1;

            int c = IntPool.get(groupIndexOfDocument(s)).compareTo(IntPool.get(groupIndexOfDocument(s1)));
            if (c == 0)
                c = IntPool.get(indexOfDocument(s)).compareTo(IntPool.get(indexOfDocument(s1)));
            return c;
        }
    };

    protected IDocumentGroup createDocumentGroup() {
        IDocumentGroup dg = super.createDocumentGroup();    //To change body of overridden methods use File | Settings | File Templates.
        return dg;
    }

    public void updateSessionStrings() {
        String[] names = getDocumentNames();
        for (int i = 0; i < names.length; i++) {
            ZDocumentComponent ezdc = (ZDocumentComponent) this.getDocument(names[i]);
            if (ezdc.isContainerViewAsChild())
                ezdc.getDesktopElement().setSessionString(ezdc.getDesktopElement().getSessionString() + makeSessionString(names[i], containerAsChildPrefix));
            else
                ezdc.getDesktopElement().setSessionString(makeSessionString(names[i]));
        }
    }

    private String makeSessionString(String name) {
        return makeSessionString(name, "");
    }

    private String makeSessionString(String name, String prefix) {
        final Session sess = new Session();
        sess.setPrefix(prefix);
        int gi = groupIndexOfDocument(name);
        sess.setGroup(gi);
        if (gi != 0) {
            int loc = (getOrientation() == JideSplitPane.HORIZONTAL_SPLIT ? this.getDividerAt(gi - 1).getLocation().x : this.getDividerAt(gi - 1).getLocation().y);
            sess.setDividerLocation(loc);
        }
        sess.setIndex(indexOfDocument(name));
        sess.setOrientation(getOrientation());
        sess.setActive(this.getActiveDocumentName().equals(name));
        sess.setDesktopElementTitle(((ZDocumentComponent) this.getDocument(name)).getDesktopElement().getTitle());
        return sess.toString();
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
        try {
            if (!this.isDocumentOpened(d.getName().toString())) {
                ZDocumentComponent zdc = new ZDocumentComponent(d, parent);
                zdc.addDocumentComponentListener(this);
                super.openDocument(zdc);
                updateDocumentComponent(zdc, zdc.getDesktopElement(), activate, true);
                return true;
            } else {
                ZDocumentComponent zdc = (ZDocumentComponent) this.getDocument(d.getName().toString());
                updateDocumentComponent(zdc, d, activate, false);
                return false;
            }
        } finally {
            checkContainerAsChildPosition();
        }
    }

    private void updateDocumentComponent(ZDocumentComponent zdc, DesktopElement d, boolean activate, boolean isNew) {
        zdc.getDesktopElement().updateComponentSession(d.retrieveComponentSessionString());
        String sess = d.getSessionString();
        if (isNew) {
            if (sess == null) {  // probably a basic open of something
                String name = zdc.getDesktopElement().getName().toString();
                if ( activate){
                    moveDocument(name, groupIndexOfDocument(name), -1);
                }
                else{
                    DocumentComponent active = getActiveDocument();
                    moveDocument(name, groupIndexOfDocument(name), -1);
                    setActiveDocument(active.getName());
                }
            } else {    // probably a session restore
                zdc.getDesktopElement().setSessionString(sess);
                restorePosition(zdc, activate);
            }
        } else {
            if (activate)
                setActiveDocument(zdc.getDesktopElement().getName().toString());
        }
    }

    public final void openDocument(DocumentComponent documentComponent) {
        throw new IllegalArgumentException("must use openDesktopElement instead of openDocument");
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

    private int countForGroup(int group) {
        String[] names = this.getDocumentNames();
        int count = 0;
        int g;
        for (int i = 0; i < names.length; i++) {
            g = groupIndexOfDocument(names[i]);
            if (g == group)
                count++;
        }
        return count;
    }

    private String debug(ZDocumentComponent d, String s) {
        return d.getDesktopElement().getTitle() + ": " + s;
    }

    private boolean debug = false;

    private void debugOut(ZDocumentComponent d, String s) {
        if (debug)
            System.out.println(debug(d, s));
    }

    void restorePosition(ZDocumentComponent zdc, boolean activate) {
       if (zdc.isContainerViewAsChild())
            return;
        try {
            final Session sess = new Session(zdc);
            int group = sess.getGroup();
            int index = sess.getIndex();
            int orient = sess.getOrientation();
            boolean active = sess.isActive();

            int ng = numGroups();
            //debugOut(zdc, "Incoming session: group = " + group + " index = " + index + " numGroups = " + ng);
            //debugOut(zdc, "Incoming session: " + ss);

            if (group >= ng) {
                if (group - ng > 0) {
                    // we're not receiving groups consecutively - this could be because we are adding something from a different workspace snapshot
                    // either way we will have to trim it to the last group and reset the index in the group to zero as a default strategy
                    debugOut(zdc, "Non-consecutive groups: group = " + group + " numGroups = " + ng);
                    group = ng;
                }
                this.newDocumentGroup(zdc.getName(), group - 1, orient);
                if (group > 0) {
                    final int loc = sess.getDividerLocation();
                    if (loc != -1) {
                        final int f_group = group;
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                setDividerLocation(getDividerAt(f_group - 1), loc);
                            }
                        });
                    }
                }
                debugOut(zdc, "Created new group: group = " + group);
            }

            try {
                if (activate || active) {
                    if (groupIndexOfDocument(zdc.getName()) != group)
                        super.moveDocument(zdc.getName(), group, -1);
                    setActiveDocument(zdc.getName());
                } else {
                    if (groupIndexOfDocument(zdc.getName()) != group) {
                        String adn = getActiveDocumentName();
                        super.moveDocument(zdc.getName(), group, -1);
                        setActiveDocument(adn);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //  if (!zdc.isContainerViewAsChild())
            //    zdc.getDesktopElement().setSessionString(null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void checkContainerAsChildPosition() {
        if (containerSession != null && containerAsChildDesktopName != null)
            try {
                final Session sess = new Session(containerSession, containerAsChildPrefix);
                int group = sess.getGroup();
                int index = sess.getIndex();
                int orient = sess.getOrientation();
                boolean active = sess.isActive();
                int cg = numGroups() - 1;

                if (group != 0) {
                    if (group == cg + 1) {
                        this.newDocumentGroup(containerAsChildDesktopName, cg, orient);
                        final int loc = sess.getDividerLocation();
                        if (loc != -1) {
                            final int f_group = group;
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    setDividerLocation(getDividerAt(f_group - 1), loc);
                                }
                            });
                        }
                    }
                }
                if (group == cg && groupIndexOfDocument(containerAsChildDesktopName) == group && countForGroup(group) - 1 == index) {
                    // at correct position for container child to be added to it's group
                    if (active) {
                        super.moveDocument(containerAsChildDesktopName, group, index);
                        setActiveDocument(containerAsChildDesktopName);
                    } else {
                        String adn = getActiveDocumentName();
                        super.moveDocument(containerAsChildDesktopName, group, index);
                        setActiveDocument(adn);
                    }
                }
                if (group == groupIndexOfDocument(containerAsChildDesktopName) && index == indexOfDocument(containerAsChildDesktopName))
                    containerSession = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    private int getSortedPosition(ZDocumentComponent zdc) {
        int mi = -1;
        String[] names = getSortedNamesForGroup(groupIndexOfDocument(zdc.getName()));
        for (int i = 0; i < names.length; i++) {
            ZDocumentComponent ezdc = (ZDocumentComponent) this.getDocument(names[i]);
            if (ezdc.isContainerViewAsChild() || ezdc == zdc)
                continue;
            if (ezdc.getDesktopElement().compareTo(zdc.getDesktopElement()) > 0) {
                mi = i;
                break;
            }
        }
        return mi;
    }

    // catching calls from user interface - ensure good ordering
    public void moveDocument(String s, int i, int i1) {
        super.moveDocument(s, i, i1);
        int mi = getSortedPosition((ZDocumentComponent) getDocument(s));
        if (mi >= 0)
            super.moveDocument(s, groupIndexOfDocument(s), mi);
    }

    // demoted container documents always first
    private String[] getPositionallySortedNames() {
        String[] names = getDocumentNames();
        Arrays.sort(names, documentPositionComparator);
        return names;
    }

    // demoted container documents always first
    private String[] getPositionallySortedNamesForExternalization() {
        String[] names = getDocumentNames();
        Arrays.sort(names, documentPositionComparatorForExternalization);
        return names;
    }

    public void sendMessage(String msg) {
        String[] names = getPositionallySortedNames();
        for (int i = 0; i < names.length; i++)
            ((ZDocumentComponent) getDocument(names[i])).sendMessage(msg);
    }

     public List<DesktopElement> evaluateCondition(String condition) {
        String[] names = getPositionallySortedNames();
         ArrayList<DesktopElement> positive = new ArrayList<DesktopElement>();
         for (int i = 0; i < names.length; i++)
            positive.addAll(((ZDocumentComponent) getDocument(names[i])).evaluateCondition(condition));
         return positive;
    }
    private String[] getSortedNamesForGroup(int group) {
        ArrayList<String> groupNames = new ArrayList<String>();
        String[] names = getDocumentNames();
        for (int i = 0; i < names.length; i++)
            if (groupIndexOfDocument(names[i]) == group)
                groupNames.add(names[i]);
        Collections.sort(groupNames, documentPositionComparator);
        return groupNames.toArray(new String[groupNames.size()]);
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
        if (!zdc.getRealComponent().hasFocus())
            zdc.getRealComponent().grabFocus();
        //zdc.getRealComponent().requestFocus();
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
        String[] names = getPositionallySortedNamesForExternalization();

        ZDocumentComponent doc;
        for (int i = 0; i < names.length; i++) {
            doc = (ZDocumentComponent) getDocument(names[i]);
            elems.addAll(doc.getDesktopElementTree(originals));
        }
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


class Session {
    private final String groupIndexTag = "groupIndex";
    private final String groupDividerLocationTag = "groupDividerLocation";
    private final String docIndexTag = "docIndex";
    private final String orientationTag = "orientation";
    private final String activeDocTag = "activeDoc";
    private final String deTitleTag = "desktopElementTitle";

    String prefix = "";
    String session;

    int group;
    int index;
    int orientation;
    int dividerLocation;
    boolean active;
    String desktopElementTitle;


    public Session(ZDocumentComponent zdc, String prefix) {
        this(zdc.getDesktopElement().getSessionString(), prefix);
    }

    public Session(ZDocumentComponent zdc) {
        this(zdc, "");
    }

    public Session(String sess, String prefix) {
        setSession(sess, prefix);
    }

    public Session() {
        setSession(null);
    }


    public String toString() {
        return constructSessionString();
    }

    public void setPrefix(String prefix) {
        setSession(session, prefix);
    }

    public void setSession(String session, String prefix) {
        this.prefix = prefix;
        setSession(session);
    }

    public void setSession(String session) {
        this.session = session;
        group = getGroupFromSession(session);
        index = getIndexFromSession(session);
        orientation = getOrientationFromSession(session);
        dividerLocation = getDividerLocationFromSession(session);
        active = getIfActiveFromSession(session);
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public int getDividerLocation() {
        return dividerLocation;
    }

    public void setDividerLocation(int dividerLocation) {
        this.dividerLocation = dividerLocation;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDesktopElementTitle() {
        return desktopElementTitle;
    }

    public void setDesktopElementTitle(String desktopElementTitle) {
        this.desktopElementTitle = desktopElementTitle;
    }

    private int getGroupFromSession(String session) {
        if (session != null)
            try {
                return Integer.valueOf(ZUtilities.extractTaggedField(session, prefix + groupIndexTag)).intValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
        return 0;
    }


    private int getIndexFromSession(String session) {
        if (session != null)
            try {
                return Integer.valueOf(ZUtilities.extractTaggedField(session, prefix + docIndexTag)).intValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
        return -1;
    }

    private int getOrientationFromSession(String session) {
        if (session != null)
            try {
                return Integer.valueOf(ZUtilities.extractTaggedField(session, prefix + orientationTag)).intValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
        return 0;
    }

    private boolean getIfActiveFromSession(String session) {
        if (session != null)
            try {
                return Boolean.valueOf(ZUtilities.extractTaggedField(session, prefix + activeDocTag)).booleanValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
        return false;
    }

    private int getDividerLocationFromSession(String session) {
        if (session != null)
            try {
                return Integer.valueOf(ZUtilities.extractTaggedField(session, prefix + groupDividerLocationTag)).intValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
        return -1;
    }

    String constructSessionString() {
        StringBuffer sb = new StringBuffer();
        sb.append(ZUtilities.makeTaggedField(prefix + groupIndexTag, String.valueOf(group)));
        sb.append(ZUtilities.makeTaggedField(prefix + groupDividerLocationTag, String.valueOf(dividerLocation)));
        sb.append(ZUtilities.makeTaggedField(prefix + docIndexTag, String.valueOf(index)));
        sb.append(ZUtilities.makeTaggedField(prefix + orientationTag, String.valueOf(getOrientation())));
        sb.append(ZUtilities.makeTaggedField(prefix + activeDocTag, String.valueOf(active)));
        sb.append(ZUtilities.makeTaggedField(prefix + deTitleTag, desktopElementTitle));
        return sb.toString();
    }
}