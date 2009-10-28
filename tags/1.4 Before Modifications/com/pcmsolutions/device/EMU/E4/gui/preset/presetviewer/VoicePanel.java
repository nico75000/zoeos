package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.events.*;
import com.pcmsolutions.device.EMU.E4.events.preset.*;
import com.pcmsolutions.device.EMU.E4.gui.*;
import com.pcmsolutions.device.EMU.E4.gui.colors.UIColors;
import com.pcmsolutions.device.EMU.E4.gui.preset.VoiceEditingIcon;
import com.pcmsolutions.device.EMU.E4.gui.preset.icons.PresetIcon;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.envelope.AmpEnvelopePanel;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.envelope.AuxEnvelopePanel;
import com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer.envelope.FilterEnvelopePanel;
import com.pcmsolutions.device.EMU.E4.gui.table.AbstractRowHeaderedAndSectionedTable;
import com.pcmsolutions.device.EMU.E4.parameter.IllegalParameterIdException;
import com.pcmsolutions.device.EMU.E4.parameter.ParameterException;
import com.pcmsolutions.device.EMU.E4.parameter.ReadableParameterModel;
import com.pcmsolutions.device.EMU.E4.parameter.EditableParameterModel;
import com.pcmsolutions.device.EMU.E4.preset.PresetException;
import com.pcmsolutions.device.EMU.E4.preset.PresetListenerAdapter;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.preset.ContextEditablePreset;
import com.pcmsolutions.device.EMU.DeviceException;
import com.pcmsolutions.gui.GriddedPanel;
import com.pcmsolutions.gui.UserMessaging;
import com.pcmsolutions.gui.ZCommandFactory;
import com.pcmsolutions.system.*;
import com.pcmsolutions.system.callback.Callback;
import com.pcmsolutions.system.tasking.ResourceUnavailableException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;

public class VoicePanel extends GriddedPanel implements ZDisposable, TitleProvider, MouseListener, Indexable, EnclosureNorthenComponentProvider {
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
    protected Impl_TableExclusiveSelectionContext tsc = new Impl_TableExclusiveSelectionContext();

    public VoicePanel init(ReadablePreset.ReadableVoice voice) throws ParameterException, DeviceException {
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

    public VoicePanel init(ReadablePreset.ReadableVoice voice, boolean showAmpEnv, boolean showFiltEnv, boolean showAuxEnv, boolean showAmp, boolean showFilt, boolean showLFO, boolean showTuning, boolean showCords) throws  ParameterException, DeviceException {
        this.voice = voice;
        this.showAmp = showAmp;
        this.showAmpEnv = showAmpEnv;
        this.showAuxEnv = showAuxEnv;
        this.showCords = showCords;
        this.showFilt = showFilt;
        this.showFiltEnv = showFiltEnv;
        this.showLFO = showLFO;
        this.showTuning = showTuning;

        //this.usingTabs = voice.getIndex().getPresetContext().getDeviceContext().getPreferences().z;
        // this.groupEnvelopesWhenUsingTabs = voice.getIndex().getPresetContext().getDeviceContext().getPreferences().getBoolean(PREF_groupEnvelopes, true);
        this.setFocusCycleRoot(true);

        generatePanels();

        if (voice.getPreset().getIcon() instanceof PresetIcon)
            icon = new VoiceEditingIcon((PresetIcon) voice.getPreset().getIcon());

        pla = new PresetListenerAdapter() {
            public void presetNameChanged(PresetNameChangeEvent ev) {
            }

            public void presetRefreshed(PresetInitializeEvent ev) {
                revalidate();
                repaint();
            }

            public void presetInitializationStatusChanged(PresetInitializationStatusChangedEvent ev) {
            }

            public void voiceAdded(VoiceAddEvent ev) {
                if (handlingVoice(ev.getVoice(), 1)) {
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
        voice.getPreset().addListener(pla);
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
            if (!(voice.getPreset().isEmpty())) {
                setEnabled(true);
                return;
            }
        } catch (PresetException e) {
            e.printStackTrace();
        }
        setEnabled(false);
    }

    protected void generatePanels() throws ParameterException, DeviceException {
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
        }
    }

    protected ReadableParameterModel[] generateParameterModelsForConsecutiveIds(int id, int num) throws ParameterException {
        ReadableParameterModel[] models = new ReadableParameterModel[num];
        for (int i = 0; i < num; i++)
            try {
                models[i] = voice.getParameterModel(IntPool.get(id + i));
            } catch (ParameterException e) {
                ZUtilities.zDisposeCollection(Arrays.asList(models));
                throw e;
            }

        return models;
    }

    protected JPanel getAmpEnvPanel() throws ParameterException {
        return new AmpEnvelopePanel().init(voice, tsc);
    }

    protected JPanel getFiltEnvPanel() throws ParameterException {
        return new FilterEnvelopePanel().init(voice, tsc);
    }

    protected JPanel getAuxEnvPanel() throws ParameterException {
        return new AuxEnvelopePanel().init(voice, tsc);
    }

    protected JPanel getCordsPanel() throws ParameterException{
        return new CordPanel().init(voice, tsc);
    }

    protected JPanel getLFOPanel() throws ParameterException, DeviceException {
        return new LFOPanel().init(voice, tsc);
    }

    protected JPanel getTuningPanel() throws ParameterException, DeviceException{
        return new TuningPanel().init(voice, tsc);
    }

    protected JPanel getAmpPanel() throws ParameterException, DeviceException {
        return new AmplifierPanel().init(voice, tsc);
    }

    protected JPanel getFiltPanel() throws ParameterException, DeviceException {
        return new FilterPanel().init(voice, tsc);
    }

    public void zDispose() {
        removeAll();
        voice.getPreset().removeListener(pla);
        ZUtilities.zDisposeCollection(generatedPanels);
        vtp.zDispose();
        tsc.zDispose();
        removeMouseListener(this);
        tsc = null;
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
        encMenuBar.zDispose();
        encMenuBar = null;
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
        if (e.getClickCount() == 2) {
            try {
                voice.getPreset().audition().post(new Callback() {
                    public void result(Exception e, boolean wasCancelled) {
                        if ( e!=null&& !wasCancelled)
                        UserMessaging.flashWarning(null, e.getMessage());
                    }
                });
                return;
            } catch (ResourceUnavailableException e1) {
                UserMessaging.flashWarning(null, e1.getMessage());
            }
        } else
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
            Object[] sels = new Object[]{voice.getPreset().getMostCapableNonContextEditablePreset()};
            try {
                JMenuItem jmi = ZCommandFactory.getMenu(sels, voice.getPreset().getDisplayName());
                JPopupMenu popup = new JPopupMenu();
                popup.add(jmi);
                ZCommandFactory.showPopup(popup, this, e);
                return true;
            } catch (PresetException e1) {
                e1.printStackTrace();
            }
        }
        return false;
    }

    public Integer getIndex() {
        return voice.getVoiceNumber();
    }

    public boolean isEnclosureNorthenComponentAvailable() {
        return true;
    }


    ZCommandFactory.ZCommandPresentationContext voiceCommandPresentationContext;


    protected EnclosureMenuBar encMenuBar;

    public Component getEnclosureNorthenComponent() {
        if (encMenuBar == null) {
            voiceCommandPresentationContext = ZCommandFactory.getToolbarPresentationContext(ReadablePreset.ReadableVoice.cmdProviderHelper.getSupportedMarkers(), Arrays.asList(new String[]{"Audition"}));
            voiceCommandPresentationContext.setTargets(new Object[]{voice});
            encMenuBar = new EnclosureMenuBar();
            encMenuBar.addStaticMenuContext(ZCommandFactory.getMenu(new Object[]{voice.getPreset().getMostCapableNonContextEditablePreset()}, "Preset"), "PRESET_MENU");
            encMenuBar.addStaticMenuContext(voiceCommandPresentationContext.getComponents(), "Voice");
            // encMenuBar.addStaticMenuContext(ZCommandFactory.getSuitableAsButtonComponents(new Object[]{voice.getIndex().getMostCapableNonContextEditablePresetDowngrade()}), "PRESET_BUTTONS");
            //   encMenuBar.addStaticMenuContext(ZCommandFactory.getMenu(new Object[]{preset.getMostCapableNonContextEditablePresetDowngrade()}, "Preset"), "PRESET");
        }
        return encMenuBar.getjMenuBar();
    }
}
