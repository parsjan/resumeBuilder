/**
 * Type-safe localStorage wrapper.
 * Always returns `null` on parse failures or SSR (no window).
 */

function isClient(): boolean {
  return typeof window !== "undefined";
}

export const storage = {
  get<T>(key: string): T | null {
    if (!isClient()) return null;
    try {
      const raw = window.localStorage.getItem(key);
      if (raw === null) return null;
      return JSON.parse(raw) as T;
    } catch {
      return null;
    }
  },

  set<T>(key: string, value: T): void {
    if (!isClient()) return;
    try {
      window.localStorage.setItem(key, JSON.stringify(value));
    } catch {
      // Storage full or access denied — fail silently
    }
  },

  remove(key: string): void {
    if (!isClient()) return;
    window.localStorage.removeItem(key);
  },

  clear(): void {
    if (!isClient()) return;
    window.localStorage.clear();
  },
};
