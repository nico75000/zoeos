package com.pcmsolutions.gui;

import com.pcmsolutions.device.EMU.E4.gui.colors.ColorContext;
import com.pcmsolutions.system.*;
import com.pcmsolutions.util.ClassUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 29-Mar-2003
 * Time: 10:11:03
 * To change this template use Options | File Templates.
 */
public class ZCommandInvocationHelper {
    public static void showPopup(final String label, final Component invoker, final Object[] userObjects, final MouseEvent e, final ColorContext cc) {
        if (cc == null)
            showPopup(label, invoker, userObjects, e);
        else
            showPopup(label, invoker, userObjects, e, cc.getFGColor(), cc.getBGColor());
    }

    public static void showPopup(final String label, final Component invoker, final Object[] userObjects, final MouseEvent e) {
        showPopup(label, invoker, userObjects, e, null, null);
    }

    public static void showPopup(final String label, final Component invoker, final Object[] userObjects, final MouseEvent e, final Color fg, final Color bg) {
        JPopupMenu p = getPopup(userObjects, fg, bg, label);
        showPopup(p, invoker, e);
    }

    public static void showPopup(final JPopupMenu popup, final Component invoker, final MouseEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                popup.show(invoker, e.getX(), e.getY());
            }
        });
    }

    private static Object[] stripNullsAndEmptyStrings(Object[] userObjects) {
        ArrayList outObjs = new ArrayList();
        for (int i = 0; i < userObjects.length; i++) {
            if (userObjects[i] != null && !userObjects[i].equals(""))
                outObjs.add(userObjects[i]);
        }
        return outObjs.toArray();
    }

    private static Object[] stripNullsAndStrings(Object[] userObjects) {
        ArrayList outObjs = new ArrayList();
        for (int i = 0; i < userObjects.length; i++) {
            if (userObjects[i] != null && !(userObjects[i] instanceof String))
                outObjs.add(userObjects[i]);
        }
        return outObjs.toArray();
    }

    public static JPopupMenu getPopup(Object[] userObjects, Color fg, Color bg, String label) {
        JPopupMenu p = getMenu(userObjects, fg, bg, label).getPopupMenu();
        return p;
    }

    public static ZCommandAction[] getLaunchButtonSuitableZCommandActions(ZCommandProvider userObject) {
        ZCommand[] allCmds = userObject.getZCommands();
        ArrayList outCmds = new ArrayList();
        for (int i = 0, n = allCmds.length; i < n; i++)
            if (allCmds[n].isSuitableAsLaunchButton())
                outCmds.add(allCmds[n]);
        ZCommandAction[] outActions = new ZCommandAction[outCmds.size()];
        for (int i = 0, n = outCmds.size(); i < n; i++)
            outActions[i] = new ZCommandAction(new ZCommand[]{(ZCommand) outCmds.get(i)});
        return outActions;
    }

    public static Component[] getMenuComponents(Object[] userObjects) {
        return getMenu(userObjects, null, null, "").getMenuComponents();
    }

    public static JMenu getMenu(Object[] userObjects, Color fg, Color bg, String label) {
        userObjects = ZUtilities.getRealObjects(stripNullsAndStrings(userObjects));
        HashMap subMenus = new HashMap();
        final JMenu menu = new JMenu(label);
        menu.setDoubleBuffered(true);
        if (fg == null)
            fg = menu.getForeground();
        if (bg == null)
            bg = menu.getBackground();

        menu.setForeground(fg);
        menu.setBackground(bg);

        if (userObjects.length != 0) {
//            if (ClassUtility.areAllSameClass(userObjects) && userObjects[0] instanceof ZCommandProvider) {
            //              ZCommand[] zcmds = ((ZCommandProvider) userObjects[0]).getZCommands();
            Object o = ClassUtility.getMostSuperObject(userObjects);

            if (o == null && ClassUtility.areAllObjectsEditableParameterModels(userObjects))
                o = userObjects[0];

            if (o != null && o instanceof ZCommandProvider) {
                ZCommand[] zcmds = ((ZCommandProvider) o).getZCommands();

                //menu.setText(o.getClass().toString());
                if (zcmds.length != 0) {
                    for (int n = 0; n < zcmds.length; n++) {
                        // seperator
                        //if (zcmds[n] == null) {
                        //  menu.addSeparator();
                        //continue;
                        // }
                        if (zcmds[n] instanceof ZMTCommand) {
                            ZMTCommand[] targetedCmds;
                            ZMTCommand currCommand = (ZMTCommand) zcmds[n];
                            while (currCommand != null) {
                                try {
                                    // make sure we have neither too many nor too few targets for this command
                                    if (currCommand.getMinNumTargets() > userObjects.length || currCommand.getMaxNumTargets() < userObjects.length)
                                        continue;
                                    targetedCmds = new ZMTCommand[1];
                                    targetedCmds[0] = currCommand;
                                    targetedCmds[0].setTargets(userObjects);
                                    amendMenu(fg, bg, targetedCmds, currCommand, menu, subMenus);
                                } catch (ZMTCommandTargetsNotSuitableException e) {
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    currCommand = currCommand.getNextMode();
                                }
                            }
                        } else {       // ZCommand
                            ZCommand[] targetedCmds;
                            targetedCmds = new ZCommand[userObjects.length];
                            for (int i = 0; i < userObjects.length; i++)
                                targetedCmds[i] = ((ZCommandProvider) userObjects[i]).getZCommands()[n];
                            amendMenu(fg, bg, targetedCmds, targetedCmds[0], menu, subMenus);
                        }
                    }

                } else
                    menu.add(new JMenuItem("=No Commands Available="), 0);

            } else {
                menu.add(new JMenuItem("=No Commands Available="), 0);
            }
        }
        return ZUtilities.sortSubMenus(menu, true);
    }

    private static void amendMenu(Color fg, Color bg, ZCommand[] targetedCmds, ZCommand currModalCommand, final JMenu menu, Map subMenus) {
        JMenuItem m;
        m = new JMenuItem();
        m.setForeground(fg);
        m.setBackground(bg);
        m.setAction(new ZCommandAction(targetedCmds));
        if (targetedCmds[0].getMnemonic() != 0)
            m.setMnemonic(targetedCmds[0].getMnemonic());
        String path = currModalCommand.getMenuPathString();

        if (!path.equals("")) { // if its a submenu
            createMenuForPath(menu, path, subMenus, fg, bg).add(m);
        } else {
            menu.add(m);
        }
    }

    private static JMenu createMenuForPath(JMenu menu, String path, Map subMenus, Color fg, Color bg) {
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
                newMenu.setForeground(fg);
                newMenu.setBackground(bg);

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
