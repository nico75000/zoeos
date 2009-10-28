/*
 * ServiceProvider.java
 *
 * Created on January 20, 2003, 1:29 PM
 */

package com.pcmsolutions.system;

/**
 *
 * @author  pmeehan
 */
public interface ZServiceProvider {
    public boolean hasService(Class serviceClass);

    public java.util.Iterator getServiceSelectors(Class c);

    public Object getService(Object requester, Class serviceClass, Object serviceSelector);

    public Object getDefaultService(Object requester, Class serviceClass);

    public void releaseService(Object requester, Object service);
}
