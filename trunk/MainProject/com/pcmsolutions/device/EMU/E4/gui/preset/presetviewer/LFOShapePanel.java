package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.gui.FuzzyLineBorder;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.geom.GeneralPath;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 27-Jul-2003
 * Time: 22:11:02
 * To change this template use Options | File Templates.
 */
public class LFOShapePanel extends JPanel {
    public final static int RANDOM = -1;
    public final static int TRIANGLE = 0;
    public final static int SINE = 1;
    public final static int SAWTOOTH = 2;
    public final static int SQUARE = 3;
    public final static int PULSE_33 = 4;
    public final static int PULSE_25 = 5;
    public final static int PULSE_16 = 6;
    public final static int PULSE_12 = 7;
    public final static int PAT_OCTAVES = 8;
    public final static int PAT_FIFTH_OCTAVE = 9;
    public final static int PAT_SUS4_TRIP = 10;
    public final static int PAT_NEENER = 11;
    public final static int PAT_SINE_12 = 12;
    public final static int PAT_SINE_135 = 13;
    public final static int PAT_SINE_NOISE = 14;
    public final static int PAT_HEMI_QUAVER = 15;


    protected int lineColorAlpha = 200;
    protected int lineThickness = 2;
    protected Stroke lineStroke = new BasicStroke(lineThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
    //protected Color lineColor = UIColors.getRlsColor();

    protected int mode = 0;
    protected boolean gradedBackground = true;

    protected float w;
    protected float h;
    protected int x;
    protected int y;
    protected Insets ins;

    protected static final float[] randomFactors;
    protected static final float[] noiseFactors;

    static {
        randomFactors = generateRandomFactors(200); // hopefully x extent of component will never be bigger than this
        noiseFactors = generateNoiseFactors(200); // hopefully x extent of component will never be bigger than this
    }

    public LFOShapePanel(String title) {
        //this.setBorder(new LineBorder(UIColors.getVoiceOverviewTableBorder(), 4, true));
        FuzzyLineBorder flb = new FuzzyLineBorder(UIColors.getTableBorder(), UIColors.getTableBorderWidth(), true, false);
        flb.setFadingIn(flb.isFadingIn());
        this.setBorder(new TitledBorder(flb, title, TitledBorder.LEFT, TitledBorder.ABOVE_TOP));
        //this.setBorder(new TitledBorder(UIColors.makeFuzzyBorder(UIColors.getTableBorder(), RowHeaderedAndSectionedTablePanel.getBorderWidth()), title, TitledBorder.LEFT, TitledBorder.ABOVE_TOP));

        Insets i = this.getInsets();
        setPreferredSize(new Dimension(i.left + i.right + 60, i.top + i.bottom + 60));
        setSize(getPreferredSize());
        this.setFocusable(false);
        //setOpaque(false);
    }

    public boolean isGradedBackground() {
        return gradedBackground;
    }

    public void setGradedBackground(boolean gradedBackground) {
        this.gradedBackground = gradedBackground;
    }

    public int getLineThickness() {
        return lineThickness;
    }

    public void setLineThickness(int lineThickness) {
        this.lineThickness = lineThickness;
        update();
    }

    public int getLineColorAlpha() {
        return lineColorAlpha;
    }

    public void setLineColorAlpha(int lineColorAlpha) {
        this.lineColorAlpha = lineColorAlpha;
        update();
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
        switch (mode) {
            case RANDOM:
                this.setToolTipText("Random");
                break;
            case TRIANGLE:
                this.setToolTipText("Triangle");
                break;
            case SINE:
                this.setToolTipText("Sine");
                break;
            case SAWTOOTH:
                this.setToolTipText("Sawtooth");
                break;
            case SQUARE:
                this.setToolTipText("Sqaure");
                break;
            case PULSE_33:
                this.setToolTipText("pat:Pulse33%");
                break;
            case PULSE_25:
                this.setToolTipText("pat:Pulse25%");
                break;
            case PULSE_16:
                this.setToolTipText("pat:Pulse16%");
                break;
            case PULSE_12:
                this.setToolTipText("pat:Pulse12%");
                break;
            case PAT_OCTAVES:
                this.setToolTipText("pat:Ocatves");
                break;
            case PAT_FIFTH_OCTAVE:
                this.setToolTipText("pat:FifthOctave");
                break;
            case PAT_SUS4_TRIP:
                this.setToolTipText("pat:Sus4Trip");
                break;
            case PAT_NEENER:
                this.setToolTipText("pat:Neener");
                break;
            case PAT_SINE_12:
                this.setToolTipText("Sine 1,2");
                break;
            case PAT_SINE_135:
                this.setToolTipText("Sine 1,3,5");
                break;
            case PAT_SINE_NOISE:
                this.setToolTipText("Sine Noise");
                break;
            case PAT_HEMI_QUAVER:
                this.setToolTipText("pat:HemiQuaver");
                break;
        }
        update();
    }

    protected void update() {
        revalidate();
        repaint();
    }

    protected void paintComponent(Graphics g) {
        //Thread.dumpStack();
        /* if (!AbstractRowHeaderedAndSectionedTable.repaint) {
             System.out.println("paintcomponent skipped on lfo");
             return;
         }*/
        Graphics2D g2d = ((Graphics2D) g);
        g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
        ins = getInsets();
        w = getWidth() - ins.right - ins.left - lineThickness * 2;
        h = getHeight() - ins.bottom - ins.top - lineThickness * 2;
        x = ins.left + lineThickness;
        y = ins.top + lineThickness;

        Stroke os = g2d.getStroke();

        super.paintComponent(g);
        if (gradedBackground) {
            GradientPaint gp;
            gp = new GradientPaint(0, 0, /*getBackground()*/Color.white, 0, (int) (getHeight() * 1.75), Color.lightGray, false);
            g2d.setPaint(gp);
            g2d.fillRect(ins.left - 1, ins.top - 1, getWidth() - ins.right - ins.left + 2, getHeight() - ins.top - ins.bottom + 2);
        }
        Shape s = null;
        switch (mode) {
            case RANDOM:
                s = drawRandom();
                break;
            case TRIANGLE:
                s = drawTriangle();
                break;
            case SINE:
                s = drawSine();
                break;
            case SAWTOOTH:
                s = drawSawtooth();
                break;
            case SQUARE:
                s = drawSquare();
                break;
            case PULSE_33:
                s = drawPulse33();
                break;
            case PULSE_25:
                s = drawPulse25();
                break;
            case PULSE_16:
                s = drawPulse16();
                break;
            case PULSE_12:
                s = drawPulse12();
                break;
            case PAT_OCTAVES:
                s = drawPatOctaves();
                break;
            case PAT_FIFTH_OCTAVE:
                s = drawPatFifthOctave();
                break;
            case PAT_SUS4_TRIP:
                s = drawPatSus4Trip();
                break;
            case PAT_NEENER:
                s = drawNeener();
                break;
            case PAT_SINE_12:
                s = drawSine12();
                break;
            case PAT_SINE_135:
                s = drawSine135();
                break;
            case PAT_SINE_NOISE:
                s = drawSineNoise();
                break;
            case PAT_HEMI_QUAVER:
                s = drawHemiQuaver();
                break;
        }

        if (s != null) {
            g2d.setColor(UIColors.applyAlpha(UIColors.getRlsColor(), lineColorAlpha));
            g2d.setStroke(lineStroke);
            g2d.draw(s);
        }

        g2d.setStroke(os);
    }

    protected static float[] generateRandomFactors(int num) {
        float[] facts = new float[num];
        for (int i = 0; i < num; i++) {
            if (i % 4 == 0)
                facts[i] = (float) ((Math.random() - 0.5) * 2);
            else
                facts[i] = 0;
            if (facts[i] > 1)
                facts[i] = 1;
            if (facts[i] < -1)
                facts[i] = -1;
        }
        return facts;
    }

    protected static float[] generateNoiseFactors(int num) {
        float[] facts = new float[num];
        for (int i = 0; i < num; i++)
            facts[i] = (float) Math.random();
        return facts;
    }

    protected Shape drawRandom() {
        GeneralPath p = new GeneralPath();
        p.moveTo(x, y + h / 2);

        //float fact;
        for (int i = 0; i < w; i++) {
            /*if (i % 4 == 0)
                fact = (float) ((Math.random() - 0.5) * 2);
            else
                fact = 0;
            if (fact > 1)
                fact = 1;
            if (fact < -1)
                fact = -1;
                */
            if (i > randomFactors.length)
                p.lineTo(x + i, y + h / 2 - (h / 2));
            else
                p.lineTo(x + i, y + h / 2 - (h / 2 * randomFactors[i]));
        }

        return p;
    }

    protected Shape drawTriangle() {
        GeneralPath p = new GeneralPath();
        p.moveTo(x, y + h / 2);
        p.lineTo(x + w * (float) 0.33333, y);
        p.lineTo(x + w * (float) 0.66666, y + h);
        p.lineTo(x + w, y + h / 2);
        return p;
    }

    protected Shape drawSine() {
        GeneralPath p = new GeneralPath();
        p.moveTo(x, y + h / 2);

        float cx;
        for (int i = 0; i < w; i++) {
            cx = i / w;
            float sinFact = (float) Math.sin(cx * 2 * Math.PI);
            p.lineTo(x + i, y + h / 2 - (h / 2 * sinFact));
        }

        return p;
    }

    protected Shape drawSawtooth() {
        GeneralPath p = new GeneralPath();
        p.moveTo(x, y + h / 2);
        p.lineTo(x + w / 2, y);
        p.lineTo(x + w / 2, y + h);
        p.lineTo(x + w, y + h / 2);
        return p;
    }

    protected Shape drawSquare() {
        return drawPulse(50);

    }

    protected Shape drawPulse33() {
        return drawPulse(33);
    }

    protected Shape drawPulse25() {
        return drawPulse(25);
    }

    protected Shape drawPulse16() {
        return drawPulse(16);

    }

    protected Shape drawPulse12() {
        return drawPulse(12);
    }

    protected Shape drawPatOctaves() {
        GeneralPath p = new GeneralPath();

        p.moveTo(x, y + h / 2);
        p.lineTo(x + w / 4, y + h / 2);
        p.lineTo(x + w / 4, y);
        p.lineTo(x + w / 2, y);
        p.lineTo(x + w / 2, y + h / 2);
        p.lineTo(x + w * 3 / 4, y + h / 2);
        p.lineTo(x + w * 3 / 4, y + h);
        p.lineTo(x + w, y + h);
        return p;
    }

    protected Shape drawPatFifthOctave() {
        GeneralPath p = new GeneralPath();

        p.moveTo(x, y + h / 2);
        p.lineTo(x + w / (float) 3.5, y + h / 2);
        p.lineTo(x + w / (float) 3.5, y);
        p.lineTo(x + w, y);
        return p;
    }

    protected Shape drawPatSus4Trip() {
        GeneralPath p = new GeneralPath();
        p.moveTo(x, y + h / 2);
        p.lineTo(x + w / 3, y + h / 2);
        p.lineTo(x + w / 3, y + h / 9);
        p.lineTo(x + w * 2 / 3, y + h / 9);
        p.lineTo(x + w * 2 / 3, y);
        p.lineTo(x + w, y);
        p.lineTo(x + w, y + h / 2);
        return p;
    }

    protected Shape drawNeener() {
        float y1 = y + h / 2;
        float y2 = y + h * 2 / 3;
        float y3 = y + h * 11 / 12;
        GeneralPath p = new GeneralPath();
        p.moveTo(x, y1);
        p.lineTo(x + w / 4, y1);
        p.lineTo(x + w / 4, y2);
        p.lineTo(x + w / 2, y2);
        p.lineTo(x + w / 2, y3);
        p.lineTo(x + w * 3 / 4, y3);
        p.lineTo(x + w * 3 / 4, y2);
        p.lineTo(x + w, y2);
        return p;
    }

    protected Shape drawSine12() {
        GeneralPath p = new GeneralPath();
        p.moveTo(x, y + h / 2);
        float cx;
        float[] xData = new float[(int) w];
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for (int i = 0; i < w; i++) {
            cx = i / w;
            xData[i] = (float) (0.5 + (2 / Math.PI) * (Math.sin(1 * cx * 2 * Math.PI) / 1 + (Math.sin(2 * cx * 2 * Math.PI) / 2)));
            if (xData[i] > max)
                max = xData[i];
            if (xData[i] < min)
                min = xData[i];
        }
        for (int i = 0; i < w; i++) {
            p.lineTo(x + i, y + h - (h * ((xData[i] - min) / (max - min))));
        }
        return p;
    }

    protected Shape drawSine135() {
        GeneralPath p = new GeneralPath();
        p.moveTo(x, y + h / 2);
        float cx;
        float[] xData = new float[(int) w];
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for (int i = 0; i < w; i++) {
            cx = i / w;
            xData[i] = (float) (0.5 + (2 / Math.PI) * (Math.sin(1 * cx * 2 * Math.PI) / 1 + (Math.sin(3 * cx * 2 * Math.PI) / 3 + (Math.sin(5 * cx * 2 * Math.PI) / 5))));
            if (xData[i] > max)
                max = xData[i];
            if (xData[i] < min)
                min = xData[i];
        }
        for (int i = 0; i < w; i++) {
            p.lineTo(x + i, y + h - (h * ((xData[i] - min) / (max - min))));
        }
        return p;
    }

    protected Shape drawSineNoise() {

        GeneralPath p = new GeneralPath();
        p.moveTo(x, y + h / 2);

        float cx;
        float sinFact;
        for (int i = 0; i < w; i++) {
            cx = i / w;
            if (i % 4 == 0)
             sinFact = (float) Math.sin(cx * 2 * Math.PI) + (float) ((Math.random() - 0.5) / 2);
            // sinFact = (float) Math.sin(cx * 2 * Math.PI) + (float) ((noiseFactors[i] - 0.5) / 2);
            else
                sinFact = (float) Math.sin(cx * 2 * Math.PI);
            if (sinFact > 1)
                sinFact = 1;
            if (sinFact < -1)
                sinFact = -1;
            p.lineTo(x + i, y + h / 2 - (h / 2 * sinFact));
        }

        return p;


        /*      GeneralPath p = new GeneralPath();
              p.moveTo(x, y + h / 2);

              float x;
              for (int i = 0; i < w; i++) {
                  x = i / w;
                  float sinFact = (float) Math.sin(x * 2 * Math.PI) + (float) (Math.random() / 10);
                  p.lineTo(x + i, y + h / 2 - (h / 2 * sinFact));
              }

              return p;*/
    }

    protected Shape drawHemiQuaver() {
        GeneralPath p = new GeneralPath();
        float cx = x,cy = (y + h / 2);
        p.moveTo(cx, cy);

        // 15
        double[] deltas = new double[]{0.25, 0.99, 0.01, 0.95, 0.01,
                                       0.99, 0.1, 0.01, 0.77, 0.55,
                                       0.01, 0.6, 0.99, 0.99, 0.4};
        int num = deltas.length;
        for (int i = 0, j = num; i < j; i++) {
            cx = x + (w * (i + 1)) / num;
            p.lineTo(cx, cy);
            cy = (float) (y + h * deltas[i]);
            p.lineTo(cx, cy);
        }
        return p;
    }

    protected Shape drawPulse(int percent) {
        GeneralPath p = new GeneralPath();
        p.moveTo(x, y);
        p.lineTo(x + w * percent / 100, y);
        p.lineTo(x + w * percent / 100, y + h);
        p.lineTo(x + w, y + h);
        return p;
    }

    public static void main(String args[]) {
        JFrame j = new JFrame();
        j.setSize(400, 400);
        LFOShapePanel lfo = new LFOShapePanel("Test");
        lfo.setBackground(Color.blue.brighter());
        j.getContentPane().add(lfo);
        j.show();
    }
}
