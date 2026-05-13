"use client";

import { useEffect } from "react";

import { reaction } from "mobx";
import { toast } from "sonner";

import { useResumeStore } from "@/hooks/useStore";
import { resumeService } from "@/services/resumeService";
import { debounce } from "@/utils/debounce";

import { useResume } from "./useResumes";

/**
 * Orchestrates the resume builder page:
 * 1. Fetches resume from API (TanStack Query) → hydrates MobX store
 * 2. Watches `isDirty` via MobX `reaction` → debounced auto-save (1.5s)
 * 3. Cleans up store on unmount
 */
export function useResumeBuilder(id: string) {
  const resumeStore = useResumeStore();
  const { data, isLoading, isError } = useResume(id);

  // ── Hydrate store ──────────────────────────────────────────────────────────
  // Use data.id as dep so refetches don't clobber unsaved edits.
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => {
    if (data) resumeStore.setResume(data);
    return () => resumeStore.reset();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [data?.id]);

  // ── Auto-save reaction ─────────────────────────────────────────────────────
  useEffect(() => {
    const save = debounce(async () => {
      const { resume, isDirty } = resumeStore;
      if (!resume || !isDirty) return;

      resumeStore.setSaving(true);
      try {
        await resumeService.update(id, resume);
        resumeStore.markClean();
      } catch {
        toast.error("Auto-save failed. Your changes will retry shortly.");
      } finally {
        resumeStore.setSaving(false);
      }
    }, 1_500);

    // `reaction` fires every time `isDirty` flips to true
    const dispose = reaction(
      () => resumeStore.isDirty,
      (isDirty) => {
        if (isDirty) save();
      }
    );

    return () => {
      dispose();
      save.cancel();
    };
  }, [id, resumeStore]);

  return { isLoading, isError };
}
