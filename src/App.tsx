import React, { useState, useEffect, useMemo } from 'react';
import { DEFAULT_SONGS } from './data/defaultSongs';
import { PracticeSong, SongSection } from './types';
import { audioEngine } from './utils/audioSynth';
import { ChordTabDisplay } from './components/ChordTabDisplay';
import { AudioPlayer } from './components/AudioPlayer';
import { Music, RefreshCw } from 'lucide-react';

export const App: React.FC = () => {
  const [currentSong, setCurrentSong] = useState<PracticeSong>(DEFAULT_SONGS[0]);
  const [isPlaying, setIsPlaying] = useState<boolean>(false);
  const [currentTimeSec, setCurrentTimeSec] = useState<number>(0);
  const [playbackSpeed, setPlaybackSpeed] = useState<number>(1.0);
  const [isLoopingSection, setIsLoopingSection] = useState<boolean>(false);
  const [loopingTargetSection] = useState<SongSection | null>(null);

  // Current active section
  const activeSection = useMemo(() => {
    return (
      currentSong.sections.find(
        (s) => currentTimeSec >= s.startTimeSec && currentTimeSec < s.endTimeSec
      ) || currentSong.sections[0]
    );
  }, [currentSong, currentTimeSec]);

  // Audio time update handler
  useEffect(() => {
    audioEngine.onTimeUpdate = (time) => {
      if (isLoopingSection && loopingTargetSection) {
        if (time >= loopingTargetSection.endTimeSec) {
          audioEngine.seek(loopingTargetSection.startTimeSec);
          setCurrentTimeSec(loopingTargetSection.startTimeSec);
          return;
        }
      }
      setCurrentTimeSec(time);
    };

    audioEngine.onEnded = () => {
      setIsPlaying(false);
      setCurrentTimeSec(0);
    };
  }, [isLoopingSection, loopingTargetSection]);

  const handleTogglePlay = () => {
    if (isPlaying) {
      audioEngine.pause();
      setIsPlaying(false);
    } else {
      audioEngine.play(currentSong, currentTimeSec);
      setIsPlaying(true);
    }
  };

  const handleSeek = (pos: number) => {
    setCurrentTimeSec(pos);
    audioEngine.seek(pos);
  };

  const handleSkip = (delta: number) => {
    const next = Math.max(0, Math.min(currentSong.totalDurationSec, currentTimeSec + delta));
    handleSeek(next);
  };

  const handleFileUpload = (file: File) => {
    const url = URL.createObjectURL(file);
    audioEngine.setCustomAudioUrl(url);
    setCurrentSong((prev) => ({
      ...prev,
      title: file.name.replace(/\.[^/.]+$/, ''),
      audioFileName: file.name,
      audioUrl: url
    }));
  };

  const handleResetSong = () => {
    audioEngine.stop();
    setIsPlaying(false);
    setCurrentTimeSec(0);
    setCurrentSong(DEFAULT_SONGS[0]);
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col items-center p-3 sm:p-6 pb-24">
      <div className="w-full max-w-xl flex flex-col gap-4">
        {/* Navigation Header */}
        <header className="flex items-center justify-between bg-slate-900 border border-slate-800 rounded-2xl p-4 shadow-lg">
          <div className="flex items-center gap-2.5">
            <div className="w-10 h-10 rounded-full bg-amber-500 flex items-center justify-center shadow-md">
              <Music className="w-5 h-5 text-slate-950" />
            </div>
            <div>
              <h1 className="text-white font-extrabold text-base leading-tight">
                Latihan Kord & Musik
              </h1>
              <p className="text-xs text-sky-400 font-medium">
                {currentSong.title} • {currentSong.bpm} BPM
              </p>
            </div>
          </div>

          {/* Song Preset Dropdown & Reset */}
          <div className="flex items-center gap-2">
            <select
              value={currentSong.id}
              onChange={(e) => {
                const s = DEFAULT_SONGS.find((x) => x.id === e.target.value);
                if (s) {
                  audioEngine.stop();
                  setIsPlaying(false);
                  setCurrentTimeSec(0);
                  setCurrentSong(s);
                }
              }}
              className="bg-slate-800 border border-slate-700 text-amber-400 text-xs rounded-xl px-2.5 py-2 font-bold focus:outline-none focus:ring-1 focus:ring-amber-500"
            >
              {DEFAULT_SONGS.map((song) => (
                <option key={song.id} value={song.id}>
                  {song.title}
                </option>
              ))}
            </select>

            <button
              onClick={handleResetSong}
              title="Reset Lagu"
              className="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-300 transition"
            >
              <RefreshCw className="w-4 h-4" />
            </button>
          </div>
        </header>

        {/* Real-time Chord Display & Synchronized Scrolling Lyrics */}
        <ChordTabDisplay
          activeSection={activeSection}
          lyrics={currentSong.lyrics || []}
          currentTimeSec={currentTimeSec}
          bpm={currentSong.bpm}
          isPlaying={isPlaying}
          onSeek={handleSeek}
        />

        {/* Audio Player Controller Bar */}
        <AudioPlayer
          isPlaying={isPlaying}
          currentTimeSec={currentTimeSec}
          durationSec={currentSong.totalDurationSec}
          playbackSpeed={playbackSpeed}
          isLooping={isLoopingSection}
          songTitle={currentSong.title}
          onTogglePlay={handleTogglePlay}
          onSeek={handleSeek}
          onSkip={handleSkip}
          onSpeedChange={setPlaybackSpeed}
          onToggleLoop={() => setIsLoopingSection(!isLoopingSection)}
          onFileUpload={handleFileUpload}
        />
      </div>
    </div>
  );
};

export default App;
