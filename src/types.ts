export interface ChordItem {
  name: string;
  durationBeats: number;
}

export interface SongSection {
  id: string;
  name: string;
  startTimeSec: number;
  endTimeSec: number;
  colorHex: string;
  chords: ChordItem[];
}

export interface ChordLyricWord {
  chord: string;
  word: string;
  timestampSec: number;
}

export interface LyricLine {
  id: string;
  startTimeSec: number;
  endTimeSec: number;
  text: string;
  chordWords: ChordLyricWord[];
}

export interface PracticeSong {
  id: string;
  title: string;
  artist: string;
  bpm: number;
  totalDurationSec: number;
  sections: SongSection[];
  lyrics: LyricLine[];
  audioUrl?: string;
  audioFileName?: string;
}

export interface ActiveChordState {
  chordName: string;
  nextChordName: string;
  beatsRemaining: number;
  beatNumber: number; // 1, 2, 3, 4
  chordProgress: number; // 0..1
  sectionName: string;
  frets: number[]; // [low E, A, D, G, B, high e]
  fingers: number[];
  baseFret: number;
}
