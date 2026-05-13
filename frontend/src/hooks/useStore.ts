"use client";

import { useRootStore } from "@/store/rootStore";
import type { AuthStore } from "@/store/authStore";
import type { ResumeStore } from "@/store/resumeStore";
import type { UiStore } from "@/store/uiStore";

// Typed selectors — keeps components decoupled from RootStore
export function useAuthStore(): AuthStore {
  return useRootStore().auth;
}

export function useResumeStore(): ResumeStore {
  return useRootStore().resume;
}

export function useUiStore(): UiStore {
  return useRootStore().ui;
}
