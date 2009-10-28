package com.pcmsolutions.device.EMU.E4.parameter;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 09-Aug-2003
 * Time: 15:09:16
 * To change this template use Options | File Templates.
 */
public interface ParameterCategories {
    public final String PRESET_GLOBALS = "Globals";
    public final String PRESET_FX_A = "Effect A";
    public final String PRESET_FX_B = "Effect B";

    public final String LINK_MAIN = "Main";
    public final String LINK_KEYWIN = "KeyWin";
    public final String LINK_VELWIN = "VelWin";
    public final String LINK_MIDIFILTERS = "MidiFilters";

    public final String VOICE_MAIN = "Main";
    public final String VOICE_KEY_WIN = "KeyWin";
    public final String VOICE_VEL_WIN = "VelWin";
    public final String VOICE_RT_WIN = "RTWin";

    public final String VOICE_LFO1 = "Lfo 1";
    public final String VOICE_LFO2 = "Lfo 2";

    public final String VOICE_CORDS = "Cords";

    public final String VOICE_TUNING = "Tuning";
    public final String VOICE_TUNING_MODIFIERS = "Tuning;Modifiers";
    public final String VOICE_TUNING_SETUP = "Tuning;Setup";

    public final String VOICE_AMPLIFIER = "Amplifier";
    public final String VOICE_FILTER = "Filter";
    public final String VOICE_FILTER_RESERVED = "Filter(Reserved)";

    public final String VOICE_AMPLIFIER_ENVELOPE = "Amplifier Envelope";
    public final String VOICE_FILTER_ENVELOPE = "Filter Envelope";
    public final String VOICE_AUX_ENVELOPE = "Auxillary Envelope";

    /*public final String VOICE_AMPLIFIER_ENVELOPE_ATTACK1 = "Amplifier;Envelope;Attack1";
    public final String VOICE_AMPLIFIER_ENVELOPE_ATTACK2 = "Amplifier;Envelope;Attack2";
    public final String VOICE_AMPLIFIER_ENVELOPE_DECAY1 = "Amplifier;Envelope;Decay1";
    public final String VOICE_AMPLIFIER_ENVELOPE_DECAY2 = "Amplifier;Envelope;Decay2";
    public final String VOICE_AMPLIFIER_ENVELOPE_RELEASE1 = "Amplifier;Envelope;Release1";
    public final String VOICE_AMPLIFIER_ENVELOPE_RELEASE2 = "Amplifier;Envelope;Release2";

    public final String VOICE_FILTER_ENVELOPE_ATTACK1 = "Filter;Envelope;Attack1";
    public final String VOICE_FILTER_ENVELOPE_ATTACK2 = "Filter;Envelope;Attack2";
    public final String VOICE_FILTER_ENVELOPE_DECAY1 = "Filter;Envelope;Decay1";
    public final String VOICE_FILTER_ENVELOPE_DECAY2 = "Filter;Envelope;Decay2";
    public final String VOICE_FILTER_ENVELOPE_RELEASE1 = "Filter;Envelope;Release1";
    public final String VOICE_FILTER_ENVELOPE_RELEASE2 = "Filter;Envelope;Release2";

    public final String VOICE_AUX_ENVELOPE_ATTACK1 = "Aux;Envelope;Attack1";
    public final String VOICE_AUX_ENVELOPE_ATTACK2 = "Aux;Envelope;Attack2";
    public final String VOICE_AUX_ENVELOPE_DECAY1 = "Aux;Envelope;Decay1";
    public final String VOICE_AUX_ENVELOPE_DECAY2 = "Aux;Envelope;Decay2";
    public final String VOICE_AUX_ENVELOPE_RELEASE1 = "Aux;Envelope;Release1";
    public final String VOICE_AUX_ENVELOPE_RELEASE2 = "Aux;Envelope;Release2";
      */

    // for use by VoiceParameterCollectionDataFlavor
    public final String MASTER_GENERAL = "General";


    public final String MASTER_TUNING = "Tuning";
    public final String MASTER_IO = "Input/Output";
    public final String MASTER_SCSI_DISK = "SCSI/Disk";
    public final String MASTER_IMPORT_OPTIONS = "Import Options";
    public final String MASTER_MIDI_PREFERENCES = "Midi preferences";
    public final String MASTER_MIDI_MODE = "Midi Mode";
    public final String MASTER_MIDI_CONTROLLERS = "Midi Controllers";

    public final String MULTIMODE = "MultiMode";

    public final String MASTER_FX_A = "FX A";
    public final String MASTER_FX_B = "FX B";
    public final String MASTER_FX_SETUP = "FX Setup";
}
