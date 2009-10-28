package com.pcmsolutions.gui.desktop;

import com.pcmsolutions.device.EMU.E4.gui.TitleProvider;
import com.pcmsolutions.device.EMU.E4.gui.TitleProviderListener;
import com.pcmsolutions.gui.ComponentGenerationException;
import com.pcmsolutions.gui.FrameMenuBarProvider;
import com.pcmsolutions.system.Indexable;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.paths.DesktopName;
import com.pcmsolutions.system.paths.ViewPath;

import javax.swing.*;

/**
 * User: paulmeehan
 * Date: 21-Jan-2004
 * Time: 06:38:59
 */
public abstract class AbstractDesktopElement implements DesktopElement {
    protected transient JComponent view;
    protected ActivityContext activityContext;
    protected ViewPath viewPath;
    protected DesktopNodeDescriptor nodalDescriptor;
    protected String sessionString;
    protected String componentSessionString;

    public AbstractDesktopElement(ViewPath viewPath, ActivityContext closeBehaviour, DesktopNodeDescriptor nodalDescriptor) {
        this.activityContext = closeBehaviour;
        this.viewPath = viewPath;
        this.nodalDescriptor = nodalDescriptor;
    }

    // should be specific to describing what this component does - e.g  "DefaultPresetEditor:43"
    // never displayed to the user - used internally by desktop manager to provide singleton documents
    public final DesktopName getName() {
        return (DesktopName) viewPath.getLastPathComponent();
    }

    public ViewPath getViewPath() {
        return viewPath;
    }

    public String retrieveComponentSessionString() {
        if (view instanceof SessionableComponent)
            componentSessionString = ((SessionableComponent) view).retrieveComponentSession();
        //else
        //  componentSessionString = null;
        return componentSessionString;
    }

    public void updateComponentSession(String sessStr) {
        if (sessStr != null && componentSessionString != null && componentSessionString.equals(sessStr))
            return;
        componentSessionString = sessStr;
        if (view instanceof SessionableComponent && componentSessionString != null && !componentSessionString.equals(""))
            ((SessionableComponent) view).restoreComponentSession(componentSessionString);
    }

    public int compareTo(Object o) {
        if (o instanceof DesktopElement) {
            DesktopElement de = (DesktopElement) o;
            try {
                if (view instanceof Indexable && de.getComponent() instanceof Indexable)
                    return ((Indexable) view).getIndex().compareTo(((Indexable) de.getComponent()).getIndex());
            } catch (ComponentGenerationException e) {
                e.printStackTrace();
            }
            // TODO!! could do more here
            return this.getName().getLogicalPaths()[0].compareTo(de.getName().getLogicalPaths()[0]);
        } else
            return 0;
    }

    // the component to be displayed
    public final JComponent getComponent() throws ComponentGenerationException {
        if (view instanceof JComponent)
            return view;
        else {
            view = createView();
            if (view instanceof SessionableComponent && componentSessionString != null && !componentSessionString.equals(""))
                ((SessionableComponent) view).restoreComponentSession(componentSessionString);
        }
        return view;
    }

    protected abstract JComponent createView() throws ComponentGenerationException;

    public DesktopNodeDescriptor getNodalDescriptor() {
        return nodalDescriptor;
    }

    public final boolean isComponentGenerated() {
        return view instanceof JComponent;
    }

    // obvious
    public final ActivityContext getActivityContext() {
        return activityContext;
    }

    public void setSessionString(String ss) {
        sessionString = ss;
    }

    public String getSessionString() {
        return sessionString;
    }

    public final String getTitle() {
        if (view instanceof TitleProvider)
            return ((TitleProvider) view).getTitle();
        else
            return "Unknown";
    }

    public final String getReducedTitle() {
        if (view instanceof TitleProvider)
            return ((TitleProvider) view).getReducedTitle();
        else
            return "Unknown";
    }

    public final void addTitleProviderListener(TitleProviderListener tpl) {
        if (view instanceof TitleProvider)
            ((TitleProvider) view).addTitleProviderListener(tpl);
    }

    public final void removeTitleProviderListener(TitleProviderListener tpl) {
        if (view instanceof TitleProvider)
            ((TitleProvider) view).removeTitleProviderListener(tpl);
    }

    public final Icon getIcon() {
        if (view instanceof TitleProvider)
            return ((TitleProvider) view).getIcon();
        else
            return null;
    }

    public final String getToolTipText() {
        if (view instanceof TitleProvider)
            return ((TitleProvider) view).getToolTipText();
        else
            return null;
    }

    public void zDispose() {

        if (view instanceof ZDisposable) {
            ((ZDisposable) view).zDispose();
            view = null;
        }
    }

    public final boolean isFrameMenuBarAvailable() {
        try {
            JComponent c = getComponent();
            if (c instanceof FrameMenuBarProvider)
                return ((FrameMenuBarProvider) c).isFrameMenuBarAvailable();
        } catch (ComponentGenerationException e) {
            e.printStackTrace();
        }
        return false;
    }

    public final JMenuBar getFrameMenuBar() {
        try {
            JComponent c = getComponent();
            if (c instanceof FrameMenuBarProvider)
                return ((FrameMenuBarProvider) c).getFrameMenuBar();
        } catch (ComponentGenerationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean equals(Object obj) {
        if (obj instanceof DesktopElement)
            return viewPath.equals(((DesktopElement) obj).getViewPath());// && name.equals(((DesktopElement) obj).getName());
        return false;
    }
}
