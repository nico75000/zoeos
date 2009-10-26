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
import java.awt.*;
import java.util.ArrayList;

/**
 * User: paulmeehan
 * Date: 19-Jan-2004
 * Time: 10:27:53
 */
public class ZDocumentComponent extends DocumentComponent implements ZDisposable, ZDocumentPaneListener {
    private DesktopElement desktopElement;
    private boolean isContainer = false;

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

    public ZDocumentComponent(DesktopElement e) throws ComponentGenerationException {
        super(new ProxyComponent(e.getComponent()), e.getName().toString());
        this.desktopElement = e;
        desktopElement.addTitleProviderListener(tpl);
        updateTitleData();
    }

    public boolean isContainer() {
        return isContainer;
    }

    public JComponent getRealComponent() {
        return ((ProxyComponent) getComponent()).getRealComponent();
    }

    public void setRealComponent(JComponent jComponent) {
        ((ProxyComponent) getComponent()).setRealComponent(jComponent);
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

    public ArrayList getDesktopElementTree(boolean originals) {
        ArrayList elems = new ArrayList();
        if (isContainer) {
            ZDocumentPane zdp = (ZDocumentPane) getRealComponent();
            if (!desktopElement.getNodalDescriptor().showingWhenContainer())
                elems.add((originals ? desktopElement : desktopElement.getCopy()));
            elems.addAll(zdp.getDesktopElementTree(originals));
        } else
            elems.add((originals ? getRealDesktopElement() : getRealDesktopElement().getCopy()));
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
                    ZDocumentPane nzdp = new ZDocumentPane();
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
                    if (desktopElement.getNodalDescriptor().showingWhenContainer())
                        nzdp.openDesktopElement(new ContainerDesktopElement(desktopElement));
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
        if (desktopElement instanceof ContainerDesktopElement)
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
                (source.getDocumentCount() == 1 && ((ZDocumentComponent) source.getDocument(source.getDocumentNameAt(0))).getDesktopElement() instanceof ContainerDesktopElement))
            try {
                this.setContainer(false);
            } catch (ComponentGenerationException e1) {
            } catch (ChildViewNotAllowedException e1) {
            }
    }

    protected class ContainerDesktopElement implements DesktopElement {
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

        public ActivityContext getActivityContext() {
            // return desktopElement.getActivityContext();
            return StaticActivityContext.FALSE;
        }

        // obvious
        public boolean isFloatable() {
            return desktopElement.isFloatable();
        }

        public void setSessionString(String ss) {
            desktopElement.setSessionString(ss);
        }

        public String getSessionString() {
            return desktopElement.getSessionString();
        }

        public DesktopElement getCopy() {
            return desktopElement.getCopy();
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

        public boolean isMenuBarAvailable() {
            return desktopElement.isMenuBarAvailable();
        }

        public JMenuBar getMenuBar() {
            return desktopElement.getMenuBar();
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
