export type SpeechStatus = 'READY' | 'UNAVAILABLE';

// iOS ships "novelty" voices alongside the real ones — never pick these for a child's prompts.
const NOVELTY_VOICES =
  /albert|bad news|bahh|bells|boing|bubbles|cellos|good news|jester|organ|superstar|trinoids|whisper|wobble|zarvox|junior|ralph|fred|kathy|grandma|grandpa|eddy|flo|reed|rocko|sandy|shelley/i;

function voiceScore(voice: SpeechSynthesisVoice): number {
  const lang = voice.lang.replace('_', '-').toLowerCase();
  if (!lang.startsWith('en')) return -1;
  let score = lang === 'en-us' ? 10 : 5;
  if (voice.localService) score += 5; // on-device voices keep working offline
  if (/enhanced|premium|natural/i.test(voice.name)) score += 3;
  if (/samantha|google us english/i.test(voice.name)) score += 2;
  if (NOVELTY_VOICES.test(voice.name)) score -= 20;
  return score;
}

/**
 * Thin wrapper around the Web Speech API's speechSynthesis, mirroring the Android
 * SpeechService: speak with interrupt-or-queue semantics, adjustable rate, stop.
 *
 * iOS only allows speech after a user gesture, so the first [speak] must happen inside a tap
 * handler (see the "Tap to play" splash in main.ts).
 */
export class SpeechService {
  readonly status: SpeechStatus = 'speechSynthesis' in window ? 'READY' : 'UNAVAILABLE';
  private voice: SpeechSynthesisVoice | null = null;
  private rate = 0.9;
  // Safari can garbage-collect in-flight utterances (and drop their queue) unless they're retained.
  private pending = new Set<SpeechSynthesisUtterance>();

  constructor() {
    if (this.status !== 'READY') return;
    this.pickVoice();
    speechSynthesis.addEventListener?.('voiceschanged', () => this.pickVoice());
  }

  get voiceName(): string | null {
    return this.voice?.name ?? null;
  }

  private pickVoice(): void {
    const best = speechSynthesis
      .getVoices()
      .filter((voice) => voiceScore(voice) >= 0)
      .sort((a, b) => voiceScore(b) - voiceScore(a))[0];
    this.voice = best ?? null;
  }

  /**
   * Speaks [text]. When [interrupt] is true (default) this cuts off anything currently playing;
   * when false it queues after what's already speaking, so e.g. quick letter callouts don't
   * chop each other off mid-word.
   */
  speak(text: string, interrupt = true): void {
    if (this.status !== 'READY') return;
    if (interrupt) {
      speechSynthesis.cancel();
      this.pending.clear();
    }
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = this.voice?.lang ?? 'en-US';
    if (this.voice) utterance.voice = this.voice;
    utterance.rate = this.rate;
    const done = () => this.pending.delete(utterance);
    utterance.onend = done;
    utterance.onerror = done;
    this.pending.add(utterance);
    speechSynthesis.speak(utterance);
  }

  setSpeechRate(rate: number): void {
    this.rate = rate;
  }

  stop(): void {
    if (this.status !== 'READY') return;
    speechSynthesis.cancel();
    this.pending.clear();
  }
}
