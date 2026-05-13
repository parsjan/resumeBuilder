"use client";

import { useMutation } from "@tanstack/react-query";
import { toast } from "sonner";

import { useResumeStore, useUiStore } from "@/hooks/useStore";
import { aiService } from "@/services/aiService";
import type { ResumeSections } from "@/types";

export function useGenerateResume() {
  const resumeStore = useResumeStore();
  const uiStore = useUiStore();

  return useMutation<ResumeSections, Error, string>({
    mutationFn: (prompt: string) =>
      aiService.generateResume({ prompt }).then((r) => r.data.data.sections),
    onSuccess: (sections) => {
      resumeStore.applySections(sections);
      uiStore.closeModal("generateResume");
      toast.success("Resume generated! Review and edit as needed.");
    },
    onError: () => {
      toast.error("Failed to generate resume. Please try again.");
    },
  });
}
