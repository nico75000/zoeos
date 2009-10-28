package org.tritonus.zuonics.sampled.aiff;

import org.tritonus.zuonics.sampled.AbstractAudioChunk;

public interface AiffINSTChunk extends AbstractAudioChunk{
    String AUDIO_FORMAT_PROPERTIES_KEY = "aiff_inst_chunk";

    int getBaseNote();

    int getDetune();

    int getLowNote();

    int getHighNote();

    int getlowVelocity();

    int getHighVelocity();

    int getGain();

    Loop getSustainLoop();

    Loop getReleaseLoop();

    public interface Loop {
        int PLAY_MODE_NO_LOOPING = 0;
        int PLAY_MODE_FORWARD_LOOPING = 1;
        int PLAY_MODE_BACKWARD_LOOPING = 2;

        int getPlayMode();

        int getBeginMarkerId();

        int getEndMarkerId();
    }
}


/*

The Instrument Chunk
The Instrument Chunk defines basic parameters that an instrument, such as a MIDI sampler, could use to play the waveform data.

Looping
Waveform data can be looped, allowing a portion of the waveform to be repeated in order to lengthen the sound. The structure below describes a loop.

typedef struct {
  short     PlayMode;
  MarkerId  beginLoop;
  MarkerId  endLoop;
} Loop;

A loop is marked with two points, a begin position and an end position. There are two ways to play a loop, forward looping and forward/backward looping. In the case of forward looping, playback begins at the beginning of the waveform, continues past the begin position and continues to the end position, at which point playback starts again at the begin position. The segment between the begin and end positions, called the loop segment, is played repeatedly until interrupted by some action, such as a musician releasing a key on a musical controller.

               ___ ___ ___ ___ ___ ___ ___ ___ ___ ___ ___ ___
sample frames |   |   |   |<--- loop segment>|   |   |   |
              |___|___|___|___|___|___|___|___|___|___|___|___|
                          ^                       ^
                     begin position	        end position

With forward/backward looping, the loop segment is first played from the begin position to the end position, and then played backwards from the end position to the begin position. This flip-flop pattern is repeated over and over again until interrupted.

The playMode specifies which type of looping is to be performed:

#define NoLooping              0
#define ForwardLooping         1
#define ForwardBackwardLooping 2

If NoLooping is specified, then the loop points are ignored during playback.

The beginLoop is a marker id that marks the begin position of the loop segment.

The endLoop marks the end position of a loop. The begin position must be less than the end position. If this is not the case, then the loop segment has 0 or negative length and no looping takes place.

The Instrument Chunk Format
The format of the data within an Instrument Chunk is described below.

#define InstrumentID 'INST'  (chunkID for Instruments Chunk)

typedef struct {
  ID     chunkID;
  long   chunkSize;

  char   baseNote;
  char   detune;
  char   lowNote;
  char   highNote;
  char   lowvelocity;
  char   highvelocity;
  short  gain;
  Loop   sustainLoop;
  Loop   releaseLoop;
} InstrumentChunk;

The ID is always INST. chunkSize should always be 20 since there are no fields of variable length.

The baseNote is the note number at which the instrument plays back the waveform data without pitch modification (ie, at the same sample rate that was used when the waveform was created). Units are MIDI note numbers, and are in the range 0 through 127. Middle C is 60.

The detune field determines how much the instrument should alter the pitch of the sound when it is played back. Units are in cents (1/100 of a semitone) and range from -50 to +50. Negative numbers mean that the pitch of the sound should be lowered, while positive numbers mean that it should be raised.

The lowNote and highNote fields specify the suggested note range on a keyboard for playback of the waveform data. The waveform data should be played if the instrument is requested to play a note between the low and high note numbers, inclusive. The base note does not have to be within this range. Units for lowNote and highNote are MIDI note values.

The lowVelocity and highVelocity fields specify the suggested range of velocities for playback of the waveform data. The waveform data should be played if the note-on velocity is between low and high velocity, inclusive. Units are MIDI velocity values, 1 (lowest velocity) through 127 (highest velocity).

The gain is the amount by which to change the gain of the sound when it is played. Units are decibels. For example, 0db means no change, 6db means double the value of each sample point (ie, every additional 6db doubles the gain), while -6db means halve the value of each sample point.

The sustainLoop field specifies a loop that is to be played when an instrument is sustaining a sound.

The releaseLoop field specifies a loop that is to be played when an instrument is in the release phase of playing back a sound. The release phase usually occurs after a key on an instrument is released.

The Instrument Chunk is optional. No more than 1 Instrument Chunk can appear in one FORM AIFF.

*/