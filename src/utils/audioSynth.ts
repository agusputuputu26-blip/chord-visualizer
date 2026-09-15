import { PracticeSong } from '../types';
import { CHORD_FINGERINGS } from '../data/defaultSongs';

const STRING_BASE_FREQS = [82.41, 110.0, 146.83, 196.0, 246.94, 329.63];

class WebAudioEngine {
  private ctx: AudioContext | null = null;
  private isRunning = false;
  private currentSong: PracticeSong | null = null;
  private startTime = 0;
  private pauseOffset = 0;
  private timerId: number | null = null;
  private customAudio: HTMLAudioElement | null = null;

  public onTimeUpdate: ((time: number) => void) | null = null;
  public onEnded: (() => void) | null = null;

  private initContext() {
    if (!this.ctx) {
      const AudioCtx = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
      this.ctx = new AudioCtx();
    }
    if (this.ctx.state === 'suspended') {
      this.ctx.resume();
    }
  }

  public setCustomAudioUrl(url: string) {
    if (this.customAudio) {
      this.customAudio.pause();
      this.customAudio.src = '';
    }
    this.customAudio = new Audio(url);
    this.customAudio.addEventListener('timeupdate', () => {
      if (this.customAudio) {
        this.onTimeUpdate?.(this.customAudio.currentTime);
      }
    });
    this.customAudio.addEventListener('ended', () => {
      this.onEnded?.();
    });
  }

  public play(song: PracticeSong, startOffsetSec: number = 0) {
    this.initContext();
    this.currentSong = song;
    this.isRunning = true;
    this.pauseOffset = startOffsetSec;
    this.startTime = (this.ctx?.currentTime ?? 0) - startOffsetSec;

    if (this.customAudio) {
      this.customAudio.currentTime = startOffsetSec;
      this.customAudio.play().catch(console.error);
      return;
    }

    if (this.timerId) clearInterval(this.timerId);
    this.timerId = window.setInterval(() => {
      if (!this.isRunning || !this.ctx || !this.currentSong) return;
      const currentPos = this.ctx.currentTime - this.startTime;
      if (currentPos >= this.currentSong.totalDurationSec) {
        this.stop();
        this.onEnded?.();
        return;
      }
      this.onTimeUpdate?.(currentPos);
      this.playSynthNoteAtTime(currentPos);
    }, 40);
  }

  public pause() {
    this.isRunning = false;
    if (this.timerId) {
      clearInterval(this.timerId);
      this.timerId = null;
    }
    if (this.ctx) {
      this.pauseOffset = this.ctx.currentTime - this.startTime;
    }
    if (this.customAudio) {
      this.customAudio.pause();
    }
  }

  public stop() {
    this.isRunning = false;
    if (this.timerId) {
      clearInterval(this.timerId);
      this.timerId = null;
    }
    this.pauseOffset = 0;
    this.lastBeepBeat = -1;
    if (this.customAudio) {
      this.customAudio.pause();
      this.customAudio.currentTime = 0;
    }
  }

  public seek(seconds: number) {
    if (this.ctx) {
      this.startTime = this.ctx.currentTime - seconds;
    }
    this.pauseOffset = seconds;
    this.lastBeepBeat = -1;
    if (this.customAudio) {
      this.customAudio.currentTime = seconds;
    }
    this.onTimeUpdate?.(seconds);
  }

  private lastBeepBeat = -1;

  private playSynthNoteAtTime(currentTime: number) {
    if (!this.ctx || !this.currentSong) return;
    const secondsPerBeat = 60.0 / this.currentSong.bpm;
    const currentBeat = Math.floor(currentTime / secondsPerBeat);

    if (currentBeat !== this.lastBeepBeat) {
      this.lastBeepBeat = currentBeat;

      // Find active section and chord
      const sections = this.currentSong.sections;
      const activeSection = sections.find(
        (s) => currentTime >= s.startTimeSec && currentTime < s.endTimeSec
      ) || sections[0];

      if (activeSection && activeSection.chords.length > 0) {
        const secOffset = Math.max(0, currentTime - activeSection.startTimeSec);
        const duration = Math.max(0.1, activeSection.endTimeSec - activeSection.startTimeSec);
        const chordDuration = duration / activeSection.chords.length;
        const chordIdx = Math.min(
          Math.floor(secOffset / chordDuration),
          activeSection.chords.length - 1
        );
        const activeChord = activeSection.chords[chordIdx];
        const fingering = CHORD_FINGERINGS[activeChord.name];

        const beatInMeasure = currentBeat % 4;

        if (fingering) {
          const now = this.ctx.currentTime;

          if (beatInMeasure === 0) {
            // Beat 1: Full rich acoustic chord strum
            fingering.frets.forEach((fret, strIdx) => {
              if (fret >= 0 && this.ctx) {
                const strumDelay = strIdx * 0.012; // 12ms delay per string
                const noteTime = now + strumDelay;
                const baseFreq = STRING_BASE_FREQS[strIdx];
                const noteFreq = baseFreq * Math.pow(2, fret / 12);

                const osc = this.ctx.createOscillator();
                const gain = this.ctx.createGain();

                osc.type = 'triangle';
                osc.frequency.setValueAtTime(noteFreq, noteTime);

                gain.gain.setValueAtTime(0.18, noteTime);
                gain.gain.exponentialRampToValueAtTime(0.0001, noteTime + 1.2);

                osc.connect(gain);
                gain.connect(this.ctx.destination);

                osc.start(noteTime);
                osc.stop(noteTime + 1.2);
              }
            });
          } else {
            // Beats 2, 3, 4: Rhythmic arpeggio of chord notes
            const strIdx = beatInMeasure === 1 ? 2 : beatInMeasure === 2 ? 3 : 4;
            const fret = fingering.frets[strIdx] ?? 0;
            if (fret >= 0) {
              const baseFreq = STRING_BASE_FREQS[strIdx];
              const noteFreq = baseFreq * Math.pow(2, fret / 12);

              const osc = this.ctx.createOscillator();
              const gain = this.ctx.createGain();

              osc.type = 'triangle';
              osc.frequency.setValueAtTime(noteFreq, now);

              gain.gain.setValueAtTime(0.15, now);
              gain.gain.exponentialRampToValueAtTime(0.0001, now + 0.5);

              osc.connect(gain);
              gain.connect(this.ctx.destination);

              osc.start(now);
              osc.stop(now + 0.5);
            }
          }
        }
      }

      // Subtle high-hat / metronome click
      const clickOsc = this.ctx.createOscillator();
      const clickGain = this.ctx.createGain();
      clickOsc.type = 'sine';
      clickOsc.frequency.setValueAtTime(currentBeat % 4 === 0 ? 2200 : 1600, this.ctx.currentTime);
      clickGain.gain.setValueAtTime(0.04, this.ctx.currentTime);
      clickGain.gain.exponentialRampToValueAtTime(0.0001, this.ctx.currentTime + 0.04);
      clickOsc.connect(clickGain);
      clickGain.connect(this.ctx.destination);
      clickOsc.start();
      clickOsc.stop(this.ctx.currentTime + 0.04);
    }
  }
}

export const audioEngine = new WebAudioEngine();
