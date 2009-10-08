package com.pcmsolutions.device.EMU.E4.zcommands;

import com.pcmsolutions.device.EMU.E4.RemoteUnreachableException;
import com.pcmsolutions.device.EMU.E4.SampleContextMacros;
import com.pcmsolutions.device.EMU.E4.preset.AggRemoteName;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchContextException;
import com.pcmsolutions.device.EMU.E4.sample.IsolatedSampleUnavailableException;
import com.pcmsolutions.device.EMU.E4.sample.NoSuchSampleException;
import com.pcmsolutions.device.EMU.E4.sample.SampleEmptyException;
import com.pcmsolutions.gui.FixedLengthTextField;
import com.pcmsolutions.gui.ZoeosFrame;
import com.pcmsolutions.system.CommandFailedException;
import com.pcmsolutions.system.ZDeviceNotRunningException;

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
public class CopyContextSampleZC extends AbstractContextEditableSampleZCommand {
    private Object[] retArray = null;
    private String oldName;

    public CopyContextSampleZC() {
        super("Copy", "Copy Sample", new String[]{"Destination ", "Rename "}, new String[]{"Destination Sample", "Destination Name"});
    }

    public JComponent getComponentForArgument(int index) throws IllegalArgumentException  // exception for index out of range
    {
        if (index > numArgs - 1)
            throw new IllegalArgumentException("Argument Out Of Range");

        if (index == 0) {
            int firstEmptyIndex = -1;
            if (retArray == null) {
                Map sampleMap = getTarget().getUserSampleNamesInContext();
                // sampleMap will be ordered
                sampleMap.remove(getTarget().getSampleNumber());
                retArray = new Object[sampleMap.size()];
                Iterator i = sampleMap.keySet().iterator();
                int k = 0;
                Integer p;
                while (i.hasNext()) {
                    p = (Integer) i.next();
                    try {
                        if (firstEmptyIndex == -1 && getTarget().getSampleContext().isSampleEmpty(p))
                            firstEmptyIndex = k;
                    } catch (NoSuchSampleException e) {
                        e.printStackTrace();
                    } catch (NoSuchContextException e) {
                        e.printStackTrace();
                    }
                    retArray[k] = new AggRemoteName(p, (String) sampleMap.get(p));
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
                name = getTarget().getSampleName();
                oldName = name;
            } catch (NoSuchSampleException e) {
            } catch (SampleEmptyException e) {
            }
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
            pn = new AggRemoteName(getTarget().getSampleNumber(), getTarget().getSampleName());
        } catch (NoSuchSampleException e) {
            pn = new AggRemoteName(getTarget().getSampleNumber(), null);
        } catch (SampleEmptyException e) {
            pn = new AggRemoteName(getTarget().getSampleNumber(), null);
        }
        try {
            if (getTarget().sampleEmpty(((AggRemoteName) arguments[0]).getIndex())) {
                return "Copy " + pn + " to  " + arguments[0];
            } else
                return "Overwrite " + arguments[0] + " with " + pn;

        } catch (NoSuchSampleException e) {
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

        try {
            String confirmStr = SampleContextMacros.getOverwriteConfirmationString(getTarget().getSampleContext(), ((AggRemoteName) arguments[0]).getIndex(), 1);

            int ok = JOptionPane.showConfirmDialog(ZoeosFrame.getInstance(), confirmStr, "Confirm Sample Copy", JOptionPane.YES_NO_OPTION);
            if (ok == 0) {
                getTarget().copySample(new Integer[]{((AggRemoteName) arguments[0]).getIndex()});
                if (oldName != null && !oldName.equals(arguments[1]))
                    getTarget().getSampleContext().setSampleName(((AggRemoteName) arguments[0]).getIndex(), arguments[1].toString());
            }

        } catch (NoSuchSampleException e) {
            throw new CommandFailedException("No Such Sample: " + e.getAggName());
        } catch (SampleEmptyException e) {
            throw new CommandFailedException("Source is Empty: " + e.getAggName());
        } catch (IsolatedSampleUnavailableException e) {
            throw new CommandFailedException("Could not isolate source sample: " + e.getMessage());
        } catch (NoSuchContextException e) {
            throw new CommandFailedException("Could not name destination sample");
        } finally {
            try {
                getTarget().getDeviceContext().sampleMemoryDefrag(false);
            } catch (ZDeviceNotRunningException e) {
            } catch (RemoteUnreachableException e) {
            }
        }
    }
}

