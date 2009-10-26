package com.pcmsolutions.device.EMU.E4.gui.packaging;

import com.pcmsolutions.device.EMU.E4.packaging.PackageHeader;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.Zoeos;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 03-Jan-2004
 * Time: 08:44:30
 * To change this template use Options | File Templates.
 */
public class PackageHeaderInfoPane extends JTextPane {
    protected PackageHeader header;

    public PackageHeaderInfoPane(PackageHeader hdr) {
        setHeader(hdr);
        setEditable(false);
        setFont(new Font("MONOSPACED", Font.PLAIN, 11));
        setPreferredSize(new Dimension(300, 250));
    }

    public PackageHeader getHeader() {
        return header;
    }

    public void setHeader(PackageHeader header) {
        this.header = header;
        generateText();
    }

    protected static final int DESC_FIELD_LEN = 18;

    protected void generateText() {
        if (header == null) {
            setText("");
            return;
        }
        StringBuffer buf = new StringBuffer();
        buf.append(ZUtilities.makeExactLengthString("Name:", DESC_FIELD_LEN) + header.getName());
        buf.append(Zoeos.lineSeperator);

        buf.append(ZUtilities.makeExactLengthString("Notes:", DESC_FIELD_LEN) + header.getNotes());
        buf.append(Zoeos.lineSeperator);

        buf.append(ZUtilities.makeExactLengthString("Creation date:", DESC_FIELD_LEN) + header.getCreationDate());
        buf.append(Zoeos.lineSeperator);

        buf.append(ZUtilities.makeExactLengthString("Device name:", DESC_FIELD_LEN) + header.getDeviceName());
         buf.append(Zoeos.lineSeperator);

        buf.append(ZUtilities.makeExactLengthString("Device version:", DESC_FIELD_LEN) + header.getDeviceVersion());
        buf.append(Zoeos.lineSeperator);

        buf.append(ZUtilities.makeExactLengthString("ZoeOS version:", DESC_FIELD_LEN) + header.getZoeosVersion());
        buf.append(Zoeos.lineSeperator);

        setText(buf.toString());
    }
}
