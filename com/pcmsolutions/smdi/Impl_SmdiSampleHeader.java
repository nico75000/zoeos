package com.pcmsolutions.smdi;

/**
 * Created by IntelliJ IDEA.
 * User: pmeehan
 * Date: 08-Sep-2003
 * Time: 21:22:56
 * To change this template use Options | File Templates.
 */
class Impl_SmdiSampleHeader implements SmdiSampleHeader {
    protected boolean doesExist;
    protected byte bitsPerWord;
    protected byte numChannels;
    protected byte loopControl;
    protected byte nameLengthInBytes;
    protected int periodInNS;
    protected int lengthInSampleFrames;
    protected int loopStart;
    protected int loopEnd;
    protected short pitch;
    protected short pitchFraction;
    protected String name;
    protected int dataOffset;

    public Impl_SmdiSampleHeader() {
    }

    public Impl_SmdiSampleHeader(boolean doesExist, byte bitsPerWord, byte numChannels, byte loopControl, byte nameLengthInBytes, int periodInNS, int lengthInSampleFrames, int loopStart, int loopEnd, short pitch, short pitchFraction, String name, int dataOffset) {
        this.doesExist = doesExist;
        this.bitsPerWord = bitsPerWord;
        this.numChannels = numChannels;
        this.loopControl = loopControl;
        this.nameLengthInBytes = nameLengthInBytes;
        this.periodInNS = periodInNS;
        this.lengthInSampleFrames = lengthInSampleFrames;
        this.loopStart = loopStart;
        this.loopEnd = loopEnd;
        this.pitch = pitch;
        this.pitchFraction = pitchFraction;
        this.name = name;
        this.dataOffset = dataOffset;
    }

    public int getDataOffset() {
        return dataOffset;
    }

    public void setDataOffset(int dataOffset) {
        this.dataOffset = dataOffset;
    }

    public byte getBitsPerWord() {
        return bitsPerWord;
    }

    public void setBitsPerWord(byte bitsPerWord) {
        this.bitsPerWord = bitsPerWord;
    }

    public boolean isDoesExist() {
        return doesExist;
    }

    public void setDoesExist(boolean doesExist) {
        this.doesExist = doesExist;
    }

    public int getLengthInSampleFrames() { // Number of frames in sample (one sample frame's elementCount is ( NumberOfChannels*BitsPerWord)/8 Bytes)
        return lengthInSampleFrames;
    }

    public void setLengthInSampleFrames(int lengthInSampleFrames) {
        this.lengthInSampleFrames = lengthInSampleFrames;
    }

    public byte getLoopControl() {
        return loopControl;
    }

    public void setLoopControl(byte loopControl) {          // Specifies if and how the defined loop between LoopStart and LoopEnd should be played
        this.loopControl = loopControl;
    }

    public int getLoopEnd() {      // sample frame where sample ends
        return loopEnd;
    }

    public void setLoopEnd(int loopEnd) {
        this.loopEnd = loopEnd;
    }

    public int getLoopStart() {     // sample frame where sample starts
        return loopStart;
    }

    public void setLoopStart(int loopStart) {
        this.loopStart = loopStart;
    }

    public String getName() {           // name of sample
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte getNameLengthInBytes() {
        return nameLengthInBytes;
    }

    public void setNameLengthInBytes(byte nameLengthInBytes) {
        this.nameLengthInBytes = nameLengthInBytes;
    }

    public byte getNumChannels() {              // 1 or 2 usually
        return numChannels;
    }

    public void setNumChannels(byte numChannels) {
        this.numChannels = numChannels;
    }

    public int getPeriodInNS() {            // 10^9 % period = sample frequency
        return periodInNS;
    }

    public void setPeriodInNS(int periodInNS) {
        this.periodInNS = periodInNS;
    }

    public short getPitch() {                  // (0..127) 60 is middle C
        return pitch;
    }

    public void setPitch(short pitch) {
        this.pitch = pitch;
    }

    public short getPitchFraction() {    // Specifies the fine tuning of the sample, measured in cents upward
        return pitchFraction;
    }
    // from the semitone. 60.00 is exactly middle C, 60.32768 is 50 cents above middle C
    // In the case that the two pitch values are unavailable, a default of 60.00 should be used.

    public void setPitchFraction(short pitchFraction) {
        this.pitchFraction = pitchFraction;
    }
}
