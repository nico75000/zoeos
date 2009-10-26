/*
 * ZoeosEvent.java
 *
 * Created on January 30, 2003, 10:39 PM
 */

package com.pcmsolutions.system;

/**
 *
 * @author  pmeehan
 */
public class ZoeosEvent extends java.util.EventObject {
    public ZoeosEvent(Object source) {
        super(source);
    }

    public String toString() {
        return "ZoeosEvent";
    }

    public void fire(ZoeosListener zl) {
    }
}
