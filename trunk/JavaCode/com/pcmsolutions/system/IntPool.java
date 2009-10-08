package com.pcmsolutions.system;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 27-Mar-2003
 * Time: 20:42:23
 * To change this template use Options | File Templates.
 */
public class IntPool {
    private final static Vector ints = new Vector();
    private final static Vector neg_ints = new Vector();
    public static final int ceiling = 10000;
    public static final int floor = -256;

    static {
        // this static initializer replaces commented code below
        ints.setSize(ceiling + 1);
        for (int i = 0; i <= ceiling; i++)
            ints.setElementAt(new Integer(i), i);

        neg_ints.setSize(-floor + 1);
        for (int i = 0; i >= floor; i--)
            neg_ints.setElementAt(new Integer(i), -i);
    }

    public static Integer get(int i) {
        if (i < floor || i > ceiling) {
            return new Integer(i);
        }

        Integer ci;
        if (i < 0) {
            int ai = -i;
            //if (ai >= neg_ints.size())
            //    neg_ints.setSize(ai + 1);

            ci = (Integer) neg_ints.elementAt(ai);

            //if (ci == null) {
            //    ci = new Integer(i);
            //    neg_ints.setElementAt(ci, ai);
            //}

        } else {
            //if (i >= ints.size())
            //    ints.setSize(i + 1);

            ci = (Integer) ints.elementAt(i);

            //if (ci == null) {
            //    ci = new Integer(i);
            //    ints.setElementAt(ci, i);
            //}
        }
        return ci;
    }
}
