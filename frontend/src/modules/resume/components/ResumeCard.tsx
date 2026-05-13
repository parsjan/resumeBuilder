"use client";

import Link from "next/link";
import { FileText, Trash2 } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardFooter } from "@/components/ui/card";
import { useUiStore } from "@/hooks/useStore";
import { formatRelative } from "@/utils/formatDate";
import { cn } from "@/utils/cn";
import type { Resume, ResumeSections } from "@/types";
import { ExportPdfButton } from "./ExportPdfButton";

// ─── Score helper (mirrors resumeStore.completionScore weights) ───────────────

function computeScore(sections: ResumeSections | null | undefined): number {
  if (!sections) return 0;
  let score = 0;
  if (sections.personalInfo?.fullName && sections.personalInfo?.email) score += 20;
  if (sections.summary?.trim()) score += 10;
  if (sections.experience?.length > 0) score += 30;
  if (sections.education?.length > 0) score += 15;
  if (sections.skills?.length > 0) score += 10;
  if (sections.projects?.length > 0) score += 10;
  if (sections.certifications?.length > 0) score += 5;
  return score;
}

// ─── Component ────────────────────────────────────────────────────────────────

interface ResumeCardProps {
  resume: Resume;
}

export function ResumeCard({ resume }: ResumeCardProps) {
  const uiStore = useUiStore();
  const sections = resume.sections ?? null;
  const score = computeScore(sections);

  const scoreColor =
    score >= 80 ? "bg-green-500" : score >= 50 ? "bg-yellow-400" : "bg-muted-foreground/40";

  const scoreLabelColor =
    score >= 80 ? "text-green-600" : score >= 50 ? "text-yellow-600" : "text-muted-foreground";

  // Quick stats visible at a glance
  const stats = [
    (sections?.experience?.length ?? 0) > 0 && `${sections!.experience.length} exp`,
    (sections?.skills?.length ?? 0) > 0 && `${sections!.skills.length} skills`,
    (sections?.projects?.length ?? 0) > 0 && `${sections!.projects.length} proj`,
  ].filter(Boolean) as string[];

  return (
    <Card className="group flex flex-col transition-shadow hover:shadow-md">
      <CardContent className="flex-1 p-5">
        {/* ── Header row ── */}
        <div className="mb-3 flex items-start justify-between gap-1.5">
          <div className="bg-primary/10 flex h-9 w-9 shrink-0 items-center justify-center rounded-lg">
            <FileText className="text-primary h-4 w-4" />
          </div>

          <div className="flex items-center gap-1 opacity-0 transition-opacity group-hover:opacity-100">
            <ExportPdfButton
              resumeId={resume.id}
              title={resume.title}
              variant="icon"
            />
            <Button
              variant="ghost"
              size="icon"
              className="text-muted-foreground hover:text-destructive h-7 w-7"
              onClick={() =>
                uiStore.openModal("deleteResume", { resumeId: resume.id, title: resume.title })
              }
              aria-label={`Delete ${resume.title}`}
            >
              <Trash2 className="h-3.5 w-3.5" />
            </Button>
          </div>
        </div>

        {/* ── Title + date ── */}
        <h3 className="mb-0.5 line-clamp-2 text-sm font-semibold leading-snug">
          {resume.title || "Untitled Resume"}
        </h3>
        <p className="text-muted-foreground mb-3 text-xs">
          Updated {formatRelative(resume.updatedAt)}
        </p>

        {/* ── Progress bar ── */}
        <div className="mb-2 flex items-center gap-2">
          <div className="bg-muted h-1.5 flex-1 overflow-hidden rounded-full">
            <div
              className={cn("h-full rounded-full transition-all duration-500", scoreColor)}
              style={{ width: `${score}%` }}
            />
          </div>
          <span className={cn("shrink-0 text-xs font-medium", scoreLabelColor)}>{score}%</span>
        </div>

        {/* ── Section stats ── */}
        {stats.length > 0 ? (
          <div className="flex flex-wrap gap-1">
            {stats.map((s) => (
              <Badge key={s} variant="secondary" className="px-1.5 py-0 text-[11px]">
                {s}
              </Badge>
            ))}
          </div>
        ) : (
          <p className="text-muted-foreground text-xs italic">No content yet</p>
        )}
      </CardContent>

      <CardFooter className="px-5 pb-5 pt-0">
        <Button asChild className="w-full" size="sm">
          <Link href={`/resume/${resume.id}`}>Open Editor</Link>
        </Button>
      </CardFooter>
    </Card>
  );
}
