"use client";

import { observer } from "mobx-react-lite";
import { Loader2, Sparkles } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { useResumeStore } from "@/hooks/useStore";
import { useGenerateSummary } from "@/modules/ai/hooks/useGenerateSummary";

const MAX_CHARS = 600;

export const SummaryEditor = observer(function SummaryEditor() {
  const resumeStore = useResumeStore();
  const summary = resumeStore.resume?.sections.summary ?? "";
  const generateSummary = useGenerateSummary();

  return (
    <div className="space-y-2">
      <Textarea
        value={summary}
        onChange={(e) => resumeStore.updateSummary(e.target.value)}
        placeholder="A concise 2–4 sentence overview of your professional background, key skills, and career goals…"
        rows={6}
        maxLength={MAX_CHARS}
        className="resize-none"
      />

      <div className="flex items-center justify-between">
        <Button
          type="button"
          size="sm"
          variant="outline"
          onClick={() => generateSummary.mutate()}
          disabled={generateSummary.isPending || !resumeStore.resume}
          className="gap-1.5 text-xs"
        >
          {generateSummary.isPending ? (
            <Loader2 className="h-3.5 w-3.5 animate-spin" />
          ) : (
            <Sparkles className="h-3.5 w-3.5 text-violet-500" />
          )}
          {generateSummary.isPending ? "Generating…" : "Generate with AI"}
        </Button>

        <p className="text-muted-foreground text-xs">
          {summary.length} / {MAX_CHARS}
        </p>
      </div>
    </div>
  );
});
