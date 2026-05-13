"use client";

import { useEffect, useState } from "react";

import { observer } from "mobx-react-lite";
import { ArrowLeft, CheckCircle2, Loader2, Save, Sparkles, Wand2 } from "lucide-react";
import Link from "next/link";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useResumeStore, useUiStore } from "@/hooks/useStore";
import { GenerateResumeModal } from "@/modules/ai/components/GenerateResumeModal";
import { useImproveResume } from "@/modules/ai/hooks/useImproveResume";
import { resumeService } from "@/services/resumeService";
import { ExportPdfButton } from "./ExportPdfButton";
import { cn } from "@/utils/cn";
import { formatScore } from "@/utils/formatters";

export const EditorToolbar = observer(function EditorToolbar() {
  const resumeStore = useResumeStore();
  const uiStore = useUiStore();
  const [titleEditing, setTitleEditing] = useState(false);
  const [savedFlash, setSavedFlash] = useState(false);

  const { resume, isDirty, isSaving, completionScore } = resumeStore;
  const improveResume = useImproveResume();

  // Show "Saved" flash briefly after a save completes
  useEffect(() => {
    if (!isDirty && !isSaving && resume) {
      setSavedFlash(true);
      const t = setTimeout(() => setSavedFlash(false), 2_000);
      return () => clearTimeout(t);
    }
  }, [isDirty, isSaving, resume]);

  async function handleManualSave() {
    if (!resume) return;
    resumeStore.setSaving(true);
    try {
      await resumeService.update(resume.id, resume);
      resumeStore.markClean();
    } finally {
      resumeStore.setSaving(false);
    }
  }

  const score = completionScore;

  const scoreColor =
    score >= 80
      ? "text-green-600"
      : score >= 50
        ? "text-yellow-600"
        : "text-muted-foreground";

  return (
    <>
      <div className="bg-background border-border flex h-14 shrink-0 items-center gap-3 border-b px-4">
        {/* ── Back button ── */}
        <Button variant="ghost" size="icon" asChild className="h-8 w-8">
          <Link href="/dashboard" aria-label="Back to dashboard">
            <ArrowLeft className="h-4 w-4" />
          </Link>
        </Button>

        {/* ── Resume title (inline edit) ── */}
        {titleEditing ? (
          <Input
            autoFocus
            value={resume?.title ?? ""}
            onChange={(e) => resumeStore.updateTitle(e.target.value)}
            onBlur={() => setTitleEditing(false)}
            onKeyDown={(e) => e.key === "Enter" && setTitleEditing(false)}
            className="h-7 w-48 text-sm font-medium"
          />
        ) : (
          <button
            onClick={() => setTitleEditing(true)}
            className="hover:bg-accent max-w-50 truncate rounded px-2 py-1 text-sm font-semibold transition-colors"
            title="Click to rename"
          >
            {resume?.title || "Untitled Resume"}
          </button>
        )}

        {/* ── Save status ── */}
        <div className="flex items-center gap-1.5 text-xs">
          {isSaving && (
            <>
              <Loader2 className="text-muted-foreground h-3.5 w-3.5 animate-spin" />
              <span className="text-muted-foreground">Saving…</span>
            </>
          )}
          {!isSaving && isDirty && (
            <span className="text-muted-foreground">Unsaved changes</span>
          )}
          {!isSaving && !isDirty && savedFlash && (
            <>
              <CheckCircle2 className="h-3.5 w-3.5 text-green-500" />
              <span className="text-green-600">Saved</span>
            </>
          )}
        </div>

        <div className="flex-1" />

        {/* ── Completion score ── */}
        <div className="flex items-center gap-2">
          <div className="bg-muted h-1.5 w-20 overflow-hidden rounded-full">
            <div
              className={cn(
                "h-full rounded-full transition-all duration-500",
                score >= 80
                  ? "bg-green-500"
                  : score >= 50
                    ? "bg-yellow-400"
                    : "bg-muted-foreground/40"
              )}
              style={{ width: `${score}%` }}
            />
          </div>
          <span className={cn("text-xs font-medium", scoreColor)}>
            {formatScore(score)}
          </span>
        </div>

        {/* ── Generate Resume ── */}
        <Button
          size="sm"
          variant="outline"
          className="h-8 gap-1.5"
          onClick={() => uiStore.openModal("generateResume")}
          disabled={!resume}
          title="Generate resume from a text prompt"
        >
          <Sparkles className="h-3.5 w-3.5 text-violet-500" />
          Generate
        </Button>

        {/* ── Improve full resume ── */}
        <Button
          size="sm"
          variant="outline"
          className="h-8 gap-1.5"
          onClick={() => improveResume.mutate()}
          disabled={improveResume.isPending || !resume}
          title="Improve the entire resume with AI"
        >
          {improveResume.isPending ? (
            <Loader2 className="h-3.5 w-3.5 animate-spin" />
          ) : (
            <Wand2 className="h-3.5 w-3.5 text-violet-500" />
          )}
          {improveResume.isPending ? "Improving…" : "Improve"}
        </Button>

        {/* ── Export PDF ── */}
        {resume && (
          <ExportPdfButton resumeId={resume.id} title={resume.title} />
        )}

        {/* ── Manual save ── */}
        <Button
          size="sm"
          variant={isDirty ? "default" : "outline"}
          onClick={handleManualSave}
          disabled={isSaving || !isDirty}
          className="h-8"
        >
          <Save className="mr-1.5 h-3.5 w-3.5" />
          Save
        </Button>
      </div>

      {/* Rendered outside the toolbar div so it portals to body correctly */}
      <GenerateResumeModal />
    </>
  );
});
