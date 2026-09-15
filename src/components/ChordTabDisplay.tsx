import React from 'react';
import { SongSection } from '../types';
import { Music, Eye } from 'lucide-react';

interface Props {
  activeSection: SongSection | null;
  currentTimeSec: number;
}

// Chord library for fretboard display
const CHORD_FINGERINGS: { [key: string]: { frets: number[]; fingers: (number | string)[] } } = {
  G: { frets: [3, 2, 0, 0, 0, 3], fingers: [2, 1, 'O', 'O', 'O', 3] },
  Em: { frets: [0, 2, 2, 0, 0, 0], fingers: ['O', 2, 3, 'O', 'O', 'O'] },
  C: { frets: [-1, 3, 2, 0, 1, 0], fingers: ['X', 3, 2, 'O', 1, 'O'] },
  D: { frets: [-1, -1, 0, 2, 3, 2], fingers: ['X', 'X', 'O', 1, 3, 2] },
  Am: { frets: [-1, 0, 2, 2, 1, 0], fingers: ['X', 'O', 2, 3, 1, 'O'] },
  F: { frets: [1, 3, 3, 2, 1, 1], fingers: [1, 3, 4, 2, 1, 1] },
  A7: { frets: [-1, 0, 2, 0, 2, 0], fingers: ['X', 'O', 2, 'O', 3, 'O'] },
  D7: { frets: [-1, -1, 0, 2, 1, 2], fingers: ['X', 'X', 'O', 2, 1, 3] },
  E7: { frets: [0, 2, 0, 1, 0, 0], fingers: ['O', 2, 'O', 1, 'O', 'O'] }
};

export const ChordTabDisplay: React.FC<Props> = ({ activeSection, currentTimeSec }) => {
  // Find current active chord in section
  let currentChordName = 'G';
  if (activeSection && activeSection.chords.length > 0) {
    const secDur = activeSection.endTimeSec - activeSection.startTimeSec;
    const progress = Math.max(0, Math.min(1, (currentTimeSec - activeSection.startTimeSec) / secDur));
    const chordIdx = Math.min(
      activeSection.chords.length - 1,
      Math.floor(progress * activeSection.chords.length)
    );
    currentChordName = activeSection.chords[chordIdx]?.name || 'G';
  }

  const chordData = CHORD_FINGERINGS[currentChordName] || CHORD_FINGERINGS['G'];

  return (
    <div className="w-full bg-slate-900 border border-slate-800 rounded-2xl p-4 shadow-xl flex flex-col gap-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-full bg-sky-500 flex items-center justify-center">
            <Music className="w-5 h-5 text-slate-950" />
          </div>
          <div>
            <h3 className="text-white font-bold text-base">Tabulasi & Diagram Kord Standar</h3>
            <p className="text-xs text-sky-400 font-medium">
              Kord Aktif: <span className="font-extrabold text-white">{currentChordName}</span>
            </p>
          </div>
        </div>

        <span className="px-2.5 py-1 bg-sky-500/10 border border-sky-500/30 text-sky-300 text-xs rounded-full font-semibold">
          Standar 6 Senar
        </span>
      </div>

      {/* Chord Ribbon */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1">
        {activeSection?.chords.map((ch, idx) => {
          const isCurrent = ch.name === currentChordName;
          return (
            <div
              key={idx}
              className={`px-4 py-2 rounded-xl border text-center transition min-w-[70px] ${
                isCurrent
                  ? 'bg-sky-500 text-slate-950 border-sky-400 font-extrabold scale-105 shadow-md'
                  : 'bg-slate-800/80 text-slate-300 border-slate-700 font-semibold'
              }`}
            >
              <div className="text-sm">{ch.name}</div>
              <div className="text-[10px] opacity-75">{ch.durationBeats} Ketuk</div>
            </div>
          );
        })}
      </div>

      {/* Fretboard Diagram */}
      <div className="bg-slate-950 border border-slate-800 rounded-xl p-3">
        <div className="text-xs font-semibold text-slate-400 mb-2 flex items-center gap-1.5">
          <Eye className="w-3.5 h-3.5 text-amber-400" />
          Fretboard Gitar (E - A - D - G - B - e)
        </div>

        <div className="grid grid-cols-6 gap-2 text-center text-xs">
          {['E', 'A', 'D', 'G', 'B', 'e'].map((stringName, strIdx) => {
            const fret = chordData.frets[strIdx];
            const finger = chordData.fingers[strIdx];

            return (
              <div key={strIdx} className="flex flex-col items-center gap-1">
                <span className="text-[10px] text-slate-500 font-mono">{stringName}</span>
                <div
                  className={`w-7 h-7 rounded-full flex items-center justify-center font-bold text-xs ${
                    fret === -1
                      ? 'bg-rose-950 text-rose-400 border border-rose-800'
                      : fret === 0
                      ? 'bg-emerald-950 text-emerald-400 border border-emerald-800'
                      : 'bg-amber-500 text-slate-950 shadow-md font-black'
                  }`}
                >
                  {finger}
                </div>
                <span className="text-[10px] text-slate-400">
                  {fret === -1 ? 'Mute' : fret === 0 ? 'Open' : `Fret ${fret}`}
                </span>
              </div>
            );
          })}
        </div>
      </div>

      {/* 6-String Tab Sheet */}
      <div className="bg-slate-950 border border-slate-800 rounded-xl p-3 overflow-hidden relative">
        <div className="text-xs font-semibold text-slate-400 mb-2">
          Garis Tabulasi Berjalan (6-String Tab)
        </div>

        <div className="font-mono text-xs text-slate-300 space-y-1 relative">
          {['e', 'B', 'G', 'D', 'A', 'E'].map((s, sIdx) => (
            <div key={sIdx} className="flex items-center gap-1">
              <span className="w-4 text-sky-400 font-bold">{s}|</span>
              <span className="tracking-widest text-slate-600">
                ---0---3---2---0---1---0---3---2---0---
              </span>
            </div>
          ))}

          {/* Animated Playhead Line */}
          <div
            style={{ left: `${((currentTimeSec % 4) / 4) * 80 + 15}%` }}
            className="absolute top-0 bottom-0 w-0.5 bg-amber-400 shadow-[0_0_8px_#F59E0B] transition-all duration-75"
          />
        </div>
      </div>
    </div>
  );
};
