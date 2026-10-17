const SOUND_NAMES = ['correct', 'wrong', 'celebrate'] as const;
type SoundName = (typeof SOUND_NAMES)[number];

/** Whether this device can vibrate at all — false on iPhone, where Safari has no Vibration API. */
export const canVibrate = typeof navigator !== 'undefined' && 'vibrate' in navigator;

/** Short correct/wrong/celebrate cues via Web Audio, plus a matching haptic buzz where supported. */
export class SoundEffects {
  private context: AudioContext | null = null;
  private buffers = new Map<SoundName, AudioBuffer>();

  /**
   * Must be called from inside a user gesture (tap): iOS keeps audio locked until then.
   * Safe to call more than once.
   */
  unlock(): void {
    if (!this.context) {
      const context = new AudioContext();
      this.context = context;
      for (const name of SOUND_NAMES) {
        fetch(`${import.meta.env.BASE_URL}sounds/${name}.wav`)
          .then((response) => response.arrayBuffer())
          .then((bytes) => context.decodeAudioData(bytes))
          .then((buffer) => this.buffers.set(name, buffer))
          .catch(() => {}); // a missing cue shouldn't break the game
      }
    }
    if (this.context.state !== 'running') void this.context.resume();
  }

  playCorrect(soundEnabled: boolean, vibrationEnabled: boolean): void {
    if (soundEnabled) this.play('correct');
    if (vibrationEnabled) vibrate(20);
  }

  playWrong(soundEnabled: boolean, vibrationEnabled: boolean): void {
    if (soundEnabled) this.play('wrong');
    if (vibrationEnabled) vibrate(45);
  }

  playCelebrate(soundEnabled: boolean, vibrationEnabled: boolean): void {
    if (soundEnabled) this.play('celebrate');
    if (vibrationEnabled) vibrate([60, 60, 60, 60, 120]);
  }

  private play(name: SoundName): void {
    const buffer = this.buffers.get(name);
    if (!this.context || !buffer) return;
    // Resume in case the OS suspended audio while the app was in the background.
    if (this.context.state !== 'running') void this.context.resume();
    const source = this.context.createBufferSource();
    source.buffer = buffer;
    source.connect(this.context.destination);
    source.start();
  }
}

function vibrate(pattern: number | number[]): void {
  if (canVibrate) navigator.vibrate(pattern);
}
