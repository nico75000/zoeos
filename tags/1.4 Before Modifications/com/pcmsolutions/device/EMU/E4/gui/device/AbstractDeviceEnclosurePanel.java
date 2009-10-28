package com.pcmsolutions.device.EMU.E4.gui.device;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.gui.TitleProvider;
import com.pcmsolutions.device.EMU.E4.gui.TitleProviderListener;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.system.*;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public abstract class AbstractDeviceEnclosurePanel extends JPanel implements ZDeviceListener, ZDisposable, TitleProvider {
    protected DeviceContext device;

    protected JPanel runningPanel = new JPanel() {
        public Color getBackground() {
            return UIColors.getDefaultBG();
        }

        public Color getForeground() {
            return UIColors.getDefaultFG();
        }

    };
    protected JPanel pendingPanel = new JPanel() {
        public Color getBackground() {
            return UIColors.getDefaultBG();
        }

        public Color getForeground() {
            return UIColors.getDefaultFG();
        }

    };
    protected JScrollPane stoppedPanel = new JScrollPane() {
        public Color getBackground() {
            return UIColors.getDefaultBG();
        }

        public Color getForeground() {
            return UIColors.getDefaultFG();
        }
    };
    protected JTextPane stoppedTextPane;
    protected JPanel nothingPanel = new JPanel() {
        public Color getBackground() {
            return UIColors.getDefaultBG();
        }

        public Color getForeground() {
            return UIColors.getDefaultFG();
        }
    };
    protected boolean runningPanelBuilt = false;
    protected JLabel errorLabel;

    public void init(DeviceContext device) throws Exception {
        this.device = device;
        errorLabel = new JLabel("= Nothing to show =") {
            public Color getForeground() {
                return UIColors.getDefaultFG();
            }

            public Color getBackground() {
                return UIColors.getDefaultBG();
            }
        };
        this.setLayout(new BorderLayout());
        buildStoppedPanel();
        buildNothingPanel();
        buildPendingPanel();
        Zoeos.addZDeviceListener(this);
    }

    private void buildPendingPanel() {
        pendingPanel.add(new JScrollPane(Zoeos.getPendingLabel()));
    }

    private void buildNothingPanel() {
        nothingPanel.add(new JScrollPane(errorLabel));
    }

    private void buildStoppedPanel() {
        JPanel innerPanel = new JPanel(new BorderLayout()) {
            public Color getForeground() {
                return UIColors.getDefaultFG();
            }

            public Color getBackground() {
                return UIColors.getDefaultBG();
            }
        };
        innerPanel.add(Zoeos.getStoppedLabel(), BorderLayout.NORTH);

        stoppedTextPane = new JTextPane() {
            public Color getForeground() {
                return UIColors.getDefaultFG();
            }

            public Color getBackground() {
                return UIColors.getDefaultBG();
            }
        };
        stoppedTextPane.setBorder(new TitledBorder("Reason"));
        stoppedTextPane.setEditable(false);

        innerPanel.add(stoppedTextPane, BorderLayout.CENTER);
        innerPanel.add(new JButton(new AbstractAction("Try Restart") {
            public void actionPerformed(ActionEvent e) {
                try {
                    Zoeos.getInstance().getDeviceManager().startDevice(device).post();
                } catch (ResourceUnavailableException e1) {
                    e1.printStackTrace();
                }
            }
        }), BorderLayout.SOUTH);

        stoppedPanel.setViewportView(innerPanel);
    }

    protected abstract void buildRunningPanel() ;

    public DeviceContext getDevice() {
        return device;
    }

    protected void syncPanel() {
        switch (device.getState()) {
            case ZExternalDevice.STATE_STOPPED:
                stoppedTextPane.setText(device.getReasonForState());
                loadPanel(stoppedPanel);
                break;
            case ZExternalDevice.STATE_PENDING:
                loadPanel(pendingPanel);
                break;
            case ZExternalDevice.STATE_RUNNING:
                if (!runningPanelBuilt)
                  //  try {
                        buildRunningPanel();
                   // } catch (ZDeviceNotRunningException e) {
                        // oops
                   //     loadPanel(stoppedPanel);
                       // break;
                 //   }
                loadPanel(runningPanel);
                break;
            default:
                loadPanel(nothingPanel);
                break;
        }
    }

    protected void loadPanel(JComponent p) {
        AbstractDeviceEnclosurePanel.this.removeAll();
        AbstractDeviceEnclosurePanel.this.add(p, BorderLayout.CENTER);
        AbstractDeviceEnclosurePanel.this.revalidate();
        AbstractDeviceEnclosurePanel.this.repaint();
    }

    public void deviceStarted(ZDeviceStartedEvent ev) {
        if (ev.getDevice() == device)
            syncPanel();
    }

    public void deviceStopped(ZDeviceStoppedEvent ev) {
        if (ev.getDevice() == device) {
            stoppedTextPane.setText(ev.getReason());
            syncPanel();
        }
    }

    public void devicePending(ZDevicePendingEvent ev) {
        if (ev.getDevice() == device)
            syncPanel();
    }

    public void deviceRemoved(ZDeviceRemovedEvent ev) {
        if (ev.getDevice() == device)
            syncPanel();
    }

    public String getTitle() {
        return this.device.getTitle();
    }

    public String getReducedTitle() {
        return device.getReducedTitle();
    }

    public void addTitleProviderListener(TitleProviderListener tpl) {
        device.addTitleProviderListener(tpl);
    }

    public void removeTitleProviderListener(TitleProviderListener tpl) {
        device.removeTitleProviderListener(tpl);
    }

    public Icon getIcon() {
        return device.getIcon();
    }

    public void zDispose() {
        Zoeos.removeZDeviceListener(this);
        removeAll();
        runningPanel = null;
        stoppedPanel = null;
        pendingPanel = null;
        nothingPanel = null;
        device = null;
    }
}
