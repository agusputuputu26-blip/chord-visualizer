import React, { useRef } from 'react';
import {
  Play,
  Pause,
  RotateCcw,
  RotateCw,
  Upload,
  Repeat,
  Gauge
} from 'lucide-react';

interface Props {
  isPlaying: boolean;
  currentTimeSec: number;
  durationSec: number;
  playbackSpeed: number;
  isLooping: boolean;
  songTitle: string;
  onTogglePlay: () => void;
  onSeek: (posSec: number) => void;
  onSkip: (deltaSec: number) => void;
  onSpeedChange: (speed: number) => void;
  onToggleLoop: () => void;
  onFileUpload: (file: File) => void;
}

export const AudioPlayer: React.FC<Props> = ({
  isPlaying,
  currentTimeSec,
  durationSec,
  playbackSpeed,
  isLooping,
  songTitle,
  onTogglePlay,
  onSeek,
  onSkip,
  onSpeedChange,
  onToggleLoop,
  onFileUpload
}) => {
  const fileInputRef = useRef<HTMLInputElement>(null);

  const formatTime = (secs: number) => {
    const m = Math.floor(secs / 60);
    const s = Math.floor(secs % 60);
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  return (
    <div className="w-full bg-slate-900 border border-slate-800 rounded-2xl p-4 shadow-xl flex flex-col gap-3">
      {/* Title & File Upload */}
      <div className="flex items-center justify-between">
        <div>
          <span className="text-[10px] text-amber-500 font-bold tracking-wider uppercase">
            PEMUTAR AUDIO & LATIHAN
          </span>
          <h2 className="text-white font-bold text-base">{songTitle}</h2>
        </div>

        <div>
          <input
            type="file"
            ref={fileInputRef}
            accept="audio/*"
            className="hidden"
            onChange={(e) => {
              const file = e.target.files?.[0];
              if (file) onFileUpload(file);
            }}
          />
          <button
            onClick={() => fileInputRef.current?.click()}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-sky-400 text-xs font-semibold border border-slate-700 transition"
          >
            <Upload className="w-3.5 h-3.5" />
            Upload Audio
          </button>
        </div>
      </div>

      {/* Progress Slider */}
      <div className="space-y-1">
        <input
          type="range"
          min={0}
          max={durationSec || 1}
          step={0.1}
          value={currentTimeSec}
          onChange={(e) => onSeek(parseFloat(e.target.value))}
          className="w-full h-1.5 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-amber-500"
        />
        <div className="flex justify-between text-[11px] font-mono text-slate-400">
          <span>{formatTime(currentTimeSec)}</span>
          <span>{formatTime(durationSec)}</span>
        </div>
      </div>

      {/* Player Controls */}
      <div className="flex items-center justify-between pt-1">
        {/* Playback speed selector */}
        <div className="flex items-center gap-1">
          <Gauge className="w-3.5 h-3.5 text-slate-500" />
          {[0.75, 1.0, 1.25].map((s) => (
            <button
              key={s}
              onClick={() => onSpeedChange(s)}
              className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                playbackSpeed === s
                  ? 'bg-amber-500 text-slate-950'
                  : 'bg-slate-800 text-slate-400 hover:bg-slate-700'
              }`}
            >
              {s}x
            </button>
          ))}
        </div>

        {/* Play / Skip Buttons */}
        <div className="flex items-center gap-2">
          <button
            onClick={() => onSkip(-5)}
            className="p-2 rounded-full text-slate-400 hover:text-white hover:bg-slate-800 transition"
            title="Mundur 5 detik"
          >
            <RotateCcw className="w-4 h-4" />
          </button>

          <button
            onClick={onTogglePlay}
            className="w-12 h-12 rounded-full bg-amber-500 hover:bg-amber-400 text-slate-950 flex items-center justify-center shadow-lg transition active:scale-95"
          >
            {isPlaying ? <Pause className="w-6 h-6 fill-current" /> : <Play className="w-6 h-6 fill-current ml-0.5" />}
          </button>

          <button
            onClick={() => onSkip(5)}
            className="p-2 rounded-full text-slate-400 hover:text-white hover:bg-slate-800 transition"
            title="Maju 5 detik"
          >
            <RotateCw className="w-4 h-4" />
          </button>
        </div>

        {/* Loop Toggle */}
        <button
          onClick={onToggleLoop}
          className={`flex items-center gap-1 px-2.5 py-1 rounded-xl text-xs font-semibold transition ${
            isLooping
              ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
              : 'text-slate-400 hover:bg-slate-800'
          }`}
        >
          <Repeat className="w-3.5 h-3.5" />
          Loop
        </button>
      </div>
    </div>
  );
};
