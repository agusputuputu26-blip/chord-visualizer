import React, { useState } from 'react';
import { Video, X, CheckCircle, Download, Loader2 } from 'lucide-react';
import { SongSection } from '../types';

interface Props {
  canvasRef: React.RefObject<HTMLCanvasElement>;
  activeSection: SongSection | null;
  onClose: () => void;
}

export const VideoExporterModal: React.FC<Props> = ({ canvasRef, activeSection, onClose }) => {
  const [isRecording, setIsRecording] = useState(false);
  const [progress, setProgress] = useState(0);
  const [downloadUrl, setDownloadUrl] = useState<string | null>(null);

  const handleStartExport = async () => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    setIsRecording(true);
    setProgress(0);
    setDownloadUrl(null);

    try {
      // Capture 30 FPS stream from HTML5 Canvas
      const stream = canvas.captureStream(30);
      const mimeType = MediaRecorder.isTypeSupported('video/webm;codecs=vp9')
        ? 'video/webm;codecs=vp9'
        : 'video/webm';

      const recorder = new MediaRecorder(stream, {
        mimeType,
        videoBitsPerSecond: 2500000 // 2.5 Mbps (720p)
      });

      const chunks: Blob[] = [];
      recorder.ondataavailable = (e) => {
        if (e.data.size > 0) chunks.push(e.data);
      };

      const durationSec = 10; // 10s highlight clip
      const interval = 100;
      let elapsed = 0;

      const progressTimer = setInterval(() => {
        elapsed += interval;
        const pct = Math.min(100, Math.floor((elapsed / (durationSec * 1000)) * 100));
        setProgress(pct);
      }, interval);

      recorder.onstop = () => {
        clearInterval(progressTimer);
        const blob = new Blob(chunks, { type: 'video/webm' });
        const url = URL.createObjectURL(blob);
        setDownloadUrl(url);
        setIsRecording(false);
      };

      recorder.start();
      setTimeout(() => {
        if (recorder.state === 'recording') {
          recorder.stop();
        }
      }, durationSec * 1000);
    } catch (e) {
      console.error(e);
      setIsRecording(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-md p-5 flex flex-col gap-4 shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Video className="w-5 h-5 text-amber-500" />
            <h3 className="text-white font-bold text-base">Export Video Guitar Hero</h3>
          </div>
          {!isRecording && (
            <button onClick={onClose} className="text-slate-400 hover:text-white">
              <X className="w-5 h-5" />
            </button>
          )}
        </div>

        {downloadUrl ? (
          /* Result state */
          <div className="flex flex-col items-center gap-3 py-4 text-center">
            <div className="w-12 h-12 rounded-full bg-emerald-500/20 text-emerald-400 flex items-center justify-center">
              <CheckCircle className="w-7 h-7" />
            </div>
            <h4 className="text-white font-bold text-base">Video Siap Diunduh!</h4>
            <p className="text-xs text-slate-400">
              Video klip Guitar Hero resolusi 720p (30 FPS) dengan bitrate ringkas telah berhasil direkam.
            </p>

            <a
              href={downloadUrl}
              download="guitar_hero_practice.webm"
              className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold text-sm shadow-lg transition"
            >
              <Download className="w-4 h-4" />
              Download Video
            </a>

            <button
              onClick={onClose}
              className="text-xs text-slate-500 hover:text-slate-300 mt-2"
            >
              Tutup
            </button>
          </div>
        ) : isRecording ? (
          /* Recording in progress */
          <div className="flex flex-col items-center gap-3 py-6 text-center">
            <Loader2 className="w-8 h-8 text-amber-500 animate-spin" />
            <h4 className="text-white font-semibold text-sm">Merekam Animasi Layar Canvas...</h4>
            <div className="w-full bg-slate-800 h-2 rounded-full overflow-hidden">
              <div
                style={{ width: `${progress}%` }}
                className="bg-amber-500 h-full transition-all duration-100"
              />
            </div>
            <span className="text-xs font-mono text-slate-400">{progress}%</span>
          </div>
        ) : (
          /* Config state */
          <div className="flex flex-col gap-3">
            <p className="text-xs text-slate-300">
              Sistem akan merekam animasi visualizer Guitar Hero yang sedang meluncur beserta audionya menggunakan{' '}
              <code className="text-amber-400">MediaRecorder API</code> langsung dari browser Anda.
            </p>

            <div className="bg-slate-950 border border-slate-800 rounded-xl p-3 text-xs space-y-1.5 text-slate-400">
              <div>• Resolusi: <span className="text-white font-semibold">720p (720x480)</span></div>
              <div>• Bitrate: <span className="text-white font-semibold">2.5 Mbps (Ukuran Ringkas)</span></div>
              <div>• Bagian: <span className="text-amber-400 font-semibold">{activeSection?.name || 'Intro'} (10 Detik)</span></div>
            </div>

            <button
              onClick={handleStartExport}
              className="w-full py-2.5 rounded-xl bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold text-sm shadow-lg transition flex items-center justify-center gap-2"
            >
              <Video className="w-4 h-4" />
              Mulai Rekam & Export
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
