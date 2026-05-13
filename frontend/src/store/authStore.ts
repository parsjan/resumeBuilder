import { computed, makeObservable, observable, action, runInAction } from "mobx";

import type { User } from "@/types";
import type { RootStore } from "./rootStore";

export class AuthStore {
  user: User | null = null;
  isAuthenticated = false;
  /**
   * Flips to true after the first /auth/me attempt resolves (success or failure).
   * Used to block rendering until we know whether the user is logged in.
   */
  isInitialized = false;

  constructor(private rootStore: RootStore) {
    makeObservable(this, {
      user: observable,
      isAuthenticated: observable,
      isInitialized: observable,
      setUser: action,
      setInitialized: action,
      logout: action,
      displayName: computed,
      initials: computed,
    });
  }

  // ─── Actions ───────────────────────────────────────────────────────────────

  setUser(user: User | null) {
    this.user = user;
    this.isAuthenticated = !!user;
  }

  setInitialized() {
    this.isInitialized = true;
  }

  logout() {
    this.user = null;
    this.isAuthenticated = false;
    // Cascade reset to dependent stores
    this.rootStore.resume.reset();
    this.rootStore.ui.reset();
  }

  // ─── Computed ──────────────────────────────────────────────────────────────

  get displayName(): string {
    return this.user?.fullName ?? "Guest";
  }

  get initials(): string {
    if (!this.user?.fullName) return "?";
    return this.user.fullName
      .split(" ")
      .filter(Boolean)
      .map((n) => n[0])
      .join("")
      .toUpperCase()
      .slice(0, 2);
  }
}
