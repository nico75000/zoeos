package com.pcmsolutions.device.EMU.E4.gui.table;

import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 07-Jun-2003
 * Time: 04:29:14
 * To change this template use Options | File Templates.
 */
public class PartialLineBorder extends LineBorder {
    private boolean top;
    private boolean bottom;

    public PartialLineBorder(Color color, boolean top, boolean bottom) {
        super(color);
        this.top = top;
        this.bottom = bottom;
    }

    public PartialLineBorder(Color color, int thickness, boolean top, boolean bottom) {
        super(color, thickness);
        this.top = top;
        this.bottom = bottom;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Color oldColor = g.getColor();
        int i;

        /// PENDING(klobad) How/should do we support Roundtangles?
        g.setColor(lineColor);
        for (i = 0; i < thickness; i++) {
            if (top)
                g.drawLine(x + i, y + i, x + width - i - 1, y + i);
            if (bottom)
                g.drawLine(x + i, y + height - i - 1, x + width - i - 1, y + height - i - 1);
        }
        g.setColor(oldColor);
    }

}
