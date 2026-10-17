import { levels, type LevelId } from './levels';

export const DEFAULT_SPEECH_RATE = 0.9;

export interface Progress {
  lastLevel: LevelId;
  starsByLevel: Record<LevelId, number>;
  phonicsEnabled: boolean;
  correctSoundEnabled: boolean;
  wrongSoundEnabled: boolean;
  celebrateSoundEnabled: boolean;
  vibrationEnabled: boolean;
  ttsEnabled: boolean;
  speechRate: number;
  darkModeEnabled: boolean;
}

export type Settings = Omit<Progress, 'lastLevel' | 'starsByLevel'>;

function defaults(): Progress {
  return {
    lastLevel: 'FIRST_WORDS',
    starsByLevel: Object.fromEntries(levels.map((l) => [l.id, 0])) as Record<LevelId, number>,
    phonicsEnabled: true,
    correctSoundEnabled: true,
    wrongSoundEnabled: true,
    celebrateSoundEnabled: true,
    vibrationEnabled: true,
    ttsEnabled: true,
    speechRate: DEFAULT_SPEECH_RATE,
    darkModeEnabled: false,
  };
}

const STORAGE_KEY = 'speaknspell.progress';

/** Persists across app restarts (in localStorage): last level played, stars per level, settings. */
export class ProgressStore {
  private value: Progress = this.load();
  private listeners = new Set<(progress: Progress) => void>();

  get progress(): Progress {
    return this.value;
  }

  subscribe(listener: (progress: Progress) => void): () => void {
    this.listeners.add(listener);
    return () => this.listeners.delete(listener);
  }

  setLastLevel(level: LevelId): void {
    this.update({ lastLevel: level });
  }

  addStar(level: LevelId): void {
    this.update({ starsByLevel: { ...this.value.starsByLevel, [level]: (this.value.starsByLevel[level] ?? 0) + 1 } });
  }

  setSetting<K extends keyof Settings>(key: K, value: Settings[K]): void {
    this.update({ [key]: value } as Partial<Progress>);
  }

  reset(): void {
    this.value = defaults();
    this.save();
  }

  private update(changes: Partial<Progress>): void {
    this.value = { ...this.value, ...changes };
    this.save();
  }

  private load(): Progress {
    const base = defaults();
    try {
      const stored = JSON.parse(localStorage.getItem(STORAGE_KEY) ?? '{}') as Partial<Progress>;
      return { ...base, ...stored, starsByLevel: { ...base.starsByLevel, ...stored.starsByLevel } };
    } catch {
      return base;
    }
  }

  private save(): void {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.value));
    } catch {
      // Storage full or blocked (e.g. private browsing) — keep playing with in-memory progress.
    }
    this.listeners.forEach((listener) => listener(this.value));
  }
}
