package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.preset.AggRemoteName;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.PresetEmptyException;
import com.pcmsolutions.gui.FixedLengthTextField;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.system.CommandFailedException;

import javax.swing.*;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 14:56:52
 * To change this template use Options | File Templates.
 */
public class CopyContextPresetZC extends AbstractContextReadablePresetZCommand {
    private Object[] retArray = null;
    private String oldName;

    public CopyContextPresetZC() {
        super("Copy", "Copy Preset", new String[]{"Destination ", "Rename "}, new String[]{"Destination Preset", "Destination Name"});
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        if (index > numArgs - 1)
            throw new IllegalArgumentException("Argument Out Of Range");

        if (index == 0) {
            int firstEmptyIndex = -1;
            if (retArray == null) {
                Map presetMap = getTarget().getPresetNamesInContext();
                // presetMap will be ordered
                presetMap.remove(getTarget().getPresetNumber());
                retArray = new Object[presetMap.size()];
                Iterator i = presetMap.keySet().iterator();
                int k = 0;
                Integer p;
                while (i.hasNext()) {
                    p = (Integer) i.next();
                    try {
                        if (firstEmptyIndex == -1 && getTarget().getPresetContext().isPresetEmpty(p))
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
        } else {
            String name = "";
            try {
                name = getTarget().getPresetName();
                oldName = name;
            } catch (NoSuchPresetException e) {
            } catch (PresetEmptyException e) {
            }
            StringBuffer bufName = new StringBuffer(name);
            if (name.length() > 16)
                name = name.substring(0, 15);

            FixedLengthTextField tf = new FixedLengthTextField(name, 16);
            tf.selectAll();

            return tf;
        }
    }

    public String getSummaryString(Object[] arguments) throws IllegalArgumentException {
        if (arguments.length < numArgs)
            throw new IllegalArgumentException("Insufficient Arguments");
        AggRemoteName pn;
        try {
            pn = new AggRemoteName(getTarget().getPresetNumber(), getTarget().getPresetName());
        } catch (NoSuchPresetException e) {
            pn = new AggRemoteName(getTarget().getPresetNumber(), null);
        } catch (PresetEmptyException e) {
            pn = new AggRemoteName(getTarget().getPresetNumber(), null);
        }
        try {
            if (getTarget().presetEmpty(((AggRemoteName) arguments[0]).getIndex())) {
                return "Copy " + pn + " to  " + arguments[0];
            } else
                return "Overwrite " + arguments[0] + " with " + pn;

        } catch (NoSuchPresetException e) {
            return "Copy " + pn + " to  " + arguments[0];

        }
    }

    public Icon getIcon() {
        //return new ImageIcon("toolbarButtonGraphics/general/copy16.gif");
        return null;
    }

    public String getMenuPathString() {
        return ";Copy";
    }

    public void execute(Object invoker, Object[] arguments) throws IllegalArgumentException, CommandFailedException  // IllegalArgumentException thrown for insufficient number of arguments
    {
        if (arguments.length < numArgs)
            throw new IllegalArgumentException("Insufficient Arguments");

        /*ContextReadablePreset targ = getTarget();
        try {
            PresetContextMacros.copyPresets(targ.getPresetContext(), new Integer[]{targ.getPresetNumber()}, new Integer[]{}, false, true, "Copy Preset");
        } catch (NoSuchPresetException e) {
            throw new CommandFailedException("No Such Preset: " + e.getAggName());
        } catch (NoSuchContextException e) {
            throw new CommandFailedException("No context");
        }
        */

        try {
            int ok = JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), "OK to overwrite preset " + ((AggRemoteName) arguments[0]).toString() + " ?", "Confirm Preset Copy", JOptionPane.YES_NO_OPTION);
            if (ok == 0)
                if (oldName != null && !oldName.equals(arguments[1]))
                    getTarget().copyPreset(((AggRemoteName) arguments[0]).getIndex(), arguments[1].toString());
                else
                    getTarget().copyPreset(((AggRemoteName) arguments[0]).getIndex());
        } catch (NoSuchPresetException e) {
            throw new CommandFailedException("No Such Preset: " + e.getAggName());
        } catch (PresetEmptyException e) {
            throw new CommandFailedException("Source is Empty: " + e.getAggName());
        }
    }
}

