import { PracticeSong } from '../types';

export interface ChordFingerData {
  frets: number[]; // [low E, A, D, G, B, high e]
  fingers: number[];
  baseFret: number;
}

export const CHORD_FINGERINGS: Record<string, ChordFingerData> = {
  G: {
    frets: [3, 2, 0, 0, 3, 3],
    fingers: [2, 1, 0, 0, 3, 4],
    baseFret: 1
  },
  Em: {
    frets: [0, 2, 2, 0, 0, 0],
    fingers: [0, 2, 3, 0, 0, 0],
    baseFret: 1
  },
  C: {
    frets: [-1, 3, 2, 0, 1, 0],
    fingers: [0, 3, 2, 0, 1, 0],
    baseFret: 1
  },
  D: {
    frets: [-1, -1, 0, 2, 3, 2],
    fingers: [0, 0, 0, 1, 3, 2],
    baseFret: 1
  },
  Am: {
    frets: [-1, 0, 2, 2, 1, 0],
    fingers: [0, 0, 2, 3, 1, 0],
    baseFret: 1
  },
  F: {
    frets: [1, 3, 3, 2, 1, 1],
    fingers: [1, 3, 4, 2, 1, 1],
    baseFret: 1
  },
  Bm: {
    frets: [-1, 2, 4, 4, 3, 2],
    fingers: [0, 1, 3, 4, 2, 1],
    baseFret: 2
  },
  A7: {
    frets: [-1, 0, 2, 0, 2, 0],
    fingers: [0, 0, 2, 0, 3, 0],
    baseFret: 1
  },
  D7: {
    frets: [-1, -1, 0, 2, 1, 2],
    fingers: [0, 0, 0, 2, 1, 3],
    baseFret: 1
  },
  E7: {
    frets: [0, 2, 0, 1, 0, 0],
    fingers: [0, 2, 0, 1, 0, 0],
    baseFret: 1
  }
};

export const DEFAULT_SONGS: PracticeSong[] = [
  {
    id: 'pop_ballad',
    title: 'Sempurna (Acoustic Jam)',
    artist: 'Latihan Kord & Lirik (G - Em - C - D)',
    bpm: 85,
    totalDurationSec: 68,
    sections: [
      {
        id: 'sec_intro',
        name: 'Intro',
        startTimeSec: 0,
        endTimeSec: 12,
        colorHex: '#6366F1',
        chords: [
          { name: 'G', durationBeats: 4 },
          { name: 'Em', durationBeats: 4 },
          { name: 'C', durationBeats: 4 },
          { name: 'D', durationBeats: 4 }
        ]
      },
      {
        id: 'sec_verse1',
        name: 'Bait 1 (Verse)',
        startTimeSec: 12,
        endTimeSec: 28,
        colorHex: '#10B981',
        chords: [
          { name: 'G', durationBeats: 4 },
          { name: 'Em', durationBeats: 4 },
          { name: 'C', durationBeats: 4 },
          { name: 'D', durationBeats: 4 }
        ]
      },
      {
        id: 'sec_chorus',
        name: 'Reff (Chorus)',
        startTimeSec: 28,
        endTimeSec: 44,
        colorHex: '#F59E0B',
        chords: [
          { name: 'C', durationBeats: 4 },
          { name: 'D', durationBeats: 4 },
          { name: 'G', durationBeats: 4 },
          { name: 'Em', durationBeats: 4 }
        ]
      },
      {
        id: 'sec_interlude',
        name: 'Interlude',
        startTimeSec: 44,
        endTimeSec: 56,
        colorHex: '#EC4899',
        chords: [
          { name: 'Am', durationBeats: 4 },
          { name: 'Bm', durationBeats: 4 },
          { name: 'C', durationBeats: 4 },
          { name: 'D', durationBeats: 4 }
        ]
      },
      {
        id: 'sec_outro',
        name: 'Outro',
        startTimeSec: 56,
        endTimeSec: 68,
        colorHex: '#8B5CF6',
        chords: [
          { name: 'C', durationBeats: 4 },
          { name: 'D', durationBeats: 4 },
          { name: 'G', durationBeats: 4 }
        ]
      }
    ],
    lyrics: [
      {
        id: 'l_intro',
        startTimeSec: 0,
        endTimeSec: 12,
        text: '(Petikan Akustik Harmoni Intro)',
        chordWords: [
          { chord: 'G', word: '(Petikan', timestampSec: 0 },
          { chord: 'Em', word: 'Akustik', timestampSec: 3 },
          { chord: 'C', word: 'Harmoni', timestampSec: 6 },
          { chord: 'D', word: 'Intro)', timestampSec: 9 }
        ]
      },
      {
        id: 'l_verse1_1',
        startTimeSec: 12,
        endTimeSec: 20,
        text: 'Kau begitu sempurna di mataku',
        chordWords: [
          { chord: 'G', word: 'Kau', timestampSec: 12 },
          { chord: 'Em', word: 'sempurna', timestampSec: 16 }
        ]
      },
      {
        id: 'l_verse1_2',
        startTimeSec: 20,
        endTimeSec: 28,
        text: 'Kau membuat diriku selalu memujamu',
        chordWords: [
          { chord: 'C', word: 'Kau', timestampSec: 20 },
          { chord: 'D', word: 'selalu', timestampSec: 24 }
        ]
      },
      {
        id: 'l_chorus_1',
        startTimeSec: 28,
        endTimeSec: 36,
        text: 'Janganlah kau pernah hancurkan rasa ini',
        chordWords: [
          { chord: 'C', word: 'Janganlah', timestampSec: 28 },
          { chord: 'D', word: 'hancurkan', timestampSec: 32 }
        ]
      },
      {
        id: 'l_chorus_2',
        startTimeSec: 36,
        endTimeSec: 44,
        text: 'Karena kaulah seluruh nafasku dan jiwaku',
        chordWords: [
          { chord: 'G', word: 'Karena', timestampSec: 36 },
          { chord: 'Em', word: 'seluruh', timestampSec: 40 }
        ]
      },
      {
        id: 'l_interlude',
        startTimeSec: 44,
        endTimeSec: 56,
        text: 'Tuk selamanya di dalam pelukanku',
        chordWords: [
          { chord: 'Am', word: 'Tuk', timestampSec: 44 },
          { chord: 'Bm', word: 'selamanya', timestampSec: 47 },
          { chord: 'C', word: 'dalam', timestampSec: 50 },
          { chord: 'D', word: 'pelukanku', timestampSec: 53 }
        ]
      },
      {
        id: 'l_outro',
        startTimeSec: 56,
        endTimeSec: 68,
        text: 'Sempurna... Kau begitu indah selamanya',
        chordWords: [
          { chord: 'C', word: 'Sempurna...', timestampSec: 56 },
          { chord: 'D', word: 'indah', timestampSec: 60 },
          { chord: 'G', word: 'selamanya', timestampSec: 64 }
        ]
      }
    ]
  },
  {
    id: 'song_kemesraan',
    title: 'Kemesraan (Folk Ballad)',
    artist: 'Latihan Kord & Lirik (C - F - G - Am)',
    bpm: 80,
    totalDurationSec: 54,
    sections: [
      {
        id: 'km_sec_intro',
        name: 'Intro',
        startTimeSec: 0,
        endTimeSec: 10,
        colorHex: '#38BDF8',
        chords: [
          { name: 'C', durationBeats: 4 },
          { name: 'F', durationBeats: 4 },
          { name: 'G', durationBeats: 4 },
          { name: 'C', durationBeats: 4 }
        ]
      },
      {
        id: 'km_sec_verse',
        name: 'Bait 1 (Verse)',
        startTimeSec: 10,
        endTimeSec: 26,
        colorHex: '#10B981',
        chords: [
          { name: 'C', durationBeats: 4 },
          { name: 'F', durationBeats: 4 },
          { name: 'G', durationBeats: 4 },
          { name: 'C', durationBeats: 4 }
        ]
      },
      {
        id: 'km_sec_chorus',
        name: 'Reff (Chorus)',
        startTimeSec: 26,
        endTimeSec: 44,
        colorHex: '#F59E0B',
        chords: [
          { name: 'F', durationBeats: 4 },
          { name: 'G', durationBeats: 4 },
          { name: 'C', durationBeats: 4 },
          { name: 'Am', durationBeats: 4 },
          { name: 'F', durationBeats: 4 },
          { name: 'G', durationBeats: 4 },
          { name: 'C', durationBeats: 4 }
        ]
      },
      {
        id: 'km_sec_outro',
        name: 'Outro',
        startTimeSec: 44,
        endTimeSec: 54,
        colorHex: '#8B5CF6',
        chords: [
          { name: 'F', durationBeats: 4 },
          { name: 'G', durationBeats: 4 },
          { name: 'C', durationBeats: 4 }
        ]
      }
    ],
    lyrics: [
      {
        id: 'km_l_intro',
        startTimeSec: 0,
        endTimeSec: 10,
        text: '(Alunan Petikan Gitar Akustik C - F - G - C)',
        chordWords: [
          { chord: 'C', word: '(Alunan', timestampSec: 0 },
          { chord: 'F', word: 'Petikan', timestampSec: 2.5 },
          { chord: 'G', word: 'Gitar', timestampSec: 5 },
          { chord: 'C', word: 'Akustik)', timestampSec: 7.5 }
        ]
      },
      {
        id: 'km_l_v1',
        startTimeSec: 10,
        endTimeSec: 18,
        text: 'Suatu hari di kala kita duduk di tepi pantai',
        chordWords: [
          { chord: 'C', word: 'Suatu', timestampSec: 10 },
          { chord: 'F', word: 'duduk', timestampSec: 14 }
        ]
      },
      {
        id: 'km_l_v2',
        startTimeSec: 18,
        endTimeSec: 26,
        text: 'Dan memandang ombak di lautan yang kian menepi',
        chordWords: [
          { chord: 'G', word: 'Dan', timestampSec: 18 },
          { chord: 'C', word: 'lautan', timestampSec: 22 }
        ]
      },
      {
        id: 'km_l_c1',
        startTimeSec: 26,
        endTimeSec: 35,
        text: 'Kemesraan ini janganlah cepat berlalu',
        chordWords: [
          { chord: 'F', word: 'Kemesraan', timestampSec: 26 },
          { chord: 'G', word: 'janganlah', timestampSec: 29 },
          { chord: 'C', word: 'berlalu', timestampSec: 32 }
        ]
      },
      {
        id: 'km_l_c2',
        startTimeSec: 35,
        endTimeSec: 44,
        text: 'Kemesraan ini ingin kukenang selalu',
        chordWords: [
          { chord: 'Am', word: 'Kemesraan', timestampSec: 35 },
          { chord: 'F', word: 'ingin', timestampSec: 38 },
          { chord: 'G', word: 'kukenang', timestampSec: 40 },
          { chord: 'C', word: 'selalu', timestampSec: 42 }
        ]
      },
      {
        id: 'km_l_out',
        startTimeSec: 44,
        endTimeSec: 54,
        text: 'Hatiku damai... Jiwaku tentram bersamamu',
        chordWords: [
          { chord: 'F', word: 'Hatiku', timestampSec: 44 },
          { chord: 'G', word: 'Jiwaku', timestampSec: 47 },
          { chord: 'C', word: 'bersamamu', timestampSec: 50 }
        ]
      }
    ]
  }
];
