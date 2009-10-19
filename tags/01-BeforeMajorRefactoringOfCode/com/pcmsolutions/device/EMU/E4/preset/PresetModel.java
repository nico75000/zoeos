package com.pcmsolutions.device.EMU.E4.preset;

import com.pcmsolutions.device.EMU.E4.gui.preset.DesktopEditingMediator;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan                impl_
 * Date: 24-May-2003
 * Time: 13:30:15
 * To change this template use Options | File Templates.
 */
public interface PresetModel {
    public void setPresetContext(PresetContext pc);

    public void setPreset(Integer p);

    public PresetContext getPresetContext();

    public Integer getPreset();

    public void setPresetEditingMediator(DesktopEditingMediator pem);

    public DesktopEditingMediator getPresetEditingMediator();
}