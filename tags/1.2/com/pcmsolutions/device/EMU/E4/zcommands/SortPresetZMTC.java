package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.parameter.GeneralParameterDescriptor;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterContext;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.PresetEmptyException;
import com.pcmsolutions.gui.IconAndTipCarrier;
import com.pcmsolutions.gui.ZDialog;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.system.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class SortPresetZMTC extends AbstractContextEditablePresetZMTCommand {
    private static final int MODE_SORT_VOICES = 0;
    private static final int MODE_SORT_LINKS = 1;
    private static final int MODE_SORT_ZONES = 2;

    private int mode = MODE_SORT_VOICES;
    private int subMode = 0;

    private static final String SORT_SETUP_PRES = "Sort Setup" + ZUtilities.DOT_POSTFIX;
    private static final String SORT_SETUP_DESC = "Create and Delete Sort types";

    private static final String MISSING_KEY_VALUE = "";

    private static final String PREF_NODE_voiceSorting = "Voice Sorts";
    private static final String PREF_NODE_linkSorting = "Link Sorts";
    private static final String PREF_NODE_zoneSorting = "Zone Sorts";

    private static String[] voiceSortKeys;
    private static String[] linkSortKeys;
    private static String[] zoneSortKeys;

    private static final Preferences voicePref = Preferences.userNodeForPackage(SortPresetZMTC.class).node(PREF_NODE_voiceSorting);
    private static final Preferences linkPref = Preferences.userNodeForPackage(SortPresetZMTC.class).node(PREF_NODE_linkSorting);
    private static final Preferences zonePref = Preferences.userNodeForPackage(SortPresetZMTC.class).node(PREF_NODE_zoneSorting);

    static {
        updateSortPreferences();
    }

    public static void updateSortPreferences() {
        try {
            voiceSortKeys = voicePref.keys();
            Arrays.sort(voiceSortKeys);
        } catch (BackingStoreException e) {
            voiceSortKeys = new String[]{};
        }
        try {
            linkSortKeys = linkPref.keys();
            Arrays.sort(linkSortKeys);
        } catch (BackingStoreException e) {
            linkSortKeys = new String[]{};
        }
        try {
            zoneSortKeys = zonePref.keys();
            Arrays.sort(zoneSortKeys);
        } catch (BackingStoreException e) {
            zoneSortKeys = new String[]{};
        }
    }

    public SortPresetZMTC() {
        this(MODE_SORT_VOICES, 0);
    }

    private SortPresetZMTC(int mode, int subMode) {
        this.mode = mode;
        this.subMode = subMode;

        switch (mode) {
            case MODE_SORT_VOICES:
                if (subMode < voiceSortKeys.length) {
                    init(voiceSortKeys[subMode], voicePref.get(voiceSortKeys[subMode], voiceSortKeys[subMode]), null, null);
                } else if (subMode == voiceSortKeys.length) {
                    init(SORT_SETUP_PRES, SORT_SETUP_DESC, null, null);
                } else
                    throw new IllegalArgumentException("Illegal submode");
                break;
            case MODE_SORT_LINKS:
                if (subMode < linkSortKeys.length) {
                    init(linkSortKeys[subMode], linkPref.get(linkSortKeys[subMode], linkSortKeys[subMode]), null, null);
                } else if (subMode == linkSortKeys.length) {
                    init(SORT_SETUP_PRES, SORT_SETUP_DESC, null, null);
                } else
                    throw new IllegalArgumentException("Illegal submode");

                break;
            case MODE_SORT_ZONES:
                if (subMode < zoneSortKeys.length) {
                    init(zoneSortKeys[subMode], zonePref.get(zoneSortKeys[subMode], zoneSortKeys[subMode]), null, null);
                } else if (subMode == zoneSortKeys.length) {
                    init(SORT_SETUP_PRES, SORT_SETUP_DESC, null, null);
                } else
                    throw new IllegalArgumentException("Illegal submode");
                break;
            default:
                throw new IllegalArgumentException("Illegal mode");
        }
    }

    private static Integer[] decodeKeyValue(String value) {
        ArrayList ints = new ArrayList();
        StringTokenizer tok = new StringTokenizer(value, Zoeos.preferenceFieldSeperator);
        while (tok.hasMoreTokens())
            ints.add(IntPool.get(Integer.parseInt(tok.nextToken())));

        return (Integer[]) ints.toArray(new Integer[ints.size()]);
    }

    private static String encodeKeyValue(Integer[] ids) {
        String s = new String("");

        for (int i = 0; i < ids.length; i++)
            s += String.valueOf(ids[i]) + Zoeos.preferenceFieldSeperator;

        return s;
    }


    public ZMTCommand getNextMode() {
        switch (mode) {
            case MODE_SORT_VOICES:
                if (subMode == voiceSortKeys.length)
                    return new SortPresetZMTC(MODE_SORT_LINKS, 0);
                else
                    return new SortPresetZMTC(MODE_SORT_VOICES, subMode + 1);

            case MODE_SORT_LINKS:
                if (subMode == linkSortKeys.length)
                    return new SortPresetZMTC(MODE_SORT_ZONES, 0);
                else
                    return new SortPresetZMTC(MODE_SORT_LINKS, subMode + 1);

            case MODE_SORT_ZONES:
                if (subMode == zoneSortKeys.length)
                    return null;
                else
                    return new SortPresetZMTC(MODE_SORT_ZONES, subMode + 1);
        }
        return null;
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        ContextEditablePreset[] presets = getTargets();
        int num = presets.length;
        //ContextBasicEditablePreset p;
        if (num == 0) {
            handlePresets(new ContextEditablePreset[]{getTarget()});
        } else {
            handlePresets(presets);
            Thread.yield();
        }
    }

    public String getMenuPathString() {
        switch (mode) {
            case MODE_SORT_VOICES:
                return ";Sort;Voices";
            case MODE_SORT_LINKS:
                return ";Sort;Links";
            case MODE_SORT_ZONES:
                return ";Sort;Zones";
        }
        return ";Sort";
    }

    private void handlePresets(ContextEditablePreset[] presets) throws CommandFailedException {
        int errors = 0;
        int n;
        switch (mode) {
            case MODE_SORT_VOICES:
                for (n = 0; n < presets.length; n++)

                    if (subMode < voiceSortKeys.length) {
                        Integer[] ids;
                        ids = decodeKeyValue(voicePref.get(voiceSortKeys[subMode], MISSING_KEY_VALUE));
                        if (ids.length > 0)
                            try {
                                presets[n].sortVoices(ids);
                            } catch (NoSuchPresetException e) {
                                errors++;
                            } catch (PresetEmptyException e) {
                                errors++;
                            }
                    } else if (subMode == voiceSortKeys.length) {
                        setupVoiceSorts();
                        return;
                    } else
                        throw new IllegalArgumentException("Illegal submode");
                break;
            case MODE_SORT_LINKS:
                for (n = 0; n < presets.length; n++)
                    if (subMode < linkSortKeys.length) {
                        Integer[] ids;
                        ids = decodeKeyValue(linkPref.get(linkSortKeys[subMode], MISSING_KEY_VALUE));
                        if (ids.length > 0)
                            try {
                                presets[n].sortLinks(ids);
                            } catch (NoSuchPresetException e) {
                                errors++;
                            } catch (PresetEmptyException e) {
                                errors++;
                            }
                    } else if (subMode == linkSortKeys.length) {
                        setupLinkSorts();
                        return;
                    } else
                        throw new IllegalArgumentException("Illegal submode");
                break;
            case MODE_SORT_ZONES:
                for (n = 0; n < presets.length; n++)
                    if (subMode < zoneSortKeys.length) {
                        Integer[] ids;
                        ids = decodeKeyValue(zonePref.get(zoneSortKeys[subMode], MISSING_KEY_VALUE));
                        if (ids.length > 0)
                            try {
                                presets[n].sortZones(ids);
                            } catch (NoSuchPresetException e) {
                                errors++;
                            } catch (PresetEmptyException e) {
                                errors++;
                            }
                    } else if (subMode == zoneSortKeys.length) {
                        setupZoneSorts();
                        return;
                    } else
                        throw new IllegalArgumentException("Illegal submode");
                break;
            default:
                throw new IllegalArgumentException("Illegal mode");
        }
        if (errors == presets.length)
            throw new CommandFailedException(presets.length > 1 ? "None of the source presets could be sorted" : "The source preset could not be sorted");
        else if (errors > 0)
            throw new CommandFailedException(errors + " of " + presets.length + " source presets could not be sorted");

    }

    private void setupVoiceSorts() {
        try {
            new SortSetupDialog(ZoeosFrame.getInstance(), "Setup Voice Sorting", voicePref, getTargets()[0].getDeviceParameterContext().getVoiceContext());
        } catch (HeadlessException e) {
            e.printStackTrace();
        } catch (ZDeviceNotRunningException e) {
            e.printStackTrace();
        }
    }

    private void setupLinkSorts() {
        try {
            new SortSetupDialog(ZoeosFrame.getInstance(), "Setup Link Sorting", linkPref, getTargets()[0].getDeviceParameterContext().getLinkContext());
        } catch (HeadlessException e) {
            e.printStackTrace();
        } catch (ZDeviceNotRunningException e) {
            e.printStackTrace();
        }

    }

    private void setupZoneSorts() {
        try {
            new SortSetupDialog(ZoeosFrame.getInstance(), "Setup Zone Sorting", zonePref, getTargets()[0].getDeviceParameterContext().getZoneContext());
        } catch (HeadlessException e) {
            e.printStackTrace();
        } catch (ZDeviceNotRunningException e) {
            e.printStackTrace();
        }
    }


    private class SortSetupDialog extends ZDialog {
        private Preferences pref;
        private JList list;
        private String[] keys;
        private ParameterContext pc;

        private static final String NEW_SORT_TITLE = "New Sort";

        public SortSetupDialog(Frame owner, String title, Preferences pref, ParameterContext pc) throws HeadlessException {
            super(owner, title, true);
            this.pref = pref;
            this.pc = pc;

            this.getContentPane().setLayout(new BorderLayout());
            this.setResizable(false);

            list = new JList();
            final ListCellRenderer rend = list.getCellRenderer();

            list.setCellRenderer(new ListCellRenderer() {
                public Component getListCellRendererComponent(
                        JList list,
                        Object value,
                        int index,
                        boolean isSelected,
                        boolean cellHasFocus) {
                    Component c = rend.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (c instanceof JComponent)
                        ((JComponent) c).setToolTipText(((IconAndTipCarrier) value).getToolTipText());
                    return c;
                }
            });

            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            refreshEntries();

            JButton okButt = new JButton(new AbstractAction("OK") {
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
            JButton addButt = new JButton(new AbstractAction("New") {
                public void actionPerformed(ActionEvent e) {
                    ArrayList sels = new ArrayList();
                    Object[] params = SortSetupDialog.this.pc.getAllParameterDescriptors().toArray();
                    if (params.length > 0) {
                        while (true) {
                            Object sel = JOptionPane.showInputDialog(SortSetupDialog.this, "Choose Parameter", NEW_SORT_TITLE, JOptionPane.QUESTION_MESSAGE, null, params, params[0]);
                            if (sel != null) {
                                sels.add(sel);
                                int opt = JOptionPane.showConfirmDialog(SortSetupDialog.this, "Add another parameter as a sort sub-key?", NEW_SORT_TITLE, JOptionPane.YES_NO_CANCEL_OPTION);
                                if (opt == 1)
                                    break;
                                else if (opt == 2)
                                    return;
                            } else
                                break;
                        }
                        String name = JOptionPane.showInputDialog(SortSetupDialog.this, "Name", null);
                        Integer[] ids = new Integer[sels.size()];
                        for (int i = 0; i < ids.length; i++)
                            ids[i] = ((GeneralParameterDescriptor) sels.get(i)).getId();
                        if (name != null) {
                            SortSetupDialog.this.pref.put(name, encodeKeyValue(ids));
                            updateSortPreferences();
                            refreshEntries();
                        }
                    }
                }
            });
            JButton removeButt = new JButton(new AbstractAction("Delete") {
                public void actionPerformed(ActionEvent e) {
                    Object sv = list.getSelectedValue();
                    if (sv != null) {
                        SortSetupDialog.this.pref.remove(sv.toString());
                        updateSortPreferences();
                        refreshEntries();
                    }
                }
            });
            JButton clearButt = new JButton(new AbstractAction("Clear") {
                public void actionPerformed(ActionEvent e) {
                    try {
                        SortSetupDialog.this.pref.clear();
                        updateSortPreferences();
                        refreshEntries();
                    } catch (BackingStoreException e1) {
                        e1.printStackTrace();
                    }
                }
            });

            JPanel buttPanel = new JPanel(new FlowLayout());
            buttPanel.add(okButt);
            buttPanel.add(addButt);
            buttPanel.add(removeButt);
            buttPanel.add(clearButt);

            JScrollPane sp = new JScrollPane(list);
            sp.setPreferredSize(new Dimension(200, 200));
            getContentPane().add(sp, BorderLayout.CENTER);
            getContentPane().add(buttPanel, BorderLayout.SOUTH);

            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            pack();
            show();
        }

        private void refreshEntries() {
            try {
                keys = pref.keys();
            } catch (BackingStoreException e) {
                keys = new String[]{};
            }

            Object[] entries = new Object[keys.length];

            for (int i = 0; i < keys.length; i++)
                entries[i] = makeEntry(keys[i]);

            list.setListData(entries);
        }

        private Object makeEntry(final String key) {
            return new IconAndTipCarrier() {
                public Icon getIcon() {
                    return null;
                }

                public String getToolTipText() {
                    Integer[] ids;
                    ids = decodeKeyValue(pref.get(key, MISSING_KEY_VALUE));
                    String s = "";
                    for (int i = 0; i < ids.length; i++)
                        try {
                            s += (s.equals("") ? pc.getParameterDescriptor(ids[i]).toString() : ", " + pc.getParameterDescriptor(ids[i]).toString());
                        } catch (IllegalParameterIdException e) {
                        }
                    return (s.equals("") ? null : s);
                }

                public String toString() {
                    return key;
                }
            };
        }
    }
}
