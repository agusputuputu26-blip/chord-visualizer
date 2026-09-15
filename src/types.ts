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

export interface PracticeSong {
  id: string;
  title: string;
  artist: string;
  bpm: number;
  totalDurationSec: number;
  sections: SongSection[];
  audioUrl?: string;
  audioFileName?: string;
}

export interface GuitarHeroNote {
  id: string;
  timestampSec: number;
  lane: number; // 0: Hijau, 1: Merah, 2: Kuning, 3: Biru, 4: Jingga
  durationSec?: number;
  chordName?: string;
}

export interface GuitarHeroSectionMarker {
  id: string;
  name: string;
  timestampSec: number;
  colorHex: string;
}

export type PracticeThemeMode = 'guitar_hero' | 'chord_tab' | 'split_both';
