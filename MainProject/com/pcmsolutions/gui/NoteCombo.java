package com.pcmsolutions.gui;

import com.pcmsolutions.system.NoteUtilities;

import javax.swing.*;

/**
 * User: paulmeehan
 * Date: 09-Sep-2004
 * Time: 01:09:06
 */
public class NoteCombo extends JComboBox {

    public void init(int lowNote, int highNoteInc) {
        NoteUtilities.Note[] notes = NoteUtilities.Note.getNoteRange(lowNote + 1, highNoteInc);
        this.setModel(new DefaultComboBoxModel(notes));
    }
    public NoteUtilities.Note getSelectedNote(){
        return (NoteUtilities.Note)this.getSelectedItem();
    }
}
