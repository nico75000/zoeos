package org.tritonus.zuonics.sampled.aiff;

import org.tritonus.zuonics.sampled.AbstractAudioChunk;
import org.tritonus.zuonics.sampled.AbstractAudioChunk;
import org.tritonus.zuonics.sampled.AbstractAudioChunk;

public interface AiffMARKChunk extends AbstractAudioChunk {
    String AUDIO_FORMAT_PROPERTIES_KEY = "aiff_mark_chunk";

    int getNumMarkers();

    Marker[] getMarkers();

    public interface Marker {
        int getID();

        int getPosition();

        String getName();
    }
}


/*
The Marker Chunk
The Marker Chunk contains markers that point to positions in the waveform data. Markers can be used for whatever purposes an application desires. The Instrument Chunk, defined later in this document, uses markers to mark loop beginning and end points.

A marker structure is as follows:

typedef short  MarkerId;

typedef struct {
  MarkerID       id;
  unsigned long  position;
  pstring        markerName;
} Marker;

The id is a number that uniquely identifies that marker within an AIFF. The id can be any positive non-zero integer, as long as no other marker within the same FORM AIFF has the same id.

The marker's position in the WaveformData is determined by the position field. Markers conceptually fall between two sample frames. A marker that falls before the first sample frame in the waveform data is at position 0, while a marker that falls between the first and second sample frame in the waveform data is at position 1. Therefore, the units for position are sample frames, not bytes nor sample points.

Sample Frames
 ___ ___ ___ ___ ___ ___ ___ ___ ___ ___ ___ ___
|   |   |   |   |   |   |   |   |   |   |   |   |
|___|___|___|___|___|___|___|___|___|___|___|___|
^                   ^                           ^
position 0          position 5                  position 12

The markerName field is a Pascal-style text string containing the name of the mark.

Note: Some "EA IFF 85" files store strings as C-strings (text bytes followed by a null terminating character) instead of Pascal-style strings. Audio IFF uses pstrings because they are more efficiently skipped over when scanning through chunks. Using pstrings, a program can skip over a string by adding the string count to the address of the first character. C strings require that each character in the string be examined for the null terminator.

Marker Chunk Format
The format for the data within a Marker Chunk is shown below.

#define MarkerID 'MARK'  ( chunkID for Marker Chunk )

typedef  struct {
  ID              chunkID;
  long            chunkSize;

  unsigned short  numMarkers;
  Marker          Markers[];
} MarkerChunk;

The ID is always MARK. chunkSize is the number of bytes in the chunk, not counting the 8 bytes used by ID and Size fields.

The numMarkers field is the number of marker structures in the Marker Chunk. If numMarkers is not 0, it is followed by that many marker structures, one after the other. Because all fields in a marker structure are an even number of bytes, the length of any marker will always be even. Thus, markers are packed together with no unused bytes between them. The markers need not be placed in any particular order.

The Marker Chunk is optional. No more than one Marker Chunk can appear in a FORM AIFF.

*/
