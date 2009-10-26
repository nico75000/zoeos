package com.pcmsolutions.gui;

import com.pcmsolutions.device.EMU.E4.zcommands.ZCommandRegistry;
import com.pcmsolutions.system.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 29-Mar-2003
 * Time: 10:11:03
 * To change this template use Options | File Templates.
 */
public class ZCommandFactory {
    public static final ZCommandPresentationContext nullContext = new ZCommandPresentationContext() {

        public Component[] getComponents() {
            return new Component[0];
        }

        public ZCommandContext[] getContexts() {
            return new ZCommandContext[0];
        }

        public void setTargets(Object[] targets) {

        }

        public void disableContext() {

        }

        public Class[] getMarkers() {
            return new Class[0];
        }

        public void zDispose() {

        }
    };

    public interface ZCommandContext extends ZDisposable {
        void setTargets(Object[] targets);

        ZCommand getCommand();

        Component getComponent();

        void setComponentStatus(boolean status);

        boolean getComponentStatus();
    }

    public interface ZCommandPresentationContext extends ZDisposable {
        Component[] getComponents();

        ZCommandContext[] getContexts();

        void setTargets(Object[] targets);

        void disableContext();

        Class[] getMarkers();
    }

    public static ZCommandPresentationContext getToolbarPresentationContext(final Class[] markers) {
        return getToolbarPresentationContext(markers, null);
    }
    public static ZCommandPresentationContext getToolbarPresentationContext(final Class[] markers,final  List<String> categories) {
        Class[] cmdClasses = ZCommandRegistry.getCommandClasses(markers);
        final HashMap subMenus = new HashMap();
        final JMenu menu = new JMenu();
        final ZCommandContext[] contexts = getZCommandContexts(cmdClasses, new ContextInitializer() {
            public Component makeComponent(ZCommand cmd) {
                String path = cmd.getMenuPathString();
                if (path.equals("")) {
                    Component c = getButton(cmd);
                    amendMenu(c, menu);
                    return c;
                } else {
                    JMenuItem jmi = getMenuItem(cmd);
                    amendMenu(jmi, path, menu, subMenus);
                    return jmi;
                }
            }

            JButton getButton(ZCommand cmd) {
                JButton b = new JButton();
                b.setMargin(new Insets(1, 1, 1, 1));
                if (cmd.getMnemonic() != 0)
                    b.setMnemonic(cmd.getMnemonic());
                return b;
            }

            JMenuItem getMenuItem(ZCommand cmd) {
                JMenuItem m = new JMenuItem();
                if (cmd.getMnemonic() != 0)
                    m.setMnemonic(cmd.getMnemonic());
                return m;
            }

            public ZCommandAction makeCommandAction(ZCommand command) {
                return new ZCommandAction(command, /*commands[0].isSuitableAsButton()*/command.getMenuPathString().equals(""));
            }

            public void setComponentAction(Component comp, Action a) {
                if (comp instanceof JButton)
                    ((JButton) comp).setAction(a);
                if (comp instanceof JMenuItem)
                    ((JMenuItem) comp).setAction(a);
            }

            public boolean acceptsCommand(ZCommand command) {
                return command.isSuitableInToolbar() && (categories == null ? true : categories.contains(command.getPresentationCategory()));
            }
        });

        return new ZCommandPresentationContext() {
            Component[] comps;

            public Component[] getComponents() {
                if (comps == null)
                    comps = menu.getMenuComponents();
                return comps;
            }

            public ZCommandContext[] getContexts() {
                return (ZCommandContext[]) contexts.clone();
            }

            public void setTargets(Object[] targets) {
                for (ZCommandContext c : contexts)
                    c.setTargets(targets);
            }

            public void disableContext() {
                for (int i = 0; i < contexts.length; i++)
                    contexts[i].setComponentStatus(false);
            }

            public Class[] getMarkers() {
                return (Class[]) markers.clone();
            }

            public void zDispose() {
                for (ZCommandContext c : contexts)
                    c.zDispose();
                comps = null;
            }
        };
    }

    public static ZCommandPresentationContext getMenuPresentationContext(final Class[] markers, final List<String> categories) {
        Class[] cmdClasses = ZCommandRegistry.getCommandClasses(markers);
        final HashMap subMenus = new HashMap();
        final JMenu menu = new JMenu();
        final ZCommandContext[] contexts = getZCommandContexts(cmdClasses, new ContextInitializer() {
            public Component makeComponent(ZCommand cmd) {
                String path = cmd.getMenuPathString();
                JMenuItem jmi = getMenuItem(cmd);
                amendMenu(jmi, path, menu, subMenus);
                return jmi;
            }

            JMenuItem getMenuItem(ZCommand cmd) {
                JMenuItem m = new JMenuItem();
                if (cmd.getMnemonic() != 0)
                    m.setMnemonic(cmd.getMnemonic());
                return m;
            }

            public ZCommandAction makeCommandAction(ZCommand command) {
                return new ZCommandAction(command, false);
            }

            public void setComponentAction(Component comp, Action a) {
                if (comp instanceof JMenuItem)
                    ((JMenuItem) comp).setAction(a);
            }

            public boolean acceptsCommand(ZCommand command) {
                return (categories == null ? true : categories.contains(command.getPresentationCategory()));
            }
        });

        return new ZCommandPresentationContext() {
            Component[] comps;

            public Component[] getComponents() {
                if (comps == null)
                    comps = menu.getMenuComponents();
                return comps;
            }

            public ZCommandContext[] getContexts() {
                return (ZCommandContext[]) contexts.clone();
            }

            public void setTargets(Object[] targets) {
                for (ZCommandContext c : contexts)
                    c.setTargets(targets);
            }

            public void disableContext() {
                for (int i = 0; i < contexts.length; i++)
                    contexts[i].setComponentStatus(false);
            }

            public Class[] getMarkers() {
                return (Class[]) markers.clone();
            }

            public void zDispose() {
                for (ZCommandContext c : contexts)
                    c.zDispose();
                comps = null;
            }
        };
    }

    public static ZCommandPresentationContext getButtonPresentationContext(final Class[] markers) {
        Class[] cmdClasses = ZCommandRegistry.getCommandClasses(markers);
        final ArrayList<Component> components = new ArrayList<Component>();
        final ZCommandContext[] contexts = getZCommandContexts(cmdClasses, new ContextInitializer() {
            public Component makeComponent(ZCommand cmd) {
                Component c = getButton(cmd);
                components.add(c);
                return c;
            }

            JButton getButton(ZCommand cmd) {
                JButton b = new JButton();
                b.setMargin(new Insets(1, 1, 1, 1));
                if (cmd.getMnemonic() != 0)
                    b.setMnemonic(cmd.getMnemonic());
                return b;
            }

            public ZCommandAction makeCommandAction(ZCommand command) {
                return new ZCommandAction(command, true);
            }

            public void setComponentAction(Component comp, Action a) {
                if (comp instanceof JButton)
                    ((JButton) comp).setAction(a);
            }

            public boolean acceptsCommand(ZCommand command) {
                return command.isSuitableAsButton();
            }
        });

        return new ZCommandPresentationContext() {
            Component[] comps;

            public Component[] getComponents() {
                if (comps == null)
                    comps = components.toArray(new Component[components.size()]);
                return comps;
            }

            public ZCommandContext[] getContexts() {
                return (ZCommandContext[]) contexts.clone();
            }

            public void setTargets(Object[] targets) {
                for (ZCommandContext c : contexts)
                    c.setTargets(targets);
            }

            public void disableContext() {
                for (int i = 0; i < contexts.length; i++)
                    contexts[i].setComponentStatus(false);
            }

            public Class[] getMarkers() {
                return (Class[]) markers.clone();
            }

            public void zDispose() {
                for (ZCommandContext c : contexts)
                    c.zDispose();
                comps = null;
            }
        };
    }

    public static void showPopup(final String label, final Component invoker, final Object[] userObjects, final MouseEvent e) {
        JPopupMenu p = getPopup(userObjects, label);
        showPopup(p, invoker, e);
    }

    public static void showPopup(final JPopupMenu popup, final Component invoker, final MouseEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                popup.show(invoker, e.getX(), e.getY());
            }
        });
    }

    public static Object[] stripNullsAndEmptyStrings(Object[] userObjects) {
        ArrayList outObjs = new ArrayList();
        for (int i = 0; i < userObjects.length; i++) {
            if (userObjects[i] != null && !userObjects[i].equals(""))
                outObjs.add(userObjects[i]);
        }
        return outObjs.toArray();
    }

    public static ZCommandProvider[] extractZCommandProviders(Object[] userObjects) {
        ArrayList<ZCommandProvider> outObjs = new ArrayList<ZCommandProvider>();
        for (Object o : userObjects)
            if (o instanceof ZCommandProvider)
                outObjs.add((ZCommandProvider) o);
        return outObjs.toArray(new ZCommandProvider[outObjs.size()]);
    }

    public static Object[] stripNullsAndStrings(Object[] userObjects) {
        ArrayList outObjs = new ArrayList();
        for (int i = 0; i < userObjects.length; i++) {
            if (userObjects[i] != null && !(userObjects[i] instanceof String))
                outObjs.add(userObjects[i]);
        }
        return outObjs.toArray();
    }

    public static JPopupMenu getPopup(Object[] userObjects, String label) {
        JPopupMenu p = getMenu(userObjects, label, true).getPopupMenu();
        return p;
    }

    public static Class[] getCommonMarkers(Object[] userObjects) {
        userObjects = ZUtilities.getRealObjects(extractZCommandProviders(userObjects));
        if (userObjects.length == 0)
            return new Class[0];
        if (userObjects.length == 1 && userObjects[0] instanceof ZCommandProvider) {
            return ((ZCommandProvider) userObjects[0]).getZCommandMarkers();
        } else {
            List common = Arrays.asList(((ZCommandProvider) userObjects[0]).getZCommandMarkers());
            for (int j = 1; j < userObjects.length; j++)
                if (userObjects[j] instanceof ZCommandProvider)
                    common = ZUtilities.intersection(common, Arrays.asList(((ZCommandProvider) userObjects[j]).getZCommandMarkers()));
                else
                    return new Class[0];
            return (Class[]) common.toArray(new Class[common.size()]);
        }
    }

    /*
    public static ZCommand[] getZCommandsForMarkers(Class[] markers, ZCommandProvider zcp) {
        ArrayList zc = new ArrayList();
        for (int i = 0; i < markers.length; i++)
            zc.addAll(Arrays.asList(zcp.getZCommands(markers[i])));
        return (ZCommand[]) zc.toArray(new ZCommand[zc.size()]);
    }
    */
    public static ZCommandContext[] getZCommandContexts(Class[] cmdClasses, ContextInitializer ci) {
        ArrayList<ZCommandContext> contexts = new ArrayList<ZCommandContext>();
        ArrayList<ZCommand> commands = new ArrayList<ZCommand>();
        for (Class<ZCommand> c : cmdClasses)
            try {
                ZCommandRegistry.insert(c.newInstance(), commands);
            } catch (Exception e) {
                e.printStackTrace();
            }
        Collections.sort(commands, zcommandMenuPathComparator);
        for (ZCommand z : commands) {
            try {
                if (ci.acceptsCommand(z))
                    contexts.add(new Impl_ZCommandContext(z, ci));

                if (z instanceof ZMTCommand) {
                    ZMTCommand zmtc = (ZMTCommand) z;
                    for (zmtc = zmtc.getNextMode(); zmtc != null; zmtc = zmtc.getNextMode())
                        if (ci.acceptsCommand(zmtc))
                            contexts.add(new Impl_ZCommandContext(zmtc, ci));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return contexts.toArray(new ZCommandContext[contexts.size()]);
    }

    public static JMenu getMenu(Object[] userObjects, String label) {
        return getMenu(userObjects, label, true);
    }

    public static JMenu getMenu(Object[] userObjects, String label, boolean sortSubMenus) {
        ZCommandPresentationContext c = ZCommandFactory.getMenuPresentationContext(getCommonMarkers(userObjects), null);
        c.setTargets(ZUtilities.getRealObjects(extractZCommandProviders(userObjects)));
        JMenu menu = new JMenu(label);
        for (Component comp : c.getComponents())
            menu.add(comp);
        if (sortSubMenus)
            return ZUtilities.sortSubMenus(menu, false);
        else
            return menu;
    }

    public static ZCommandPresentationContext getTargetedButtonPresentationContext(Object[] userObjects) {
        ZCommandPresentationContext c = getButtonPresentationContext(getCommonMarkers(userObjects));
        c.setTargets(ZUtilities.getRealObjects(extractZCommandProviders(userObjects)));
        return c;
    }

    public static ZCommandPresentationContext getTargetedMenuPresetentationContext(Object[] userObjects) {
        ZCommandPresentationContext c = getMenuPresentationContext(getCommonMarkers(userObjects), null);
        c.setTargets(ZUtilities.getRealObjects(extractZCommandProviders(userObjects)));
        return c;
    }

    public static ZCommandPresentationContext getTargetedToolbarPresentationContext(Object[] userObjects) {
        ZCommandPresentationContext c = getToolbarPresentationContext(getCommonMarkers(userObjects), null);
        c.setTargets(ZUtilities.getRealObjects(extractZCommandProviders(userObjects)));
        return c;
    }

    private static final Comparator<ZCommand> zcommandMenuPathComparator = new Comparator<ZCommand>() {
        public int compare(ZCommand zCommand, ZCommand zCommand1) {
            return zCommand.getMenuPathString().compareTo(zCommand1.getMenuPathString());
        }
    };

    private static final Comparator<ZCommandContext> zcommandContextMenuPathComparator = new Comparator<ZCommandContext>() {
        public int compare(ZCommandContext zCommandContext, ZCommandContext zCommandContext1) {
            return zcommandMenuPathComparator.compare(zCommandContext.getCommand(), zCommandContext1.getCommand());
        }
    };

    private static void amendMenu(JMenuItem m, String path, final JMenu menu, Map subMenus) {
        if (!path.equals("")) { // if its a submenu
            createMenuForPath(menu, path, subMenus).add(m);
        } else {
            menu.add(m);
        }
    }

    private static void amendMenu(Component c, final JMenu menu) {
        menu.add(c);
    }

    private static JMenu createMenuForPath(JMenu menu, String path, Map subMenus) {
        StringTokenizer t = new StringTokenizer(path, ";", false);
        int numToks = t.countTokens();
        if (numToks == 0)
            throw new IllegalArgumentException("not a valid menu path");

        String[] toks = new String[numToks];
        int c = 0;

        // get tokens into an array
        while (t.hasMoreTokens())
            toks[c++] = t.nextToken();

        String currPath = "";
        JMenu lastMenu = null;
        JMenu newMenu;
        for (int n = 0; n < toks.length; n++) {
            currPath = currPath + ";" + toks[n];

            if (!subMenus.containsKey(currPath)) {
                newMenu = new JMenu(toks[n]);

                if (lastMenu == null)
                    menu.add(newMenu);
                else
                    lastMenu.add(newMenu);

                subMenus.put(currPath, newMenu);
                lastMenu = newMenu;
            } else
                lastMenu = (JMenu) subMenus.get(currPath);
        }
        return (JMenu) subMenus.get(path);
    }
}

interface ContextInitializer {
    Component makeComponent(ZCommand cmd);

    ZCommandAction makeCommandAction(ZCommand command);

    void setComponentAction(Component comp, Action a);

    boolean acceptsCommand(ZCommand command);
}

abstract class AbstractZCommandContext implements ZCommandFactory.ZCommandContext {
    private Component comp;
    private boolean status;
    private ContextInitializer contextInitializer;
    private ZCommandAction action;
    private ZCommand command;

    protected AbstractZCommandContext(ZCommand command, ContextInitializer contextInitializer) {
        this.contextInitializer = contextInitializer;
        setCommand(command);
    }

    final public void setComponentStatus(boolean status) {
        this.status = status;
        if (comp != null)
            comp.setEnabled(status);
    }

    final public Component getComponent() {
        checkCommand(command);
        if (comp == null)
            comp = contextInitializer.makeComponent(command);
        comp.setEnabled(status);
        return comp;
    }

    private static void checkCommand(ZCommand command) {
        if (command == null)
            throw new IllegalArgumentException("cannot generate the ZCommandContext component with a null ZCommand object");
    }

    public boolean getComponentStatus() {
        return status;
    }

    public ZCommand getCommand() {
        return command;
    }

    private void setCommand(ZCommand command) {
        checkCommand(command);
        if (!contextInitializer.acceptsCommand(command))
            throw new IllegalArgumentException("supplied ZCommand not acceptable for ZCommandContext");

        if (this.command != command) {
            this.command = command;
            makeComponentAction();
        }
    }

    private void makeComponentAction() {
        action = contextInitializer.makeCommandAction(command);
        contextInitializer.setComponentAction(getComponent(), action);
    }

    public void zDispose() {
        comp = null;
        command = null;
        contextInitializer = null;
        action = null;
    }
}

class Impl_ZCommandContext extends AbstractZCommandContext {

    protected Impl_ZCommandContext(ZCommand command, ContextInitializer i) {
        super(command, i);
        setComponentStatus(false);
    }

    public void setTargets(Object[] targets) {
        targets = ZUtilities.getRealObjects(ZCommandFactory.extractZCommandProviders(targets));
        ZCommand cmd = getCommand();
        if (targets.length >= cmd.getMinNumTargets() && targets.length <= cmd.getMaxNumTargets()) {
            try {
                cmd.setTargets(targets);
                setComponentStatus(true);
                return;
            } catch (ZCommandTargetsNotSuitableException e) {
            }
        }
        setComponentStatus(false);
    }
}
