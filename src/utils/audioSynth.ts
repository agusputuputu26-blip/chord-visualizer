import { PracticeSong } from '../types';

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

  public getCustomAudioElement(): HTMLAudioElement | null {
    return this.customAudio;
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

    // Web Audio Synthesizer Loop
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
    }, 50);
  }

  private lastBeepBeat = -1;
  private playSynthNoteAtTime(currentTime: number) {
    if (!this.ctx || !this.currentSong) return;
    const secondsPerBeat = 60.0 / this.currentSong.bpm;
    const currentBeat = Math.floor(currentTime / secondsPerBeat);

    if (currentBeat !== this.lastBeepBeat) {
      this.lastBeepBeat = currentBeat;

      // Metronome / synth beat pluck
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();

      const isFirstBeat = currentBeat % 4 === 0;
      osc.type = isFirstBeat ? 'triangle' : 'sine';
      osc.frequency.setValueAtTime(isFirstBeat ? 440 : 330, this.ctx.currentTime);

      gain.gain.setValueAtTime(0.15, this.ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + 0.12);

      osc.connect(gain);
      gain.connect(this.ctx.destination);

      osc.start();
      osc.stop(this.ctx.currentTime + 0.12);
    }
  }

  public pause() {
    this.isRunning = false;
    if (this.timerId) {
      clearInterval(this.timerId);
      this.timerId = null;
    }
    if (this.customAudio) {
      this.customAudio.pause();
    }
  }

  public stop() {
    this.pause();
    this.pauseOffset = 0;
  }

  public seek(posSec: number) {
    this.pauseOffset = posSec;
    if (this.ctx) {
      this.startTime = this.ctx.currentTime - posSec;
    }
    if (this.customAudio) {
      this.customAudio.currentTime = posSec;
    }
    this.onTimeUpdate?.(posSec);
  }
}

export const audioSynth = new WebAudioEngine();
