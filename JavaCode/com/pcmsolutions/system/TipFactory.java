package com.pcmsolutions.system;

import java.util.Arrays;
import java.util.Collections;


/**
 * User: paulmeehan
 * Date: 28-Dec-2004
 * Time: 19:41:12
 */
public class TipFactory {
    static final String FONT = "<font face=\"Arial\" size = 3>";
    static final String PG = "<p>";

    static final String[] tips = new String[]{
        FONT +
            "The Emulator crashes - it happens! Say you have been working on a few important presets in ZoeOS prior to the crash and you haven't recently done a bank save. You can" +
            " reboot the Emulator and use the preset_assertRemote command (it's in the Utility sub-menu) to send the presets cached in ZoeOS back to the Emulator. Of course this will not" +
            " include any lost sample data, but if you were just refernecing ROM samples or have backups of user samples, then you are ready to go again!"+
            PG + 
            "Finally a bank refresh should be performed in ZoeOS as the Emulator has been rebooted.",

        FONT +
            "You can enter note values such as a4, c#5 or g0 in filter fields that have units in Hertz." +
            PG +
            "This will resolve to the closest natural frequency for the given note.",

        FONT +
            "E-MU never published the SysEx for editing of Cords 18-23 even though it exists. You won't be able to edit or save them in ZoeOS because of this.",

        FONT +
            "E-MU never published the SysEx for editing the RFX card even though it exists. You won't be able to edit RFX parameters in ZoeOS because of this.",

        FONT +
            "You can mouse wheel over the LFO images to change the shape.",

        FONT +
            "You can program BPM delays in the voice delay field. Entering a value such as 1.25b will resolve to a ms value that represents 1.25 <i>beats</i> at the current system tempo.<br>" +
            PG +
            "Entering a value such as 2.5B will resolve to 2.5 <i>bars</i> at the current system tempo." +
            PG +
            "You could add the voice delay parameter to the preset user table and program a multi-tap or BPM delay in a single table column.",

        FONT +
            "You can audition individual samples, voices and zones in sequence by selecting <i>AudS</i> for selected voices and zones and <i>Audition [Stepped]</i> for selected samples.",

        FONT +
            "You can type any decimal fraction or number into a numeric field to have it resolved to the nearest legal value.",

        FONT +
            "Drag and drop functionality is available in many places. Experiment.",

        FONT +
            "You can drop samples from the Samples palette directly onto the background of the preset editor to create new voices/zones." +
            PG +
            "If the samples you drop are labelled with a note value their original key will be set automatically and you could then execute AutoMap to have an instantly playable preset.",

        FONT +
            "You can configure the preset user defined table by selecting the <i>Configure preset user table</i> in the <i>Devices palette</i> or by" +
            " right clicking on the preset user table itself. With this functionality you can design arbitrary tables of voice parameters.",

        FONT +
            "To change how you view the Keyboard, Velocity or Realtime windows in the preset main table, double click the window label e.g Key Win, to cycle through three viewing modes." +
            PG +
            "Alternatively you can right click the same labels to select one of the three modes from a popup.",

        FONT +
            "You can double click the label at the top of a table column to select all the cells in that column quickly as one operation.",

        FONT +
            "You can quickly change cell values by mouse wheeling freely over cells on tables without a selection." +
            PG +
            "Mouse wheeling on a table with selected cells quickly and simultaneously changes all the values in the selected cells.",

        FONT +
            "Remember a bi-directional midi connection must be established between ZoeOS and an Emulator IV. Please refer to the documentation section <i>Setting Up</i> for more information on this procedure.",

        FONT +
            "You can sort the voices, zones or links of any preset by selecting the relevant option from the <i>Sort</i> sub-menu in the preset commands." +
            PG +
            "Sorting is very flexible and can be performed using any set of parameters as the sort keys.",

        FONT +
            "Use the <i>Open [exclusive]</i> preset command to clear the workspace and open just the selected presets that are the targets of the command.",

        FONT +
            "You can use CTRL-C (copy) and CTRL-V (paste) on drag and drop enabled tables. This is useful if the source and destination tables of a drag and drop operation are not visible simultaneously.",

        FONT +
            "You can drag and drop samples between machines that are SCSI connected and SMDI coupled.",

        FONT +
            "When you select a property in the system or device properties palette, a useful description of that property is given at the bottom of the palette window.",

        FONT +
            "Aside from the explicit audition commands, you can double-click in the far left header column of the Multimode, Presets or Samples palettes to audition the item on that row (auditioning must be enabled).",

        FONT +
            "When the device property <i>Partitioned voice editing</i> is set to true, each voice section will get a seperate window during a voice edit. Some users may prefer this to the " +
            "the \"all on one page\" editing style." +
            PG +
            "Furthermore, you can specify if you want to group the three envelope sections together with the <i>Group envelopes</i> property.",

        FONT +
            "You can erase any user samples referenced by a preset by selecting the <i>Sample/Erase samples</i> command in the preset commands." +
            PG +
            "The <i>Erase samples</i> command might precede the command to erase a preset (These commands are distinct to provide maximum flexibility).",

        FONT +
            "ZoeOS provides 5 voice and zone keyboard mapping functions. Select some voices or some zones on a preset table and visit the <i>Key mapping</i> " +
            "menu for more information. Tooltips describing the behaviour of each function are provided."
    };
    static int index = -1;

    static {
        Collections.shuffle(Arrays.asList(tips));
    }

    static int nextIndex() {
        return index = Math.min(index + 1, tips.length - 1);
    }

    static int prevIndex() {
        return index = Math.max((index - 1), 0);
    }

    public static String getNextTip() {
        return tips[nextIndex()];
    }

    public static String getPreviousTip() {
        return tips[prevIndex()];
    }
}
