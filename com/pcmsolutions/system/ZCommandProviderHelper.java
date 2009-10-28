package com.pcmsolutions.system;

import com.pcmsolutions.device.EMU.E4.zcommands.ZCommandRegistry;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 13-Mar-2003
 * Time: 06:36:35
 * To change this template use Options | File Templates.
 */
public class ZCommandProviderHelper {
    private Class[] markerClasses;

    public ZCommandProviderHelper(Class markerClass) {
        this.markerClasses = new Class[]{markerClass};
    }

    public ZCommandProviderHelper(Class markerClass, ZCommandProviderHelper superHelper) {
        this.markerClasses = concatClasses(superHelper.getSupportedMarkers(), markerClass);
    }

    private Class[] concatClasses(Class[] c1, Class c) {
        Class[] c2 = new Class[c1.length + 1];
        System.arraycopy(c1, 0, c2, 0, c1.length);
        c2[c1.length] = c;
        return c2;
    }

    public Class[] getSupportedMarkers() {
        return markerClasses;
    }

    public Class[] getClassesForMarker(Class marker) {
        return ZCommandRegistry.getCommandClasses(marker);
    }

    public ZCommand[] getCommandObjects(Class markerClass, Object target) {
        Class[] cmdClasses = getClassesForMarker(markerClass);
        ArrayList<ZCommand> cmdObjects = new ArrayList<ZCommand>();
        for (int n = 0; n < cmdClasses.length; n++) {
            try {
                ZCommandRegistry.insert((ZCommand) cmdClasses[n].newInstance(), cmdObjects);
            } catch (InstantiationException e) {
                System.out.println("Problem creating ZCommand object(" + e.getClass().toString() + ")");
                continue;
            } catch (IllegalAccessException e) {
                System.out.println("Problem creating ZCommand object(" + e.getClass().toString() + ")");
                continue;
            } catch (IllegalArgumentException e) {
                System.out.println("Problem setting target for ZCommand object (" + e.getClass().toString() + ")");
                continue;
            }/* catch (ZCommandTargetsNotSuitableException e) {
                System.out.println("Problem setting target for ZCommand object (" + e.getClass().toString() + ")");
                continue;
            } */
        }
        ZCommand[] zco = new ZCommand[cmdObjects.size()];
        cmdObjects.toArray(zco);
        return zco;
    }
}
