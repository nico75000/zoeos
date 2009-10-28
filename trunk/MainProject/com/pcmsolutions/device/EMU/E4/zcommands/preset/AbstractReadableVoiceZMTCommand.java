package com.pcmsolutions.device.EMU.E4.zcommands.preset;

import com.pcmsolutions.device.EMU.E4.preset.ReadablePreset;
import com.pcmsolutions.device.EMU.E4.zcommands.E4ReadableVoiceZCommandMarker;
import com.pcmsolutions.system.AbstractZMTCommand;
import com.pcmsolutions.system.ZCommandTargetsNotSpecifiedException;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 22-Mar-2003
 * Time: 15:05:57
 * To change this template use Options | File Templates.
 */
public abstract class AbstractReadableVoiceZMTCommand extends AbstractZMTCommand<ReadablePreset.ReadableVoice> implements E4ReadableVoiceZCommandMarker {
    public String getPresentationCategory() {
          return "Voice";
      }
}
