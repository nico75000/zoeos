package com.pcmsolutions.util;

import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;

import java.util.ArrayList;


/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 12-Mar-2003
 * Time: 11:42:56
 * To change this template use Options | File Templates.
 */
public class ClassUtility {

    public static int firstIndexOfClass(Object[] objs, Class c, boolean fromFront) {
        if (fromFront) {
            for (int i = 0; i < objs.length; i++)
                if (c.isInstance(objs[i]))
                    return i;
        } else {
            for (int i = objs.length - 1; i >= 0; i--)
                if (c.isInstance(objs[i]))
                    return i;
        }
        return -1;
    }

    public static boolean areAllInstanceOf(Object[] objects, Class c) {
        if (objects.length == 0)
            return false;

        for (int n = 0; n < objects.length; n++)
            if (!c.isInstance(objects[n]))
                return false;
        return true;
    }

    public static int instanceCount(Object[] objects, Class c) {
        int count = 0;
        for (int n = 0; n < objects.length; n++)
            if (!c.isInstance(objects[n]))
                count++;
        return count;
    }

    public static Object[] extractInstanceOf(Object[] objects, Class c) {
        ArrayList instances = new ArrayList();
        for (int i = 0; i < objects.length; i++)
            if (c.isInstance(objects[i]))
                instances.add(objects[i]);
        return instances.toArray();
    }


    public static boolean areAllSameClass(Object[] objects) {
        if (objects.length == 0)
            return false;

        Class curr;
        Class first = objects[0].getClass();

        for (int n = 1; n < objects.length; n++) {
            curr = objects[n].getClass();

            if (curr != first)
                return false;
        }
        return true;
    }    

    // object returned is an element from the array passed in and is an instance of the most super type
    // null returned if there is no object that can represent the most super ( i.e at least one object is not on the same inheritance branch as all the others)
    public static Object getMostSuperObject(Object[] objects) {
        if (objects.length == 0 || objects[0] == null)
            return null;

        Class currClass;
        Object currObject;
        Object mostSuperObject = objects[0];
        Class mostSuperClass = mostSuperObject.getClass();

        for (int n = 1; n < objects.length; n++) {
            currObject = objects[n];
            if (currObject == null)
                return null;

            currClass = currObject.getClass();
            // make sure they are on the same inheritance branch
            if (currClass.isAssignableFrom(mostSuperClass)) {
                mostSuperClass = currClass;
                mostSuperObject = currObject;
            } else if (mostSuperClass.isAssignableFrom(currClass))
                ;// do nothing
            else    // not on same inheritance branch so return null
                return null;
        }
        return mostSuperObject;
    }


    public static boolean areAllObjectsEditableParameterModels(Object[] objects) {
        for (int i = 0,j = objects.length; i < j; i++) {
            if (!(objects[i] instanceof EditableParameterModel))
                return false;
        }
        return true;
    }

    // object returned is an element from the array passed in and is an instance of the most super type
    // null returned if there is no object that can represent the most super ( i.e at least one object is not on the same inheritance branch as all the others)
    public static Object getMostSuperObjectIgnoreNullsAndEmptyStrings(Object[] objects) {
        if (objects.length == 0)
            return null;

        Class currClass;
        Object currObject;
        int i = 0;
        Object mostSuperObject = objects[i];
        while (mostSuperObject == null || mostSuperObject.equals(""))
            if (i >= objects.length - 1)
                return null;
            else
                mostSuperObject = objects[++i];

        Class mostSuperClass = mostSuperObject.getClass();

        for (int n = i + 1; n < objects.length; n++) {
            currObject = objects[n];
            if (currObject == null || currObject.equals(""))
                continue;
            currClass = currObject.getClass();
            // make sure they are on the same inheritance branch
            if (currClass.isAssignableFrom(mostSuperClass)) {
                mostSuperClass = currClass;
                mostSuperObject = currObject;
            } else if (mostSuperClass.isAssignableFrom(currClass))
                ;// do nothing
            else    // not on same inheritance branch so return null
                return null;
        }
        return mostSuperObject;
    }

    // object returned is an element from the array passed in and is an instance of the most super type
    // null returned if there is no object that can represent the most super ( i.e at least one object is not on the same inheritance branch as all the others)
    public static Object getMostSuperObjectIgnoreNullsAndStrings(Object[] objects) {
        if (objects.length == 0)
            return null;

        Class currClass;
        Object currObject;
        int i = 0;
        Object mostSuperObject = objects[i];
        while (mostSuperObject == null || mostSuperObject instanceof String)
            if (i >= objects.length - 1)
                return null;
            else
                mostSuperObject = objects[++i];

        Class mostSuperClass = mostSuperObject.getClass();

        for (int n = i + 1; n < objects.length; n++) {
            currObject = objects[n];
            if (currObject == null || currObject.equals(""))
                continue;
            currClass = currObject.getClass();
            // make sure they are on the same inheritance branch
            if (currClass.isAssignableFrom(mostSuperClass)) {
                mostSuperClass = currClass;
                mostSuperObject = currObject;
            } else if (mostSuperClass.isAssignableFrom(currClass))
                ;// do nothing
            else    // not on same inheritance branch so return null
                return null;
        }
        return mostSuperObject;
    }
}
