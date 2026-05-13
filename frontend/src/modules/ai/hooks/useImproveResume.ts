"use client";

import { useMutation } from "@tanstack/react-query";
import { toast } from "sonner";

import { useResumeStore } from "@/hooks/useStore";
import { aiService } from "@/services/aiService";
import type { ResumeSections } from "@/types";

export function useImproveResume() {
  const resumeStore = useResumeStore();

  return useMutation<ResumeSections, Error, void>({
    mutationFn: () => {
      const id = resumeStore.resume?.id;
      if (!id) throw new Error("No resume loaded");
      return aiService.improveResume(id).then((r) => r.data.data.sections);
    },
    onSuccess: (sections) => {
      resumeStore.applySections(sections);
      toast.success("Resume improved! Review the changes.");
    },
    onError: () => {
      toast.error("Failed to improve resume. Please try again.");
    },
  });
}
