import React, { useEffect, useRef } from 'react';
import { SongSection, LyricLine } from '../types';
import { CHORD_FINGERINGS } from '../data/defaultSongs';
import { ArrowRight, Disc3 } from 'lucide-react';

interface Props {
  activeSection: SongSection | null;
  lyrics: LyricLine[];
  currentTimeSec: number;
  bpm: number;
  isPlaying: boolean;
  onSeek: (seconds: number) => void;
}

export const ChordTabDisplay: React.FC<Props> = ({
  activeSection,
  lyrics,
  currentTimeSec,
  bpm,
  isPlaying,
  onSeek
}) => {
  const lyricsContainerRef = useRef<HTMLDivElement>(null);
  const activeLineRef = useRef<HTMLDivElement>(null);

  // Determine current beat in 4/4 measure
  const secondsPerBeat = 60.0 / bpm;
  const currentTotalBeat = Math.floor(currentTimeSec / secondsPerBeat);
  const beatNumber = (currentTotalBeat % 4) + 1; // 1, 2, 3, 4

  // Determine active chord & next chord
  let currentChordName = 'G';
  let nextChordName = 'Em';
  let chordProgress = 0;
  let beatsRemaining = 2;
  let chordIdx = 0;

  if (activeSection && activeSection.chords.length > 0) {
    const secOffset = Math.max(0, currentTimeSec - activeSection.startTimeSec);
    const duration = Math.max(0.1, activeSection.endTimeSec - activeSection.startTimeSec);
    const chordDuration = duration / activeSection.chords.length;
    chordIdx = Math.min(
      Math.floor(secOffset / chordDuration),
      activeSection.chords.length - 1
    );

    const currentChord = activeSection.chords[chordIdx];
    currentChordName = currentChord?.name || 'G';

    const currentChordOffset = secOffset - chordIdx * chordDuration;
    chordProgress = Math.min(1, Math.max(0, currentChordOffset / chordDuration));

    // Next chord
    if (chordIdx + 1 < activeSection.chords.length) {
      nextChordName = activeSection.chords[chordIdx + 1].name;
    } else {
      nextChordName = activeSection.chords[0].name;
    }

    const beatInChord = Math.floor(currentChordOffset / secondsPerBeat) % 4;
    beatsRemaining = Math.max(1, 4 - beatInChord);
  }

  const chordData = CHORD_FINGERINGS[currentChordName] || CHORD_FINGERINGS['G'];

  // Find active lyric line
  const activeLyric = lyrics.find(
    (l) => currentTimeSec >= l.startTimeSec && currentTimeSec < l.endTimeSec
  ) || lyrics.slice().reverse().find((l) => currentTimeSec >= l.startTimeSec) || lyrics[0];

  // Auto scroll to active lyric line
  useEffect(() => {
    if (activeLineRef.current && lyricsContainerRef.current) {
      const container = lyricsContainerRef.current;
      const target = activeLineRef.current;
      const targetTop = target.offsetTop - container.offsetTop - 80;
      container.scrollTo({ top: targetTop, behavior: 'smooth' });
    }
  }, [activeLyric?.id]);

  return (
    <div className="w-full flex flex-col gap-5">
      {/* 1. Main Chord Card: Beat Pulse, Big Current Chord, Next Chord, & Finger Guide */}
      <div className="w-full bg-gradient-to-b from-slate-900 to-slate-950 border border-amber-500/40 rounded-3xl p-5 md:p-6 shadow-2xl flex flex-col gap-5">
        
        {/* Top Header: Sync Status + 4/4 Beat Pulse Meter */}
        <div className="flex items-center justify-between flex-wrap gap-2 pb-2 border-b border-slate-800">
          <div className="flex items-center gap-2">
            <span
              className={`w-3 h-3 rounded-full ${
                isPlaying ? 'bg-emerald-400 animate-ping' : 'bg-slate-500'
              }`}
            />
            <span className="text-xs font-bold tracking-wider text-emerald-400 uppercase">
              {isPlaying ? 'Sinkron Audio 100%' : 'Audio Jeda'}
            </span>
          </div>

          {/* 4 Beats Meter */}
          <div className="flex items-center gap-2">
            <span className="text-xs text-slate-400 font-medium">Ketukan:</span>
            {[1, 2, 3, 4].map((b) => {
              const isCurrent = isPlaying && b === beatNumber;
              return (
                <div
                  key={b}
                  className={`w-7 h-7 rounded-full flex items-center justify-center text-xs font-black transition-all duration-100 ${
                    isCurrent
                      ? 'bg-amber-400 text-slate-950 scale-125 shadow-lg shadow-amber-500/50'
                      : b < beatNumber
                      ? 'bg-sky-950 text-sky-400 border border-sky-800/60'
                      : 'bg-slate-800 text-slate-500'
                  }`}
                >
                  {b}
                </div>
              );
            })}
          </div>
        </div>

        {/* Center Chord Display */}
        <div className="flex items-center justify-between gap-4">
          {/* Big Active Chord */}
          <div className="flex-1 flex flex-col items-center justify-center text-center">
            <span className="text-xs font-bold uppercase tracking-widest text-amber-400 mb-1">
              Kunci Sedang Berjalan
            </span>
            <div className="text-6xl md:text-7xl font-black text-amber-300 drop-shadow-[0_0_25px_rgba(245,158,11,0.4)] tracking-tight">
              {currentChordName}
            </div>
            <span className="text-xs text-slate-400 mt-1">
              {activeSection?.name || 'Intro'} • {bpm} BPM (4/4 Birama)
            </span>
          </div>

          {/* Next Chord Box */}
          <div className="bg-slate-800/90 border border-slate-700/80 rounded-2xl px-4 py-3 flex flex-col items-center gap-1 shadow-inner min-w-[110px]">
            <div className="flex items-center gap-1 text-[11px] font-semibold text-sky-400">
              <span>Berikutnya</span>
              <ArrowRight className="w-3 h-3" />
            </div>
            <div className="text-2xl font-bold text-white tracking-wide">
              {nextChordName}
            </div>
            <span className="text-[10px] text-slate-400">
              {beatsRemaining} ketukan lagi
            </span>
          </div>
        </div>

        {/* Chord Measure Progress Bar */}
        <div className="w-full bg-slate-800/80 h-2 rounded-full overflow-hidden">
          <div
            className="h-full bg-amber-400 transition-all duration-75 ease-linear rounded-full"
            style={{ width: `${chordProgress * 100}%` }}
          />
        </div>

        {/* Finger Placement on 6 Strings (E A D G B e) */}
        <div className="w-full bg-slate-950/80 border border-slate-800 rounded-2xl p-3.5 flex flex-col gap-2">
          <div className="flex items-center justify-between text-xs text-slate-400">
            <span className="font-semibold">Posisi Jari & Senar Gitar ({currentChordName})</span>
            <span className="text-sky-400 font-mono text-[11px]">Fret Dasar: {chordData.baseFret}</span>
          </div>

          <div className="grid grid-cols-6 gap-2">
            {['E', 'A', 'D', 'G', 'B', 'e'].map((strName, idx) => {
              const fret = chordData.frets[idx];
              const finger = chordData.fingers[idx];
              const isMuted = fret < 0;
              const isOpen = fret === 0;

              return (
                <div key={idx} className="flex flex-col items-center gap-1">
                  <span className="text-[10px] font-mono font-bold text-slate-500">{strName}</span>
                  <div
                    className={`w-8 h-8 rounded-lg flex items-center justify-center font-black text-xs transition ${
                      isMuted
                        ? 'bg-slate-800 text-slate-500'
                        : isOpen
                        ? 'bg-emerald-600 text-white'
                        : 'bg-amber-400 text-slate-950 shadow-md'
                    }`}
                  >
                    {isMuted ? 'X' : isOpen ? '0' : fret}
                  </div>
                  <span className="text-[9px] text-slate-400">
                    {finger > 0 ? `Jari ${finger}` : isOpen ? 'Buka' : 'Mati'}
                  </span>
                </div>
              );
            })}
          </div>
        </div>

        {/* Section Progression Ribbon */}
        {activeSection && activeSection.chords.length > 0 && (
          <div className="flex items-center gap-2 overflow-x-auto pb-1">
            {activeSection.chords.map((ch, idx) => {
              const isActive = idx === chordIdx;
              return (
                <div
                  key={idx}
                  className={`px-3.5 py-1.5 rounded-xl border text-center transition min-w-[65px] ${
                    isActive
                      ? 'bg-amber-400 text-slate-950 border-amber-300 font-black scale-105 shadow-md'
                      : 'bg-slate-800 text-slate-300 border-slate-700 font-semibold'
                  }`}
                >
                  <div className="text-sm">{ch.name}</div>
                  <div className="text-[10px] opacity-75">{ch.durationBeats} Ketuk</div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* 2. Real-time Synchronized Lyrics & Chords (Karaoke Style) */}
      <div className="w-full bg-slate-900 border border-slate-800 rounded-3xl p-5 md:p-6 shadow-xl flex flex-col gap-3">
        <div className="flex items-center justify-between pb-3 border-b border-slate-800">
          <div className="flex items-center gap-2">
            <Disc3 className="w-5 h-5 text-sky-400 animate-spin" />
            <h3 className="text-white font-bold text-base">Lirik & Kord Berjalan Waktu Nyata</h3>
          </div>
          <span className="px-2.5 py-1 bg-sky-500/10 border border-sky-500/30 text-sky-400 text-xs rounded-full font-semibold">
            Sinkron Karaoke
          </span>
        </div>

        {/* Scrollable lyrics list */}
        <div
          ref={lyricsContainerRef}
          className="w-full max-h-[300px] overflow-y-auto space-y-3 pr-1 scroll-smooth"
        >
          {lyrics.map((line) => {
            const isActive = activeLyric?.id === line.id;
            return (
              <div
                key={line.id}
                ref={isActive ? activeLineRef : null}
                onClick={() => onSeek(line.startTimeSec)}
                className={`p-3.5 rounded-2xl cursor-pointer transition-all duration-200 border ${
                  isActive
                    ? 'bg-slate-800 border-amber-400/80 shadow-lg'
                    : 'bg-slate-950/40 border-transparent hover:bg-slate-800/40 text-slate-400'
                }`}
              >
                {/* Chords row above text */}
                <div className="flex items-center gap-3 flex-wrap mb-1">
                  {line.chordWords.map((cw, cIdx) => {
                    const isWordActive =
                      isActive && currentTimeSec >= cw.timestampSec;
                    return (
                      <span
                        key={cIdx}
                        className={`text-xs font-black px-2 py-0.5 rounded-md transition ${
                          isWordActive
                            ? 'bg-amber-400 text-slate-950 shadow-md font-black'
                            : 'bg-slate-800 text-sky-400 font-bold'
                        }`}
                      >
                        {cw.chord}
                      </span>
                    );
                  })}
                </div>

                {/* Lyrics text */}
                <div
                  className={`text-sm md:text-base transition font-medium ${
                    isActive ? 'text-white font-bold' : 'text-slate-400'
                  }`}
                >
                  {line.text}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};
