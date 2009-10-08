package com.pcmsolutions.device.EMU.E4.gui;

import com.pcmsolutions.device.EMU.E4.gui.table.EditingTable;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.gui.MouseWheelBehaviour;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.threads.ZDBModifyThread;

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

    public static boolean areAllOfValue(ReadableParameterModel[] models, int value) throws ParameterUnavailableException {
        for (int i = 0; i < models.length; i++)
            if (models[i].getValue().intValue() != value)
                return false;
        return true;
    }

    public static Set getIdSet(ReadableParameterModel[] models) {
        Set s = new HashSet();
        for (int i = 0; i < models.length; i++)
            s.add(models[i].getParameterDescriptor().getId());
        return s;
    }

    public static Set getValueSet(ReadableParameterModel[] models) throws ParameterUnavailableException {
        Set s = new HashSet();
        for (int i = 0; i < models.length; i++)
            s.add(models[i].getValue());
        return s;
    }

    /*public static ReadableParameterModel[] extractReadableParameterModels(Object[] objs) {
       return extractReadableParameterModels(objs, true);
   }*/

    public static EditableParameterModel[] extractEditableParameterModels(Object[] objs, boolean expandWrapped) {
        ArrayList epm = new ArrayList();
        for (int i = 0; i < objs.length; i++)
            if (expandWrapped && objs[i] instanceof ParameterModelWrapper)
            //if (objs[i] instanceof ParameterModelWrapper && ClassUtility.areAllInstanceOf(((ParameterModelWrapper) objs[i]).getWrappedObjects(), EditableParameterModel.class))
                epm.addAll(Arrays.asList(extractEditableParameterModels(((ParameterModelWrapper) objs[i]).getWrappedObjects(), expandWrapped)));
            else if (objs[i] instanceof EditableParameterModel)
                epm.add(objs[i]);

        return (EditableParameterModel[]) epm.toArray(new EditableParameterModel[epm.size()]);
    }

    // returns list of non-zero length lists
    public static List extractEditableParameterModelChainGroups(Object[] objs) {
        ArrayList grps = new ArrayList();
        EditableParameterModel[] models = extractEditableParameterModels(objs);
        Object o;
        if (models.length > 0)
            for (int i = 0; i < models.length; i++) {
                boolean matched = false;
                for (int j = 0; j < grps.size(); j++) {
                    ArrayList l = (ArrayList) grps.get(j);
                    o = l.get(0);
                    if (models[i].isEditChainableWith(o) && (o instanceof EditableParameterModel && ((EditableParameterModel) o).isEditChainableWith(models[i]))) {
                        l.add(models[i]);
                        matched = true;
                        break;
                    }
                }
                if (!matched)
                    grps.add(new ArrayList(Arrays.asList(new EditableParameterModel[]{models[i]})));
            }
        return grps;
    }

    // grps is a list of non-zero length lists
    public static void dispatchEditChainGroups(List grps, EditableParameterModel.EditChainValueProvider ecvp) {
        for (int i = 0; i < grps.size(); i++) {
            List gl = (List) grps.get(i);
            EditableParameterModel cm;
            try {
                cm = (EditableParameterModel) gl.get(0);
                if (gl.size() == 1)
                    cm.setValue(ecvp.getValue(cm, cm));
                else
                    cm.setValue(ecvp, (EditableParameterModel[]) gl/*.subList(1,gl.size())*/.toArray(new EditableParameterModel[gl.size()/*-1*/]));
            } catch (ParameterUnavailableException e) {
            } catch (ParameterValueOutOfRangeException e) {
            }
        }
    }

    public static Integer[] getValuesForStringValues(String[] strVal, ReadableParameterModel[] models) throws ParameterValueOutOfRangeException {
        Integer[] vals = new Integer[models.length];
        for (int i = 0; i < models.length; i++)
            vals[i] = models[i].getParameterDescriptor().getValueForString(strVal[i]);
        return vals;
    }

    public static Integer[] getValuesForUnitlessStringValues(String[] strVal, ReadableParameterModel[] models) throws ParameterValueOutOfRangeException {
        Integer[] vals = new Integer[models.length];
        for (int i = 0; i < models.length; i++)
            vals[i] = models[i].getParameterDescriptor().getValueForUnitlessString(strVal[i]);
        return vals;
    }

    public static Integer[] extractCurrentValues(ReadableParameterModel[] models) throws ParameterUnavailableException {
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


// get factor of range
    public static double getFOR(ReadableParameterModel p, Integer value) {
        return ((double) (value.intValue() - p.getParameterDescriptor().getMinValue().intValue())) / (p.getParameterDescriptor().getMaxValue().intValue() - p.getParameterDescriptor().getMinValue().intValue());
    }

// get factor of range
    public static double getFOR(ReadableParameterModel p) throws ParameterUnavailableException {
        return ((double) (p.getValue().intValue() - p.getParameterDescriptor().getMinValue().intValue())) / (p.getParameterDescriptor().getMaxValue().intValue() - p.getParameterDescriptor().getMinValue().intValue());
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
        } catch (ParameterUnavailableException e) {
        } catch (ParameterValueOutOfRangeException e) {
        }
    }

    public static void repeatParameterModels(final Object[] objs) {
        dispatchEditChainGroups(extractEditableParameterModelChainGroups(objs), new EditableParameterModel.EditChainValueProvider() {
            double repeatFOR = Double.MIN_VALUE;

            public Integer getValue(EditableParameterModel model, EditableParameterModel leadModel) {
                if (repeatFOR == Double.MIN_VALUE)
                    try {
                        repeatFOR = getFOR(model);
                    } catch (ParameterUnavailableException e) {
                    }
                return calcFOR(model, repeatFOR);
            }
        });
    }

    public static void offsetParameterModels(final Object[] objs, final double factorOfRange) {
        dispatchEditChainGroups(extractEditableParameterModelChainGroups(objs), new EditableParameterModel.EditChainValueProvider() {
            double repeatFOR = Double.MIN_VALUE;

            public Integer getValue(EditableParameterModel model, EditableParameterModel leadModel) {
                GeneralParameterDescriptor gpd = model.getParameterDescriptor();
                int range = gpd.getMaxValue().intValue() - gpd.getMinValue().intValue();
                int offset = (int) Math.round(range * factorOfRange);
                try {
                    return gpd.constrainValue(IntPool.get(model.getValue().intValue() + offset));
                } catch (ParameterUnavailableException e) {
                }
                return null;
            }
        });
    }

    public static void minimizeParameterModels(final Object[] objs) {
        dispatchEditChainGroups(extractEditableParameterModelChainGroups(objs), new EditableParameterModel.EditChainValueProvider() {
            public Integer getValue(EditableParameterModel model, EditableParameterModel leadModel) {
                return model.getParameterDescriptor().getMinValue();
            }
        });
    }

    public static void maximizeParameterModels(final Object[] objs) {
        dispatchEditChainGroups(extractEditableParameterModelChainGroups(objs), new EditableParameterModel.EditChainValueProvider() {
            public Integer getValue(EditableParameterModel model, EditableParameterModel leadModel) {
                return model.getParameterDescriptor().getMaxValue();
            }
        });
    }

    public static void defaultParameterModels(final Object[] objs) {
        dispatchEditChainGroups(extractEditableParameterModelChainGroups(objs), new EditableParameterModel.EditChainValueProvider() {
            public Integer getValue(EditableParameterModel model, EditableParameterModel leadModel) {
                return model.getParameterDescriptor().getDefaultValue();
            }
        });
    }

    public static void minimizeParameterModel(EditableParameterModel pm) {
        minimizeParameterModels(new EditableParameterModel[]{pm});
    }

    public static void maximizeParameterModel(EditableParameterModel pm) {
        maximizeParameterModels(new EditableParameterModel[]{pm});
    }

    public static void wheelParameterModels(final int rotation, final Object[] selObjs) {
        dispatchEditChainGroups(extractEditableParameterModelChainGroups(selObjs), new EditableParameterModel.EditChainValueProvider() {
            public Integer getValue(EditableParameterModel model, EditableParameterModel leadModel) throws ParameterUnavailableException {
                int tr = rotation;
                Integer nv = (leadModel != null ? leadModel.getValue() : model.getValue());
                Integer fv = nv;
                if (tr < 0)
                    while (tr++ != 0) {
                        nv = (MouseWheelBehaviour.upPolarity ? model.getParameterDescriptor().getNextValue(nv) : model.getParameterDescriptor().getPreviousValue(nv));
                        if (nv == null)
                            break;
                        else
                            fv = nv;
                    }
                else
                    while (tr-- != 0) {
                        nv = (MouseWheelBehaviour.upPolarity ? model.getParameterDescriptor().getPreviousValue(nv) : model.getParameterDescriptor().getNextValue(nv));
                        if (nv == null)
                            break;
                        else
                            fv = nv;
                    }
                return fv;
            }
        });
    }

    public static EditableParameterModelGroup[] getEditableParameterModelGroups(EditableParameterModelProvider[] source, Integer[] ids) throws IllegalParameterIdException {
        EditableParameterModelGroup[] grps = new EditableParameterModelGroup[ids.length];
        EditableParameterModel[] models = null;
        try {
            for (int i = 0,j = ids.length; i < j; i++) {
                models = new EditableParameterModel[source.length];
                try {
                    for (int k = 0,l = source.length; k < l; k++)
                        models[k] = source[k].getEditableParameterModel(ids[i]);
                } catch (IllegalParameterIdException e) {
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

    public static EditableParameterModelGroup[] getEditableParameterModelGroups(EditableParameterModelProvider[] source, int startId, int count) throws IllegalParameterIdException {
        EditableParameterModelGroup[] grps = new EditableParameterModelGroup[count];
        EditableParameterModel[] models = null;
        try {
            for (int i = 0,j = count; i < j; i++) {
                models = new EditableParameterModel[source.length];
                try {
                    for (int k = 0,l = source.length; k < l; k++)
                        models[k] = source[k].getEditableParameterModel(IntPool.get(startId + i));
                } catch (IllegalParameterIdException e) {
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
    public static volatile boolean syncModelEditingToUI = true;
    private static final Object mouseWheelMonitor = new Object();

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
        if (ParameterModelUtilities.syncModelEditingToUI)
            ParameterModelUtilities.wheelParameterModels(rotation, objs);
        else
            new ZDBModifyThread("Wheel moved") {
                public void run() {
                    synchronized (mouseWheelMonitor) {
                        ParameterModelUtilities.wheelParameterModels(rotation, objs);
                    }
                }
            }.start();
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
        public void actionPerformed(ActionEvent e) {
            try {
                if (e.getSource() instanceof EditingTable)
                    ParameterModelUtilities.maximizeParameterModels(((EditingTable) e.getSource()).getSelObjects());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    };

    private static final Action aValueMin = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            try {
                if (e.getSource() instanceof EditingTable)
                    ParameterModelUtilities.minimizeParameterModels(((EditingTable) e.getSource()).getSelObjects());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    };

    private static final double valueJumpFactor = 0.05;
    private static final Action aValueJumpUp = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            try {
                if (e.getSource() instanceof EditingTable)
                    ParameterModelUtilities.offsetParameterModels(((EditingTable) e.getSource()).getSelObjects(), valueJumpFactor);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    };
    private static final Action aValueJumpDown = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            try {
                if (e.getSource() instanceof EditingTable)
                    ParameterModelUtilities.offsetParameterModels(((EditingTable) e.getSource()).getSelObjects(), -valueJumpFactor);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    };

    private static final Action aValueDefault = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            try {
                if (e.getSource() instanceof EditingTable)
                    ParameterModelUtilities.defaultParameterModels(((EditingTable) e.getSource()).getSelObjects());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    };
    private static final Action aValueRepeatFirst = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
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
