import { action, computed, makeObservable, observable } from "mobx";

import type {
  CertificationItem,
  EducationItem,
  ExperienceItem,
  PersonalInfo,
  ProjectItem,
  Resume,
  ResumeSection,
  ResumeSections,
  SectionType,
} from "@/types";
import type { RootStore } from "./rootStore";

type ArraySection = "experience" | "education" | "projects" | "certifications";
type BulletSection = "experience" | "projects";
type ArraySectionItem = ExperienceItem | EducationItem | ProjectItem | CertificationItem;

export function generateId(): string {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 7)}`;
}

const DEFAULT_SECTIONS: ResumeSections = {
  personalInfo: { fullName: "", email: "", phone: "", location: "", linkedIn: "", website: "" },
  summary: "",
  experience: [],
  education: [],
  skills: [],
  projects: [],
  certifications: [],
};

const SECTION_WEIGHTS: Record<SectionType, number> = {
  personalInfo: 20,
  summary: 10,
  experience: 30,
  education: 15,
  skills: 10,
  projects: 10,
  certifications: 5,
};

export class ResumeStore {
  resume: Resume | null = null;
  isDirty = false;
  isSaving = false;
  activeSection: SectionType = "personalInfo";

  constructor(private rootStore: RootStore) {
    makeObservable(this, {
      resume: observable,
      isDirty: observable,
      isSaving: observable,
      activeSection: observable,
      // ── Core ──────────────────────────────────────────────────────────────
      setResume: action,
      updateSection: action,
      setActiveSection: action,
      setSaving: action,
      markClean: action,
      reset: action,
      // ── Field updaters ────────────────────────────────────────────────────
      updateTitle: action,
      updatePersonalInfoField: action,
      updateSummary: action,
      updateExperienceItem: action,
      updateEducationItem: action,
      updateProjectItem: action,
      updateCertificationItem: action,
      // ── Array item CRUD ───────────────────────────────────────────────────
      addItem: action,
      removeItem: action,
      reorderItems: action,
      // ── Bullets ───────────────────────────────────────────────────────────
      updateBullet: action,
      addBullet: action,
      removeBullet: action,
      reorderBullets: action,
      // ── Skills ────────────────────────────────────────────────────────────
      addSkill: action,
      removeSkill: action,
      updateSkill: action,
      // ── AI ────────────────────────────────────────────────────────────────
      applySections: action,
      // ── Computed ──────────────────────────────────────────────────────────
      completionScore: computed,
      hasResume: computed,
    });
  }

  // ─── Core ─────────────────────────────────────────────────────────────────

  setResume(data: Resume | null) {
    if (data) {
      // Ensure all sections exist — a newly created resume has sections: {}
      this.resume = {
        ...data,
        sections: { ...DEFAULT_SECTIONS, ...data.sections },
      };
    } else {
      this.resume = null;
    }
    this.isDirty = false;
    if (data) this.activeSection = "personalInfo";
  }

  /**
   * Replace an entire section value in-place.
   * Avoids spreading the entire resume object so only the changed section triggers observers.
   */
  updateSection(section: SectionType, value: ResumeSection) {
    if (!this.resume) return;
    (this.resume.sections as unknown as Record<string, unknown>)[section] = value;
    this.isDirty = true;
  }

  setActiveSection(section: SectionType) {
    this.activeSection = section;
  }

  setSaving(saving: boolean) {
    this.isSaving = saving;
  }

  markClean() {
    this.isDirty = false;
  }

  reset() {
    this.resume = null;
    this.isDirty = false;
    this.isSaving = false;
    this.activeSection = "personalInfo";
  }

  // ─── Field updaters ───────────────────────────────────────────────────────

  updateTitle(title: string) {
    if (!this.resume) return;
    this.resume.title = title;
    this.isDirty = true;
  }

  updatePersonalInfoField(field: keyof PersonalInfo, value: string) {
    if (!this.resume) return;
    (this.resume.sections.personalInfo as unknown as Record<string, unknown>)[field] = value;
    this.isDirty = true;
  }

  updateSummary(value: string) {
    if (!this.resume) return;
    this.resume.sections.summary = value;
    this.isDirty = true;
  }

  /** Patch any subset of fields on an experience item. */
  updateExperienceItem(index: number, patch: Partial<ExperienceItem>) {
    if (!this.resume) return;
    Object.assign(this.resume.sections.experience[index], patch);
    this.isDirty = true;
  }

  updateEducationItem(index: number, patch: Partial<EducationItem>) {
    if (!this.resume) return;
    Object.assign(this.resume.sections.education[index], patch);
    this.isDirty = true;
  }

  updateProjectItem(index: number, patch: Partial<ProjectItem>) {
    if (!this.resume) return;
    Object.assign(this.resume.sections.projects[index], patch);
    this.isDirty = true;
  }

  updateCertificationItem(index: number, patch: Partial<CertificationItem>) {
    if (!this.resume) return;
    Object.assign(this.resume.sections.certifications[index], patch);
    this.isDirty = true;
  }

  // ─── Array item CRUD ──────────────────────────────────────────────────────

  addItem(sectionType: ArraySection) {
    if (!this.resume) return;

    const blank: Record<ArraySection, ArraySectionItem> = {
      experience: {
        id: generateId(),
        company: "",
        role: "",
        startDate: "",
        current: false,
        bullets: [""],
      } satisfies ExperienceItem,
      education: {
        id: generateId(),
        institution: "",
        degree: "",
        field: "",
        startDate: "",
      } satisfies EducationItem,
      projects: {
        id: generateId(),
        name: "",
        description: "",
        bullets: [""],
        technologies: [],
      } satisfies ProjectItem,
      certifications: {
        id: generateId(),
        name: "",
        issuer: "",
        date: "",
      } satisfies CertificationItem,
    };

    (this.resume.sections[sectionType] as ArraySectionItem[]).push(blank[sectionType]);
    this.isDirty = true;
  }

  removeItem(sectionType: ArraySection, id: string) {
    if (!this.resume) return;
    const section = this.resume.sections[sectionType] as ArraySectionItem[];
    const idx = section.findIndex((item) => item.id === id);
    if (idx !== -1) {
      section.splice(idx, 1);
      this.isDirty = true;
    }
  }

  reorderItems(sectionType: ArraySection, fromIndex: number, toIndex: number) {
    if (!this.resume) return;
    const section = this.resume.sections[sectionType] as ArraySectionItem[];
    if (fromIndex < 0 || toIndex < 0 || fromIndex >= section.length || toIndex >= section.length)
      return;
    const [moved] = section.splice(fromIndex, 1);
    section.splice(toIndex, 0, moved);
    this.isDirty = true;
  }

  // ─── Bullets ──────────────────────────────────────────────────────────────

  updateBullet(
    sectionType: BulletSection,
    itemIndex: number,
    bulletIndex: number,
    text: string
  ) {
    if (!this.resume) return;
    const section = this.resume.sections[sectionType] as Array<{ bullets?: string[] }>;
    if (!section?.[itemIndex]?.bullets) return;
    section[itemIndex].bullets![bulletIndex] = text;
    this.isDirty = true;
  }

  addBullet(sectionType: BulletSection, itemIndex: number) {
    if (!this.resume) return;
    const item = this.resume.sections[sectionType][itemIndex] as { bullets: string[] };
    if (item?.bullets) {
      item.bullets.push("");
      this.isDirty = true;
    }
  }

  removeBullet(sectionType: BulletSection, itemIndex: number, bulletIndex: number) {
    if (!this.resume) return;
    const item = this.resume.sections[sectionType][itemIndex] as { bullets: string[] };
    if (item?.bullets && item.bullets.length > 1) {
      item.bullets.splice(bulletIndex, 1);
      this.isDirty = true;
    }
  }

  reorderBullets(
    sectionType: BulletSection,
    itemIndex: number,
    fromIndex: number,
    toIndex: number
  ) {
    if (!this.resume) return;
    const item = this.resume.sections[sectionType][itemIndex] as { bullets: string[] };
    if (!item?.bullets) return;
    const [moved] = item.bullets.splice(fromIndex, 1);
    item.bullets.splice(toIndex, 0, moved);
    this.isDirty = true;
  }

  // ─── Skills ───────────────────────────────────────────────────────────────

  addSkill(skill: string) {
    if (!this.resume) return;
    const trimmed = skill.trim();
    if (!trimmed) return;
    if (!this.resume.sections.skills.includes(trimmed)) {
      this.resume.sections.skills.push(trimmed);
      this.isDirty = true;
    }
  }

  removeSkill(index: number) {
    if (!this.resume) return;
    this.resume.sections.skills.splice(index, 1);
    this.isDirty = true;
  }

  updateSkill(index: number, value: string) {
    if (!this.resume) return;
    this.resume.sections.skills[index] = value;
    this.isDirty = true;
  }

  // ─── AI ───────────────────────────────────────────────────────────────────

  /**
   * Replace all resume sections in-place (used by AI generate/improve).
   * Preserves resume ID, title, and timestamps; only swaps out content.
   */
  applySections(sections: ResumeSections) {
    if (!this.resume) return;
    this.resume.sections = sections;
    this.isDirty = true;
  }

  // ─── Computed ─────────────────────────────────────────────────────────────

  get completionScore(): number {
    if (!this.resume) return 0;
    const s = this.resume.sections;
    if (!s) return 0;

    const filled: Record<SectionType, boolean> = {
      personalInfo: !!(s.personalInfo?.fullName && s.personalInfo?.email),
      summary: s.summary?.trim().length > 0,
      experience: (s.experience?.length ?? 0) > 0,
      education: (s.education?.length ?? 0) > 0,
      skills: (s.skills?.length ?? 0) > 0,
      projects: (s.projects?.length ?? 0) > 0,
      certifications: (s.certifications?.length ?? 0) > 0,
    };

    return (Object.entries(filled) as [SectionType, boolean][]).reduce(
      (score, [section, done]) => score + (done ? SECTION_WEIGHTS[section] : 0),
      0
    );
  }

  get hasResume(): boolean {
    return this.resume !== null;
  }
}
