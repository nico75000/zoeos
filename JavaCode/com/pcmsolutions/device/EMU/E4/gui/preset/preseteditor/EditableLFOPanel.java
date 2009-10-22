package com.pcmsolutions.device.EMU.E4.gui.preset.preseteditor;

import com.pcmsolutions.device.EMU.E4.gui.ParameterModelUtilities;
import com.pcmsolutions.device.EMU.E4.gui.TableExclusiveSelectionContext;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.LFOShapePanel;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTablePanel;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.device.EMU.database.NoSuchContextException;
import com.pcmsolutions.device.EMU.database.EmptyException;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.ZUtilities;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 04-Jul-2003
 * Time: 21:45:43
 * To change this template use Options | File Templates.
 */
public class EditableLFOPanel extends JPanel implements ChangeListener, ZDisposable, MouseWheelListener {
    protected LFOShapePanel lfo1ShapePanel;
    protected LFOShapePanel lfo2ShapePanel;
    protected List lfo1Ids;
    protected List lfo2Ids;
    protected EditableParameterModel[] lfo1Models;
    protected EditableParameterModel[] lfo2Models;

    protected int shapeModelIndex1;
    protected int shapeModelIndex2;

    public EditableLFOPanel init(final ContextEditablePreset.EditableVoice[] voices, TableExclusiveSelectionContext tsc) throws ParameterException, DeviceException {
        this.setFocusable(false);
        if (voices == null || voices.length < 1)
            throw new IllegalArgumentException("Need at least one voice for am EditableLFOPanel");

        //this.setLayout(new TopAligningFlowLayout(FlowLayout.RIGHT));
        lfo1Ids = voices[0].getPreset().getDeviceParameterContext().getVoiceContext().getIdsForCategory(ParameterCategories.VOICE_LFO1);
        lfo2Ids = voices[0].getPreset().getDeviceParameterContext().getVoiceContext().getIdsForCategory(ParameterCategories.VOICE_LFO2);

        lfo1Models = ParameterModelUtilities.getEditableParameterModelGroups(voices, (Integer[]) lfo1Ids.toArray(new Integer[lfo1Ids.size()]));
        lfo2Models = ParameterModelUtilities.getEditableParameterModelGroups(voices, (Integer[]) lfo2Ids.toArray(new Integer[lfo2Ids.size()]));
        shapeModelIndex1 = ParameterModelUtilities.indexOfId(lfo1Models, ID.lfo1Shape);
        shapeModelIndex2 = ParameterModelUtilities.indexOfId(lfo2Models, ID.lfo2Shape);

        Action r1t = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                        try {
                            voices[0].getPreset().refreshVoiceParameters(voices[0].getVoiceNumber(), (Integer[]) lfo1Ids.toArray(new Integer[lfo1Ids.size()]));
                        } catch (PresetException e1) {
                            e1.printStackTrace();
                        }
            }
        };
        r1t.putValue("tip", "Refresh LFO 1");
        Action r2t = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                        try {
                            voices[0].getPreset().refreshVoiceParameters(voices[0].getVoiceNumber(), (Integer[]) lfo2Ids.toArray(new Integer[lfo2Ids.size()]));
                        } catch (PresetException e1) {
                            e1.printStackTrace();
                        }
            }
        };
        r2t.putValue("tip", "Refresh LFO 2");
        RowHeaderedAndSectionedTablePanel lfo1Panel;
        RowHeaderedAndSectionedTablePanel lfo2Panel;

        EditableVoiceParameterTable evpt;

        evpt=new EditableVoiceParameterTable(voices, ParameterCategories.VOICE_LFO1, lfo1Models, "LFO 1");
        tsc.addTableToContext(evpt);
        lfo1Panel = new RowHeaderedAndSectionedTablePanel().init(evpt, "Show LFO 1", UIColors.getTableBorder(), r1t);

        evpt =new EditableVoiceParameterTable(voices, ParameterCategories.VOICE_LFO2, lfo2Models, "LFO 2");
        tsc.addTableToContext(evpt);
        lfo2Panel = new RowHeaderedAndSectionedTablePanel().init(evpt, "Show LFO 2", UIColors.getTableBorder(), r2t);

        lfo1ShapePanel = new LFOShapePanel("LFO 1") {
            public Color getBackground() {
                return UIColors.getDefaultBG();
            }
        };
        lfo2ShapePanel = new LFOShapePanel("LFO 2") {
            public Color getBackground() {
                return UIColors.getDefaultBG();
            }
        };
        lfo1ShapePanel.addMouseWheelListener(this);
        lfo2ShapePanel.addMouseWheelListener(this);

        lfo1Models[1].addChangeListener(this);
        lfo2Models[1].addChangeListener(this);

        this.add(lfo1Panel);
        Box b = new Box(BoxLayout.Y_AXIS) {
            public Color getBackground() {
                return UIColors.getDefaultBG();
            }

        };
        b.add(lfo1ShapePanel);
        b.add(lfo2ShapePanel);
        this.add(b);
        this.add(lfo2Panel);

        updateLfoShapePanel(lfo1ShapePanel, lfo1Models[1]);
        updateLfoShapePanel(lfo2ShapePanel, lfo2Models[1]);

        return this;
    }

    public void stateChanged(ChangeEvent e) {
        if (((ReadableParameterModel) e.getSource()).getParameterDescriptor().getId().equals(lfo1Ids.get(1)))
            updateLfoShapePanel(lfo1ShapePanel, (ReadableParameterModel) e.getSource());
        else if (((ReadableParameterModel) e.getSource()).getParameterDescriptor().getId().equals(lfo2Ids.get(1)))
            updateLfoShapePanel(lfo2ShapePanel, (ReadableParameterModel) e.getSource());
    }

    protected void updateLfoShapePanel(LFOShapePanel p, ReadableParameterModel pm) {
        try {
            p.setMode(pm.getValue().intValue());
        } catch (ParameterException e) {
        }
    }

    public Color getBackground() {
        return UIColors.getDefaultBG();
    }

    public void zDispose() {
        ZUtilities.zDisposeCollection(Arrays.asList(lfo1Models));
        ZUtilities.zDisposeCollection(Arrays.asList(lfo2Models));
        lfo1ShapePanel.removeMouseWheelListener(this);
        lfo2ShapePanel.removeMouseWheelListener(this);
        lfo1Ids.clear();
        lfo2Ids.clear();

        lfo1Models = null;
        lfo2Models = null;
        lfo1ShapePanel = null;
        lfo2ShapePanel = null;
        lfo1Ids = null;
        lfo2Ids = null;
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        int su = e.getWheelRotation();
        if (e.getSource() == lfo1ShapePanel) {
            if (shapeModelIndex1 != -1)
                ParameterModelUtilities.wheelParameterModels(su, new Object[]{lfo1Models[shapeModelIndex1]}, false);
        } else if (e.getSource() == lfo2ShapePanel)
            if (shapeModelIndex2 != -1)
                ParameterModelUtilities.wheelParameterModels(su, new Object[]{lfo2Models[shapeModelIndex2]}, false);
    }
}