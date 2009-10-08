package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.preset.ContextBasicEditablePreset;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.PresetEmptyException;
import com.pcmsolutions.gui.FixedLengthTextField;
import com.pcmsolutions.gui.ZDialog;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZMTCommandTargetsNotSuitableException;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:36:45
 * To change this template use Options | File Templates.
 */
public class RenamePresetAllZMTC extends AbstractContextBasicEditablePresetZMTCommand {

    public RenamePresetAllZMTC() {
        super("Rename All", "Rename All Presets with a single name", new String[]{"Name"}, new String[]{"New name for presets"});
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        String name = "";
        try {
            name = getTargets()[0].getPresetName();
        } catch (NoSuchPresetException e) {
        } catch (PresetEmptyException e) {
        }
        if (name.length() > 16)
            name = name.substring(0, 15);

        FixedLengthTextField tf = new FixedLengthTextField(name, 16);
        tf.selectAll();

        return tf;
    }

    public void setTargets(Object[] targets) throws IllegalArgumentException, ZMTCommandTargetsNotSuitableException {
        if (targets.length < 2)
            throw new IllegalArgumentException("not enough targets");
        super.setTargets(targets);
    }

    public int getMinNumTargets() {
        return 2;
    }

    public String getSummaryString(Object[] arguments) throws IllegalArgumentException {
        return "Rename All Presets";
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        ContextBasicEditablePreset[] presets = getTargets();

        for (int i = 0; i < presets.length; i++)
            try {
                presets[i].setPresetName(arguments[0].toString());
            } catch (NoSuchPresetException e) {
            } catch (PresetEmptyException e) {
            }
    }

    public ZDialog generateVerificationDialog() {
        return null;
    }

    public String getMenuPathString() {
        return ";Special Naming";
    }

    public Icon getIcon() {
        return null;
    }
}
