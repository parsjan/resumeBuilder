"use client";

import React, { createContext, useContext, useMemo } from "react";

import { AuthStore } from "./authStore";
import { ResumeStore } from "./resumeStore";
import { UiStore } from "./uiStore";

// ─── RootStore ────────────────────────────────────────────────────────────────

export class RootStore {
  auth: AuthStore;
  resume: ResumeStore;
  ui: UiStore;

  constructor() {
    // Pass `this` so child stores can cross-reference without circular imports.
    // Child stores declare rootStore as `private` — no API calls allowed inside stores.
    this.auth = new AuthStore(this);
    this.resume = new ResumeStore(this);
    this.ui = new UiStore(this);
  }
}

// ─── Context ──────────────────────────────────────────────────────────────────

const RootStoreContext = createContext<RootStore | null>(null);

export function RootStoreProvider({ children }: { children: React.ReactNode }) {
  // `useMemo` with [] ensures a single store instance per Provider mount.
  const store = useMemo(() => new RootStore(), []);

  return React.createElement(RootStoreContext.Provider, { value: store }, children);
}

// ─── Hook ─────────────────────────────────────────────────────────────────────

export function useRootStore(): RootStore {
  const store = useContext(RootStoreContext);
  if (!store) {
    throw new Error("useRootStore must be used within <RootStoreProvider>");
  }
  return store;
}
