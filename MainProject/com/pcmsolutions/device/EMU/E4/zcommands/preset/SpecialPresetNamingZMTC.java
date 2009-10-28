package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.preset.ContextBasicEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.PresetException;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.gui.FixedLengthTextField;
import com.pcmsolutions.gui.zcommand.AbstractZCommandField;
import com.pcmsolutions.gui.zcommand.ZCommandDialog;
import com.pcmsolutions.gui.zcommand.ZCommandField;
import com.pcmsolutions.system.ZMTCommand;
import com.pcmsolutions.system.ZUtilities;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class SpecialPresetNamingZMTC extends AbstractContextBasicEditablePresetZMTCommand {
    private static final int MAX_LENGTH = 8;
    private int mode;

    private static final AbstractZCommandField<FixedLengthTextField, String> inputField = new AbstractZCommandField<FixedLengthTextField, String>(new FixedLengthTextField("", MAX_LENGTH), "", "") {
        public String getValue() {
            return getComponent().getText();
        }
    };
    private final static ZCommandDialog cmdDlg = new ZCommandDialog();

    static {
        cmdDlg.init("", new ZCommandField[]{inputField});
    }


    private static final String[] presStrings = new String[]{"Prefix", "Postfix", "Numercial prefix", "Numerical postfix", "To uppercase", "To lowercase", "Remove whitespace"};
    private static final String[] descStrings = new String[]{"Prefix preset name", "Postfix preset name", "Numerically prefix preset name", "Numerically postfix preset name", "Change all characters to lowercase", "Change all characters to uppercase", "Remove all whitespace from name"};
    private static final String[] argPresStrings = new String[]{"Prefix", "Postfix", "Base number", "Base number"};
    private static final String[] argDescStrings = new String[]{"Prefix for preset name", "Postfix for preset name", "Base number for numerical  prefix", "Base number for numerical postfix"};


    public SpecialPresetNamingZMTC() {
        mode = 0;
    }

    private SpecialPresetNamingZMTC(int mode) {
        this.mode = mode;
    }

    public int getMinNumTargets() {
        return 1;
    }

    public ZMTCommand getNextMode() {
        if (mode == 6)
            return null;

        return new SpecialPresetNamingZMTC(mode + 1);
    }

    public boolean handleTarget(ContextBasicEditablePreset p, int total, int curr) throws Exception {
        final ContextBasicEditablePreset[] presets = getTargets().toArray(new ContextBasicEditablePreset[numTargets()]);
        if (mode < 4) {
            cmdDlg.setTitle(presStrings[mode]);
            inputField.setLabel(argPresStrings[mode]);
            inputField.getComponent().setToolTipText(argDescStrings[mode]);
            if (mode == 2 || mode == 3)
                inputField.getComponent().setText("0");
            else
                inputField.getComponent().setText("");
            inputField.getComponent().selectAll();
        }
        switch (mode) {
            case 0: // prefix
                cmdDlg.run(new ZCommandDialog.Executable() {
                    public void execute() throws Exception {
                        String s = inputField.getValue();
                        if (!s.trim().equals(""))
                            for (int i = 0; i < presets.length; i++)
                                try {
                                    presets[i].setPresetName(s.trim() + presets[i].getName());
                                } catch (PresetException e) {
                                } catch (EmptyException e) {
                                }
                    }
                });

                break;
            case 1: // postfix
                cmdDlg.run(new ZCommandDialog.Executable() {
                    public void execute() throws Exception {
                        String s = inputField.getValue();
                        int len = s.length();
                        for (int i = 0; i < presets.length; i++)
                            try {
                                String n = presets[i].getName();
                                if (n.length() + len > DeviceContext.MAX_NAME_LENGTH)
                                    presets[i].setPresetName(n.substring(0, DeviceContext.MAX_NAME_LENGTH - len) + s);
                                else
                                    presets[i].setPresetName(n + s);
                            } catch (PresetException e) {
                            } catch (EmptyException e) {
                            }
                    }
                });

                break;
            case 2: // numerical prefix
                cmdDlg.run(new ZCommandDialog.Executable() {
                    public void execute() throws Exception {
                        String s = inputField.getValue();
                        int base = Integer.parseInt(s);
                        for (int i = 0; i < presets.length; i++)
                            try {
                                presets[i].setPresetName(base++ + presets[i].getName());
                            } catch (PresetException e) {
                            } catch (EmptyException e) {
                            }
                    }
                });
                break;
            case 3: // numerical postfix
                cmdDlg.run(new ZCommandDialog.Executable() {
                    public void execute() throws Exception {
                        String s = inputField.getValue();
                        int base = Integer.parseInt(s);
                        int len = s.length();
                        for (int i = 0; i < presets.length; i++)
                            try {
                                presets[i].setPresetName(ZUtilities.postfixString(presets[i].getName(), String.valueOf(base++), DeviceContext.MAX_NAME_LENGTH));
                            } catch (PresetException e) {
                            } catch (EmptyException e) {
                            }
                    }
                });
                break;
            case 4:  // to upper
                for (int i = 0; i < presets.length; i++)
                    try {
                        presets[i].setPresetName(presets[i].getName().toUpperCase());
                    } catch (PresetException e) {
                    } catch (EmptyException e) {
                    }
                break;
            case 5: // to lower
                for (int i = 0; i < presets.length; i++)
                    try {
                        presets[i].setPresetName(presets[i].getName().toLowerCase());
                    } catch (PresetException e) {
                    } catch (EmptyException e) {
                    }
                break;
            case 6: // remove whitespace
                for (int i = 0; i < presets.length; i++)
                    try {
                        presets[i].setPresetName(ZUtilities.removeDelimiters(presets[i].getName()));
                    } catch (PresetException e) {
                    } catch (EmptyException e) {
                    }
        }
        return false;
    }

    public String getMenuPathString() {
        return ";Special naming";
    }

    public String getPresentationString() {
        return presStrings[mode];
    }

    public String getDescriptiveString() {
        return descStrings[mode];
    }
}
