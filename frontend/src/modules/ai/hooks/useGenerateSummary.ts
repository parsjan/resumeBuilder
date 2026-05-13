"use client";

import { useMutation } from "@tanstack/react-query";
import { toast } from "sonner";

import { useResumeStore } from "@/hooks/useStore";
import { aiService } from "@/services/aiService";

export function useGenerateSummary() {
  const resumeStore = useResumeStore();

  return useMutation({
    mutationFn: () => {
      const id = resumeStore.resume?.id;
      if (!id) throw new Error("No resume loaded");
      return aiService.generateSummary(id).then((r) => r.data.data.summary);
    },
    onSuccess: (summary) => {
      resumeStore.updateSummary(summary);
      toast.success("Summary generated!");
    },
    onError: () => {
      toast.error("Failed to generate summary. Please try again.");
    },
  });
}
