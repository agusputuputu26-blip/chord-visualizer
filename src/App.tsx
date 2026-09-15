import React, { useState, useEffect, useRef, useMemo } from 'react';
import { DEFAULT_SONGS, generateGuitarHeroTrack } from './data/defaultSongs';
import { PracticeSong, SongSection, PracticeThemeMode } from './types';
import { audioSynth } from './utils/audioSynth';
import { GuitarHeroCanvas } from './components/GuitarHeroCanvas';
import { ChordTabDisplay } from './components/ChordTabDisplay';
import { AudioPlayer } from './components/AudioPlayer';
import { SectionList } from './components/SectionList';
import { VideoExporterModal } from './components/VideoExporterModal';
import { Music, Zap, Layers, RefreshCw } from 'lucide-react';

export const App: React.FC = () => {
  const [currentSong, setCurrentSong] = useState<PracticeSong>(DEFAULT_SONGS[0]);
  const [themeMode, setThemeMode] = useState<PracticeThemeMode>('chord_tab');
  const [isPlaying, setIsPlaying] = useState<boolean>(false);
  const [currentTimeSec, setCurrentTimeSec] = useState<number>(0);
  const [playbackSpeed, setPlaybackSpeed] = useState<number>(1.0);
  const [isLoopingSection, setIsLoopingSection] = useState<boolean>(false);
  const [loopingTargetSection, setLoopingTargetSection] = useState<SongSection | null>(null);
  const [isExportModalOpen, setIsExportModalOpen] = useState<boolean>(false);

  const canvasRef = useRef<HTMLCanvasElement>(null);

  // Generate Guitar Hero track notes
  const { notes, markers } = useMemo(() => {
    return generateGuitarHeroTrack(currentSong);
  }, [currentSong]);

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
    audioSynth.onTimeUpdate = (time) => {
      // Handle section looping if enabled
      if (isLoopingSection && loopingTargetSection) {
        if (time >= loopingTargetSection.endTimeSec) {
          audioSynth.seek(loopingTargetSection.startTimeSec);
          setCurrentTimeSec(loopingTargetSection.startTimeSec);
          return;
        }
      }
      setCurrentTimeSec(time);
    };

    audioSynth.onEnded = () => {
      setIsPlaying(false);
      setCurrentTimeSec(0);
    };
  }, [isLoopingSection, loopingTargetSection]);

  const handleTogglePlay = () => {
    if (isPlaying) {
      audioSynth.pause();
      setIsPlaying(false);
    } else {
      audioSynth.play(currentSong, currentTimeSec);
      setIsPlaying(true);
    }
  };

  const handleSeek = (pos: number) => {
    setCurrentTimeSec(pos);
    audioSynth.seek(pos);
  };

  const handleSkip = (delta: number) => {
    const next = Math.max(0, Math.min(currentSong.totalDurationSec, currentTimeSec + delta));
    handleSeek(next);
  };

  const handleFileUpload = (file: File) => {
    const url = URL.createObjectURL(file);
    audioSynth.setCustomAudioUrl(url);
    setCurrentSong((prev) => ({
      ...prev,
      title: file.name.replace(/\.[^/.]+$/, ''),
      audioFileName: file.name,
      audioUrl: url
    }));
  };

  const handleJumpToSection = (section: SongSection) => {
    handleSeek(section.startTimeSec);
  };

  const handleToggleLoopSection = (section: SongSection) => {
    if (isLoopingSection && loopingTargetSection?.id === section.id) {
      setIsLoopingSection(false);
      setLoopingTargetSection(null);
    } else {
      setIsLoopingSection(true);
      setLoopingTargetSection(section);
      handleSeek(section.startTimeSec);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col items-center p-3 sm:p-6 pb-24">
      <div className="w-full max-w-xl flex flex-col gap-4">
        {/* Navigation Header */}
        <header className="flex items-center justify-between bg-slate-900 border border-slate-800 rounded-2xl p-4 shadow-lg">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-full bg-amber-500 flex items-center justify-center shadow-md">
              <Music className="w-5 h-5 text-slate-950" />
            </div>
            <div>
              <h1 className="text-white font-extrabold text-base leading-tight">
                Latihan Musik Interaktif
              </h1>
              <p className="text-xs text-slate-400 font-medium">
                {currentSong.bpm} BPM • {currentSong.sections.length} Bagian
              </p>
            </div>
          </div>

          {/* Song Preset Dropdown */}
          <div className="flex items-center gap-1.5">
            <select
              value={currentSong.id}
              onChange={(e) => {
                const s = DEFAULT_SONGS.find((x) => x.id === e.target.value);
                if (s) {
                  audioSynth.stop();
                  setIsPlaying(false);
                  setCurrentTimeSec(0);
                  setCurrentSong(s);
                }
              }}
              className="bg-slate-800 border border-slate-700 text-sky-400 text-xs rounded-xl px-2.5 py-1.5 font-semibold focus:outline-none focus:ring-1 focus:ring-sky-500"
            >
              {DEFAULT_SONGS.map((song) => (
                <option key={song.id} value={song.id}>
                  {song.title}
                </option>
              ))}
            </select>
          </div>
        </header>

        {/* Theme Selection Card */}
        <section className="bg-slate-900 border border-slate-800 rounded-2xl p-3 shadow-lg flex flex-col gap-2.5">
          <div className="flex items-center justify-between text-xs px-1">
            <span className="font-bold text-slate-400 uppercase tracking-wider flex items-center gap-1.5">
              <Layers className="w-3.5 h-3.5 text-amber-400" />
              PILIH TEMA TAMPILAN
            </span>
            <span className="text-[11px] font-semibold text-amber-400">
              {themeMode === 'guitar_hero'
                ? 'Tema Guitar Hero'
                : themeMode === 'chord_tab'
                ? 'Tema Tabulasi Standar'
                : 'Tema Gabungan'}
            </span>
          </div>

          <div className="grid grid-cols-3 gap-2">
            {/* Option 1: Tab Standar */}
            <button
              onClick={() => setThemeMode('chord_tab')}
              className={`p-2.5 rounded-xl border flex flex-col items-center gap-1 transition ${
                themeMode === 'chord_tab'
                  ? 'bg-sky-600 text-white border-sky-400 shadow-md scale-[1.02]'
                  : 'bg-slate-950 border-slate-800 text-slate-400 hover:border-slate-700'
              }`}
            >
              <Music className="w-4 h-4" />
              <span className="font-bold text-xs">Tab Standar</span>
              <span className="text-[10px] opacity-80">Fretboard & Tab</span>
            </button>

            {/* Option 2: Guitar Hero */}
            <button
              onClick={() => setThemeMode('guitar_hero')}
              className={`p-2.5 rounded-xl border flex flex-col items-center gap-1 transition ${
                themeMode === 'guitar_hero'
                  ? 'bg-red-600 text-white border-red-400 shadow-md scale-[1.02]'
                  : 'bg-slate-950 border-slate-800 text-slate-400 hover:border-slate-700'
              }`}
            >
              <Zap className="w-4 h-4" />
              <span className="font-bold text-xs">Guitar Hero</span>
              <span className="text-[10px] opacity-80">Balok Meluncur</span>
            </button>

            {/* Option 3: Both */}
            <button
              onClick={() => setThemeMode('split_both')}
              className={`p-2.5 rounded-xl border flex flex-col items-center gap-1 transition ${
                themeMode === 'split_both'
                  ? 'bg-amber-600 text-white border-amber-400 shadow-md scale-[1.02]'
                  : 'bg-slate-950 border-slate-800 text-slate-400 hover:border-slate-700'
              }`}
            >
              <span className="text-sm">🔀</span>
              <span className="font-bold text-xs">Keduanya</span>
              <span className="text-[10px] opacity-80">Split View</span>
            </button>
          </div>
        </section>

        {/* Audio Player Controller */}
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

        {/* Primary Views based on Theme */}
        {(themeMode === 'guitar_hero' || themeMode === 'split_both') && (
          <GuitarHeroCanvas
            notes={notes}
            sectionMarkers={markers}
            currentTimeSec={currentTimeSec}
            bpm={currentSong.bpm}
            isPlaying={isPlaying}
            onExportVideoClick={() => setIsExportModalOpen(true)}
            canvasRef={canvasRef}
          />
        )}

        {(themeMode === 'chord_tab' || themeMode === 'split_both') && (
          <ChordTabDisplay
            activeSection={activeSection}
            currentTimeSec={currentTimeSec}
          />
        )}

        {/* Song Structure Panel */}
        <SectionList
          sections={currentSong.sections}
          activeSection={activeSection}
          currentPositionSec={currentTimeSec}
          isLoopingSection={isLoopingSection}
          onJumpToSection={handleJumpToSection}
          onToggleLoopSection={handleToggleLoopSection}
        />
      </div>

      {/* Video Exporter Modal */}
      {isExportModalOpen && (
        <VideoExporterModal
          canvasRef={canvasRef}
          activeSection={activeSection}
          onClose={() => setIsExportModalOpen(false)}
        />
      )}
    </div>
  );
};
export default App;
