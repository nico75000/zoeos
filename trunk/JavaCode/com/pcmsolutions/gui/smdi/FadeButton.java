package com.pcmsolutions.gui.smdi;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: paulmeehan
 * Date: 15-Nov-2003
 * Time: 02:58:33
 * To change this template use Options | File Templates.
 */
public class FadeButton extends JButton {
    protected boolean foregroundPolarity = false;
    protected float xFact;
    protected float yFact;
    protected int xArc = 3;
    protected int yArc = 3;
    protected boolean cyclic = true;
    protected float xInsetFact = 0.15F;
    protected float yInsetFact = 0.15F;

    public FadeButton(boolean polarity, float xDiv, float yDiv) {
        this.foregroundPolarity = polarity;
        this.xFact = xDiv;
        this.yFact = yDiv;
        this.setBorder(null);
    }

    public FadeButton(Action a, boolean polarity, float xDiv, float yDiv) {
        this(polarity, xDiv, yDiv);
        setAction(a);
    }

    public boolean isForegroundPolarity() {
        return foregroundPolarity;
    }

    public void setForegroundPolarity(boolean foregroundPolarity) {
        this.foregroundPolarity = foregroundPolarity;
    }

    protected void paintComponent(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.white);
        g2.fillRect(0,0,w,h);
        for (int i = 0; i < h; i++) {
            g2.setColor(UIColors.applyAlpha(getBackground(), UIColors.getFuzzyAlpha(i, h, foregroundPolarity)));
            g2.drawLine(0, i, w, i);
        }

        GradientPaint gp;
        gp = new GradientPaint(0, 0, (foregroundPolarity ? getForeground() : getBackground()), w * xFact, h * yFact, (foregroundPolarity ? getBackground() : getForeground()), cyclic);
        g2.setPaint(gp);
        g2.fillRoundRect((int) (xInsetFact * w), (int) (yInsetFact * h), (int) (w - xInsetFact * w * 2), (int) (h - yInsetFact * h * 2), xArc, yArc);
        //g2.fillRect((int) (xInsetFact * w), (int) (yInsetFact * h), (int) (w - xInsetFact * w * 2), (int) (h - yInsetFact * h * 2));
    }
}
