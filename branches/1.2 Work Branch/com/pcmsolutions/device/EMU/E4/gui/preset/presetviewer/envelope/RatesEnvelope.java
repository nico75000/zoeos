package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.envelope;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.system.ZDisposable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.beans.beancontext.BeanContextSupport;
import java.beans.PropertyChangeSupport;

public class RatesEnvelope extends JPanel implements RatesEnvelopeListener, ZDisposable {
    protected static int segmentColorAlpha = 230;

    protected static int segmentLineThickness = 2;
    protected static Color seperatorColor = UIColors.applyAlpha(Color.black, segmentColorAlpha);

    protected static final Color background2 = Color.white;
    protected static boolean gradedBackground = true;

    protected static Stroke segmentStroke = new BasicStroke(segmentLineThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
    protected static Stroke filledSegmentStroke = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
    protected static Stroke delimiterStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3, 3}, 0);

    //protected boolean zoomFixed = false;
    protected boolean fill = true;

    protected float usedSegTime;
    protected RatesEnvelopeModel model;

    public final static int MODE_FIXED = 0;
    public final static int MODE_FIXED_ZOOMED = 1;
    public final static int MODE_SCALED = 2;
    protected int mode = MODE_SCALED;

    protected float scaleFactor = (float) Math.pow(2, 16);

    protected static float MAX_SEG_TIME = 16384;

    protected final static int NUM_SEGMENTS = 6;

    public float getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
        this.update();
    }

    public RatesEnvelope(RatesEnvelopeModel model) {
        this.model = model;

        setOpaque(true);
        //setBackground(background1);
        //setPreferredSize(new Dimension(20, 20));
        updateUsedDuration();
        model.addRatesEnvelopeListener(this);
        this.setFocusable(false);
    }

    protected double log2(double a) {
        return (double) ((Math.log(a) / Math.log(2.0)));
    }

    public void envelopeChanged(RatesEnvelopeModel model) {
        update();
    }

    protected void updateUsedDuration() {
        if (mode == MODE_SCALED)
            usedSegTime = calcScaledDuration(model.getAtk1Rate()) + calcScaledDuration(model.getAtk2Rate()) + calcScaledDuration(model.getDec1Rate()) + calcScaledDuration(model.getDec2Rate()) + calcScaledDuration(model.getRls1Rate()) + calcScaledDuration(model.getRls2Rate());
        else
            usedSegTime = calcFixedDuration(model.getAtk1Rate()) + calcFixedDuration(model.getAtk2Rate()) + calcFixedDuration(model.getDec1Rate()) + calcFixedDuration(model.getDec2Rate()) + calcFixedDuration(model.getRls1Rate()) + calcFixedDuration(model.getRls2Rate());
    }

    protected void update() {
        updateUsedDuration();
        revalidate();
        repaint();
    }

    protected void paintComponent(Graphics g) {
        Insets ins = getInsets();
        float w = getWidth() - ins.right - ins.left;
        float h = getHeight() - ins.bottom - ins.top;
        int x = ins.left;
        int y = ins.top;

        Graphics2D g2 = (Graphics2D) g;
        super.paintComponent(g);
        if (gradedBackground) {
            GradientPaint gp;
            gp = new GradientPaint(0, 0, getBackground(), 0, getHeight() * 2, background2, false);
            g2.setPaint(gp);
            g2.fillRect(0 + x - 1, 0 + y - 1, (int) w + 2, (int) h + 2);
        }


        Line2D baseLine = new Line2D.Float(x, y + calcLevelY(h, model.getBaseLevel()), x + w, y + calcLevelY(h, model.getBaseLevel()));
        GeneralPath atkPath = new GeneralPath();
        GeneralPath decPath = new GeneralPath();
        GeneralPath rlsPath = new GeneralPath();

        float accumRate = 0;
        float currX, currY;

        float baseY = y + calcLevelY(h, model.getBaseLevel());
        float segmentBaseX;

        // stateStart point
        currX = x;
        currY = baseY;
        atkPath.moveTo(currX, currY);

        // atk1 point
        segmentBaseX = currX;
        accumRate = model.getAtk1Rate();
        currX += calcRateX(w, accumRate);
        currY = y + calcLevelY(h, model.getAtk1Level());
        atkPath.lineTo(currX, currY);

        // atk2 point
        accumRate = model.getAtk2Rate();
        currX += calcRateX(w, accumRate);
        currY = y + calcLevelY(h, model.getAtk2Level());
        // separator lineStroke
        Line2D atkDecSeperatorLine = new Line2D.Float(currX, y, currX, y + h);
        atkPath.lineTo(currX, currY);

        if (fill) {
            // return to baseline
            atkPath.lineTo(currX, baseY);
            // return to first x in segment
            atkPath.lineTo(segmentBaseX, baseY);
            atkPath.closePath();
        }

        // dec1 point
        segmentBaseX = currX;
        decPath.moveTo(currX, currY);
        accumRate = model.getDec1Rate();
        currX += calcRateX(w, accumRate);
        currY = y + calcLevelY(h, model.getDec1Level());
        decPath.lineTo(currX, currY);

        // dec2 point
        accumRate = model.getDec2Rate();
        currX += calcRateX(w, accumRate);
        currY = y + calcLevelY(h, model.getDec2Level());
        // separator lineStroke
        Line2D decRlsSeperatorLine = new Line2D.Float(currX, y, currX, y + h);
        decPath.lineTo(currX, currY);

        if (fill) {
            // return to baseline
            decPath.lineTo(currX, baseY);
            // return to first x in segment
            decPath.lineTo(segmentBaseX, baseY);
            decPath.closePath();
        }

        // rls1 point
        segmentBaseX = currX;
        rlsPath.moveTo(currX, currY);
        accumRate = model.getRls1Rate();
        currX += calcRateX(w, accumRate);
        currY = y + calcLevelY(h, model.getRls1Level());
        rlsPath.lineTo(currX, currY);

        // rls2 point
        accumRate = model.getRls2Rate();
        currX += calcRateX(w, accumRate);
        currY = y + calcLevelY(h, model.getRls2Level());
        rlsPath.lineTo(currX, currY);

        if (fill) {
            // return to baseline
            rlsPath.lineTo(currX, baseY);
            // return to first x in segment
            rlsPath.lineTo(segmentBaseX, baseY);
            rlsPath.closePath();
        }

        Stroke os = g2.getStroke();

        g2.setColor(seperatorColor);
        g2.setStroke(delimiterStroke);
        g2.draw(baseLine);
        g2.draw(atkDecSeperatorLine);
        g2.draw(decRlsSeperatorLine);

        if (fill) {
            g2.setStroke(segmentStroke);

            GradientPaint gp;
            gp = new GradientPaint(0, 0, getAtkColor(), 0, getHeight(), getBackground(), false);
            g2.setPaint(gp);

            //g2.setColor(atkColor);
            g2.fill(atkPath);

            gp = new GradientPaint(0, 0, getDecColor(), 0, getHeight(), getBackground(), false);
            g2.setPaint(gp);
            //g2.setColor(decColor);
            g2.fill(decPath);

            gp = new GradientPaint(0, 0, getRlsColor(), 0, getHeight(), getBackground(), false);
            g2.setPaint(gp);
            //g2.setColor(rlsColor);
            g2.fill(rlsPath);

            g2.setStroke(filledSegmentStroke);
        } else
            g2.setStroke(segmentStroke);

        g2.setColor(getAtkColor());
        g2.draw(atkPath);
        g2.setColor(getDecColor());
        g2.draw(decPath);
        g2.setColor(getRlsColor());
        g2.draw(rlsPath);

        g2.setStroke(os);
    }

    protected float calcLevelY(float h, float level) {
        return h * (model.getMaxLevel() - level) / (model.getMaxLevel() - model.getMinLevel());
    }

    // T = Tmax - Tmax * (log2(128-Rate)/(log2(128))
    // r 0..127
    protected float calcScaledDuration(float r) {
        if (r < 0 || r > model.getMaxRate())
            throw new IllegalArgumentException("Rate not in  allowed range");


        //  return (float) (/*MAX_SEG_TIME -*/ MAX_SEG_TIME * (  log2(8 - log2(128 - r)) / log2(8)));
        return (float) (MAX_SEG_TIME - MAX_SEG_TIME * (log2(128 - r) / log2(128)));

        /* double frac = log2(128 - r) / log2(128);
         double frac2 = (log2(1024 - r*8 - 8) +log2(8) )/ log2(1024);
          float x = MAX_SEG_TIME - (float) (MAX_SEG_TIME * frac);
         float x2 = MAX_SEG_TIME - (float) (MAX_SEG_TIME * frac2);
          // System.out.println("  Rate = " + r + "  Fraction = " + frac + "   Scaled to = " + x);
         //System.out.println("  Rate2 = " + r + "  Fraction2 = " + frac2 + "   Scaled2 to = " + x2);
          return x;*/
    }

    protected float calcFixedDuration(float r) {
        if (r < 0 || r > model.getMaxRate())
            throw new IllegalArgumentException("Rate not in  allowed range");

        return (MAX_SEG_TIME * r) / model.getMaxRate();
    }

    /*  protected double calcScaledDuration(double r) {
          if (r < 0 || r > model.getMaxRate())
              throw new IllegalArgumentException("Rate not in  allowed range");

          float maxRate = model.getMaxRate();
          float range = (maxRate * scaleFactor) + 1;
          double res = maxRate - maxRate * (log2(range - ((r * scaleFactor))) / log2(range));
          //System.out.println("SF =  " + scaleFactor + "  Rate " + r + " scaled to " + res);
          return res;
      }*/
    // T = Tmax - Tmax * (log2(128-Rate)/(log2(128))

    protected float calcRateX(float w, float rate) {
        switch (mode) {
            case MODE_FIXED:
                return (w * calcFixedDuration(rate)) / (MAX_SEG_TIME * NUM_SEGMENTS);
            case MODE_SCALED:
                return (w * (float) calcScaledDuration(rate)) / usedSegTime;
            case MODE_FIXED_ZOOMED:
                return (w * calcFixedDuration(rate)) / usedSegTime;
        }
        throw new IllegalArgumentException("Illegal mode");
    }

    public boolean isFill() {
        return fill;
    }

    public void setFill(boolean fill) {
        if (fill != this.fill) {
            //this.firePropertyChange("fill", this.fill, fill);
            this.fill = fill;
            revalidate();
            repaint();
        }
    }

    public boolean isGradedBackground() {
        return gradedBackground;
    }

    public void setGradedBackground(boolean gradedBackground) {
        this.gradedBackground = gradedBackground;
    }

    public Stroke getDelimiterStroke() {
        return delimiterStroke;
    }

    public void setDelimiterStroke(Stroke delimiterStroke) {
        this.delimiterStroke = delimiterStroke;
    }

    public int getSegmentColorAlpha() {
        return segmentColorAlpha;
    }

    public void setSegmentColorAlpha(int segmentColorAlpha) {
        this.segmentColorAlpha = segmentColorAlpha;
    }

    public int getSegmentLineThickness() {
        return segmentLineThickness;
    }

    public void setSegmentLineThickness(int segmentLineThickness) {
        this.segmentLineThickness = segmentLineThickness;
    }

    public Stroke getSegmentStroke() {
        return segmentStroke;
    }

    public void setSegmentStroke(Stroke segmentStroke) {
        this.segmentStroke = segmentStroke;
    }

    public Color getSeperatorColor() {
        return seperatorColor;
    }

    public Color getAtkColor() {
        return UIColors.applyAlpha(UIColors.getAtkColor(), segmentColorAlpha);
    }

    public Color getDecColor() {
        return UIColors.applyAlpha(UIColors.getDecColor(), segmentColorAlpha);
    }

    public Color getRlsColor() {
        return UIColors.applyAlpha(UIColors.getRlsColor(), segmentColorAlpha);
    }

    public Color getBackground() {
        return UIColors.getDefaultBG();
    }

    public Color getForeground() {
        return UIColors.getDefaultFG();
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        if (mode != this.mode) {
            this.mode = mode;
            revalidate();
            repaint();
        }
    }

    public void toggleMode() {
        if (this.mode == MODE_FIXED)
            setMode(MODE_SCALED);
        else
            setMode(MODE_FIXED);
    }

    public static void main(String args[]) {
        JFrame j = new JFrame();
        j.setSize(400, 400);
        RatesEnvelopeModel rem = new DefaultRatesEnvelopeModel();
        RatesEnvelope re = new RatesEnvelope(rem);

        rem.setAtk1Rate(40);
        rem.setAtk1Level(-50);
        rem.setAtk2Rate(40);
        rem.setAtk2Level(100);
        rem.setDec1Rate(40);
        rem.setDec1Level(-100);
        rem.setDec2Rate(127);
        rem.setDec2Level(100);
        rem.setRls1Rate(40);
        rem.setRls1Level(40);
        rem.setRls2Rate(40);
        rem.setRls2Level(40);

        j.getContentPane().add(re);
        j.show();
    }

    public void zDispose() {
        model.removeRatesEnvelopeListener(this);
        model = null;
    }
}
