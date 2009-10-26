package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.RemoteObjectStates;
import com.pcmsolutions.device.EMU.E4.events.*;
import com.pcmsolutions.device.EMU.E4.gui.TitleProvider;
import com.pcmsolutions.device.EMU.E4.gui.TitleProviderListener;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.preset.VoiceEditingIcon;
import com.pcmsolutions.device.EMU.E4.gui.preset.icons.PresetIcon;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.envelope.AmpEnvelopePanel;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.envelope.AuxEnvelopePanel;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.envelope.FilterEnvelopePanel;
import com.pcmsolutions.device.EMU.E4.gui.table.AbstractRowHeaderedAndSectionedTable;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;
import com.pcmsolutions.device.EMU.E4.preset.NoSuchPresetException;
import com.pcmsolutions.device.EMU.E4.preset.PresetListenerAdapter;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.gui.GriddedPanel;
import com.pcmsolutions.gui.ZCommandInvocationHelper;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZDeviceNotRunningException;
import com.pcmsolutions.system.ZDisposable;
import com.pcmsolutions.system.ZUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

public class VoicePanel extends GriddedPanel implements ZDisposable, TitleProvider, MouseListener {
    protected boolean showAmpEnv;
    protected boolean showFiltEnv;
    protected boolean showAuxEnv;
    protected boolean showLFO;
    protected boolean showTuning;
    protected boolean showAmp;
    protected boolean showFilt;
    protected boolean showCords;

    protected JPanel ampEnvPanel;
    protected JPanel filtEnvPanel;
    protected JPanel auxEnvPanel;
    protected JPanel lfoPanel;
    protected JPanel tuningPanel;
    protected JPanel ampPanel;
    protected JPanel filtPanel;
    protected JPanel cordsPanel;
    protected ArrayList generatedPanels = new ArrayList();

    protected ReadablePreset.ReadableVoice voice;

    protected Icon icon = null;

    protected int currx = 0;
    protected int curry = 0;

    protected PresetListenerAdapter pla;

    protected String title;

    protected VoiceTitleProvider vtp;

    public VoicePanel init(ReadablePreset.ReadableVoice voice) throws ZDeviceNotRunningException, IllegalParameterIdException {
        return init(voice, true, true, true, true, true, true, true, true);
    }

    public Color getBackground() {
        return UIColors.getDefaultBG();
    }

    public Color getForeground() {
        return UIColors.getDefaultFG();
    }

    private class VoicePanelFocusTraversalPolicy extends ContainerOrderFocusTraversalPolicy {
        /*    public Component getComponentAfter(Container focusCycleRoot,
                                               Component aComponent) {
                Component c = aComponent;
                 do{
                    c =super.getComponentAfter(focusCycleRoot, aComponent);
                 }while(c!= null && !(c instanceof AbstractRowHeaderedAndSectionedTable));
                return c;
            }
          */

        protected boolean accept(Component aComponent) {
            return super.accept(aComponent) & aComponent instanceof AbstractRowHeaderedAndSectionedTable;
        }

    }

    public VoicePanel init(ReadablePreset.ReadableVoice voice, boolean showAmpEnv, boolean showFiltEnv, boolean showAuxEnv, boolean showAmp, boolean showFilt, boolean showLFO, boolean showTuning, boolean showCords) throws ZDeviceNotRunningException, IllegalParameterIdException {
        this.voice = voice;
        this.showAmp = showAmp;
        this.showAmpEnv = showAmpEnv;
        this.showAuxEnv = showAuxEnv;
        this.showCords = showCords;
        this.showFilt = showFilt;
        this.showFiltEnv = showFiltEnv;
        this.showLFO = showLFO;
        this.showTuning = showTuning;

        //this.usingTabs = voice.getPreset().getPresetContext().getDeviceContext().getDevicePreferences().z;
        // this.groupEnvelopesWhenUsingTabs = voice.getPreset().getPresetContext().getDeviceContext().getDevicePreferences().getBoolean(PREF_groupEnvelopes, true);
        this.setFocusCycleRoot(true);

        generatePanels();

        if (voice.getPreset().getIcon() instanceof PresetIcon)
            icon = new VoiceEditingIcon((PresetIcon) voice.getPreset().getIcon());

        pla = new PresetListenerAdapter() {
            public void presetNameChanged(PresetNameChangeEvent ev) {
            }

            public void presetRefreshed(PresetRefreshEvent ev) {
                revalidate();
                repaint();
            }

            public void presetInitialized(PresetInitializeEvent ev) {
                revalidate();
                repaint();
            }

            public void presetInitializationStatusChanged(PresetInitializationStatusChangedEvent ev) {
            }

            public void voiceAdded(VoiceAddEvent ev) {
                if (handlingVoice(ev.getVoice(), ev.getNumberOfVoices())) {
                    revalidate();
                    repaint();
                }
            }

            public void voiceRemoved(VoiceRemoveEvent ev) {
                if (handlingVoice(ev.getVoice(), 1)) {
                    revalidate();
                    repaint();
                }
            }
        };
        vtp = makeVoiceTitleProvider();
        voice.getPreset().addPresetListener(pla);
        addMouseListener(this);
        return this;
    }

    protected VoiceTitleProvider makeVoiceTitleProvider() {
        VoiceTitleProvider vtp = new VoiceTitleProvider();
        vtp.init(voice);
        return vtp;
    }

    protected boolean handlingVoice(Integer lowVoice, int count) {
        Integer pvn = VoicePanel.this.voice.getVoiceNumber();
        if (lowVoice.intValue() >= pvn.intValue() && lowVoice.intValue() + count - 1 <= pvn.intValue())
            return true;

        return false;
    }

    private void checkEmpty() {
        try {
            if (!(voice.getPreset().getPresetState() == RemoteObjectStates.STATE_EMPTY)) {
                setEnabled(true);
                return;
            }
        } catch (NoSuchPresetException e) {
            e.printStackTrace();
        }
        setEnabled(false);
    }

    protected void generatePanels() throws IllegalParameterIdException, ZDeviceNotRunningException {
        JPanel lfoSection = new JPanel() {
            public Color getBackground() {
                return UIColors.getDefaultBG();
            }

            public Color getForeground() {
                return UIColors.getDefaultFG();
            }
        };
        lfoSection.setLayout(new BoxLayout(lfoSection, BoxLayout.Y_AXIS));
        JPanel tuningSection = new JPanel() {
            public Color getBackground() {
                return UIColors.getDefaultBG();
            }

            public Color getForeground() {
                return UIColors.getDefaultFG();
            }

        };
        tuningSection.setLayout(new BoxLayout(tuningSection, BoxLayout.Y_AXIS));

        try {
            if (showAmpEnv) {
                ampEnvPanel = getAmpEnvPanel();
                generatedPanels.add(ampEnvPanel);
                this.addAnchoredComponent(ampEnvPanel, 0, 0, 2, 1, GridBagConstraints.NORTHWEST);
            }

            if (showFiltEnv) {
                filtEnvPanel = getFiltEnvPanel();
                generatedPanels.add(filtEnvPanel);
                this.addAnchoredComponent(filtEnvPanel, 1, 0, 2, 1, GridBagConstraints.NORTHWEST);
            }
            if (showAuxEnv) {
                auxEnvPanel = getAuxEnvPanel();
                generatedPanels.add(auxEnvPanel);
                this.addAnchoredComponent(auxEnvPanel, 2, 0, 2, 1, GridBagConstraints.NORTHWEST);
            }
            if (showCords) {
                cordsPanel = getCordsPanel();
                generatedPanels.add(cordsPanel);
                this.addAnchoredComponent(cordsPanel, 0, 2, 1, 3, GridBagConstraints.EAST);
            }

            if (showFilt) {
                filtPanel = getFiltPanel();
                generatedPanels.add(filtPanel);
                this.addAnchoredComponent(filtPanel, 3, 0, GridBagConstraints.NORTHWEST);
            }

            if (showLFO) {
                lfoPanel = getLFOPanel();
                generatedPanels.add(lfoPanel);
                this.addAnchoredComponent(lfoPanel, 3, 1, 2, 1, GridBagConstraints.NORTHWEST);
            }

            if (showAmp) {
                ampPanel = getAmpPanel();
                generatedPanels.add(ampPanel);
                this.addAnchoredComponent(ampPanel, 4, 0, GridBagConstraints.NORTHWEST);
            }

            if (showTuning) {
                tuningPanel = getTuningPanel();
                generatedPanels.add(tuningPanel);
                this.addAnchoredComponent(tuningPanel, 4, 1, 2, 1, GridBagConstraints.NORTHWEST);
            }
        } catch (IllegalParameterIdException e) {
            ZUtilities.zDisposeCollection(generatedPanels);
            throw e;
        } catch (ZDeviceNotRunningException e) {
            ZUtilities.zDisposeCollection(generatedPanels);
            throw e;
        }
    }

    protected ReadableParameterModel[] generateParameterModelsForConsecutiveIds(int id, int num) throws IllegalParameterIdException {
        ReadableParameterModel[] models = new ReadableParameterModel[num];
        for (int i = 0; i < num; i++)
            try {
                models[i] = voice.getParameterModel(IntPool.get(id + i));
            } catch (IllegalParameterIdException e) {
                ZUtilities.zDisposeCollection(Arrays.asList(models));
                throw e;
            }

        return models;
    }

    protected JPanel getAmpEnvPanel() throws IllegalParameterIdException {
        return new AmpEnvelopePanel().init(voice);
    }

    protected JPanel getFiltEnvPanel() throws IllegalParameterIdException {
        return new FilterEnvelopePanel().init(voice);
    }

    protected JPanel getAuxEnvPanel() throws IllegalParameterIdException {
        return new AuxEnvelopePanel().init(voice);
    }

    protected JPanel getCordsPanel() throws IllegalParameterIdException, ZDeviceNotRunningException {
        return new CordPanel().init(voice);
    }

    protected JPanel getLFOPanel() throws ZDeviceNotRunningException, IllegalParameterIdException {
        return new LFOPanel().init(voice);
    }

    protected JPanel getTuningPanel() throws ZDeviceNotRunningException, IllegalParameterIdException {
        return new TuningPanel().init(voice);
    }

    protected JPanel getAmpPanel() throws ZDeviceNotRunningException, IllegalParameterIdException {
        return new AmplifierPanel().init(voice);
    }

    protected JPanel getFiltPanel() throws ZDeviceNotRunningException, IllegalParameterIdException {
        return new FilterPanel().init(voice);
    }

    public void zDispose() {
        removeAll();
        voice.getPreset().removePresetListener(pla);
        ZUtilities.zDisposeCollection(generatedPanels);
        vtp.zDispose();
        removeMouseListener(this);
        voice = null;
        pla = null;
        vtp = null;
        generatedPanels.clear();
        generatedPanels = null;
        ampEnvPanel = null;
        filtEnvPanel = null;
        auxEnvPanel = null;
        lfoPanel = null;
        tuningPanel = null;
        ampPanel = null;
        filtPanel = null;
        cordsPanel = null;
    }

    public String getTitle() {
        return vtp.getTitle();
    }

    public String getReducedTitle() {
        return vtp.getTitle();
    }

    public final void addTitleProviderListener(TitleProviderListener tpl) {
        vtp.addTitleProviderListener(tpl);
    }

    public final void removeTitleProviderListener(TitleProviderListener tpl) {
        vtp.removeTitleProviderListener(tpl);
    }

    public Icon getIcon() {
        return vtp.getIcon();
    }

    public String getToolTipText() {
        return vtp.getToolTipText();
    }

    public void mouseDragged(java.awt.event.MouseEvent mouseEvent) {
    }

    public void mouseClicked(java.awt.event.MouseEvent e) {
        checkPopup(e);
    }

    public void mouseEntered(java.awt.event.MouseEvent e) {
    }

    public void mouseExited(java.awt.event.MouseEvent e) {
    }

    public void mousePressed(java.awt.event.MouseEvent e) {
        checkPopup(e);
    }

    public void mouseReleased(java.awt.event.MouseEvent e) {
        checkPopup(e);
    }

    public boolean checkPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            Object[] sels = new Object[]{voice.getPreset().getMostCapableNonContextEditablePresetDowngrade()};
            try {
                JMenuItem jmi = ZCommandInvocationHelper.getMenu(sels, null, null, voice.getPreset().getPresetDisplayName());
                JPopupMenu popup = new JPopupMenu();
                popup.add(jmi);
                ZCommandInvocationHelper.showPopup(popup, this, e);
                return true;
            } catch (NoSuchPresetException e1) {
                e1.printStackTrace();
            }
        }
        return false;
    }
}
