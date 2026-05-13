import { z } from "zod";

export const personalInfoSchema = z.object({
  fullName: z.string().min(2, "Full name is required"),
  email: z.string().email("Invalid email address"),
  phone: z.string().optional(),
  location: z.string().optional(),
  linkedIn: z.string().url("Invalid URL").optional().or(z.literal("")),
  website: z.string().url("Invalid URL").optional().or(z.literal("")),
});

export const experienceItemSchema = z.object({
  id: z.string(),
  company: z.string().min(1, "Company name is required"),
  role: z.string().min(1, "Role is required"),
  startDate: z.string().min(1, "Start date is required"),
  endDate: z.string().optional(),
  current: z.boolean(),
  bullets: z.array(z.string()),
});

export const educationItemSchema = z.object({
  id: z.string(),
  institution: z.string().min(1, "Institution is required"),
  degree: z.string().min(1, "Degree is required"),
  field: z.string().min(1, "Field of study is required"),
  startDate: z.string().min(1, "Start date is required"),
  endDate: z.string().optional(),
  gpa: z.string().optional(),
});

export const projectItemSchema = z.object({
  id: z.string(),
  name: z.string().min(1, "Project name is required"),
  description: z.string().min(1, "Description is required"),
  url: z.string().url("Invalid URL").optional().or(z.literal("")),
  bullets: z.array(z.string()),
  technologies: z.array(z.string()),
});

export const resumeCreateSchema = z.object({
  title: z.string().min(1, "Resume title is required"),
});

export type PersonalInfoFormValues = z.infer<typeof personalInfoSchema>;
export type ExperienceItemFormValues = z.infer<typeof experienceItemSchema>;
export type EducationItemFormValues = z.infer<typeof educationItemSchema>;
export type ProjectItemFormValues = z.infer<typeof projectItemSchema>;
export type ResumeCreateFormValues = z.infer<typeof resumeCreateSchema>;
