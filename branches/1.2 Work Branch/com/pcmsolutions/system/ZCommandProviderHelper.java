package com.pcmsolutions.system;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 13-Mar-2003
 * Time: 06:36:35
 * To change this template use Options | File Templates.
 */
public class ZCommandProviderHelper {
    private Class filterClass;
    private Vector commands = new Vector();
    private static final String PREF_ZCommands = "ZCommands";

    public ZCommandProviderHelper(Class classFilter, String defaultZCommands) {
        this.filterClass = classFilter;
        String s = Preferences.userNodeForPackage(classFilter).get(PREF_ZCommands, defaultZCommands);
        if (s != null) {
            Enumeration tok = new StringTokenizer(s, Zoeos.preferenceFieldSeperator);

            String next = null;
            ArrayList cmds = new ArrayList();
            while (tok.hasMoreElements())
                cmds.add(tok.nextElement());

            //Collections.sort(cmds);
            for (int i = 0, j = cmds.size(); i < j; i++)
                try {
                    addCommandClass((String) cmds.get(i));
                } catch (ClassNotFoundException e) {
                    System.out.println("Configured ZCommand " + cmds.get(i) + " is not a valid class.");
                }
        }
    }

    private void addCommandClass(String classPath) throws ClassNotFoundException {
        /*if (classPath.equals("seperator")) {
            // treated as a menu seperator
            commands.addDesktopElement(null);
            return;
        } */
        Class c = Class.forName(classPath);
        if (filterClass.isAssignableFrom(c) && ZCommand.class.isAssignableFrom(c))
            commands.add(c);
        else
            System.out.println("Configured ZCommand " + c.toString() + "class not of the correct type.");
    }

    public Class[] getCommandClasses() {
        return (Class[])commands.toArray(new Class[commands.size()]);
    }

    public ZCommand[] getCommandObjects(Object target) {
        Class[] cmdClasses = getCommandClasses();
        ArrayList cmdObjects = new ArrayList();
        ZCommand cmd;
        for (int n = 0; n < cmdClasses.length; n++) {
            try {
                // if (cmdClasses[n] == null)
                // treated as a menu seperator
                //   cmdObjects.addDesktopElement(null);
                // else {
                cmd = (ZCommand) cmdClasses[n].newInstance();
                cmd.setTarget(target);
                cmdObjects.add(cmd);
                // }
            } catch (InstantiationException e) {
                System.out.println("Problem creating ZCommand object(" + e.getClass().toString() + ")");
                continue;
            } catch (IllegalAccessException e) {
                System.out.println("Problem creating ZCommand object(" + e.getClass().toString() + ")");
                continue;
            } catch (IllegalArgumentException e) {
                System.out.println("Problem setting target for ZCommand object (" + e.getClass().toString() + ")");
                continue;
            }
        }
        ZCommand[] zco = new ZCommand[cmdObjects.size()];
        cmdObjects.toArray(zco);
        return zco;
    }
}
