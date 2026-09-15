import React, { useEffect, useRef, useState } from 'react';
import { GuitarHeroNote, GuitarHeroSectionMarker } from '../types';
import { Flame, Video } from 'lucide-react';

interface Props {
  notes: GuitarHeroNote[];
  sectionMarkers: GuitarHeroSectionMarker[];
  currentTimeSec: number;
  bpm: number;
  isPlaying: boolean;
  onExportVideoClick: () => void;
  canvasRef: React.RefObject<HTMLCanvasElement>;
}

const LANE_COLORS = ['#22C55E', '#EF4444', '#EAB308', '#3B82F6', '#F97316'];
const LANE_NAMES = ['HIJAU', 'MERAH', 'KUNING', 'BIRU', 'JINGGA'];

export const GuitarHeroCanvas: React.FC<Props> = ({
  notes,
  sectionMarkers,
  currentTimeSec,
  bpm,
  onExportVideoClick,
  canvasRef
}) => {
  const [score, setScore] = useState(0);
  const [streak, setStreak] = useState(0);
  const [multiplier, setMultiplier] = useState(1);
  const [hitFlashes, setHitFlashes] = useState<{ [lane: number]: number }>({});
  const lastProcessedNoteRef = useRef<string | null>(null);

  // Check notes crossing hit line
  useEffect(() => {
    const hitWindow = 0.08;
    notes.forEach((note) => {
      const diff = note.timestampSec - currentTimeSec;
      if (diff >= -hitWindow && diff <= hitWindow && note.id !== lastProcessedNoteRef.current) {
        lastProcessedNoteRef.current = note.id;
        setHitFlashes((prev) => ({ ...prev, [note.lane]: Date.now() }));
        setStreak((s) => {
          const next = s + 1;
          setMultiplier(next >= 30 ? 4 : next >= 20 ? 3 : next >= 10 ? 2 : 1);
          return next;
        });
        setScore((sc) => sc + 50 * multiplier);
      }
    });
  }, [currentTimeSec, notes, multiplier]);

  // Render HTML5 Canvas
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const width = canvas.width;
    const height = canvas.height;
    const hitLineY = height - 70;
    const highwayTopY = 30;
    const lookAheadSec = 2.0;

    // Clear canvas
    ctx.fillStyle = '#060911';
    ctx.fillRect(0, 0, width, height);

    // Highway Geometry (3D Trapezoid perspective)
    const topWidth = width * 0.44;
    const bottomWidth = width * 0.94;
    const topX = (width - topWidth) / 2;
    const bottomX = (width - bottomWidth) / 2;

    // Highway Surface gradient
    const grad = ctx.createLinearGradient(0, highwayTopY, 0, height);
    grad.addColorStop(0, '#0F172A');
    grad.addColorStop(1, '#1E293B');

    ctx.beginPath();
    ctx.moveTo(topX, highwayTopY);
    ctx.lineTo(topX + topWidth, highwayTopY);
    ctx.lineTo(bottomX + bottomWidth, height - 10);
    ctx.lineTo(bottomX, height - 10);
    ctx.closePath();
    ctx.fillStyle = grad;
    ctx.fill();

    // Perspective Lane Dividers
    for (let i = 0; i <= 5; i++) {
      const frac = i / 5.0;
      const lx1 = topX + topWidth * frac;
      const lx2 = bottomX + bottomWidth * frac;

      ctx.strokeStyle = i === 0 || i === 5 ? '#38BDF8' : 'rgba(148, 163, 184, 0.25)';
      ctx.lineWidth = i === 0 || i === 5 ? 3 : 1.2;
      ctx.beginPath();
      ctx.moveTo(lx1, highwayTopY);
      ctx.lineTo(lx2, height - 10);
      ctx.stroke();
    }

    // Scrolling Beat Bars
    const secPerBeat = 60.0 / bpm;
    const beatOffset = (currentTimeSec % secPerBeat) / secPerBeat;
    for (let b = 0; b <= 6; b++) {
      const barProg = Math.max(0, Math.min(1, (b - beatOffset) / 6.0));
      const by = highwayTopY + (hitLineY - highwayTopY) * barProg;
      const curW = topWidth + (bottomWidth - topWidth) * barProg;
      const curLeft = (width - curW) / 2;

      ctx.strokeStyle = 'rgba(100, 116, 139, 0.25)';
      ctx.lineWidth = 1;
      ctx.beginPath();
      ctx.moveTo(curLeft, by);
      ctx.lineTo(curLeft + curW, by);
      ctx.stroke();
    }

    // Section Markers Floating down the highway
    sectionMarkers.forEach((marker) => {
      const timeDelta = marker.timestampSec - currentTimeSec;
      if (timeDelta >= -0.4 && timeDelta <= lookAheadSec) {
        const progress = Math.max(0, Math.min(1, 1.0 - timeDelta / lookAheadSec));
        const my = highwayTopY + (hitLineY - highwayTopY) * progress;
        const curW = topWidth + (bottomWidth - topWidth) * progress;
        const curLeft = (width - curW) / 2;

        ctx.fillStyle = marker.colorHex;
        ctx.beginPath();
        ctx.roundRect(curLeft + 4, my - 10, curW - 8, 20, 10);
        ctx.fill();

        ctx.strokeStyle = '#FFFFFF';
        ctx.lineWidth = 1.5;
        ctx.stroke();

        ctx.fillStyle = '#FFFFFF';
        ctx.font = 'bold 11px sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText(`★ ${marker.name.toUpperCase()} ★`, width / 2, my + 4);
      }
    });

    // Falling Notes / Chord Blocks
    const now = Date.now();
    const visibleNotes = notes.filter((n) => {
      const d = n.timestampSec - currentTimeSec;
      return d >= -0.2 && d <= lookAheadSec;
    });

    visibleNotes.forEach((note) => {
      const timeDelta = note.timestampSec - currentTimeSec;
      const progress = Math.max(0, Math.min(1.05, 1.0 - timeDelta / lookAheadSec));

      const ny = highwayTopY + (hitLineY - highwayTopY) * progress;
      const curW = topWidth + (bottomWidth - topWidth) * progress;
      const curLeft = (width - curW) / 2;
      const laneW = curW / 5.0;

      const nCenterX = curLeft + laneW * (note.lane + 0.5);
      const nRadius = 10 + progress * 16;
      const color = LANE_COLORS[note.lane];

      // Note glow
      ctx.save();
      ctx.shadowColor = color;
      ctx.shadowBlur = 12;
      ctx.fillStyle = color;
      ctx.beginPath();
      ctx.arc(nCenterX, ny, nRadius, 0, Math.PI * 2);
      ctx.fill();

      // White ring
      ctx.strokeStyle = '#FFFFFF';
      ctx.lineWidth = 2.5;
      ctx.stroke();

      // Dark jewel center
      ctx.fillStyle = '#0F172A';
      ctx.beginPath();
      ctx.arc(nCenterX, ny, nRadius * 0.35, 0, Math.PI * 2);
      ctx.fill();
      ctx.restore();
    });

    // Hit Line (Garis Sasaran)
    ctx.strokeStyle = '#F59E0B';
    ctx.lineWidth = 3.5;
    ctx.beginPath();
    ctx.moveTo(bottomX, hitLineY);
    ctx.lineTo(bottomX + bottomWidth, hitLineY);
    ctx.stroke();

    // 5 Receptor Target Rings
    const targetLaneW = bottomWidth / 5.0;
    for (let lane = 0; lane < 5; lane++) {
      const targetCenterX = bottomX + targetLaneW * (lane + 0.5);
      const baseColor = LANE_COLORS[lane];
      const flashTime = hitFlashes[lane] || 0;
      const isHit = now - flashTime < 180;

      if (isHit) {
        ctx.fillStyle = '#FFFFFF';
        ctx.beginPath();
        ctx.arc(targetCenterX, hitLineY, 28, 0, Math.PI * 2);
        ctx.fill();
      }

      ctx.strokeStyle = isHit ? '#FFFFFF' : baseColor;
      ctx.lineWidth = isHit ? 5 : 3;
      ctx.beginPath();
      ctx.arc(targetCenterX, hitLineY, 22, 0, Math.PI * 2);
      ctx.stroke();

      ctx.fillStyle = isHit ? baseColor : '#090D16';
      ctx.beginPath();
      ctx.arc(targetCenterX, hitLineY, 15, 0, Math.PI * 2);
      ctx.fill();
    }
  }, [currentTimeSec, notes, sectionMarkers, bpm, hitFlashes, canvasRef]);

  const activeMarker = sectionMarkers.filter((m) => m.timestampSec <= currentTimeSec).pop();

  return (
    <div className="w-full bg-slate-900 border border-slate-800 rounded-2xl p-4 shadow-xl flex flex-col gap-3">
      {/* Top Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-full bg-red-600 flex items-center justify-center">
            <Flame className="w-5 h-5 text-white" />
          </div>
          <div>
            <h3 className="text-white font-bold text-base">Visualizer Guitar Hero</h3>
            <p className="text-xs text-amber-400 font-semibold">
              {activeMarker ? `Bagian: ${activeMarker.name}` : 'Bersiap...'}
            </p>
          </div>
        </div>

        <button
          onClick={onExportVideoClick}
          className="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-amber-400 font-semibold text-xs border border-amber-500/30 transition shadow"
        >
          <Video className="w-4 h-4" />
          Export Video
        </button>
      </div>

      {/* HUD Stats */}
      <div className="flex items-center justify-between bg-slate-950/80 px-3 py-1.5 rounded-xl border border-slate-800 text-xs">
        <div className="flex items-center gap-1.5">
          <span className="text-slate-400">COMBO</span>
          <span
            className={`px-1.5 py-0.5 rounded font-black text-white text-[10px] ${
              multiplier === 4
                ? 'bg-purple-600'
                : multiplier === 3
                ? 'bg-blue-600'
                : multiplier === 2
                ? 'bg-emerald-600'
                : 'bg-amber-600'
            }`}
          >
            {multiplier}X
          </span>
          <span className="text-slate-500 text-[10px] ml-1">({streak} hit)</span>
        </div>

        <div className="font-mono font-bold text-slate-100">
          SKOR: {score.toLocaleString()}
        </div>
      </div>

      {/* Canvas */}
      <div className="relative w-full rounded-xl overflow-hidden border border-slate-800 bg-[#060911]">
        <canvas
          ref={canvasRef}
          width={720}
          height={480}
          className="w-full h-auto max-h-[380px] object-contain block mx-auto"
        />
      </div>

      {/* Interactive Fret Buttons */}
      <div className="grid grid-cols-5 gap-2">
        {LANE_NAMES.map((name, idx) => (
          <button
            key={idx}
            onClick={() => {
              setHitFlashes((prev) => ({ ...prev, [idx]: Date.now() }));
              setScore((s) => s + 25);
            }}
            style={{ backgroundColor: LANE_COLORS[idx] }}
            className="py-2.5 rounded-xl text-white font-black text-xs shadow-lg active:scale-95 transition"
          >
            {name}
          </button>
        ))}
      </div>
    </div>
  );
};
