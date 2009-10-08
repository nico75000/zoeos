package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.Win;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.system.ZDisposable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

public class RangeWindow extends JPanel implements RangeWindowListener, ZDisposable, MouseListener, PreferenceChangeListener {
    protected int segmentColorAlpha = 120;
    protected int segmentLineThickness = 3;
    protected Color atkColor = UIColors.applyAlpha(Color.green, segmentColorAlpha);
    protected Color decColor = UIColors.applyAlpha(Color.orange, segmentColorAlpha);
    protected Color rlsColor = UIColors.applyAlpha(Color.red, segmentColorAlpha);
    protected Color seperatorColor = UIColors.applyAlpha(Color.black, segmentColorAlpha);

    protected Color background1 = Color.LIGHT_GRAY;
    protected Color background2 = Color.white;
    protected boolean gradedBackground = true;

    protected Stroke segmentStroke = new BasicStroke(segmentLineThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
    protected Stroke delimiterStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3, 3}, 0);

    //protected boolean zoomFixed = false;
    protected boolean fillSegments = false;

    protected float usedRateRange;
    protected RangeWindowModel model;

    protected Preferences prefs;
    protected static final String PREF_envelopeMode = "envelopeMode";
    protected static final String PREF_envelopeFill = "envelopeFill";

    public final static int MODE_FIXED = 0;
    public final static int MODE_FIXED_ZOOMED = 1;
    public final static int MODE_SCALED = 2;
    protected int mode = MODE_SCALED;

    protected float scaleFactor = (float) Math.pow(2, 16);

    protected final static int NUM_SEGMENTS = 6;

    public float getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
        this.update();
    }

    public RangeWindow(RangeWindowModel model, String prefNode) {
        this.model = model;
        this.prefs = Preferences.userNodeForPackage(this.getClass()).node(prefNode);

        prefs.addPreferenceChangeListener(this);

        mode = prefs.getInt(PREF_envelopeMode, mode);
        fillSegments = prefs.getBoolean(PREF_envelopeFill, fillSegments);

        setOpaque(true);
        setBackground(background1);
        //setPreferredSize(new Dimension(20, 20));
        updateUsedRateRange();
        model.addRangeWindowListener(this);
        addMouseListener(this);
    }

    protected double log2(double a) {
        return (Math.log(a) / Math.log(2.0));
    }


    public void rangeWindowChanged(RangeWindowModel model) {
        update();
    }

    protected void updateUsedRateRange() {
        if (mode == MODE_SCALED)
            usedRateRange = (float) (calcScaledRate(model.getAtk1Rate()) + calcScaledRate(model.getAtk2Rate()) + calcScaledRate(model.getDec1Rate()) + calcScaledRate(model.getDec2Rate()) + calcScaledRate(model.getRls1Rate()) + calcScaledRate(model.getRls2Rate()));
        else
            usedRateRange = model.getAtk1Rate() + model.getAtk2Rate() + model.getDec1Rate() + model.getDec2Rate() + model.getRls1Rate() + model.getRls2Rate();

    }

    protected void update() {
        updateUsedRateRange();
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
        if (gradedBackground) {
            GradientPaint gp;
            gp = new GradientPaint(0, 0, background1, 0, getHeight() * 2, background2, false);
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        } else
            super.paintComponent(g);

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

        if (fillSegments) {
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

        if (fillSegments) {
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

        if (fillSegments) {
            // return to baseline
            rlsPath.lineTo(currX, baseY);
            // return to first x in segment
            rlsPath.lineTo(segmentBaseX, baseY);
            rlsPath.closePath();
        }

        g2.setColor(seperatorColor);
        g2.setStroke(delimiterStroke);
        g2.draw(baseLine);
        g2.draw(atkDecSeperatorLine);
        g2.draw(decRlsSeperatorLine);

        if (fillSegments) {
            g2.setStroke(segmentStroke);
            g2.setColor(atkColor);
            g2.fill(atkPath);
            g2.setColor(decColor);
            g2.fill(decPath);
            g2.setColor(rlsColor);
            g2.fill(rlsPath);
        } else {
            g2.setStroke(segmentStroke);
            g2.setColor(atkColor);
            g2.draw(atkPath);
            g2.setColor(decColor);
            g2.draw(decPath);
            g2.setColor(rlsColor);
            g2.draw(rlsPath);
        }
    }

    protected float calcLevelY(float h, float level) {
        return h * (model.getMaxLevel() - level) / (model.getMaxLevel() - model.getMinLevel());
    }

    // T = Tmax - Tmax * (log2(128-Rate)/(log2(128))
    // r 0..127
    /*protected double calcScaledDuration(double tMax, double r) {
        if (r < 0 || r > model.getMaxRate())
            throw new IllegalArgumentException("Rate not in  allowed range");

        return tMax - tMax * (log2(128 - r) / log2(128));
    } */
    protected double calcScaledRate(double r) {
        if (r < 0 || r > model.getMaxRate())
            throw new IllegalArgumentException("Rate not in  allowed range");

        float maxRate = model.getMaxRate();
        float range = (maxRate * scaleFactor) + 1;
        double res = maxRate - maxRate * (log2(range - ((r * scaleFactor))) / log2(range));
        //System.out.println("SF =  " + scaleFactor + "  Rate " + r + " scaled to " + res);
        return res;
    }

    protected float calcRateX(float w, float rate) {
        switch (mode) {
            case MODE_FIXED:
                return (w * rate) / (model.getMaxRate() * NUM_SEGMENTS);
            case MODE_SCALED:
                return (w * (float) calcScaledRate(rate)) / usedRateRange;
            case MODE_FIXED_ZOOMED:
                return (w * rate) / usedRateRange;
        }
        throw new IllegalArgumentException("Illegal mode");
    }

    public Color getBackground1() {
        return background1;
    }

    public void setBackground1(Color background1) {
        this.background1 = background1;
    }

    public Color getBackground2() {
        return background2;
    }

    public void setBackground2(Color background2) {
        this.background2 = background2;
    }

    public boolean isFillSegments() {
        return fillSegments;
    }

    public void setFillSegments(boolean fillSegments) {
        prefs.putBoolean(PREF_envelopeFill, fillSegments);
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

    public void setSeperatorColor(Color seperatorColor) {
        this.seperatorColor = seperatorColor;
    }

    public Color getAtkColor() {
        return atkColor;
    }

    public void setAtkColor(Color atkColor) {
        this.atkColor = atkColor;
    }

    public Color getDecColor() {
        return decColor;
    }

    public void setDecColor(Color decColor) {
        this.decColor = decColor;
    }

    public Color getRlsColor() {
        return rlsColor;
    }

    public void setRlsColor(Color rlsColor) {
        this.rlsColor = rlsColor;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        prefs.putInt(PREF_envelopeMode, mode);
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
        RangeWindowModel rem = new DefaultRangeWindowModel();
        RangeWindow re = new RangeWindow(rem, "test");

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
        model.removeRangeWindowListener(this);
        prefs.removePreferenceChangeListener(this);
        model = null;
    }

    public void mouseClicked(MouseEvent e) {
        checkPopup(e);
    }

    public void mousePressed(MouseEvent e) {
        checkPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        checkPopup(e);
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    protected boolean checkPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            JPopupMenu p = new JPopupMenu();
            boolean addFixed = false, addZoomFixed = false, addScaled = false;
            switch (mode) {
                case MODE_FIXED:
                    addFixed = false;
                    addZoomFixed = true;
                    addScaled = true;
                    break;
                case MODE_SCALED:
                    addFixed = true;
                    addScaled = false;
                    addZoomFixed = true;
                    break;
                case MODE_FIXED_ZOOMED:
                    addFixed = true;
                    addScaled = true;
                    addZoomFixed = false;
                    break;
                default:
            }

            if (addScaled)
                p.add(new AbstractAction("Scale") {
                    public void actionPerformed(ActionEvent e) {
                        setMode(MODE_SCALED);
                        //updateTitleData();
                    }
                });
            if (addFixed)
                p.add(new AbstractAction("Fixed") {
                    public void actionPerformed(ActionEvent e) {
                        setMode(MODE_FIXED);
                        //updateTitleData();
                    }
                });
            /*if (addZoomFixed)
                p.addDesktopElement(new AbstractAction("Fixed ( Zoomed)") {
                    public void actionPerformed(ActionEvent e) {
                        setMode(MODE_FIXED_ZOOMED);
                        //updateTitleData();
                    }
                });*/
            /*for (int i = 1; i <= 32; i++) {
                final int j = i;
                p.addDesktopElement(new AbstractAction(String.valueOf(i)) {
                    public void actionPerformed(ActionEvent e) {
                        setScaleFactor((float) Math.pow(2, j));
                        updateTitleData();
                    }
                });
            } */

            p.add(new AbstractAction("Toggle Fill") {
                public void actionPerformed(ActionEvent e) {
                    setFillSegments(!isFillSegments());
                }
            });
            p.show(this, e.getX(), e.getY());
        }

        return true;
    }

    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().equals(PREF_envelopeMode)) {
            mode = prefs.getInt(PREF_envelopeMode, mode);
            update();
        } else if (evt.getKey().equals(PREF_envelopeFill)) {
            fillSegments = prefs.getBoolean(PREF_envelopeFill, fillSegments);
            update();
        }
    }
}
