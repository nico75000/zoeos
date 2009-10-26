package com.pcmsolutions.device.EMU.E4.gui.preset.presetviewer;

import com.pcmsolutions.device.EMU.E4.events.*;
import com.pcmsolutions.device.EMU.E4.gui.TitleProvider;
import com.pcmsolutions.device.EMU.E4.gui.TitleProviderListener;
import com.pcmsolutions.device.EMU.E4.gui.TitleProviderListenerHelper;
import com.pcmsolutions.device.EMU.E4.gui.preset.VoiceEditingIcon;
import com.pcmsolutions.device.EMU.E4.gui.preset.icons.PresetIcon;
import com.pcmsolutions.device.EMU.E4.preset.PresetListenerAdapter;
import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.system.IntPool;
import com.pcmsolutions.system.ZDisposable;

import javax.swing.*;

/**
 * User: paulmeehan
 * Date: 11-Feb-2004
 * Time: 01:39:24
 */
public class VoiceTitleProvider implements TitleProvider, ZDisposable {
    protected ReadablePreset.ReadableVoice voice;
    protected PresetListenerAdapter pla;
    protected final TitleProviderListenerHelper tplh = new TitleProviderListenerHelper(this);
    protected Icon icon = null;
    protected String title;

    public void init(ReadablePreset.ReadableVoice voice) {
        this.voice = voice;
        pla = new PresetListenerAdapter() {
            public void presetNameChanged(PresetNameChangeEvent ev) {
                updateTitle();
            }

            public void presetRefreshed(PresetRefreshEvent ev) {
                updateTitle();
            }

            public void presetInitialized(PresetInitializeEvent ev) {
                updateTitle();
            }

            public void presetInitializationStatusChanged(PresetInitializationStatusChangedEvent ev) {
                updateTitle();
            }

            public void voiceAdded(VoiceAddEvent ev) {
                updateTitle();
            }

            public void voiceRemoved(VoiceRemoveEvent ev) {
                updateTitle();
            }
        };
        voice.getPreset().addPresetListener(pla);
        updateTitle();
    }

    protected void updateTitle() {

        title = "V" + IntPool.get(voice.getVoiceNumber().intValue() + 1);
        if (voice.getPreset().getIcon() instanceof PresetIcon)
            icon = new VoiceEditingIcon((PresetIcon) voice.getPreset().getIcon());
        else
            icon = null;
        tplh.fireTitleProviderDataChanged();
    }

    public String getTitle() {
        return title;
    }

    public String getReducedTitle() {
        return getTitle();
    }

    public void addTitleProviderListener(TitleProviderListener tpl) {
        tplh.addTitleProviderListener(tpl);
    }

    public void removeTitleProviderListener(TitleProviderListener tpl) {
        tplh.removeTitleProviderListener(tpl);
    }

    public Icon getIcon() {
        return icon;
    }

    public String getToolTipText() {
        return getTitle();
    }

    public void zDispose() {
        voice.getPreset().removePresetListener(pla);
    }
}
