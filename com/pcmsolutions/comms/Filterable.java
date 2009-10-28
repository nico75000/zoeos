/*
 * Filterable.java
 *
 * Created on December 16, 2002, 3:38 AM
 */

package com.pcmsolutions.comms;

/**
 *
 * @author  pmeehan
 */
public interface Filterable {
    public Object filter(ByteStreamable o);
}
