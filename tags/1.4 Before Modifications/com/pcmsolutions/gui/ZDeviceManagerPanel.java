package com.pcmsolutions.gui;

import com.pcmsolutions.system.*;
import com.pcmsolutions.system.callback.Callback;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;
import com.pcmsolutions.system.tasking.TicketRunnable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 28-Mar-2003
 * Time: 22:26:01
 * To change this template use Options | File Templates.
 */
public class ZDeviceManagerPanel extends JPanel implements ZDeviceManagerListener, ChangeListener {
    protected ZDeviceManager zdm;
    private Window parent;

    protected UnidentifiedMessagePanel unidentifiedPanel;
    protected DuplicateDevicePanel dupPanel;
    protected PendingDevicePanel pendPanel;
    protected RunningDevicePanel runPanel;
    protected StoppedDevicePanel stopPanel;
    protected Box buttBox;

    protected JCheckBox startBarr;
    protected JCheckBox autoHuntAtStartup;
    protected JCheckBox serializeDeviceMarshalling;

    public ZDeviceManagerPanel(Window parent) {
        super(new FlowLayout());
        this.parent = parent;
        //setOpaque(false);
        zdm = Zoeos.getInstance().getDeviceManager();

        ToolTipManager.sharedInstance().registerComponent(this);
        this.setAlignmentY(Component.TOP_ALIGNMENT);

        unidentifiedPanel = new UnidentifiedMessagePanel();
        unidentifiedPanel.setAlignmentY(Component.TOP_ALIGNMENT);

        dupPanel = new DuplicateDevicePanel();
        dupPanel.setAlignmentY(Component.TOP_ALIGNMENT);

        pendPanel = new PendingDevicePanel();
        pendPanel.setAlignmentY(Component.TOP_ALIGNMENT);

        runPanel = new RunningDevicePanel();
        runPanel.setAlignmentY(Component.TOP_ALIGNMENT);

        stopPanel = new StoppedDevicePanel();
        stopPanel.setAlignmentY(Component.TOP_ALIGNMENT);

        buttBox = new Box(BoxLayout.Y_AXIS);
        final JButton h = new JButton();
        h.setAction(new AbstractAction("Hunt") {
            public void actionPerformed(ActionEvent e) {
                h.setEnabled(false);
                try {
                    Zoeos.getInstance().getSystemQ().getPostableTicket(new TicketRunnable() {
                        public void run() throws Exception {
                            try {
                                zdm.performHunt();
                            } finally {
                            }
                        }
                    }, "performHunt").post(new Callback() {
                        public void result(Exception e, boolean wasCancelled) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    h.setEnabled(true);
                                }
                            });
                        }
                    });
                } catch (ResourceUnavailableException e1) {
                    e1.printStackTrace();
                    h.setEnabled(true);
                }
            }
        });
        h.setToolTipText("Hunt For Devices");
        buttBox.add(h);

        JButton r = new JButton(new AbstractAction("Refresh") {
            public void actionPerformed(ActionEvent e) {
                pendingListChanged();
                startedListChanged();
                stoppedListChanged();
                duplicateListChanged();
                unidentifiedListChanged();
            }
        });
        r.setToolTipText("Refresh Device Lists");
        buttBox.add(r);

        autoHuntAtStartup = new JCheckBox(new AbstractAction("Auto Hunt at Startup") {
            public void actionPerformed(ActionEvent e) {
                ZoeosPreferences.ZPREF_autoHuntAtStartup.putValue(autoHuntAtStartup.isSelected());
            }
        });
        buttBox.add(autoHuntAtStartup);

        serializeDeviceMarshalling = new JCheckBox(new AbstractAction("Serialize Device Marshalling") {
            public void actionPerformed(ActionEvent e) {
                ZoeosPreferences.ZPREF_serializeDeviceMarshalling.putValue(serializeDeviceMarshalling.isSelected());
            }
        });
        buttBox.add(serializeDeviceMarshalling);

        startBarr = new JCheckBox(new AbstractAction("Stop Device Marshalling at Pending") {
            public void actionPerformed(ActionEvent e) {
                //zdm.setStartBarrier(startBarr.isSelected());
                ZoeosPreferences.ZPREF_stopHuntAtPending.putValue(startBarr.isSelected());
            }
        });
        buttBox.add(startBarr);

        add(buttBox);

        add(unidentifiedPanel);

        add(dupPanel);

        add(pendPanel);

        add(runPanel);

        add(stopPanel);
        zdm.addDeviceManagerListener(this);
        refreshPrefs();
        ZoeosPreferences.addGlobalChangeListener(this);
    }

    void refreshPrefs() {
        startBarr.setSelected(ZoeosPreferences.ZPREF_stopHuntAtPending.getValue());
        autoHuntAtStartup.setSelected(ZoeosPreferences.ZPREF_autoHuntAtStartup.getValue());
        serializeDeviceMarshalling.setSelected(ZoeosPreferences.ZPREF_serializeDeviceMarshalling.getValue());
    }

    public void pendingListChanged() {
        pendPanel.refreshList();
    }

    public void startedListChanged() {
        runPanel.refreshList();
    }

    public void stoppedListChanged() {
        stopPanel.refreshList();
    }

    public void unidentifiedListChanged() {
        unidentifiedPanel.refreshList();
    }

    public void duplicateListChanged() {
        dupPanel.refreshList();
    }

    public void stateChanged(ChangeEvent e) {
        refreshPrefs();
    }

    private abstract class AbstractZDeviceStatePanel extends JPanel implements ListSelectionListener, MouseListener, ListCellRenderer {
        protected JScrollPane sp;
        protected JList list;
        protected JButton button1;
        protected JButton button2;
        protected JLabel label;
        protected Color listColor = Color.white;
        private JLabel listLabel;

        public AbstractZDeviceStatePanel(String title) {
            super();
            //setOpaque(false);
            setPreferredSize(new Dimension(150, 150));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            // setup list
            listLabel = new JLabel();
            listLabel.setOpaque(true);
            list = new JList() {
                public String getToolTipText() {
                    ZExternalDevice d = (ZExternalDevice) list.getSelectedValue();
                    if (d != null)
                        return d.getDeviceConfigReport();
                    return "No Device Selected";
                }

                /*  protected void paintComponent(Graphics g) {
                      int w = getWidth();
                      int h = getHeight();
                      Graphics2D g2d = ((Graphics2D) g);
                      GradientPaint gp = new GradientPaint((int) (w * 0.75), 0, Color.white, w, 0, listColor, false);
                      g2d.setPaint(gp);
                      g2d.fillRect(0, 0, w, h);
                      g2d.setColor(Zoeos.logoColor);
                      g2d.setFont(Zoeos.logoSmallFont);
                      g2d.drawString(Zoeos.logoStr, 0, h / 2);
                      super.paintComponent(g);

                  }
                  */
            };
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.addListSelectionListener(this);
            //list.setOpaque(false);
            list.addMouseListener(this);
            list.setCellRenderer(this);

            // setup scroll pane
            sp = new JScrollPane();
            sp.setViewportView(list);
            sp.setAlignmentX(Component.CENTER_ALIGNMENT);
            //sp.setOpaque(false);

            // label it
            label = new JLabel(title);
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            //label.setOpaque(false);

            this.add(label);
            this.add(sp);
        }

        public void setActions(Action a, Action b) {
            Box buttBox = Box.createVerticalBox();
            buttBox.setAlignmentX(Component.CENTER_ALIGNMENT);
            if (a != null) {
                button1 = new JButton();
                button1.setAlignmentX(Component.LEFT_ALIGNMENT);
                button1.setAction(a);
                buttBox.add(button1);
            }
            if (b != null) {
                button2 = new JButton();
                button2.setAlignmentX(Component.LEFT_ALIGNMENT);
                button2.setAction(b);
                buttBox.add(button2);
            }
            add(buttBox);
        }

        // should only be called on AWT Event thread
        public abstract void refreshList();

        public void mouseDragged(java.awt.event.MouseEvent mouseEvent) {
        }

        public void mouseClicked(java.awt.event.MouseEvent e) {
            checkPopup(e);
        }

        public void mouseEntered(java.awt.event.MouseEvent e) {
        }

        public void mouseExited(java.awt.event.MouseEvent e) {
        }

        public void mousePressed(java.awt.event.MouseEvent e) {
            checkPopup(e);
        }

        public void mouseReleased(java.awt.event.MouseEvent e) {
            checkPopup(e);
        }

        public boolean checkPopup(final MouseEvent e) {
            if (e.isPopupTrigger()) {
                Object sel = list.getSelectedValue();

                if (sel != null) {
                    final Object userObjects[] = new Object[1];
                    userObjects[0] = sel;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            ZCommandFactory.showPopup("ZDevice >", AbstractZDeviceStatePanel.this, userObjects, e);
                        }
                    });
                    return true;
                }
            }
            return false;
        }

        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            //listLabel.setBackground(listColor);
            if (isSelected) {
                listLabel.setBackground(listColor);
            } else {
                listLabel.setBackground(Color.white);
            }

            listLabel.setText(value.toString());
            return listLabel;
        }
    }

    private class UnidentifiedMessagePanel extends AbstractZDeviceStatePanel {
        public UnidentifiedMessagePanel() {
            super("Unidentified Messages");
            Action clear = new AbstractAction("Clear") {
                public void actionPerformed(ActionEvent e) {
                    zdm.clearUnidentified();
                }
            };
            setActions(clear, null);
            listColor = Color.LIGHT_GRAY;
            refreshList();
        }

        // should only be called on AWT Event thread
        public void refreshList() {
            List l = zdm.getUnidentifiedList();
            list.setListData(l.toArray());
            boolean v = this.isVisible();
            if (l.size() > 0)
                list.setSelectedIndex(0);
        }

        public void valueChanged(ListSelectionEvent e) {
        }
    }

    private class DuplicateDevicePanel extends AbstractZDeviceStatePanel {
        public DuplicateDevicePanel() {
            super("Duplicates");
            Action clear = new AbstractAction("Clear") {
                public void actionPerformed(ActionEvent e) {
                    zdm.clearDuplicates();
                }
            };
            listColor = Color.LIGHT_GRAY;
            setActions(clear, null);
            refreshList();
        }

        // should only be called on AWT Event thread
        public void refreshList() {
            Map m = zdm.getDuplicateMap();
            list.setListData(m.keySet().toArray());
            boolean v = this.isVisible();
            if (m.size() > 0) {
                list.setSelectedIndex(0);
                this.setVisible(true);
                if (!v)
                    parent.pack();
            } else {
                this.setVisible(false);
                if (v)
                    parent.pack();
            }
        }

        public void valueChanged(ListSelectionEvent e) {
        }

    }

    private class PendingDevicePanel extends AbstractZDeviceStatePanel {
        public PendingDevicePanel() {
            super("Pending State");
            Action init = new AbstractAction("Start >>") {
                public void actionPerformed(ActionEvent e) {
                    ZExternalDevice d = (ZExternalDevice) list.getSelectedValue();
                    if (d != null)
                        try {
                            zdm.startDevice(d).post();
                        } catch (ResourceUnavailableException e1) {
                            e1.printStackTrace();
                        }
                }
            };
            Action remove = new AbstractAction("Remove") {
                public void actionPerformed(ActionEvent e) {
                    ZExternalDevice d = (ZExternalDevice) list.getSelectedValue();
                    if (d != null)
                        try {
                            zdm.removeDevice(d, false).post();
                        } catch (ResourceUnavailableException e1) {
                            e1.printStackTrace();
                        }
                }
            };
            setActions(init, remove);
            listColor = new Color(255, 222, 118); // very light orange
            refreshList();
        }

        // should only be called on AWT Event thread
        public void refreshList() {
            List l = zdm.getPendingList();
            list.setListData(l.toArray());
            boolean v = this.isVisible();
            if (l.size() > 0) {
                list.setSelectedIndex(0);
                this.setVisible(true);
                if (!v)
                    parent.pack();
            } else {
                this.setVisible(false);
                if (v)
                    parent.pack();
            }
        }

        public void valueChanged(ListSelectionEvent e) {
        }
    }

    private class RunningDevicePanel extends AbstractZDeviceStatePanel {
        public RunningDevicePanel() {
            super("Running State");
            Action stop = new AbstractAction("Stop >>") {
                public void actionPerformed(ActionEvent e) {
                    ZExternalDevice d = (ZExternalDevice) list.getSelectedValue();
                    if (d != null)
                        try {
                            zdm.stopDevice(d, "User intervention").post();
                        } catch (ResourceUnavailableException e1) {
                            e1.printStackTrace();
                        }
                }
            };
            setActions(stop, null);
            listColor = new Color(200, 238, 205);  // very light green
            refreshList();
        }

        // should only be called on AWT Event thread
        public void refreshList() {
            List l = zdm.getRunningList();
            list.setListData(l.toArray());
            boolean v = this.isVisible();
            if (l.size() > 0)
                list.setSelectedIndex(0);
        }

        public void valueChanged(ListSelectionEvent e) {
        }
    }

    private class StoppedDevicePanel extends AbstractZDeviceStatePanel {
        public StoppedDevicePanel() {
            super("Stopped State");
            Action start = new AbstractAction("<< Start") {
                public void actionPerformed(ActionEvent e) {
                    ZExternalDevice d = (ZExternalDevice) list.getSelectedValue();
                    if (d != null)
                        try {
                            zdm.startDevice(d).post();
                        } catch (ResourceUnavailableException e1) {
                            e1.printStackTrace();
                        }
                }
            };
            Action remove = new AbstractAction("Remove >>") {
                public void actionPerformed(ActionEvent e) {
                    ZExternalDevice d = (ZExternalDevice) list.getSelectedValue();
                    if (d != null)
                        try {
                            zdm.removeDevice(d, true).post();
                        } catch (ResourceUnavailableException e1) {
                            e1.printStackTrace();
                        }
                }
            };
            setActions(start, remove);
            listColor = new Color(254, 111, 83);
            refreshList();
        }

        // should only be called on AWT Event thread
        public void refreshList() {
            List l = zdm.getStoppedList();
            list.setListData(l.toArray());
            boolean v = this.isVisible();
            if (l.size() > 0)
                list.setSelectedIndex(0);
        }

        public void valueChanged(ListSelectionEvent e) {
        }
    }
}
