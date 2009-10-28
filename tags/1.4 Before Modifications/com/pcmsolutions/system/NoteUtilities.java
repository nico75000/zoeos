package com.pcmsolutions.system;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 19-Oct-2003
 * Time: 22:42:02
 * To change this template use Options | File Templates.
 */
public class NoteUtilities {
    private static final String[] formattedNotes = new String[]{"C ", "C#", "D ", "D#", "E ", "F ", "F#", "G ", "G#", "A ", "A#", "B "};
    private static final String[] notes = new String[]{"c", "c#", "d", "d#", "e", "f", "f#", "g", "g#", "a", "a#", "b"};
    private static final List noteList = Arrays.asList(notes);

    private static int getNoteIndex(String note) {
        return noteList.indexOf(note.toLowerCase());
    }


    public static double getFreqForNote(int note) {
        return 440 * Math.pow(2, ((note - 69) / 12.0));
    }

    public static boolean isWhite(int key) {
        return !isBlack(key);
    }

    public static boolean isBlack(int key) {
        int mod = key % 12;
        if (mod == 1 || mod == 3 || mod == 6 || mod == 8 || mod == 10)
            return true;
        else
            return false;
    }

    public static List<String> getNoteStrings() {
        ArrayList<String> noteValueStrings = new ArrayList<String>();
        int oct = -2;
        for (int i = 0, n = 128; i < n; i++) {
            if (i != 0)
                oct = i / 12 - 2;
            noteValueStrings.add(formattedNotes[i % 12] + (oct < 0 ? String.valueOf(oct) : " " + oct));
        }
        return noteValueStrings;
    }

    private static final Pattern note_patt = Pattern.compile("\\s[a-gA-G][\\s#]?-?[0-9]");

    public static Note getNoteFromName(String name) {
        Matcher m = note_patt.matcher(name.subSequence(0, name.length()));
        if (m.find()) {
            int n = Note.getValueForString(name.substring(m.start(), m.end()));
            if (n != -1)
                return new Note(n);
        }
        return null;
    }

    public static class Note {
        private int noteValue;
        private String noteString;
        private final static Note[] allNotes = new Note[128];

        static {
            for (int i = 0; i < 128; i++)
                allNotes[i] = new Note(i);
        }

        private Note(int noteValue) {
            this.noteValue = noteValue;
            this.noteString = getStringForValue(noteValue);
        }

        public static Note[] getAllNotes() {
            return (Note[]) allNotes.clone();
        }

        public static String[] getAllNoteStrings() {
            String[] strs = new String[allNotes.length];
            for (int i = 0; i < strs.length; i++)
                strs[i] = allNotes[i].getNoteString();
            return strs;
        }

        public static Note[] getNoteRange(int low, int high) {
            if (low > high || low < 0 || high > 127)
                throw new IllegalArgumentException("illegal low/high");

            NoteUtilities.Note[] notes = new Note[high - low + 1];
            for (int i = 0, j = high - low; i <= j; i++)
                notes[i] = getNote(i + low);
            return notes;
        }

        public static Note getNote(int i) {
            if (i < 0 || i > 127)
                throw new IllegalArgumentException("note out of range");
            return allNotes[i];
        }

        public static String getStringForValue(int value) {
            int oct = (value == 0 ? -2 : value / 12 - 2);
            return formattedNotes[value % 12] + (oct < 0 ? String.valueOf(oct) : " " + oct);
        }

        public static int getValueForString(String str) {
            int oct = 0;
            int noteIndex = 0;
            boolean valid = false;

            try {
                String str2 = ZUtilities.removeDelimiters(str);
                if (str2.length() > 1)
                    if (str2.substring(1, 2).equals("#")) {
                        noteIndex = getNoteIndex(str2.substring(0, 2));
                        if (noteIndex != -1) {
                            if (str2.length() > 2) {
                                oct = Integer.parseInt(str2.substring(2));
                                if (oct >= -2 && oct <= 8)
                                    valid = true;
                            }
                        }
                    } else {
                        noteIndex = getNoteIndex(str2.substring(0, 1));
                        if (noteIndex != -1) {
                            oct = Integer.parseInt(str2.substring(1));
                            if (oct >= -2 && oct <= 8)
                                valid = true;
                        }
                    }
                if (valid)
                    return (oct + 2) * 12 + noteIndex;
            } catch (Exception e) {
            }
            return -1;
        }

        public String getNoteString() {
            return noteString;
        }

        public int getNoteValue() {
            return noteValue;
        }

        public String toString() {
            return noteString;
        }
    }
}
