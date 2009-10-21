/*
 * @(#)FastGradientPaintDemo.java
 *
 * Copyright 2002 - 2004 JIDE Software Inc. All rights reserved.
 */

import com.jidesoft.combobox.ColorChooserPanel;
import com.jidesoft.combobox.ColorComboBox;
import com.jidesoft.converter.ObjectConverterManager;
import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.plaf.longhorn.LonghornWindowsUtils;
import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.swing.JideSwingUtilities;
import com.jidesoft.utils.SystemInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Demoed Component: {@link com.jidesoft.swing.JideSwingUtilities#fillGradient(java.awt.Graphics2D, java.awt.Shape, java.awt.Color, java.awt.Color, boolean)}
 * <br>
 * Required jar files: jide-common.jar, jide-components.jar, jide-grids.jar
 * <br>
 * Required L&F: Jide L&F extension required
 */
public class FastGradientPaintDemo extends JFrame {

    private static FastGradientPaintDemo _frame;

    public FastGradientPaintDemo(String title) throws HeadlessException {
        super(title);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            LookAndFeelFactory.installJideExtension();
            LonghornWindowsUtils.installAdditionalClassDefaults(UIManager.getDefaults());
            LonghornWindowsUtils.installAdditionalComponentDefaults(UIManager.getDefaults());
        }
        catch (ClassNotFoundException e) {
        }
        catch (InstantiationException e) {
        }
        catch (IllegalAccessException e) {
        }
        catch (UnsupportedLookAndFeelException e) {
        }

        _frame = new FastGradientPaintDemo("Demo of FastGradientPaint");
        _frame.setIconImage(JideIconsFactory.getImageIcon(JideIconsFactory.JIDE32).getImage());
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        _frame.getContentPane().setLayout(new BorderLayout());
        _frame.getContentPane().add(createSplitPane(), BorderLayout.CENTER);

        _frame.pack();

        _frame.setVisible(true);

    }

    private static JComponent createSplitPane() {
        JPanel pane = new JPanel();
        pane.setBackground(Color.WHITE);
        pane.setBorder(BorderFactory.createEmptyBorder(2, 10, 10, 10));
        pane.setLayout(new JideBoxLayout(pane, JideBoxLayout.PAGE_AXIS, 6));
        pane.add(createOptionPanel(_frame), JideBoxLayout.FIX);
        pane.add(new JLabel("<HTML><B>Moving mouse over the gradient paint above to see the actual Java code being used</B></HTML>", JLabel.CENTER), JideBoxLayout.FIX);
        PaintPanel panel1 = new PaintPanel("Fast Gradient Paint", 0);
        PaintPanel panel2 = new PaintPanel("Normal Gradient Paint", 1);
        PaintPanel panel3 = new PaintPanel("Single Color Paint (for comparison purpose)", 2);
        pane.add(panel1);
        pane.add(panel2);
        pane.add(panel3);
        pane.add(Box.createGlue(), JideBoxLayout.VARY);
        return pane;
    }

    static class PaintPanel extends CollapsiblePane {
        public PaintPanel(String title, int type) {
            super(title);
            setOpaque(false);
            getContentPane().setLayout(new BorderLayout(0, 6));
            GradientPanel gradientPanel = new GradientPanel(type);
            getContentPane().add(new ResultPanel(gradientPanel), BorderLayout.BEFORE_FIRST_LINE);
            getContentPane().add(gradientPanel, BorderLayout.CENTER);
            getContentPane().setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            getContentPane().setBackground(Color.WHITE);
        }
    }

    static class ResultPanel extends JPanel {
        protected JLabel _resultLabel;
        GradientPanel _gradientPanel;

        public ResultPanel(GradientPanel gradientPanel) {
            _gradientPanel = gradientPanel;
            setLayout(new BorderLayout());
            JButton button = new JButton("Repaint " + _repeat + " times");
            add(button, BorderLayout.AFTER_LINE_ENDS);
            button.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    NumberFormat format = NumberFormat.getInstance();
                    format.setMaximumFractionDigits(2);
// uncomment this if-else statement if you are using JDK1.5 so that you can leverage the new nanoTime function to get a more acurate result.
//                    if (SystemInfo.isJdk15Above()) {
//                        ArrayList results = new ArrayList(_repeat);
//                        double gap = 0;
//                        for (int i = 0; i < _repeat; i++) {
//                            long start = System.nanoTime();
//                            _gradientPanel.paintImmediately(0, 0, _gradientPanel.getWidth(), _gradientPanel.getHeight());
//                            long end = System.nanoTime();
//                            long stepGap = end - start;
//                            results.add(new Long(stepGap));
//                            gap += stepGap;
//                        }
//                        if (gap > 1000000) {
//                            gap /= 1000000.0;
//                            _resultLabel.setText("<HTML>Repainting " + _repeat + " times took <FONT COLOR=RED>" + format.format(gap) + "</FONT> millisecond</HTML>");
//                        }
//                        else if (gap > 1000) {
//                            gap /= 1000.0;
//                            _resultLabel.setText("<HTML>Repainting " + _repeat + " times took <FONT COLOR=RED>" + format.format(gap) + "</FONT> microsecond</HTML>");
//                        }
//                        else {
//                            _resultLabel.setText("<HTML>Repainting " + _repeat + " times took <FONT COLOR=RED>" + format.format(gap) + "</FONT> nanosecond</HTML>");
//                        }
//                    }
//                    else {
                        long gap = 0;
                        long start = System.currentTimeMillis();
                        for (int i = 0; i < _repeat; i++) {
                            _gradientPanel.paintImmediately(0, 0, _gradientPanel.getWidth(), _gradientPanel.getHeight());
                        }
                        long end = System.currentTimeMillis();
                        gap = end - start;
                        _resultLabel.setText("<HTML>Repainting " + _repeat + " times took <FONT COLOR=RED>" + format.format(gap) + "</FONT> millisecond</HTML>");
//                    }
                }
            });
            _resultLabel = new JLabel("");
            add(_resultLabel, BorderLayout.CENTER);
            setOpaque(false);
        }
    }

    static class GradientPanel extends JPanel {
        int type;

        public GradientPanel(int type) {
            this.type = type;
            if (type == 0) {
                setToolTipText("<HTML><B>Code used:</B> <BR>&nbsp;&nbsp;" +
                        "Rectangle rect = ...;<BR>&nbsp;&nbsp;" +
                        "JideSwingUtilities.fillGradient(g2d, rect, startColor, endColor, <FONT COLOR=BLUE>false</FONT>);" +
                        "</HTML>");
            }
            else if (type == 1) {
                setToolTipText("<HTML><B>Code used:</B> <BR>&nbsp;&nbsp;" +
                        "Rectangle rect = ...;<BR>&nbsp;&nbsp;" +
                        "GradientPaint paint = <FONT COLOR=BLUE>new</FONT> GradientPaint(rect.<FONT COLOR=PURPLE>x</FONT>, rect.<FONT COLOR=PURPLE>y</FONT>, startColor, rect.<FONT COLOR=PURPLE>width </FONT> + rect.<FONT COLOR=PURPLE>x</FONT>, rect.<FONT COLOR=PURPLE>y</FONT>, endColor);<BR>&nbsp;&nbsp;" +
                        "g2d.setPaint(paint);<BR>&nbsp;&nbsp;" +
                        "g2d.fill(rect);" +
                        "</HTML>");
            }
            else if (type == 2) {
                setToolTipText("<HTML><B>Code used:</B> <BR>&nbsp;&nbsp;" +
                        "Rectangle rect = ...;<BR>&nbsp;&nbsp;" +
                        "g.setColor(startColor);<BR>&nbsp;&nbsp;" +
                        "g.fill(rect.<FONT COLOR=PURPLE>x</FONT>, rect.<FONT COLOR=PURPLE>y</FONT>, rect.<FONT COLOR=PURPLE>width</FONT>, rect.<FONT COLOR=PURPLE>height</FONT>);" +
                        "</HTML>");
            }
        }

        protected void paintComponent(Graphics g) {
            Rectangle rect = new Rectangle(0, 0, getWidth(), getHeight());
            switch (type) {
                case 0:
                    JideSwingUtilities.fillGradient((Graphics2D) g, rect, _startColor, _endColor, _vertical);
                    break;
                case 1:
                    JideSwingUtilities.fillNormalGradient((Graphics2D) g, rect, _startColor, _endColor, _vertical);
                    break;
                case 2:
                    g.setColor(_startColor);
                    g.fillRect(rect.x, rect.y, rect.width, rect.height);
                    break;
            }
        }

        public Dimension getPreferredSize() {
            return new Dimension(500, 200);
        }

        public Point getToolTipLocation(MouseEvent event) {
            return new Point(4, 4);
        }
    }

    static Color _startColor = Color.BLUE;
    static Color _endColor = Color.YELLOW;
    static boolean _vertical = false;
    static int _repeat = 100;

    static JPanel createOptionPanel(final JFrame frame) {
        ObjectConverterManager.initDefaultConverter();
        JPanel panel = new JPanel();
        panel.add(new JLabel("Start Color: "));
        ColorComboBox startColorComboBox = new ColorComboBox(ColorChooserPanel.PALETTE_COLOR_40);
        startColorComboBox.setSelectedColor(_startColor);
        startColorComboBox.setAllowDefaultColor(false);
        startColorComboBox.setAllowMoreColors(false);
        startColorComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    _startColor = (Color) e.getItem();
                    if (_startColor == null) {
                        _startColor = Color.BLACK;
                    }
                    frame.repaint();
                }
            }
        });
        panel.add(startColorComboBox);
        panel.add(new JLabel("End Color: "));
        ColorComboBox endColorComboBox = new ColorComboBox(ColorChooserPanel.PALETTE_COLOR_40);
        endColorComboBox.setAllowDefaultColor(false);
        endColorComboBox.setSelectedColor(_endColor);
        endColorComboBox.setAllowMoreColors(false);
        endColorComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    _endColor = (Color) e.getItem();
                    if (_endColor == null) {
                        _endColor = Color.WHITE;
                    }
                    frame.repaint();
                }
            }
        });
        panel.add(endColorComboBox);

        JCheckBox checkbox = new JCheckBox("Vertical");
        checkbox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    _vertical = true;
                }
                else {
                    _vertical = false;
                }
                frame.repaint();
            }
        });
        checkbox.setOpaque(false);
        panel.add(checkbox);

        panel.setOpaque(false);
        return panel;
    }
}
