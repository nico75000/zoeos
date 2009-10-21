/*
 * @(#)SampleIconsFactory.java
 *
 * Copyright 2002-2003 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.icons.IconsFactory;

import javax.swing.*;

/**
 * A helper class to contain icons for demo of JIDE products.
 */
public class IntelliJIconsFactory {

    public static class ProjectOptions {
        public final static String PATHS = "idea/general/configurableProjectPaths.png";
        public final static String COMPILER = "idea/general/configurableCompiler.png";
        public final static String RUNDEBUG = "idea/general/configurableRunDebug.png";
        public final static String DEBUGGER = "idea/general/configurableDebugger.png";
        public final static String LOCALVCS = "idea/general/configurableLocalVCS.png";
        public final static String VCSSUPPORT = "idea/general/configurableVcs.png";
        public final static String WEB = "idea/general/configurableWeb.png";
        public final static String EJB = "idea/general/configurableEjb.png";
        public final static String JAVADOC = "idea/general/configurableJavadoc.png";
        public final static String MISCELLANEOUS = "idea/general/configurableMisc.png";
    }

    public static ImageIcon getImageIcon(String name) {
        if (name != null)
            return IconsFactory.getImageIcon(IntelliJIconsFactory.class, name);
        else
            return null;
    }

    public static void main(String[] argv) {
        IconsFactory.generateHTML(IntelliJIconsFactory.class);
    }


}
