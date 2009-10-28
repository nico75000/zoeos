package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.device.EMU.E4.parameter.GeneralParameterDescriptor;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterContext;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.gui.IconAndTipCarrier;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.gui.ZDialog;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.system.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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

    private static final String SORT_SETUP_PRES = "Sort setup" + ZUtilities.DOT_POSTFIX;
    private static final String SORT_SETUP_DESC = "Create, execute and delete sorting presets";

    private static final String MISSING_KEY_VALUE = "";

    private static final String PREF_NODE_voiceSorting = "Voice sorts";
    private static final String PREF_NODE_linkSorting = "Link sorts";
    private static final String PREF_NODE_zoneSorting = "Zone sorts";

    private static String[] voiceSortKeys;
    private static String[] linkSortKeys;
    private static String[] zoneSortKeys;

    private static final String VOICES_PRES = "Voices";
    private static final String ZONES_PRES = "Zones";
    private static final String LINKS_PRES = "Links";

    private static final String VOICES_DESC = "Sort voices";
    private static final String ZONES_DESC = "Sort zones";
    private static final String LINKS_DESC = "Sort links";

    private static final Preferences voicePref = Preferences.userNodeForPackage(SortPresetZMTC.class.getClass()).node(PREF_NODE_voiceSorting);
    private static final Preferences linkPref = Preferences.userNodeForPackage(SortPresetZMTC.class.getClass()).node(PREF_NODE_linkSorting);
    private static final Preferences zonePref = Preferences.userNodeForPackage(SortPresetZMTC.class.getClass()).node(PREF_NODE_zoneSorting);

    private int mode = MODE_SORT_VOICES;
    // private int subMode = 0;
    private String presString;
    private String descString;

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
        this(MODE_SORT_VOICES);
    }

    private SortPresetZMTC(int mode/*, int subMode*/) {
        this.mode = mode;
        //this.subMode = subMode;

        switch (mode) {
            case MODE_SORT_VOICES:
                presString = VOICES_PRES;
                descString = VOICES_DESC;
                /*
                if (subMode < voiceSortKeys.length) {
                    presString = voiceSortKeys[subMode];
                    descString = voicePref.get(voiceSortKeys[subMode], voiceSortKeys[subMode]);
                } else if (subMode == voiceSortKeys.length) {
                    presString = SORT_SETUP_PRES;
                    descString = SORT_SETUP_DESC;
                } else
                    throw new IllegalArgumentException("Illegal submode");
                    */
                break;
            case MODE_SORT_LINKS:
                presString = LINKS_PRES;
                descString = LINKS_DESC;
                /*
                if (subMode < linkSortKeys.length) {
                    presString = linkSortKeys[subMode];
                    descString = linkPref.get(linkSortKeys[subMode], linkSortKeys[subMode]);
                } else if (subMode == linkSortKeys.length) {
                    presString = SORT_SETUP_PRES;
                    descString = SORT_SETUP_DESC;
                } else
                    throw new IllegalArgumentException("Illegal submode");
                */
                break;
            case MODE_SORT_ZONES:
                presString = ZONES_PRES;
                descString = ZONES_DESC;
                /*
                if (subMode < zoneSortKeys.length) {
                    presString = zoneSortKeys[subMode];
                    descString = zonePref.get(voiceSortKeys[subMode], voiceSortKeys[subMode]);
                } else if (subMode == zoneSortKeys.length) {
                    presString = SORT_SETUP_PRES;
                    descString = SORT_SETUP_DESC;
                } else
                    throw new IllegalArgumentException("Illegal submode");
                    */
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
                // if (subMode == voiceSortKeys.length)
                return new SortPresetZMTC(MODE_SORT_LINKS);
                // else
                //   return new SortPresetZMTC(MODE_SORT_VOICES, subMode + 1);

            case MODE_SORT_LINKS:
                //  if (subMode == linkSortKeys.length)
                return new SortPresetZMTC(MODE_SORT_ZONES);
                //   else
                //     return new SortPresetZMTC(MODE_SORT_LINKS, subMode + 1);

            case MODE_SORT_ZONES:
                //  if (subMode == zoneSortKeys.length)
                return null;
                //   else
                //     return new SortPresetZMTC(MODE_SORT_ZONES, subMode + 1);
        }
        return null;
    }

    public String getPresentationString() {
        return presString;
    }

    public String getDescriptiveString() {
        return descString;
    }

    public String getMenuPathString() {
        /*
        switch (mode) {
            case MODE_SORT_VOICES:
                return ";Sort;Voices";
            case MODE_SORT_LINKS:
                return ";Sort;Links";
            case MODE_SORT_ZONES:
                return ";Sort;Zones";
        }
        */
        return ";Sort";
    }

    public boolean handleTarget(ContextEditablePreset contextEditablePreset, int total, int curr) throws Exception {
        handlePresets();
        return false;
    }

    private void handlePresets() throws CommandFailedException, ZCommandTargetsNotSpecifiedException {
        ContextEditablePreset[] presets = getTargets().toArray(new ContextEditablePreset[numTargets()]);
        int errors = 0;
        int n;
        switch (mode) {
            case MODE_SORT_VOICES:
                setupVoiceSorts();
                /*
                for (n = 0; n < presets.length; n++)

                    if (subMode < voiceSortKeys.length) {
                        Integer[] ids;
                        ids = decodeKeyValue(voicePref.get(voiceSortKeys[subMode], MISSING_KEY_VALUE));
                        if (ids.length > 0)
                            try {
                                presets[n].sortVoices(ids);
                            } catch (PresetException e) {
                                errors++;
                            }
                    } else if (subMode == voiceSortKeys.length) {
                        setupVoiceSorts();
                        return;
                    } else
                        throw new IllegalArgumentException("Illegal submode");
                        */
                break;
            case MODE_SORT_LINKS:
                setupLinkSorts();
                /*
                for (n = 0; n < presets.length; n++)
                    if (subMode < linkSortKeys.length) {
                        Integer[] ids;
                        ids = decodeKeyValue(linkPref.get(linkSortKeys[subMode], MISSING_KEY_VALUE));
                        if (ids.length > 0)
                            try {
                                presets[n].sortLinks(ids);
                            } catch (PresetException e) {
                                errors++;
                            }
                    } else if (subMode == linkSortKeys.length) {
                        setupLinkSorts();
                        return;
                    } else
                        throw new IllegalArgumentException("Illegal submode");
                        */
                break;
            case MODE_SORT_ZONES:
                setupZoneSorts();
                /*
                for (n = 0; n < presets.length; n++)
                    if (subMode < zoneSortKeys.length) {
                        Integer[] ids;
                        ids = decodeKeyValue(zonePref.get(zoneSortKeys[subMode], MISSING_KEY_VALUE));
                        if (ids.length > 0)
                            try {
                                presets[n].sortZones(ids);
                            } catch (PresetException e) {
                                errors++;
                            }
                    } else if (subMode == zoneSortKeys.length) {
                        setupZoneSorts();
                        return;
                    } else
                        throw new IllegalArgumentException("Illegal submode");
                        */
                break;
            default:
                throw new IllegalArgumentException("Illegal mode");
        }
        if (errors == presets.length)
            throw new CommandFailedException(presets.length > 1 ? "None of the source presets could be sorted" : "The source preset could not be sorted");
        else if (errors > 0)
            throw new CommandFailedException(errors + " of " + presets.length + " source presets could not be sorted");

    }

    private void setupVoiceSorts() throws ZCommandTargetsNotSpecifiedException {
        try {
            new SortSetupDialog(ZoeosFrame.getInstance(), "Voice Sorting", voicePref, getTargets().get(0).getDeviceParameterContext().getVoiceContext());
        } catch (HeadlessException e) {
            e.printStackTrace();
        } catch (DeviceException e) {
            e.printStackTrace();
        }
    }

    private void setupLinkSorts() throws ZCommandTargetsNotSpecifiedException {
        try {
            new SortSetupDialog(ZoeosFrame.getInstance(), "Link Sorting", linkPref, getTargets().get(0).getDeviceParameterContext().getLinkContext());
        } catch (HeadlessException e) {
            e.printStackTrace();
        } catch (DeviceException e) {
            e.printStackTrace();
        }
    }

    private void setupZoneSorts() throws ZCommandTargetsNotSpecifiedException {
        try {
            new SortSetupDialog(ZoeosFrame.getInstance(), "Zone Sorting", zonePref, getTargets().get(0).getDeviceParameterContext().getZoneContext());
        } catch (HeadlessException e) {
            e.printStackTrace();
        } catch (DeviceException e) {
            e.printStackTrace();
        }
    }

    private class SortSetupDialog extends ZDialog {
        private Preferences pref;
        private JList list;
        private String[] keys;
        private ParameterContext pc;

        private static final String NEW_SORT_TITLE = "New Sort";

        JButton executeButt, removeButt, clearButt;

        public SortSetupDialog(Frame owner, String title, Preferences pref, ParameterContext pc) throws HeadlessException {
            super(owner, title, true);
            this.pref = pref;
            this.pc = pc;

            this.getContentPane().setLayout(new BorderLayout());
            this.setResizable(false);

            list = new JList();
            final ListCellRenderer rend = list.getCellRenderer();

            list.setCellRenderer(new ListCellRenderer() {
                public Component getListCellRendererComponent(JList list,
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
            list.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting())
                        updateButtons();
                }
            });
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JButton doneButt = new JButton(new AbstractAction("Done") {
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });

            executeButt = new JButton(new AbstractAction("Execute") {
                public void actionPerformed(ActionEvent e) {
                    try {
                        execute();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    } finally {
                    }
                    dispose();
                }
            });
            executeButt.setEnabled(false);

            JButton addButt = new JButton(new AbstractAction("New") {
                public void actionPerformed(ActionEvent e) {
                    ArrayList sels = new ArrayList();
                    Object[] params = SortSetupDialog.this.pc.getAllParameterDescriptors().toArray();
                    if (params.length > 0) {
                        while (true) {
                            Object sel = JOptionPane.showInputDialog(SortSetupDialog.this, "Choose parameter", NEW_SORT_TITLE, JOptionPane.QUESTION_MESSAGE, null, params, params[0]);
                            if (sel != null) {
                                sels.add(sel);
                                int opt = JOptionPane.showConfirmDialog(SortSetupDialog.this, "Add another parameter as a sort sub-key?", NEW_SORT_TITLE, JOptionPane.YES_NO_CANCEL_OPTION);
                                if (opt == 1)
                                    break;
                                else if (opt == 2)
                                    return;
                            } else
                                return;
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
            removeButt = new JButton(new AbstractAction("Delete") {
                public void actionPerformed(ActionEvent e) {
                    Object sv = list.getSelectedValue();
                    if (sv != null) {
                        SortSetupDialog.this.pref.remove(sv.toString());
                        updateSortPreferences();
                        refreshEntries();
                        updateButtons();
                    }
                }
            });
            removeButt.setEnabled(false);

            clearButt = new JButton(new AbstractAction("Clear") {
                public void actionPerformed(ActionEvent e) {
                    try {
                        if (UserMessaging.askYesNo("Are you sure you want to delete all configured sorts?")) {
                            SortSetupDialog.this.pref.clear();
                            updateSortPreferences();
                            refreshEntries();
                            updateButtons();
                        }
                    } catch (BackingStoreException e1) {
                        e1.printStackTrace();
                    }
                }
            });

            JPanel buttPanel = new JPanel(new FlowLayout());
            buttPanel.add(doneButt);
            buttPanel.add(executeButt);
            buttPanel.add(addButt);
            buttPanel.add(removeButt);
            buttPanel.add(clearButt);

            JScrollPane sp = new JScrollPane(list);
            sp.setPreferredSize(new Dimension(200, 200));
            getContentPane().add(sp, BorderLayout.CENTER);
            getContentPane().add(buttPanel, BorderLayout.SOUTH);

            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            refreshEntries();

            pack();
            show();
        }

        private void updateButtons() {
            if (list.getSelectedIndex() >= 0) {
                executeButt.setEnabled(true);
                removeButt.setEnabled(true);
            } else {
                executeButt.setEnabled(false);
                removeButt.setEnabled(false);
            }
            clearButt.setEnabled(list.getModel().getSize() > 0);
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
            Arrays.sort(entries, new Comparator<Object>() {
                public int compare(Object o, Object o1) {
                    return o.toString().compareTo(o1.toString());
                }
            });
            list.setListData(entries);
            if (entries.length > 0)
                list.setSelectedIndex(0);
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

        private void execute() throws ZCommandTargetsNotSpecifiedException {
            ContextEditablePreset[] presets = getTargets().toArray(new ContextEditablePreset[numTargets()]);
            int n;
            Object selObj = list.getSelectedValue();
            String key;
            switch (mode) {
                case MODE_SORT_VOICES:
                    key = voicePref.get(selObj.toString(), MISSING_KEY_VALUE);
                    if (key != null) {
                        final Integer[] ids = decodeKeyValue(key);
                        if (ids.length > 0)
                            for (n = 0; n < presets.length; n++) {
                                try {
                                    presets[n].sortVoices(ids);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                    }
                    break;
                case MODE_SORT_LINKS:
                    key = linkPref.get(selObj.toString(), MISSING_KEY_VALUE);
                    if (key != null) {
                        final Integer[] ids = decodeKeyValue(key);
                        if (ids.length > 0)
                            for (n = 0; n < presets.length; n++) {
                                try {
                                    presets[n].sortLinks(ids);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                    }
                    break;
                case MODE_SORT_ZONES:
                    key = zonePref.get(selObj.toString(), MISSING_KEY_VALUE);
                    if (key != null) {
                        final Integer[] ids = decodeKeyValue(key);
                        if (ids.length > 0)
                            for (n = 0; n < presets.length; n++) {
                                try {
                                    presets[n].sortZones(ids);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Illegal mode");
            }
        }
    }
}
