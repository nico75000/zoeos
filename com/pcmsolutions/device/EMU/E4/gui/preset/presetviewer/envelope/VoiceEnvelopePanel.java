package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.envelope;

import com.pcmsolutions.device.EMU.E4.gui.TitleProvider;
import com.pcmsolutions.device.EMU.E4.gui.TitleProviderListener;
import com.pcmsolutions.device.EMU.E4.gui.TitleProviderListenerHelper;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.table.RowHeaderedAndSectionedTablePanel;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterValueOutOfRangeException;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;
import com.pcmsolutions.device.EMU.E4.preset.*;
import com.pcmsolutions.gui.FuzzyLineBorder;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.ZUtilities;
import com.pcmsolutions.system.preferences.ZBoolPref;
import com.pcmsolutions.system.preferences.ZEnumPref;
import com.pcmsolutions.system.threads.ZDBModifyThread;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

public class VoiceEnvelopePanel extends JPanel implements TitleProvider, ZDisposable {
    // protected Vector listeners = new Vector();
    protected RatesEnvelope envelope;
    protected RatesEnvelopeModel envModel;
    protected ReadablePreset.ReadableVoice voice;
    protected VoiceEnvelopeTableModel envTableModel;
    protected VoiceEnvelopeTable envTable;
    protected String title;

    public VoiceEnvelopePanel init(ReadablePreset.ReadableVoice voice, String category, Integer startId, String title, Action toggleAction) throws IllegalParameterIdException {
        ReadableParameterModel[] models = new ReadableParameterModel[12];
        generateParameterModels(voice, startId, models);
        init(voice, category, models, title, toggleAction);
        this.setFocusable(false);
        return this;
    }

    protected void generateParameterModels(ReadablePreset.ReadableVoice voice, Integer startId, ReadableParameterModel[] models) throws IllegalParameterIdException {
        for (int i = 0; i < 12; i++)
            try {
                models[i] = voice.getParameterModel(IntPool.get(startId.intValue() + i));
            } catch (IllegalParameterIdException e) {
                ZUtilities.zDisposeCollection(Arrays.asList(models));
                throw e;
            }
    }

    protected VoiceEnvelopePanel init(final ReadablePreset.ReadableVoice voice, String category, ReadableParameterModel[] models, String title, Action toggleAction) {
        this.voice = voice;
        //this.setLayout(new GridLayout(1, 2));
        this.setLayout(new FlowLayout(FlowLayout.LEADING));
        envModel = new VoiceEnvelopeModel(voice, models[0].getParameterDescriptor().getId());
        envelope = new RatesEnvelope(envModel);
        makeModelAndTable(category, models, title);
        RowHeaderedAndSectionedTablePanel etp;
        final Integer[] ids = new Integer[models.length];

        for (int i = 0,j = models.length; i < j; i++)
            ids[i] = models[i].getParameterDescriptor().getId();

        Action ra = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                new ZDBModifyThread("Refresh Envelope") {
                    public void run() {
                        try {
                            voice.getPreset().refreshVoiceParameters(voice.getVoiceNumber(), ids);
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
        ra.putValue("tip", "Refresh " + title);
        etp = new RowHeaderedAndSectionedTablePanel().init(envTable, "SHOW " + title, UIColors.getTableBorder(), ra);

        etp.getHideButton().setAction(toggleAction);
        etp.getHideButton().setToolTipText("Toggle Envelope Mode");

        FuzzyLineBorder flb = new FuzzyLineBorder(UIColors.getTableBorder(),UIColors.getTableBorderWidth(), true);
        flb.setFadingIn(!flb.isFadingIn());
        envelope.setBorder(new TitledBorder(flb, title, TitledBorder.LEFT, TitledBorder.ABOVE_TOP));

        //env.setBorder(new TitledBorder(UIColors.makeFuzzyBorder(UIColors.getTableBorder(), RowHeaderedAndSectionedTablePanel.getBorderWidth()), title, TitledBorder.LEFT, TitledBorder.ABOVE_TOP));

        envelope.setPreferredSize(new Dimension((int) (etp.getPreferredSize().getWidth() * 0.7), (int) (etp.getPreferredSize().getHeight() * 1.2)));

        add(etp);
        add(envelope);

        return this;
    }

    public Color getBackground() {
        return UIColors.getDefaultBG();
    }

    public Color getForeground() {
        return UIColors.getDefaultFG();
    }

    protected void makeModelAndTable(String category, ReadableParameterModel[] models, String title) {
        envTableModel = new VoiceEnvelopeTableModel(models);
        envTable = new VoiceEnvelopeTable(voice, category, envTableModel, title);
        envTable.setHidingSelectionOnFocusLost(true);
    }

    public String getTitle() {
        return title;
    }

    public String getReducedTitle() {
        return title;
    }

    protected TitleProviderListenerHelper tplh = new TitleProviderListenerHelper(this);

    public void addTitleProviderListener(TitleProviderListener tpl) {
        tplh.addTitleProviderListener(tpl);
    }

    public void removeTitleProviderListener(TitleProviderListener tpl) {
        tplh.removeTitleProviderListener(tpl);
    }

    public Icon getIcon() {
        return null;
    }

    public RatesEnvelope getEnvelope() {
        return envelope;
    }

    public RatesEnvelopeModel getEnvModel() {
        return envModel;
    }

    public void zDispose() {
        //listeners.clear();
        envModel.zDispose();
        envelope.zDispose();
        //envTableModel.zDispose();
        envTable.zDispose();
        //listeners = null;
        envModel = null;
        envelope = null;
        envTableModel = null;
        envTable = null;
        voice = null;
    }
}
