// ─── User ────────────────────────────────────────────────────────────────────

export interface User {
  id: string;
  email: string;
  fullName: string;
  avatarUrl?: string;
  createdAt: string;
}

// ─── Auth ─────────────────────────────────────────────────────────────────────

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload {
  fullName: string;
  email: string;
  password: string;
}

// ─── Resume ───────────────────────────────────────────────────────────────────

export type SectionType =
  | "personalInfo"
  | "summary"
  | "experience"
  | "education"
  | "skills"
  | "projects"
  | "certifications";

export interface PersonalInfo {
  fullName: string;
  email: string;
  phone?: string;
  location?: string;
  linkedIn?: string;
  website?: string;
}

export interface ExperienceItem {
  id: string;
  company: string;
  role: string;
  startDate: string;
  endDate?: string;
  current: boolean;
  bullets: string[];
}

export interface EducationItem {
  id: string;
  institution: string;
  degree: string;
  field: string;
  startDate: string;
  endDate?: string;
  gpa?: string;
}

export interface ProjectItem {
  id: string;
  name: string;
  description: string;
  url?: string;
  bullets: string[];
  technologies: string[];
}

export interface CertificationItem {
  id: string;
  name: string;
  issuer: string;
  date: string;
  url?: string;
}

export interface ResumeSections {
  personalInfo: PersonalInfo;
  summary: string;
  experience: ExperienceItem[];
  education: EducationItem[];
  skills: string[];
  projects: ProjectItem[];
  certifications: CertificationItem[];
}

export type ResumeSection = ResumeSections[SectionType];

export interface Resume {
  id: string;
  title: string;
  createdAt: string;
  updatedAt: string;
  sections: ResumeSections;
}

// ─── AI ───────────────────────────────────────────────────────────────────────

export interface ImproveBulletPayload {
  text: string;
  context?: string;
}

export interface ImproveBulletResponse {
  improved: string;
  suggestions: string[];
}

export interface GenerateResumePayload {
  prompt: string;
}

export interface GenerateResumeResponse {
  sections: ResumeSections;
}

export interface ImproveResumeResponse {
  sections: ResumeSections;
}

export interface JobAnalysisPayload {
  resumeId: string;
  jobDescription: string;
}

export interface JobAnalysisResponse {
  matchScore: number;
  missingKeywords: string[];
  suggestions: string[];
}

// ─── API ──────────────────────────────────────────────────────────────────────

export interface ApiResponse<T> {
  data: T;
  message?: string;
}

export interface PaginatedResponse<T> {
  data: T[];
  total: number;
  page: number;
  pageSize: number;
}

export interface ApiError {
  message: string;
  statusCode: number;
  errors?: Record<string, string[]>;
}
