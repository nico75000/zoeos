package com.pcmsolutions.gui.desktop;

import com.jidesoft.document.DocumentComponent;
import com.jidesoft.document.DocumentPane;
import com.jidesoft.swing.JideTabbedPane;
import com.pcmsolutions.device.EMU.E4.gui.TitleProviderListener;
import com.pcmsolutions.gui.ComponentGenerationException;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.ZoeosPreferences;
import com.pcmsolutions.system.paths.DesktopName;
import com.pcmsolutions.system.paths.ViewPath;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.awt.*;

/**
 * User: paulmeehan
 * Date: 19-Jan-2004
 * Time: 10:27:53
 */
public class ZDocumentComponent extends DocumentComponent implements ZDisposable, TreeNode, ZDocumentPaneListener {
    private DesktopElement desktopElement;
    private boolean isContainer = false;
    TreeNode parent;

    private static class ProxyComponent extends JPanel {
        private JComponent realComponent;

        public ProxyComponent(JComponent comp) {
            super(new BorderLayout());
            setRealComponent(comp);
        }

        public JComponent getRealComponent() {
            return realComponent;
        }

        public void setRealComponent(JComponent realComponent) {
            this.realComponent = realComponent;
            this.removeAll();
            this.add(realComponent, BorderLayout.CENTER);
            this.revalidate();
            this.repaint();
        }
    }

    private final TitleProviderListener tpl = new TitleProviderListener() {
        public void titleProviderDataChanged(Object source) {
            updateTitleData();
            getDocumentPane().updateDocument(getName());
        }
    };

    public ZDocumentComponent(DesktopElement e, TreeNode parent) throws ComponentGenerationException {
        super(new ProxyComponent(e.getComponent()), e.getName().toString());
        this.parent = parent;
        this.desktopElement = e;
        desktopElement.addTitleProviderListener(tpl);
        updateTitleData();
    }

    public boolean isContainer() {
        return isContainer;
    }

    public boolean isContainerViewAsChild() {
        return desktopElement instanceof ContainerDesktopElement;
    }

    public JComponent getRealComponent() {
        return ((ProxyComponent) getComponent()).getRealComponent();
    }

    public void setRealComponent(JComponent jComponent) {
        ((ProxyComponent) getComponent()).setRealComponent(jComponent);
    }

    boolean areSibling(DesktopElement de1, DesktopElement de2) {
        ViewPath vp1 = de1.getViewPath();
        ViewPath vp2 = de2.getViewPath();
        if (vp1.getPathCount() == vp2.getPathCount() && vp1.getParentPath().equals(vp2.getParentPath()))
            return true;
        return false;
    }

    boolean isChildOf(DesktopElement de1, DesktopElement de2) {
        ViewPath vp1 = de1.getViewPath();
        ViewPath vp2 = de2.getViewPath();
        if (vp1.getPathCount() == vp2.getPathCount() - 1 && vp1.getPath().equals(vp2.getParentPath()))
            return true;
        return false;
    }

    public boolean addDesktopElement(DesktopElement de, boolean activate) throws ComponentGenerationException, ChildViewNotAllowedException, LogicalHierarchyException {
        if (!desktopElement.getName().isLogicalDescendant(de.getName()))
            throw new LogicalHierarchyException();
        setContainer(true);
        ZDocumentPane zdp = (ZDocumentPane) getRealComponent();
        return zdp.openDesktopElement(de, activate);
    }

    public boolean removeElement(String name) {
        if (isContainer) {
            ZDocumentPane zdp = (ZDocumentPane) getRealComponent();
            if (zdp.isDocumentOpened(name)) {
                zdp.closeDocument(name);
                return true;
            }
        }
        return false;
    }

    public void sendMessage(String msg) {
        if (isContainer) {
            ZDocumentPane zdp = (ZDocumentPane) getRealComponent();
            zdp.sendMessage(msg);
            // container element should ignore message
        }
        desktopElement.getActivityContext().sendMessage(msg);
    }
     public List<DesktopElement> evaluateCondition(String condition) {
         ArrayList<DesktopElement> positive = new ArrayList<DesktopElement>();
         if (isContainer) {
            ZDocumentPane zdp = (ZDocumentPane) getRealComponent();
            positive.addAll(zdp.evaluateCondition(condition));
            // container element should ignore message
        }
        if ( desktopElement.getActivityContext().testCondition(condition))
            positive.add(desktopElement.getCopy());
         return positive;
    }
    public ArrayList getDesktopElementTree(boolean originals) {
        ArrayList elems = new ArrayList();
        if (isContainer) {
            ZDocumentPane zdp = (ZDocumentPane) getRealComponent();
            if (!desktopElement.getNodalDescriptor().showingWhenContainer())
                elems.add((originals ? desktopElement : desktopElement.getCopy()));
            elems.addAll(zdp.getDesktopElementTree(originals));
        } else
            elems.add((originals ? getRealDesktopElement() : getRealDesktopElement().getCopy()));
        //  elems.add((originals ? getDesktopElement() : getDesktopElement().getCopy()));
        return elems;
    }

    public void setContainer(boolean container) throws ComponentGenerationException, ChildViewNotAllowedException {
        if (container != isContainer) {
            if (isContainer) {
                // contract
                ZDocumentPane zdp = (ZDocumentPane) getRealComponent();
                zdp.removeZDocumentPaneListener(this);
                zdp.closeAll();
                zdp.zDispose();
                setRealComponent(desktopElement.getComponent());
            } else {
                // expand
                if (desktopElement.getNodalDescriptor().allowsChildren()) {
                    ZDocumentPane nzdp = new ZDocumentPane(this);
                    nzdp.setTabbedPaneCustomizer(new DocumentPane.TabbedPaneCustomizer() {
                        public void customize(JideTabbedPane jideTabbedPane) {
                            jideTabbedPane.setRightClickSelect(true);
                            jideTabbedPane.setBoxStyleTab(ZoeosPreferences.ZPREF_boxStyleTabs.getValue());
                            if (!desktopElement.getNodalDescriptor().isShowingCloseButton()) {
                                jideTabbedPane.setUseDefaultShowCloseButtonOnTab(false);
                                jideTabbedPane.setShowCloseButtonOnTab(false);
                            }
                        }
                    });
                    nzdp.addZDocumentPaneListener(this);
                    nzdp.setGroupsAllowed(desktopElement.getNodalDescriptor().allowsGrouping());
                    nzdp.setReorderAllowed(desktopElement.getNodalDescriptor().allowsReordering());
                    if (desktopElement.getNodalDescriptor().showingWhenContainer()){
                        nzdp.containerSession = desktopElement.getSessionString();
                        nzdp.containerAsChildDesktopName = desktopElement.getName().toString();
                        nzdp.openDesktopElement(new ContainerDesktopElement(desktopElement));
                    }
                    setRealComponent(nzdp);
                } else
                    throw new ChildViewNotAllowedException();
            }
            isContainer = container;
        }
    }

    public void updateTitleData() {
        this.setTitle(desktopElement.getTitle());
        if (desktopElement.getIcon() != null)
            this.setIcon(desktopElement.getIcon());
        this.setTooltip(desktopElement.getToolTipText());
    }

    public DesktopElement getRealDesktopElement() {
        if (isContainerViewAsChild())
            return ((ContainerDesktopElement) desktopElement).desktopElement;
        return desktopElement;
    }

    public DesktopElement getDesktopElement() {
        return desktopElement;
    }

    public void zDispose() {
        try {
            if (isContainer)
                ((ZDocumentPane) this.getRealComponent()).zDispose();
            desktopElement.removeTitleProviderListener(tpl);
            desktopElement.zDispose();
            desktopElement = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ZDocumentComponentClosed(ZDocumentPane source) {
        if (source.getDocumentCount() == 0 ||
                (source.getDocumentCount() == 1 && ((ZDocumentComponent) source.getDocument(source.getDocumentNameAt(0))).isContainerViewAsChild()))
            try {
                this.setContainer(false);
            } catch (ComponentGenerationException e1) {
            } catch (ChildViewNotAllowedException e1) {
            }
    }

    // TREE NODE
    public TreeNode getChildAt(int childIndex) {
        if (!isContainer())
            return null;
        ZDocumentPane zdp = (ZDocumentPane) getRealComponent();
        return (ZDocumentComponent) zdp.getDocument(zdp.getDocumentNameAt(childIndex));
    }

    public int getChildCount() {
        if (!isContainer())
            return 0;
        ZDocumentPane zdp = (ZDocumentPane) getRealComponent();
        return zdp.getDocumentCount();
    }

    public TreeNode getParent() {
        return parent;
    }

    public int getIndex(TreeNode node) {
        if (node instanceof ZDocumentComponent)
            return ((ZDocumentPane) getRealComponent()).indexOfDocument(((ZDocumentComponent) node).getName());
        else
            return -1;
    }

    public boolean getAllowsChildren() {
        return true;
    }

    public boolean isLeaf() {
        return !isContainer();
    }

    public Enumeration<TreeNode> children() {
        if (isContainer()) {
            return new Enumeration<TreeNode>() {
                int next = 0;
                ZDocumentPane zdp = (ZDocumentPane) getRealComponent();

                public boolean hasMoreElements() {
                    return next < zdp.getDocumentCount();
                }

                public TreeNode nextElement() {
                    return (ZDocumentComponent) zdp.getDocument(zdp.getDocumentNameAt(next));
                }
            };
        } else
            return new Enumeration<TreeNode>() {
                public boolean hasMoreElements() {
                    return false;
                }

                public TreeNode nextElement() {
                    return null;
                }
            };
    }

    protected class ContainerDesktopElement implements DesktopElement {
        //String session;
        protected DesktopElement desktopElement;
        private final DesktopNodeDescriptor dnd = new DesktopNodeDescriptor() {
            public boolean allowsChildren() {
                return false;
            }

            public boolean showingWhenContainer() {
                return false;
            }

            public String getComponentAsContainerTitle() {
                return null;
            }

            public String getComponentAsContainerReducedTitle() {
                return null;
            }

            public boolean allowsGrouping() {
                return false;
            }

            public boolean allowsReordering() {
                return false;
            }

            public void customizePopupMenu(JPopupMenu popup) {
                // desktopElement.getNodalDescriptor().customizePopupMenu(popup);
            }

            public boolean isShowingCloseButton() {
                return false;
            }
        };
        //private static final String PARENT_PREFIX = "PARENT:";

        public ContainerDesktopElement(DesktopElement desktopElement) {
            this.desktopElement = desktopElement;
        }

        // should be specific to describing what this component does - e.g  "DefaultPresetEditor:43"
        // never displayed to the user - used internally by desktop manager to provide singleton documents
        public DesktopName getName() {
            return desktopElement.getName();
        }

        public ViewPath getViewPath() {
            return desktopElement.getViewPath();
        }

        // the component to be displayed
        public JComponent getComponent() throws ComponentGenerationException {
            return desktopElement.getComponent();
        }

        public boolean isComponentGenerated() {
            return desktopElement.isComponentGenerated();
        }

        public DesktopNodeDescriptor getNodalDescriptor() {
            return dnd;
        }

        final ActivityContext ac = new ActivityContext() {
            public boolean tryClosing() {
                return false;
            }

            public void sendMessage(String msg) {
                // TODO!! should this be here?
                // desktopElement.getActivityContext().sendMessage(msg);
            }

            public boolean testCondition(String condition) {
                return desktopElement.getActivityContext().testCondition(condition);
            }

            public void closed() {
            }

            public void activated() {
                desktopElement.getActivityContext().activated();
            }

            public void deactivated() {
                desktopElement.getActivityContext().deactivated();
            }
        };

        public ActivityContext getActivityContext() {
            // return desktopElement.getActivityContext();
            //return StaticActivityContext.FALSE;
            return ac;
        }

        public String retrieveComponentSessionString() {
            return desktopElement.retrieveComponentSessionString();
        }

        public void updateComponentSession(String sessStr) {
            desktopElement.updateComponentSession(sessStr);
        }

        public void setSessionString(String ss) {
            desktopElement.setSessionString(ss);
            //  session = ss;
        }

        public String getSessionString() {
            return desktopElement.getSessionString();
            // return session;
        }

        public DesktopElement getCopy() {
            return desktopElement.getCopy();
            // ContainerDesktopElement cde = new ContainerDesktopElement(desktopElement);
            // cde.session = session;
            //  return cde;
        }

        public String getTitle() {
            return desktopElement.getNodalDescriptor().getComponentAsContainerTitle();
        }

        public String getReducedTitle() {
            return desktopElement.getNodalDescriptor().getComponentAsContainerReducedTitle();
        }

        public void addTitleProviderListener(TitleProviderListener tpl) {
            desktopElement.addTitleProviderListener(tpl);
        }

        public void removeTitleProviderListener(TitleProviderListener tpl) {
            desktopElement.removeTitleProviderListener(tpl);
        }

        public Icon getIcon() {
            return desktopElement.getIcon();
        }

        public String getToolTipText() {
            return desktopElement.getToolTipText();
        }

        public void zDispose() {
        }

        public boolean isFrameMenuBarAvailable() {
            return desktopElement.isFrameMenuBarAvailable();
        }

        public JMenuBar getFrameMenuBar() {
            return desktopElement.getFrameMenuBar();
        }

        public boolean hasExpired() {
            return desktopElement.hasExpired();
        }

        public String toString() {
            return desktopElement.toString();
        }

        public int compareTo(Object o) {
            return desktopElement.compareTo(o);
        }
    }
}
