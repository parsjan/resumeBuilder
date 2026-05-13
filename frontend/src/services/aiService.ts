import type {
  ApiResponse,
  GenerateResumePayload,
  GenerateResumeResponse,
  ImproveBulletPayload,
  ImproveBulletResponse,
  ImproveResumeResponse,
  JobAnalysisPayload,
  JobAnalysisResponse,
} from "@/types";

import apiClient from "./apiClient";

export const aiService = {
  improveBullet: (payload: ImproveBulletPayload) =>
    apiClient.post<ApiResponse<ImproveBulletResponse>>("/ai/improve-bullet", payload),

  generateSummary: (resumeId: string) =>
    apiClient.post<ApiResponse<{ summary: string }>>("/ai/generate-summary", { resumeId }),

  suggestSkills: (resumeId: string) =>
    apiClient.post<ApiResponse<{ skills: string[] }>>("/ai/suggest-skills", { resumeId }),

  generateResume: (payload: GenerateResumePayload) =>
    apiClient.post<ApiResponse<GenerateResumeResponse>>("/ai/generate-resume", payload),

  improveResume: (resumeId: string) =>
    apiClient.post<ApiResponse<ImproveResumeResponse>>("/ai/improve-resume", { resumeId }),

  analyzeJob: (payload: JobAnalysisPayload) =>
    apiClient.post<ApiResponse<JobAnalysisResponse>>("/ai/analyze-job", payload),
};
