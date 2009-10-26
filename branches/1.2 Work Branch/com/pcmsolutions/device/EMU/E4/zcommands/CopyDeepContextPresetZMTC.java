package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.PresetContextMacros;
import com.pcmsolutions.device.EMU.E4.RemoteObjectStates;
import com.pcmsolutions.device.EMU.E4.preset.AggRemoteName;
import com.pcmsolutions.device.EMU.E4.preset.ContextReadablePreset;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:56:52
 * To change this template use Options | File Templates.
 */
public class CopyDeepContextPresetZMTC extends AbstractContextReadablePresetZMTCommand {
    private Object[] retArray = null;
    private JCheckBox arg1, arg2, arg3;

    private static final String PREF_copyEmpties = "copyEmpties";
    private static final String PREF_emptiesAtDest = "emptiesAtDest";
    private static final String PREF_translateLinks = "translateLinks";
    private static final Preferences prefs = Preferences.userNodeForPackage(CopyDeepContextPresetZMTC.class).node("CopyDeepContextPresetZMTC");

    public CopyDeepContextPresetZMTC() {
        super("Copy Deep", "Copy preset and linked presets (recursively)", new String[]{"Destination", "", "", ""}, new String[]{"Destination Preset", "Copy empties at source", "Search for (and only use) empties at destination", "Translate link indexes at destination"});
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        switch (index) {
            case 0:
                int firstEmptyIndex = -1;

                ContextReadablePreset[] targets = getTargets();
                if (retArray == null) {
                    Map presetMap = targets[0].getPresetNamesInContext();
                    // presetMap will be ordered
                    for (int i = 0; i < targets.length; i++)
                        presetMap.remove(targets[i].getPresetNumber());

                    retArray = new Object[presetMap.size()];
                    Iterator i = presetMap.keySet().iterator();
                    int k = 0;
                    Integer p;
                    while (i.hasNext()) {
                        p = (Integer) i.next();
                        try {
                            if (firstEmptyIndex == -1 && targets[0].getPresetContext().getPresetState(p) == RemoteObjectStates.STATE_EMPTY)
                                firstEmptyIndex = k;
                        } catch (NoSuchPresetException e) {
                            e.printStackTrace();
                        } catch (NoSuchContextException e) {
                            e.printStackTrace();
                        }
                        retArray[k] = new AggRemoteName(p, (String) presetMap.get(p));
                        k++;
                    }
                }

                JComboBox j = new JComboBox(retArray);
                if (firstEmptyIndex != -1)
                    j.setSelectedIndex(firstEmptyIndex);
                return j;
            case 1: // copy empties at source
                arg1 = new JCheckBox("Copy empties at source") {
                    {
                        addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                prefs.putBoolean(PREF_copyEmpties, arg1.isSelected());
                            }
                        });
                    }
                };
                arg1.setSelected(prefs.getBoolean(PREF_copyEmpties, true));
                return arg1;
            case 2: // search for (and only use) empties at destination
                arg2 = new JCheckBox("Search for (and only use) empties at destination") {
                    {
                        addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                arg1.setEnabled(!arg2.isSelected());
                                prefs.putBoolean(PREF_emptiesAtDest, arg2.isSelected());
                            }
                        });
                    }
                };
                arg2.setSelected(prefs.getBoolean(PREF_emptiesAtDest, false));
                if (arg2.isSelected())
                    arg1.setEnabled(false);
                return arg2;
            case 3: // translate link indexes
                arg3 = new JCheckBox("Translate link indexes at destination") {
                    {
                        addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                prefs.putBoolean(PREF_translateLinks, arg3.isSelected());
                            }
                        });
                    }
                };
                arg3.setSelected(prefs.getBoolean(PREF_translateLinks, true));
                return arg3;
        }
        throw new IllegalArgumentException("Argument index out of range");
    }

    public String getSummaryString(Object[] arguments) throws IllegalArgumentException {
        return "Deep Copy";
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        if (arguments.length < numArgs)
            throw new IllegalArgumentException("Insufficient Arguments");

        ContextReadablePreset[] presets = getTargets();
        Integer[] indexes = ZUtilities.extractIndexes(presets);
        int notCopied = 0;
        try {
            notCopied = PresetContextMacros.copyPresetDeep(indexes, presets[0].getPresetContext(), ((AggRemoteName) arguments[0]).getIndex(), ((Boolean) arguments[1]).booleanValue(), ((Boolean) arguments[2]).booleanValue(), ((Boolean) arguments[3]).booleanValue(), true);
            return;
        } catch (NoSuchPresetException e) {
            throw new CommandFailedException("No such preset");
        } catch (NoSuchContextException e) {
            throw new CommandFailedException("Missing preset context");
        }
    }

    public String getMenuPathString() {
        return ";Copy";
    }
}


