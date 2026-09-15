import React from 'react';
import { SongSection } from '../types';
import { Layers, Play, Repeat } from 'lucide-react';

interface Props {
  sections: SongSection[];
  activeSection: SongSection | null;
  currentPositionSec: number;
  isLoopingSection: boolean;
  onJumpToSection: (section: SongSection) => void;
  onToggleLoopSection: (section: SongSection) => void;
}

export const SectionList: React.FC<Props> = ({
  sections,
  activeSection,
  currentPositionSec,
  isLoopingSection,
  onJumpToSection,
  onToggleLoopSection
}) => {
  return (
    <div className="w-full bg-slate-900 border border-slate-800 rounded-2xl p-4 shadow-xl flex flex-col gap-3">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Layers className="w-5 h-5 text-amber-500" />
          <h3 className="text-white font-bold text-base">Struktur Lagu (Patokan Latihan)</h3>
        </div>
        <span className="text-xs text-slate-400 font-medium">{sections.length} Bagian</span>
      </div>

      {/* List */}
      <div className="flex flex-col gap-2">
        {sections.map((sec) => {
          const isActive = activeSection?.id === sec.id;
          const duration = sec.endTimeSec - sec.startTimeSec;
          const secProgress = isActive
            ? Math.max(0, Math.min(1, (currentPositionSec - sec.startTimeSec) / duration))
            : 0;

          return (
            <div
              key={sec.id}
              onClick={() => onJumpToSection(sec)}
              style={{ borderColor: isActive ? sec.colorHex : 'rgba(30, 41, 59, 0.8)' }}
              className={`relative overflow-hidden p-3 rounded-xl border transition cursor-pointer flex items-center justify-between ${
                isActive ? 'bg-slate-800/90 shadow-md' : 'bg-slate-950/60 hover:bg-slate-850'
              }`}
            >
              {/* Internal progress fill for active section */}
              {isActive && (
                <div
                  style={{ width: `${secProgress * 100}%`, backgroundColor: sec.colorHex }}
                  className="absolute left-0 top-0 bottom-0 opacity-10 pointer-events-none transition-all duration-100"
                />
              )}

              <div className="flex items-center gap-3 z-10">
                <div
                  style={{ backgroundColor: sec.colorHex }}
                  className="w-3 h-3 rounded-full flex-shrink-0"
                />
                <div>
                  <div className="flex items-center gap-2">
                    <span className="text-white font-bold text-sm">{sec.name}</span>
                    {isActive && (
                      <span className="px-1.5 py-0.2 bg-emerald-500/20 text-emerald-400 text-[10px] rounded font-extrabold">
                        AKTIF
                      </span>
                    )}
                  </div>
                  <div className="text-xs text-slate-400 font-mono">
                    {sec.startTimeSec}s - {sec.endTimeSec}s ({duration}s) • {sec.chords.map((c) => c.name).join(' - ')}
                  </div>
                </div>
              </div>

              {/* Action buttons */}
              <div className="flex items-center gap-1 z-10">
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    onToggleLoopSection(sec);
                  }}
                  className={`p-1.5 rounded-lg text-xs transition ${
                    isActive && isLoopingSection
                      ? 'bg-amber-500 text-slate-950 font-bold'
                      : 'text-slate-400 hover:text-white hover:bg-slate-800'
                  }`}
                  title="Ulangi bagian ini"
                >
                  <Repeat className="w-3.5 h-3.5" />
                </button>

                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    onJumpToSection(sec);
                  }}
                  className="p-1.5 rounded-lg text-slate-400 hover:text-sky-400 hover:bg-slate-800 transition"
                  title="Lompat ke bagian ini"
                >
                  <Play className="w-3.5 h-3.5 fill-current" />
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
