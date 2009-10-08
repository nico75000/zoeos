package com.pcmsolutions.device.EMU.E4.gui.table;

import com.pcmsolutions.device.EMU.E4.gui.Hideable;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.gui.FadeLabel;
import com.pcmsolutions.gui.FocusAlerter;
import com.pcmsolutions.gui.FuzzyLineBorder;
import com.pcmsolutions.gui.smdi.FadeButton;
import com.pcmsolutions.system.ZDisposable;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;


public class RowHeaderedAndSectionedTablePanel extends JPanel implements ComponentListener, Hideable, ZDisposable {
    private JPanel mainBox;
    private JButton hideButton;
    private String showText;
    private boolean showSectionHeader;
    private RowHeaderedAndSectionedTable rowHeaderedTable;
    private RowHeaderedTableScrollPane rowHeaderedTableScrollPane;

    public RowHeaderedAndSectionedTablePanel init(RowHeaderedAndSectionedTable rhst, String showText, Color bdrColor, Action topLeftAction) {
        return init(rhst, showText, bdrColor, topLeftAction, true);
    }

    public Color getBackground() {
        return UIColors.getDefaultBG();
    }

    public Color getForeground() {
        return UIColors.getDefaultFG();
    }

    public RowHeaderedAndSectionedTablePanel init(RowHeaderedAndSectionedTable rhst, String showText, Color bdrColor, Action topLeftAction, boolean showSectionHeader) {
        this.rowHeaderedTable = rhst;
        this.showText = showText;
        this.showSectionHeader = showSectionHeader;
        this.setLayout(new BorderLayout());
        Box sectionBox = new Box(BoxLayout.X_AXIS);
        sectionBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
        this.setBackground(UIColors.getDefaultBG());
        if (bdrColor != null) {
            //Border b = UIColors.makeFuzzyBorder(bdrColor, borderWidth);
            String title = rhst.getTableTitle();
            if (title != null && !title.equals(""))
                this.setBorder(new TitledBorder(new FuzzyLineBorder(bdrColor, UIColors.getTableBorderWidth(), true), title, TitledBorder.LEFT, TitledBorder.ABOVE_TOP));
            else
                this.setBorder(new FuzzyLineBorder(bdrColor, UIColors.getTableBorderWidth(), true));
        }

        rhst.getTable().addComponentListener(this);

        //mainBox = new Box(BoxLayout.Y_AXIS);
        mainBox = new JPanel(new BorderLayout());
        mainBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        hideButton = new FadeButton(false, 0, 2.0F / 3) {
            public Color getForeground() {
                return Color.white;
            }

            public Color getBackground() {
                return UIColors.getTableFirstSectionBG();
            }
        };
        hideButton.setFocusable(false);
        hideButton.setToolTipText("Hide");

        // TODO !! decide on this
        // com.incors.plaf.alloy.AlloyCommonUtilities.set3DBackground(hideButton, UIColors.getUtilityButtonBG());
        //hideButton.setBackground(new Color(224, 203, 104));
        //hideButton.setBackground(UIColors.getUtilityButtonBG());

        JPanel hideButtonPanel = new JPanel(new BorderLayout());
        hideButtonPanel.setFocusable(false);
        if (showSectionHeader) {
            SectionData[] sd = rhst.getSectionData();
            FadeLabel j;
            Dimension d;
            //int usedSectionWidth = 0;
            for (int i = 0, n = sd.length; i < n; i++) {
                //usedSectionWidth += sd[i].sectionWidth;
                j = new FadeLabel(sd[i].sectionName, JLabel.CENTER);
                //j.setFadingIn(true);
                //j.setOpaque(true);
                j.setBackground(sd[i].sectionBG);
                j.setForeground(sd[i].sectionFG);
                d = new Dimension(sd[i].sectionWidth, (int) j.getPreferredSize().getHeight());
                j.setPreferredSize(d);
                j.setMinimumSize(d);
                j.setMaximumSize(d);
                sectionBox.add(j);
                if (sd[i].ml != null)
                    j.addMouseListener(sd[i].ml);

                // setup the hide button panel to have same background and height as first section
                if (i == 0) {
                    hideButtonPanel.setBackground(sd[0].sectionBG);
                    hideButtonPanel.add(hideButton, BorderLayout.CENTER);
                    hideButtonPanel.setPreferredSize(new Dimension((int) getPreferredSize().getWidth(), (int) j.getPreferredSize().getHeight()));
                }
            }
            sectionBox.add(hideButtonPanel, 0);
            mainBox.add(sectionBox, BorderLayout.NORTH);
        }

        JButton button = new FadeButton(true, 0, 2.0F / 3) {
            public Color getForeground() {
                return Color.white;
            }

            public Color getBackground() {
                return UIColors.getTableFirstSectionBG();
            }
        };

        if (topLeftAction != null) {
            button.setAction(topLeftAction);
            button.setFocusable(false);
            Object o = topLeftAction.getValue("tip");
            if (o != null)
                button.setToolTipText(o.toString());
        }

        // TODO !! decide on this
        //com.incors.plaf.alloy.AlloyCommonUtilities.set3DBackground(button, UIColors.getRefreshButtonBG());
        //button.setBackground(new Color(175, 200, 175));
        //button.setBackground(UIColors.getRefreshButtonBG());

        rowHeaderedTableScrollPane = new RowHeaderedTableScrollPane(rhst, button);
        rowHeaderedTableScrollPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
        rowHeaderedTableScrollPane.setFocusable(false);

        //hideButton.setBackground(Color.orange);
        //hideButton.setPreferredSize(new Dimension((int) rowHeaderedTableScrollPane.getPreferredSize().getWidth() - usedSectionWidth, (int) sectionBox.getComponent(0).getPreferredSize().getHeight()));
        //hideButton.setPreferredSize(new Dimension((int) rhst.getRowHeader().getWidth(), (int) sectionBox.getComponent(0).getPreferredSize().getHeight()));
        // create enclosing box for section label box and rowHeaderedTable scroll pane

        mainBox.add(rowHeaderedTableScrollPane, BorderLayout.CENTER);
        mainBox.setFocusable(false);

        // add main box to panel
        add(mainBox);

        // TODO!! might need to put this back in PM: 08/10/03
        // setMaximumSize(getPreferredSize());
        this.setFocusable(false);
        rhst.getTable().addFocusListener(FocusAlerter.getInstance());

        return this;
    }


    public RowHeaderedTableScrollPane getRowHeaderedTableScrollPane() {
        return rowHeaderedTableScrollPane;
    }

    public RowHeaderedTable getRowHeaderedTable() {
        return rowHeaderedTable;
    }

    public void componentResized(ComponentEvent e) {
        /*SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                revalidate();
            }
        });*/
        revalidate();
    }

    public void componentMoved(ComponentEvent e) {
        /*SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                revalidate();
            }
        });*/

        revalidate();
    }

    public void componentShown(ComponentEvent e) {
        /*SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                revalidate();
            }
        });*/

        revalidate();
    }

    public void componentHidden(ComponentEvent e) {
        /*SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                revalidate();
            }
        });*/

        revalidate();
    }

    public JButton getHideButton() {
        return hideButton;
    }

    public Component getComponent() {
        return this;
    }

    public String getShowButtonText() {
        return showText;
    }

    public void zDispose() {
        rowHeaderedTable.zDispose();
        mainBox = null;
        rowHeaderedTable = null;
    }
}

