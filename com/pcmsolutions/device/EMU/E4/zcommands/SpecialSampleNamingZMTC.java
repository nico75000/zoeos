package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.DeviceContext;
import com.pcmsolutions.device.EMU.E4.sample.ContextBasicEditableSample;
import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.device.EMU.E4.sample.SampleEmptyException;
import com.pcmsolutions.gui.FixedLengthTextField;
import com.pcmsolutions.gui.ZDialog;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZMTCommand;
import com.pcmsolutions.system.ZMTCommandTargetsNotSuitableException;
import com.pcmsolutions.system.ZUtilities;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class SpecialSampleNamingZMTC extends AbstractContextBasicEditableSampleZMTCommand {
    private int mode;

    private static final String[] presStrings = new String[]{"Prefix", "Postfix", "Numercial Prefix", "Numerical Postfix", "To Uppercase", "To Lowercase", "Remove whitesapce"};
    private static final String[] descStrings = new String[]{"Prefix Sample Name", "Postfix Sample Name", "Numerically Prefix Sample Name", "Numerically Postfix Sample Name", "Change all characters to uppercase", "Change all characters to lowercase", "Remove all whitespace from name"};
    private static final String[][] argPresStrings = new String[][]{new String[]{"Prefix"}, new String[]{"Postfix"}, new String[]{"Base number"}, new String[]{"Base number"}, null, null, null};
    private static final String[][] argDescStrings = new String[][]{new String[]{"Prefix for sample name"}, new String[]{"Postfix for sample name"}, new String[]{"Base number for numerical  prefix"}, new String[]{"Base number for numerical postfix"}, null, null, null};

    private static final int MAX_LENGTH = 8;

    public SpecialSampleNamingZMTC() {
        super(presStrings[0], descStrings[0], argPresStrings[0], argDescStrings[0]);
        mode = 0;
    }

    public SpecialSampleNamingZMTC(int mode) {
        super(presStrings[mode], descStrings[mode], argPresStrings[mode], argDescStrings[mode]);
        this.mode = mode;
    }

    public int getMinNumTargets() {
        return 1;
    }

    public ZMTCommand getNextMode() {
        if (mode == 6)
            return null;

        return new SpecialSampleNamingZMTC(mode + 1);
    }

    public void setTargets(Object[] targets) throws IllegalArgumentException, ZMTCommandTargetsNotSuitableException {
        if (targets.length < 1)
            throw new IllegalArgumentException("not enough targets");
        super.setTargets(targets);
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        String name = "";
        FixedLengthTextField tf = new FixedLengthTextField(name, MAX_LENGTH);
        if (mode == 2 || mode == 3)
            tf.setText("0");
        tf.selectAll();
        return tf;
    }

    public String getSummaryString(Object[] arguments) throws IllegalArgumentException {
        return null;
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        ContextBasicEditableSample[] samples = getTargets();
        String s = null;
        int len;
        if (mode < 4)
            s = arguments[0].toString();
        switch (mode) {
            case 0: // prefix
                if (s.trim().equals(""))
                    break;
                for (int i = 0; i < samples.length; i++)
                    try {
                        samples[i].setSampleName(s.trim() + samples[i].getSampleName());
                    } catch (NoSuchSampleException e) {
                    } catch (SampleEmptyException e) {
                    }
                break;
            case 1: // postfix
                len = s.length();
                for (int i = 0; i < samples.length; i++)
                    try {
                        String n = samples[i].getSampleName();
                        if (n.length() + len > DeviceContext.MAX_NAME_LENGTH)
                            samples[i].setSampleName(n.substring(0, DeviceContext.MAX_NAME_LENGTH - len) + s);
                        else
                            samples[i].setSampleName(n + s);
                    } catch (NoSuchSampleException e) {
                    } catch (SampleEmptyException e) {
                    }

                break;
            case 2: // numerical prefix
                try {
                    int base = Integer.parseInt(s);
                    for (int i = 0; i < samples.length; i++)
                        try {
                            samples[i].setSampleName(base++ + samples[i].getSampleName());
                        } catch (NoSuchSampleException e) {
                        } catch (SampleEmptyException e) {
                        }
                } catch (NumberFormatException e) {
                    throw new CommandFailedException("Not a valid number");
                }

                break;
            case 3: // numerical postfix
                try {
                    int base = Integer.parseInt(s);
                    len = s.length();
                    for (int i = 0; i < samples.length; i++)
                        try {
                            samples[i].setSampleName(ZUtilities.postfixString(samples[i].getSampleName(), String.valueOf(base++), DeviceContext.MAX_NAME_LENGTH));
                        } catch (NoSuchSampleException e) {
                        } catch (com.pcmsolutions.device.EMU.E4.sample.SampleEmptyException e) {
                        }
                } catch (NumberFormatException e) {
                    throw new CommandFailedException("Not a valid number");
                }
                break;
            case 4:  // to upper
                for (int i = 0; i < samples.length; i++)
                    try {
                        samples[i].setSampleName(samples[i].getSampleName().toUpperCase());
                    } catch (NoSuchSampleException e) {
                    } catch (com.pcmsolutions.device.EMU.E4.sample.SampleEmptyException e) {
                    }
                break;
            case 5: // to lower
                for (int i = 0; i < samples.length; i++)
                    try {
                        samples[i].setSampleName(samples[i].getSampleName().toLowerCase());
                    } catch (NoSuchSampleException e) {
                    } catch (com.pcmsolutions.device.EMU.E4.sample.SampleEmptyException e) {
                    }
                break;
            case 6: // remove whitespace
                for (int i = 0; i < samples.length; i++)
                    try {
                        samples[i].setSampleName(ZUtilities.removeDelimiters(samples[i].getSampleName()));
                    } catch (NoSuchSampleException e) {
                    } catch (com.pcmsolutions.device.EMU.E4.sample.SampleEmptyException e) {
                    }
        }
    }

    public String getMenuPathString() {
        return ";Special Naming";
    }

    public ZDialog generateVerificationDialog() {
        return null;
    }

    public Icon getIcon() {
        return null;
    }
}
