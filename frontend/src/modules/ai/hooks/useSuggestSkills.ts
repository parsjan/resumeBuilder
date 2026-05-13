"use client";

import { useMutation } from "@tanstack/react-query";
import { toast } from "sonner";

import { useResumeStore } from "@/hooks/useStore";
import { aiService } from "@/services/aiService";

export function useSuggestSkills() {
  const resumeStore = useResumeStore();

  return useMutation({
    mutationFn: () => {
      const id = resumeStore.resume?.id;
      if (!id) throw new Error("No resume loaded");
      return aiService.suggestSkills(id).then((r) => r.data.data.skills);
    },
    onSuccess: (skills) => {
      const added = skills.filter((s) => {
        const alreadyHas = resumeStore.resume?.sections.skills.includes(s);
        if (!alreadyHas) resumeStore.addSkill(s);
        return !alreadyHas;
      });
      toast.success(
        added.length > 0
          ? `Added ${added.length} skill suggestion${added.length > 1 ? "s" : ""}.`
          : "No new skills to add — you already have them all!"
      );
    },
    onError: () => {
      toast.error("Failed to suggest skills. Please try again.");
    },
  });
}
