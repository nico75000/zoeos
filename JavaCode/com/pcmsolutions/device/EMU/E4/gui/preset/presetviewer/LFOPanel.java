package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTablePanel;
import com.pcmsolutions.device.EMU.E4.parameter.*;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.system.ZDeviceNotRunningException;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.threads.ZDBModifyThread;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 04-Jul-2003
 * Time: 21:45:43
 * To change this template use Options | File Templates.
 */
public class LFOPanel extends JPanel implements ChangeListener, ZDisposable {
    protected LFOShapePanel lfo1ShapePanel;
    protected LFOShapePanel lfo2ShapePanel;
    protected List lfo1Ids;
    protected List lfo2Ids;
    protected ReadableParameterModel[] lfo1Models;
    protected ReadableParameterModel[] lfo2Models;

    public LFOPanel init(final ReadablePreset.ReadableVoice voice) throws ZDeviceNotRunningException, IllegalParameterIdException {
        //this.setLayout(new TopAligningFlowLayout(FlowLayout.RIGHT));
        lfo1Ids = voice.getPreset().getDeviceParameterContext().getVoiceContext().getIdsForCategory(ParameterCategories.VOICE_LFO1);
        lfo2Ids = voice.getPreset().getDeviceParameterContext().getVoiceContext().getIdsForCategory(ParameterCategories.VOICE_LFO2);

        lfo1Models = new ReadableParameterModel[lfo1Ids.size()];
        lfo2Models = new ReadableParameterModel[lfo2Ids.size()];

        try {
            for (int i = 0, j = lfo1Ids.size(); i < j; i++)
                lfo1Models[i] = voice.getParameterModel((Integer) lfo1Ids.get(i));
            for (int i = 0, j = lfo2Ids.size(); i < j; i++)
                lfo2Models[i] = voice.getParameterModel((Integer) lfo2Ids.get(i));
        } catch (IllegalParameterIdException e) {
            ZUtilities.zDisposeCollection(Arrays.asList(lfo1Models));
            ZUtilities.zDisposeCollection(Arrays.asList(lfo2Models));
            throw e;
        }

        Action r1t = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                new ZDBModifyThread("Refresh LFO 1") {
                    public void run() {
                        try {
                            voice.getPreset().refreshVoiceParameters(voice.getVoiceNumber(), (Integer[]) lfo1Ids.toArray(new Integer[lfo1Ids.size()]));
                        } catch (NoSuchContextException e1) {
                            e1.printStackTrace();
                        } catch (PresetEmptyException e1) {
                            e1.printStackTrace();
                        } catch (NoSuchPresetException e1) {
                            e1.printStackTrace();
                        } catch (NoSuchVoiceException e1) {
                            e1.printStackTrace();
                        } catch (ParameterValueOutOfRangeException e1) {
                            e1.printStackTrace();
                        } catch (IllegalParameterIdException e1) {
                            e1.printStackTrace();
                        }
                    }
                }.start();
            }
        };
        r1t.putValue("tip", "Refresh LFO 1");
        Action r2t = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                new ZDBModifyThread("Refresh LFO 2") {
                    public void run() {
                        try {
                            voice.getPreset().refreshVoiceParameters(voice.getVoiceNumber(), (Integer[]) lfo2Ids.toArray(new Integer[lfo2Ids.size()]));
                        } catch (NoSuchContextException e1) {
                            e1.printStackTrace();
                        } catch (PresetEmptyException e1) {
                            e1.printStackTrace();
                        } catch (NoSuchPresetException e1) {
                            e1.printStackTrace();
                        } catch (NoSuchVoiceException e1) {
                            e1.printStackTrace();
                        } catch (ParameterValueOutOfRangeException e1) {
                            e1.printStackTrace();
                        } catch (IllegalParameterIdException e1) {
                            e1.printStackTrace();
                        }

                    }
                }.start();
            }
        };
        r2t.putValue("tip", "Refresh LFO 2");
        RowHeaderedAndSectionedTablePanel lfo1Panel;
        RowHeaderedAndSectionedTablePanel lfo2Panel;
        lfo1Panel = new RowHeaderedAndSectionedTablePanel().init(new VoiceParameterTable(voice, ParameterCategories.VOICE_LFO1, lfo1Models, "LFO 1"), "Show LFO 1", UIColors.getTableBorder(), r1t);
        lfo2Panel = new RowHeaderedAndSectionedTablePanel().init(new VoiceParameterTable(voice, ParameterCategories.VOICE_LFO2, lfo2Models, "LFO 2"), "Show LFO 2", UIColors.getTableBorder(), r2t);

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
        } catch (ParameterUnavailableException e1) {
            e1.printStackTrace();
        }
    }

    public Color getBackground() {
        return UIColors.getDefaultBG();
    }

    public void zDispose() {
        //lfo1Models[1].removeChangeListener(this);
        //lfo2Models[1].removeChangeListener(this);
        ZUtilities.zDisposeCollection(Arrays.asList(lfo1Models));
        ZUtilities.zDisposeCollection(Arrays.asList(lfo2Models));
        lfo1Models = null;
        lfo2Models = null;
        lfo1ShapePanel = null;
        lfo2ShapePanel = null;
        lfo1Ids.clear();
        lfo1Ids = null;
        lfo2Ids.clear();
        lfo2Ids = null;
    }
}
