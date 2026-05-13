"use client";

import { observer } from "mobx-react-lite";
import { Loader2, AlertTriangle } from "lucide-react";

import { useResumeStore } from "@/hooks/useStore";
import { camelToTitle } from "@/utils/formatters";
import type { SectionType } from "@/types";

import { useResumeBuilder } from "../hooks/useResumeBuilder";
import { SectionNav } from "./SectionNav";
import { EditorToolbar } from "./EditorToolbar";
import { PersonalInfoEditor } from "./editors/PersonalInfoEditor";
import { SummaryEditor } from "./editors/SummaryEditor";
import { ExperienceEditor } from "./editors/ExperienceEditor";
import { EducationEditor } from "./editors/EducationEditor";
import { SkillsEditor } from "./editors/SkillsEditor";
import { ProjectsEditor } from "./editors/ProjectsEditor";
import { CertificationsEditor } from "./editors/CertificationsEditor";
import { ResumePreview } from "./preview/ResumePreview";

// ─── Section editor registry ──────────────────────────────────────────────────

const EDITORS: Record<SectionType, React.ComponentType> = {
  personalInfo: PersonalInfoEditor,
  summary: SummaryEditor,
  experience: ExperienceEditor,
  education: EducationEditor,
  skills: SkillsEditor,
  projects: ProjectsEditor,
  certifications: CertificationsEditor,
};

// ─── Shell ────────────────────────────────────────────────────────────────────

interface ResumeBuilderShellProps {
  resumeId: string;
}

export const ResumeBuilderShell = observer(function ResumeBuilderShell({
  resumeId,
}: ResumeBuilderShellProps) {
  const { isLoading, isError } = useResumeBuilder(resumeId);
  const resumeStore = useResumeStore();

  if (isLoading) {
    return (
      <div className="flex h-full items-center justify-center gap-3">
        <Loader2 className="text-primary h-6 w-6 animate-spin" />
        <span className="text-muted-foreground text-sm">Loading resume…</span>
      </div>
    );
  }

  if (isError || !resumeStore.resume) {
    return (
      <div className="flex h-full flex-col items-center justify-center gap-3 text-center">
        <AlertTriangle className="text-destructive h-8 w-8" />
        <p className="font-medium">Could not load resume</p>
        <p className="text-muted-foreground text-sm">
          The resume may have been deleted or you don&apos;t have access to it.
        </p>
      </div>
    );
  }

  const ActiveEditor = EDITORS[resumeStore.activeSection];

  return (
    <div className="flex h-full flex-col overflow-hidden">
      {/* ── Top toolbar ── */}
      <EditorToolbar />

      <div className="flex min-h-0 flex-1">
        {/* ══════════════════════════════════════════════════
            LEFT — Editor panel
        ══════════════════════════════════════════════════ */}
        <div className="flex w-[52%] min-w-0 flex-col border-r">
          {/* Section navigation */}
          <div className="border-b px-3 py-2">
            <SectionNav />
          </div>

          {/* Active section editor */}
          <div className="flex-1 overflow-y-auto p-4">
            <div className="mb-3">
              <h2 className="text-sm font-semibold">
                {camelToTitle(resumeStore.activeSection)}
              </h2>
            </div>
            <ActiveEditor />
          </div>
        </div>

        {/* ══════════════════════════════════════════════════
            RIGHT — Live preview
        ══════════════════════════════════════════════════ */}
        <div className="bg-muted/30 flex-1 overflow-y-auto">
          <ResumePreview />
        </div>
      </div>
    </div>
  );
});
