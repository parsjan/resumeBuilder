"use client";

import { observer } from "mobx-react-lite";
import {
  Award,
  Briefcase,
  Code2,
  FileText,
  FolderOpen,
  GraduationCap,
  User,
} from "lucide-react";

import { useResumeStore } from "@/hooks/useStore";
import type { ResumeSections, SectionType } from "@/types";
import { cn } from "@/utils/cn";

interface SectionMeta {
  type: SectionType;
  label: string;
  icon: React.ElementType;
}

const SECTIONS: SectionMeta[] = [
  { type: "personalInfo", label: "Personal Info", icon: User },
  { type: "summary", label: "Summary", icon: FileText },
  { type: "experience", label: "Experience", icon: Briefcase },
  { type: "education", label: "Education", icon: GraduationCap },
  { type: "skills", label: "Skills", icon: Code2 },
  { type: "projects", label: "Projects", icon: FolderOpen },
  { type: "certifications", label: "Certifications", icon: Award },
];

function isFilled(sections: ResumeSections, type: SectionType): boolean {
  switch (type) {
    case "personalInfo":
      return !!(sections.personalInfo?.fullName && sections.personalInfo?.email);
    case "summary":
      return (sections.summary?.trim().length ?? 0) > 0;
    case "experience":
      return (sections.experience?.length ?? 0) > 0;
    case "education":
      return (sections.education?.length ?? 0) > 0;
    case "skills":
      return (sections.skills?.length ?? 0) > 0;
    case "projects":
      return (sections.projects?.length ?? 0) > 0;
    case "certifications":
      return (sections.certifications?.length ?? 0) > 0;
  }
}

export const SectionNav = observer(function SectionNav() {
  const resumeStore = useResumeStore();
  const sections = resumeStore.resume?.sections;

  return (
    <nav className="flex flex-col gap-0.5" aria-label="Resume sections">
      {SECTIONS.map(({ type, label, icon: Icon }) => {
        const active = resumeStore.activeSection === type;
        const filled = sections ? isFilled(sections, type) : false;

        return (
          <button
            key={type}
            onClick={() => resumeStore.setActiveSection(type)}
            className={cn(
              "flex items-center gap-2.5 rounded-md px-3 py-2 text-sm font-medium transition-colors",
              active
                ? "bg-primary text-primary-foreground"
                : "text-muted-foreground hover:bg-accent hover:text-accent-foreground"
            )}
          >
            <Icon className="h-4 w-4 shrink-0" />
            <span className="flex-1 text-left">{label}</span>
            {/* Filled indicator */}
            <span
              className={cn(
                "h-1.5 w-1.5 rounded-full transition-colors",
                filled
                  ? active
                    ? "bg-primary-foreground/70"
                    : "bg-green-500"
                  : "bg-transparent"
              )}
            />
          </button>
        );
      })}
    </nav>
  );
});
