package com.pcmsolutions.device.EMU.E4.gui;

import com.pcmsolutions.device.EMU.E4.gui.table.EditingTable;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 04-Jul-2003
 * Time: 04:23:47
 * To change this template use Options | File Templates.
 */
public class ParameterModelUtilities {
    public static int indexOfId(ReadableParameterModel[] models, Integer id) {
        for (int i = 0; i < models.length; i++)
            if (models[i].getParameterDescriptor().getId().equals(id))
                return i;
        return -1;
    }

    public static EditableParameterModel[] extractEditableParameterModels(Object[] objs) {
        return extractEditableParameterModels(objs, true);
    }

    public static boolean areAllOfId(ReadableParameterModel[] models, int id) {
        for (int i = 0; i < models.length; i++)
            if (models[i].getParameterDescriptor().getId().intValue() != id)
                return false;
        return true;
    }

    public static boolean areAllOfValue(ReadableParameterModel[] models, int value) throws ParameterException {
        for (int i = 0; i < models.length; i++)
            if (models[i].getValue().intValue() != value)
                return false;
        return true;
    }

    public static void scaleValues(List<EditableParameterModel> models, float percent) throws ParameterException {
        for (Iterator<EditableParameterModel> i = models.iterator(); i.hasNext();)
            scaleValue(i.next(), percent);
    }

    public static void scaleValue(EditableParameterModel p, float percent) throws ParameterException {
        p.setValue(getScaledValue(p, percent));
    }

    public static Integer getScaledValue(ReadableParameterModel p, float percent) throws ParameterException {
        int maxv = p.getParameterDescriptor().getMaxValue().intValue();
        int minv = p.getParameterDescriptor().getMinValue().intValue();
        int currv = p.getValue().intValue() - minv;
        int newv = (int) Math.round((minv + (currv * percent) / 100.0F));

        if (newv > maxv)
            newv = maxv;

        return IntPool.get(newv);
    }

    public static Set getIdSet(ReadableParameterModel[] models) {
        Set s = new HashSet();
        for (int i = 0; i < models.length; i++)
            s.add(models[i].getParameterDescriptor().getId());
        return s;
    }

    public static Set getValueSet(ReadableParameterModel[] models) throws ParameterException {
        Set s = new HashSet();
        for (int i = 0; i < models.length; i++)
            s.add(models[i].getValue());
        return s;
    }

    public static EditableParameterModel[] extractEditableParameterModels(Object[] objs, boolean expandWrapped) {
        ArrayList epm = new ArrayList();
        for (int i = 0; i < objs.length; i++)
            if (expandWrapped && objs[i] instanceof ParameterModelWrapper)
                epm.addAll(Arrays.asList(extractEditableParameterModels(((ParameterModelWrapper) objs[i]).getWrappedObjects(), expandWrapped)));
            else if (objs[i] instanceof EditableParameterModel)
                epm.add(objs[i]);

        return (EditableParameterModel[]) epm.toArray(new EditableParameterModel[epm.size()]);
    }

    public static Integer[] getValuesForStringValues(String[] strVal, ReadableParameterModel[] models) throws ParameterException {
        Integer[] vals = new Integer[models.length];
        for (int i = 0; i < models.length; i++)
            vals[i] = models[i].getParameterDescriptor().getValueForString(strVal[i]);
        return vals;
    }

    public static Integer[] getValuesForUnitlessStringValues(String[] strVal, ReadableParameterModel[] models) throws ParameterException {
        Integer[] vals = new Integer[models.length];
        for (int i = 0; i < models.length; i++)
            vals[i] = models[i].getParameterDescriptor().getValueForUnitlessString(strVal[i]);
        return vals;
    }

    public static Integer[] extractCurrentValues(ReadableParameterModel[] models) throws ParameterException {
        Integer[] vals = new Integer[models.length];
        for (int i = 0; i < models.length; i++)
            vals[i] = models[i].getValue();
        return vals;
    }

    public static Integer[] extractMinimumValues(ReadableParameterModel[] models) {
        Integer[] vals = new Integer[models.length];
        for (int i = 0; i < models.length; i++)
            vals[i] = models[i].getParameterDescriptor().getMinValue();
        return vals;
    }

    public static Integer[] extractMaximumValues(ReadableParameterModel[] models) {
        Integer[] vals = new Integer[models.length];
        for (int i = 0; i < models.length; i++)
            vals[i] = models[i].getParameterDescriptor().getMaxValue();
        return vals;
    }

    public static Integer[] extractDefaultValues(ReadableParameterModel[] models) {
        Integer[] vals = new Integer[models.length];
        for (int i = 0; i < models.length; i++)
            vals[i] = models[i].getParameterDescriptor().getDefaultValue();
        return vals;
    }

    public static Double[] extractCurrentFORs(ReadableParameterModel[] models) throws ParameterException {
        Double[] vals = new Double[models.length];
        for (int i = 0; i < models.length; i++)
            vals[i] = new Double(getFOR(models[i]));
        return vals;
    }

// get factor of range
    public static double getFOR(ReadableParameterModel p, Integer value) {
        return getFOR(p.getParameterDescriptor(), value);
        //return ((double) (value.intValue() - p.getParameterDescriptor().getMinValue().intValue())) / (p.getParameterDescriptor().getMaxValue().intValue() - p.getParameterDescriptor().getMinValue().intValue());
    }

// get factor of range
    public static double getFOR(ReadableParameterModel p) throws ParameterException {
        return getFOR(p.getParameterDescriptor(), p.getValue());
        //return ((double) (p.getValue().intValue() - p.getParameterDescriptor().getMinValue().intValue())) / (p.getParameterDescriptor().getMaxValue().intValue() - p.getParameterDescriptor().getMinValue().intValue());
    }

    public static double getFOR(GeneralParameterDescriptor pd, Integer value) {
        return ((double) (value.intValue() - pd.getMinValue().intValue())) / (pd.getMaxValue().intValue() - pd.getMinValue().intValue());
    }

// calc factor of range
    public static Integer calcFOR(EditableParameterModel p, double FOR) {
        GeneralParameterDescriptor pd = p.getParameterDescriptor();
        return IntPool.get((int) Math.round(pd.getMinValue().intValue() + (pd.getMaxValue().intValue() - pd.getMinValue().intValue()) * FOR));
    }

// calc percent of range
    public static Integer calcPOR(EditableParameterModel p, double percent) {
        GeneralParameterDescriptor pd = p.getParameterDescriptor();
        Integer v = IntPool.get((int) Math.round(pd.getMinValue().intValue() + ((pd.getMaxValue().intValue() - pd.getMinValue().intValue()) * percent) / 100));
        return v;
    }

// set factor of range
    public static void setFOR(EditableParameterModel p, double FOR) {
        try {
            p.setValue(calcFOR(p, FOR));
        } catch (ParameterException e) {
        }
    }

    public static void repeatParameterModels(final Object[] objs, int repeats) throws ParameterException {
        EditableParameterModel[] models = extractEditableParameterModels(objs);
        if (repeats >= models.length || models.length == 0)
            return;
        double repeatFORs[] = new double[repeats];
        Arrays.fill(repeatFORs, Double.MIN_VALUE);
        for (int i = 0; i < models.length; i++) {
            if (repeatFORs[i % repeats] == Double.MIN_VALUE)
                repeatFORs[i % repeats] = getFOR(models[i]);
            try {
                models[i].setValue(calcFOR(models[i], repeatFORs[i % repeats]));
            } catch (ParameterUnavailableException e) {
            } catch (ParameterValueOutOfRangeException e) {
            }
        }
    }

    public static void repeatParameterModels(final Object[] objs) throws ParameterException {
        repeatParameterModels(objs, 1);
    }

    public static void offsetParameterModels(final Object[] objs, final double factorOfRange) {
        EditableParameterModel[] models = extractEditableParameterModels(objs);
        for (int i = 0; i < models.length; i++) {
            GeneralParameterDescriptor gpd = models[i].getParameterDescriptor();
            int range = gpd.getMaxValue().intValue() - gpd.getMinValue().intValue();
            int offset = (int) Math.round(range * factorOfRange);
            try {
                models[i].setValue(gpd.constrainValue(IntPool.get(models[i].getValue().intValue() + offset)));
            } catch (ParameterException e) {
            }
        }
    }

    public static void minimizeParameterModels(final Object[] objs) {
        EditableParameterModel[] models = extractEditableParameterModels(objs);
        for (int i = 0; i < models.length; i++)
            try {
                models[i].setValue(models[i].getParameterDescriptor().getMinValue());
            } catch (ParameterException e) {
            }
    }

    public static void maximizeParameterModels(final Object[] objs) {
        EditableParameterModel[] models = extractEditableParameterModels(objs);
        for (int i = 0; i < models.length; i++)
            try {
                models[i].setValue(models[i].getParameterDescriptor().getMaxValue());
            } catch (ParameterException e) {
            }
    }

    public static void defaultParameterModels(final Object[] objs) {
        EditableParameterModel[] models = extractEditableParameterModels(objs);
        for (int i = 0; i < models.length; i++)
            try {
                models[i].setValue(models[i].getParameterDescriptor().getDefaultValue());
            } catch (ParameterException e) {
            }
    }

    public static void offsetFromFOR(EditableParameterModel p, double FOR, double offsetInUnits) throws ParameterException {
        int maxv = p.getParameterDescriptor().getMaxValue().intValue();
        int minv = p.getParameterDescriptor().getMinValue().intValue();
        double offsetAsFOR = offsetInUnits / (maxv - minv);
        p.setValue(calcFOR(p, FOR));
        p.offsetValue(new Double(offsetAsFOR));
    }

    public static void minimizeParameterModel(EditableParameterModel pm) {
        minimizeParameterModels(new EditableParameterModel[]{pm});
    }

    public static void maximizeParameterModel(EditableParameterModel pm) {
        maximizeParameterModels(new EditableParameterModel[]{pm});
    }

    public static Double[] getReveresedFORs(ReadableParameterModel[] models) throws ParameterException {
        Double[] currVals = extractCurrentFORs(models);
        Double[] outVals = new Double[currVals.length];
        for (int i = 0; i < currVals.length; i++)
            outVals[currVals.length - i - 1] = currVals[i];
        return outVals;
    }

    public static Double[] getPositionallyReflectedFORs(ReadableParameterModel[] models) throws ParameterException {
        Double[] currVals = extractCurrentFORs(models);
        Double temp;
        for (int i = 0, j = currVals.length - 1; i < currVals.length / 2; i++, j--) {
            temp = currVals[i];
            currVals[i] = currVals[j];
            currVals[j] = temp;
        }
        return currVals;
    }

    public static Double[] getRotatedFORs(ReadableParameterModel[] models, final int amt) throws ParameterException {
        Double[] currVals = extractCurrentFORs(models);
        List l = Arrays.asList(currVals);
        Collections.rotate(l, amt);
        return currVals;
    }

    public static void reflectPositionallyModels(EditableParameterModel[] models) throws ParameterException {
        applyFORsToModels(models, getPositionallyReflectedFORs(models));
    }

    public static void reverseModels(EditableParameterModel[] models) throws ParameterException {
        applyFORsToModels(models, getReveresedFORs(models));
    }

    public static void applyFORsToModels(EditableParameterModel[] models, Double[] FORs) throws ParameterException {
        for (int i = 0; i < models.length; i++)
            models[i].setValue(calcFOR(models[i], FORs[i].doubleValue()));
    }

    public static void applyFORToModel(EditableParameterModel model, Double FOR) throws ParameterException {
        model.setValue(calcFOR(model, FOR.doubleValue()));
    }

    public static void rotateModels(EditableParameterModel[] models, int amt) throws ParameterException {
        applyFORsToModels(models, getRotatedFORs(models, amt));
    }

    // setting expand to true will allow offset wheeling ( as opposed to absolute) for groups and collections
    public static void wheelParameterModels(final int rotation, final Object[] selObjs, boolean expand) {
        final EditableParameterModel[] models = extractEditableParameterModels(selObjs, expand);
        offsetModels(models, -rotation);
    }

    public static Integer calcIntegerOffset(GeneralParameterDescriptor pd, final Double offsetAsFOR) {
        return IntPool.get((int) Math.round((pd.getMaxValue().intValue() - pd.getMinValue().intValue()) * offsetAsFOR.doubleValue()));
    }
    public static void offsetModels(final EditableParameterModel[] models, int offset) {
        for (int i = 0; i < models.length; i++)
            try {
                models[i].offsetValue(IntPool.get(offset));
            } catch (ParameterException e) {
                e.printStackTrace();
            }
    }

    public static void offsetModels(final EditableParameterModel[] models, double FOR) {
        for (int i = 0; i < models.length; i++)
            try {
                models[i].offsetValue(new Double(FOR));
            } catch (ParameterException e) {
                e.printStackTrace();
            }
    }

    public static EditableParameterModelGroup[] getEditableParameterModelGroups(EditableParameterModelProvider[] source, Integer[] ids) throws ParameterException {
        EditableParameterModelGroup[] grps = new EditableParameterModelGroup[ids.length];
        EditableParameterModel[] models = null;
        try {
            for (int i = 0, j = ids.length; i < j; i++) {
                models = new EditableParameterModel[source.length];
                try {
                    for (int k = 0, l = source.length; k < l; k++)
                        models[k] = source[k].getEditableParameterModel(ids[i]);
                } catch (ParameterException e) {
                    ZUtilities.zDisposeCollection(Arrays.asList(models));
                    throw e;
                }
                grps[i] = new EditableParameterModelGroup(models);
            }
        } catch (IllegalParameterIdException e) {
            ZUtilities.zDisposeCollection(Arrays.asList(grps));
            throw e;
        }
        return grps;
    }

    public static EditableParameterModelGroup[] getEditableParameterModelGroups(EditableParameterModelProvider[] source, int startId, int count) throws ParameterException {
        EditableParameterModelGroup[] grps = new EditableParameterModelGroup[count];
        EditableParameterModel[] models = null;
        try {
            for (int i = 0, j = count; i < j; i++) {
                models = new EditableParameterModel[source.length];
                try {
                    for (int k = 0, l = source.length; k < l; k++)
                        models[k] = source[k].getEditableParameterModel(IntPool.get(startId + i));
                } catch (ParameterException e) {
                    ZUtilities.zDisposeCollection(Arrays.asList(models));
                    throw e;
                }
                grps[i] = new EditableParameterModelGroup(models);
            }
        } catch (IllegalParameterIdException e) {
            ZUtilities.zDisposeCollection(Arrays.asList(grps));
            throw e;
        }
        return grps;
    }

// TABLE RELATED EDITING STUFF
    public static volatile boolean syncModelEditingToUI = false;
    // private static final Object mouseWheelMonitor = new Object();

    private static final MouseWheelListener mwl = new MouseWheelListener() {
        public void mouseWheelMoved(final MouseWheelEvent e) {
            if (e.getSource() instanceof EditingTable)
                doWheelMoved((EditingTable) e.getSource(), e);
        }
    };

    private static Object[] getWheelTargets(EditingTable t, MouseWheelEvent e) {
        Object[] selObjs = t.getSelObjects();
        if (selObjs.length == 0) {
            Object o = t.getValueAt(t.rowAtPoint(e.getPoint()), t.columnAtPoint(e.getPoint()));
            if (o != null)
                return new Object[]{o};
        }
        return selObjs;
    }

    private static void doWheelMoved(final EditingTable t, final MouseWheelEvent e) {
        taskWheelMoved(getWheelTargets(t, e), e.getWheelRotation());
    }

    private static void doWheelMoved(EditingTable t, final int rotation) {
        taskWheelMoved(t.getSelObjects(), rotation);
    }

    private static void taskWheelMoved(final Object[] objs, final int rotation) {
        if (rotation == 0 || objs.length == 0)
            return;
        ParameterModelUtilities.wheelParameterModels(rotation, objs, false);
    }

    private static final KeyStroke valueUp = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, KeyEvent.CTRL_MASK);
    private static final KeyStroke valueDown = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, KeyEvent.CTRL_MASK);
    private static final KeyStroke valueMax = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);
    private static final KeyStroke valueMin = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);
    private static final KeyStroke valueJumpUp = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, KeyEvent.CTRL_MASK);
    private static final KeyStroke valueJumpDown = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, KeyEvent.CTRL_MASK);
    private static final KeyStroke valueDefault = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);
    private static final KeyStroke valueRepeatFirst = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);

    private static final Action aValueUp = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            try {
                if (e.getSource() instanceof EditingTable)
                    doWheelMoved((EditingTable) e.getSource(), -1);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    };

    private static final Action aValueDown = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            try {
                if (e.getSource() instanceof EditingTable)
                    doWheelMoved((EditingTable) e.getSource(), 1);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    };

    private static final Action aValueMax = new AbstractAction() {
        public void actionPerformed(final ActionEvent e) {
            try {
                if (e.getSource() instanceof EditingTable)
                    ParameterModelUtilities.maximizeParameterModels(((EditingTable) e.getSource()).getSelObjects());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    };

    private static final Action aValueMin = new AbstractAction() {
        public void actionPerformed(final ActionEvent e) {
            try {
                if (e.getSource() instanceof EditingTable)
                    ParameterModelUtilities.minimizeParameterModels(((EditingTable) e.getSource()).getSelObjects());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    };

    private static final double valueJumpFactor = 0.05;

    public static final void jumpUp(final EditableParameterModel[] models) {
        offsetModels(models, valueJumpFactor);
    }

    public static final void jumpDown(final EditableParameterModel[] models) {
        offsetModels(models, -valueJumpFactor);
    }

    private static final Action aValueJumpUp = new AbstractAction() {
        public void actionPerformed(final ActionEvent e) {
            if (e.getSource() instanceof EditingTable)
                jumpUp(extractEditableParameterModels(((EditingTable) e.getSource()).getSelObjects(), false));
        }
    };

    private static final Action aValueJumpDown = new AbstractAction() {
        public void actionPerformed(final ActionEvent e) {
            if (e.getSource() instanceof EditingTable)
                jumpDown(extractEditableParameterModels(((EditingTable) e.getSource()).getSelObjects(), false));
        }
    };

    private static final Action aValueDefault = new AbstractAction() {
        public void actionPerformed(final ActionEvent e) {
            try {
                if (e.getSource() instanceof EditingTable)
                    ParameterModelUtilities.defaultParameterModels(((EditingTable) e.getSource()).getSelObjects());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    };

    private static final Action aValueRepeatFirst = new AbstractAction() {
        public void actionPerformed(final ActionEvent e) {
            try {
                if (e.getSource() instanceof EditingTable)
                    ParameterModelUtilities.repeatParameterModels(((EditingTable) e.getSource()).getSelObjects());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    };

    public static void registerTableForEditableParameterModelShortcuts(EditingTable t) {
        t.registerKeyboardAction(aValueUp, "valueUp", valueUp, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        t.registerKeyboardAction(aValueDown, "valueDown", valueDown, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        t.registerKeyboardAction(aValueMax, "valueMax", valueMax, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        t.registerKeyboardAction(aValueMin, "valueMin", valueMin, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        t.registerKeyboardAction(aValueJumpUp, "valueJumpUp", valueJumpUp, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        t.registerKeyboardAction(aValueJumpDown, "valueJumpDown", valueJumpDown, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        t.registerKeyboardAction(aValueDefault, "valueDefault", valueDefault, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        t.registerKeyboardAction(aValueRepeatFirst, "valueRepeatFirst", valueRepeatFirst, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        t.addMouseWheelListener(mwl);
    }
}
