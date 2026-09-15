import React, { useState, useEffect, useMemo } from 'react';
import { DEFAULT_SONGS } from './data/defaultSongs';
import { PracticeSong, SongSection } from './types';
import { audioEngine } from './utils/audioSynth';
import { ChordTabDisplay } from './components/ChordTabDisplay';
import { AudioPlayer } from './components/AudioPlayer';
import { Music, RefreshCw, Download, Smartphone, X, Check } from 'lucide-react';

export const App: React.FC = () => {
  const [currentSong, setCurrentSong] = useState<PracticeSong>(DEFAULT_SONGS[0]);
  const [isPlaying, setIsPlaying] = useState<boolean>(false);
  const [currentTimeSec, setCurrentTimeSec] = useState<number>(0);
  const [playbackSpeed, setPlaybackSpeed] = useState<number>(1.0);
  const [isLoopingSection, setIsLoopingSection] = useState<boolean>(false);
  const [loopingTargetSection] = useState<SongSection | null>(null);
  const [showInstallModal, setShowInstallModal] = useState<boolean>(false);
  const [deferredPrompt, setDeferredPrompt] = useState<any>(null);
  const [isInstalled, setIsInstalled] = useState<boolean>(false);

  // Catch PWA beforeinstallprompt event
  useEffect(() => {
    const handleBeforeInstall = (e: Event) => {
      e.preventDefault();
      setDeferredPrompt(e);
    };

    window.addEventListener('beforeinstallprompt', handleBeforeInstall);

    if (window.matchMedia('(display-mode: standalone)').matches) {
      setIsInstalled(true);
    }

    return () => {
      window.removeEventListener('beforeinstallprompt', handleBeforeInstall);
    };
  }, []);

  const handleTriggerInstall = async () => {
    if (deferredPrompt) {
      deferredPrompt.prompt();
      const { outcome } = await deferredPrompt.userChoice;
      if (outcome === 'accepted') {
        setIsInstalled(true);
      }
      setDeferredPrompt(null);
    } else {
      setShowInstallModal(true);
    }
  };

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
        {/* Navigation Header with Guitar Logo */}
        <header className="flex items-center justify-between bg-slate-900 border border-slate-800 rounded-2xl p-3.5 sm:p-4 shadow-lg">
          <div className="flex items-center gap-3">
            {/* Guitar App Icon */}
            <button
              onClick={() => setShowInstallModal(true)}
              title="Lihat Icon & Pasang ke Layar"
              className="group relative w-12 h-12 rounded-2xl overflow-hidden border-2 border-amber-400/80 shadow-lg shadow-amber-500/20 bg-slate-950 flex-shrink-0 transition transform active:scale-95 hover:border-amber-300"
            >
              <img
                src="/guitar-icon.svg"
                alt="Logo Gitar Aplikasi"
                className="w-full h-full object-cover"
                onError={(e) => {
                  (e.currentTarget as HTMLImageElement).src = '/app-icon.jpg';
                }}
              />
              <div className="absolute inset-0 bg-amber-400/10 opacity-0 group-hover:opacity-100 transition" />
            </button>

            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-white font-black text-base leading-tight">
                  Latihan Kord Gitar
                </h1>
                <button
                  onClick={handleTriggerInstall}
                  className="hidden xs:inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-amber-400/15 border border-amber-400/30 text-amber-300 text-[10px] font-bold hover:bg-amber-400/25 transition"
                >
                  <Smartphone className="w-3 h-3" />
                  <span>{isInstalled ? 'Terpasang' : 'Pasang di HP'}</span>
                </button>
              </div>
              <p className="text-xs text-sky-400 font-medium">
                {currentSong.title} • {currentSong.bpm} BPM
              </p>
            </div>
          </div>

          {/* Song Preset Dropdown, Install button & Reset */}
          <div className="flex items-center gap-1.5 sm:gap-2">
            <button
              onClick={() => setShowInstallModal(true)}
              title="Instal ke Layar Utama"
              className="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 border border-amber-500/30 text-amber-400 transition flex items-center gap-1 text-xs font-bold"
            >
              <Smartphone className="w-4 h-4" />
              <span className="hidden sm:inline">Pasang Icon</span>
            </button>

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
              className="bg-slate-800 border border-slate-700 text-amber-400 text-xs rounded-xl px-2.5 py-2 font-bold focus:outline-none focus:ring-1 focus:ring-amber-500 max-w-[130px] sm:max-w-none truncate"
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

        {/* Install & App Icon Modal */}
        {showInstallModal && (
          <div className="fixed inset-0 z-50 bg-black/75 backdrop-blur-sm flex items-center justify-center p-4">
            <div className="bg-slate-900 border border-amber-500/40 rounded-3xl p-5 sm:p-6 w-full max-w-md shadow-2xl flex flex-col gap-5 text-slate-100 animate-in fade-in zoom-in-95 duration-150">
              {/* Header */}
              <div className="flex items-center justify-between pb-3 border-b border-slate-800">
                <div className="flex items-center gap-2">
                  <Smartphone className="w-5 h-5 text-amber-400" />
                  <h3 className="font-extrabold text-base text-white">
                    Pasang Icon ke Layar Utama
                  </h3>
                </div>
                <button
                  onClick={() => setShowInstallModal(false)}
                  className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-400 hover:text-white transition"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              {/* Large Guitar Icon Preview */}
              <div className="flex flex-col items-center justify-center gap-3 py-2">
                <div className="w-24 h-24 rounded-3xl overflow-hidden border-2 border-amber-400 shadow-2xl shadow-amber-500/30 bg-slate-950 p-0.5">
                  <img
                    src="/guitar-icon.svg"
                    alt="Icon Gitar Aplikasi"
                    className="w-full h-full object-cover rounded-2xl"
                    onError={(e) => {
                      (e.currentTarget as HTMLImageElement).src = '/app-icon.jpg';
                    }}
                  />
                </div>
                <div className="text-center">
                  <h4 className="font-black text-lg text-amber-300">Latihan Kord Gitar</h4>
                  <p className="text-xs text-slate-400">
                    Icon gitar resmi untuk instalasi di layar HP & desktop
                  </p>
                </div>
              </div>

              {/* Install Trigger or Instructions */}
              {deferredPrompt ? (
                <button
                  onClick={handleTriggerInstall}
                  className="w-full py-3 px-4 bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 font-black rounded-2xl shadow-lg shadow-amber-500/30 flex items-center justify-center gap-2 transition text-sm"
                >
                  <Smartphone className="w-4 h-4" />
                  <span>Pasang Sekarang ke Layar HP</span>
                </button>
              ) : (
                <div className="bg-slate-950/70 border border-slate-800 rounded-2xl p-3.5 flex flex-col gap-2.5 text-xs">
                  <div className="font-bold text-amber-400">Cara Memasang ke Layar:</div>
                  <div className="flex items-start gap-2 text-slate-300">
                    <span className="w-5 h-5 rounded-full bg-slate-800 text-sky-400 flex items-center justify-center flex-shrink-0 font-bold text-[11px]">1</span>
                    <span><strong>Android / Chrome:</strong> Ketuk tombol menu browser (titik 3 di kanan atas), lalu pilih <strong>"Tambahkan ke Layar Utama"</strong> atau <strong>"Instal Aplikasi"</strong>.</span>
                  </div>
                  <div className="flex items-start gap-2 text-slate-300">
                    <span className="w-5 h-5 rounded-full bg-slate-800 text-sky-400 flex items-center justify-center flex-shrink-0 font-bold text-[11px]">2</span>
                    <span><strong>iPhone / Safari:</strong> Ketuk ikon <strong>Bagikan (Share)</strong> di bagian bawah, lalu pilih <strong>"Tambah ke Layar Utama"</strong>.</span>
                  </div>
                  <div className="flex items-start gap-2 text-slate-300">
                    <span className="w-5 h-5 rounded-full bg-slate-800 text-emerald-400 flex items-center justify-center flex-shrink-0 font-bold text-[11px]">✓</span>
                    <span>Aplikasi akan otomatis muncul di beranda dengan logo icon gitar elegan ini!</span>
                  </div>
                </div>
              )}

              {/* Download Icon Assets */}
              <div className="flex items-center gap-2 pt-1 border-t border-slate-800">
                <a
                  href="/guitar-icon.svg"
                  download="logo-gitar.svg"
                  className="flex-1 py-2 px-3 bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-xl text-xs font-bold text-slate-200 flex items-center justify-center gap-1.5 transition text-center"
                >
                  <Download className="w-3.5 h-3.5 text-amber-400" />
                  <span>Unduh SVG</span>
                </a>
                <a
                  href="/app-icon.jpg"
                  download="logo-gitar.jpg"
                  className="flex-1 py-2 px-3 bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-xl text-xs font-bold text-slate-200 flex items-center justify-center gap-1.5 transition text-center"
                >
                  <Download className="w-3.5 h-3.5 text-sky-400" />
                  <span>Unduh JPG</span>
                </a>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default App;
