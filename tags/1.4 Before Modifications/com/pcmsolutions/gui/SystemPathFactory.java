package com.pcmsolutions.gui;

import com.pcmsolutions.system.Impl_SystemEntryPoint;
import com.pcmsolutions.system.SystemEntryPoint;
import com.pcmsolutions.system.Zoeos;
import com.pcmsolutions.system.paths.LogicalPath;

/**
 * User: paulmeehan
 * Date: 15-Feb-2004
 * Time: 11:07:58
 */
public class SystemPathFactory {
    public static final SystemEntryPoint systemEntryPoint = new Impl_SystemEntryPoint(Zoeos.class, "ZoeOS");
    private static final String PATH_ELEMENT_ZOEOS = "ZoeOS";
    private static final String PATH_ELEMENT_PROPERTIES = "Properties";
    //private static final String PATH_ELEMENT_PIANO = "Piano";

    public static LogicalPath providePropertiesPath() {
        return new LogicalPath(systemEntryPoint, new Object[]{PATH_ELEMENT_ZOEOS, PATH_ELEMENT_PROPERTIES});
    }
    /*
    public static LogicalPath providePianoPath() {
        return new LogicalPath(systemEntryPoint, new Object[]{PATH_ELEMENT_ZOEOS, PATH_ELEMENT_PIANO});
    } */
}
