package com.pcmsolutions.util;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 18-Apr-2003
 * Time: 18:21:11
 * To change this template use Options | File Templates.
 */
public class ScreenUtilities {

    // centres centeringRect about refRect
    public static Point centreRect(Rectangle refRect, Rectangle centeringRect) {
        Point c1 = refRect.getLocation();
        Point c2 = centeringRect.getLocation();
        Point outLoc;
        c1.translate((int) refRect.getWidth() / 2, (int) refRect.getHeight() / 2);
        c2.translate((int) centeringRect.getWidth() / 2, (int) centeringRect.getHeight() / 2);
        outLoc = new Point(centeringRect.getLocation());
        outLoc.translate((int) (c1.getX() - c2.getX()), (int) (c1.getY() - c2.getY()));
        return outLoc;
    }
}
