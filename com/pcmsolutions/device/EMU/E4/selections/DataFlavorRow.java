package com.pcmsolutions.device.EMU.E4.selections;

import java.awt.datatransfer.DataFlavor;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 07-Aug-2003
 * Time: 14:56:14
 * To change this template use Options | File Templates.
 */
public class DataFlavorRow extends DataFlavor {
    public DataFlavorRow() {
    }

    public DataFlavorRow(String mimeType) throws ClassNotFoundException {
        super(mimeType);
    }

    public DataFlavorRow(String mimeType, String humanPresentableName) {
        super(mimeType, humanPresentableName);
    }

    public DataFlavorRow(String mimeType, String humanPresentableName, ClassLoader classLoader) throws ClassNotFoundException {
        super(mimeType, humanPresentableName, classLoader);
    }

    public DataFlavorRow(Class representationClass, String humanPresentableName) {
        super(representationClass, humanPresentableName);
    }
}
