import { PracticeSong, GuitarHeroNote, GuitarHeroSectionMarker } from '../types';

export const DEFAULT_SONGS: PracticeSong[] = [
  {
    id: 'pop_ballad',
    title: 'Acoustic Pop Jam',
    artist: 'Guitar Master Track',
    bpm: 90,
    totalDurationSec: 52,
    sections: [
      {
        id: 'intro',
        name: 'Intro',
        startTimeSec: 0,
        endTimeSec: 10,
        colorHex: '#38BDF8', // Cyan
        chords: [
          { name: 'G', durationBeats: 4 },
          { name: 'Em', durationBeats: 4 }
        ]
      },
      {
        id: 'verse_1',
        name: 'Verse 1',
        startTimeSec: 10,
        endTimeSec: 24,
        colorHex: '#10B981', // Emerald
        chords: [
          { name: 'G', durationBeats: 4 },
          { name: 'Em', durationBeats: 4 },
          { name: 'C', durationBeats: 4 },
          { name: 'D', durationBeats: 4 }
        ]
      },
      {
        id: 'chorus',
        name: 'Chorus',
        startTimeSec: 24,
        endTimeSec: 38,
        colorHex: '#F59E0B', // Amber
        chords: [
          { name: 'C', durationBeats: 4 },
          { name: 'D', durationBeats: 4 },
          { name: 'G', durationBeats: 4 },
          { name: 'Em', durationBeats: 4 }
        ]
      },
      {
        id: 'solo',
        name: 'Interlude / Solo',
        startTimeSec: 38,
        endTimeSec: 46,
        colorHex: '#8B5CF6', // Purple
        chords: [
          { name: 'Am', durationBeats: 4 },
          { name: 'D', durationBeats: 4 }
        ]
      },
      {
        id: 'outro',
        name: 'Outro',
        startTimeSec: 46,
        endTimeSec: 52,
        colorHex: '#EF4444', // Red
        chords: [
          { name: 'G', durationBeats: 8 }
        ]
      }
    ]
  },
  {
    id: 'blues_jam',
    title: 'Midnight Blues Jam',
    artist: '12-Bar Blues Track',
    bpm: 105,
    totalDurationSec: 48,
    sections: [
      {
        id: 'head',
        name: 'Blues Head',
        startTimeSec: 0,
        endTimeSec: 14,
        colorHex: '#38BDF8',
        chords: [
          { name: 'A7', durationBeats: 4 },
          { name: 'D7', durationBeats: 4 },
          { name: 'A7', durationBeats: 4 }
        ]
      },
      {
        id: 'turnaround',
        name: 'Turnaround',
        startTimeSec: 14,
        endTimeSec: 28,
        colorHex: '#F59E0B',
        chords: [
          { name: 'E7', durationBeats: 4 },
          { name: 'D7', durationBeats: 4 },
          { name: 'A7', durationBeats: 4 }
        ]
      },
      {
        id: 'lead_solo',
        name: 'Guitar Solo',
        startTimeSec: 28,
        endTimeSec: 48,
        colorHex: '#EF4444',
        chords: [
          { name: 'A7', durationBeats: 4 },
          { name: 'D7', durationBeats: 4 },
          { name: 'E7', durationBeats: 4 },
          { name: 'A7', durationBeats: 4 }
        ]
      }
    ]
  }
];

export function generateGuitarHeroTrack(song: PracticeSong): {
  notes: GuitarHeroNote[];
  markers: GuitarHeroSectionMarker[];
} {
  const notes: GuitarHeroNote[] = [];
  const markers: GuitarHeroSectionMarker[] = [];
  const secondsPerBeat = 60.0 / song.bpm;

  song.sections.forEach((sec) => {
    markers.push({
      id: `m_${sec.id}`,
      name: sec.name,
      timestampSec: sec.startTimeSec,
      colorHex: sec.colorHex
    });

    const chordCount = sec.chords.length;
    if (chordCount > 0) {
      const sectionDuration = sec.endTimeSec - sec.startTimeSec;
      const chordDuration = sectionDuration / chordCount;

      sec.chords.forEach((chord, chordIdx) => {
        const chordStartTime = sec.startTimeSec + (chordIdx * chordDuration);
        const lanes = mapChordToLanes(chord.name);

        // Beat 1: Chord hit (multiple notes)
        lanes.forEach((lane) => {
          notes.push({
            id: `n_${sec.id}_${chordIdx}_0_${lane}`,
            timestampSec: chordStartTime,
            lane,
            chordName: chord.name
          });
        });

        // Sub-beats 2, 3, 4
        const subBeats = [
          { time: chordStartTime + secondsPerBeat, lane: (lanes[0] + 1) % 5 },
          { time: chordStartTime + secondsPerBeat * 2, lane: (lanes[lanes.length - 1] + 2) % 5 },
          { time: chordStartTime + secondsPerBeat * 3, lane: lanes[1] ?? 3 }
        ];

        subBeats.forEach((sb, beatIdx) => {
          if (sb.time < sec.endTimeSec) {
            notes.push({
              id: `n_${sec.id}_${chordIdx}_${beatIdx + 1}_${sb.lane}`,
              timestampSec: sb.time,
              lane: sb.lane,
              chordName: chord.name
            });
          }
        });
      });
    }
  });

  return {
    notes: notes.sort((a, b) => a.timestampSec - b.timestampSec),
    markers: markers.sort((a, b) => a.timestampSec - b.timestampSec)
  };
}

function mapChordToLanes(name: string): number[] {
  switch (name.toUpperCase().trim()) {
    case 'G': return [0, 2, 4];
    case 'EM': return [0, 1, 3];
    case 'C': return [1, 2, 4];
    case 'D': return [2, 3, 4];
    case 'AM': return [1, 2, 3];
    case 'F': return [0, 1, 2, 4];
    case 'A7': return [1, 3];
    case 'D7': return [2, 3];
    case 'E7': return [0, 2];
    default: return [1, 2];
  }
}
