import type { ApiResponse, PaginatedResponse, Resume, SectionType, ResumeSection } from "@/types";

import apiClient from "./apiClient";

export const resumeService = {
  getAll: () => apiClient.get<PaginatedResponse<Resume>>("/resumes"),

  getById: (id: string) => apiClient.get<ApiResponse<Resume>>(`/resumes/${id}`),

  create: (payload: { title: string }) =>
    apiClient.post<ApiResponse<Resume>>("/resumes", payload),

  update: (id: string, payload: Partial<Resume>) =>
    apiClient.patch<ApiResponse<Resume>>(`/resumes/${id}`, payload),

  updateSection: (id: string, section: SectionType, value: ResumeSection) =>
    apiClient.patch<ApiResponse<Resume>>(`/resumes/${id}/sections/${section}`, { value }),

  delete: (id: string) => apiClient.delete(`/resumes/${id}`),

  exportPdf: (id: string, onProgress?: (progress: number) => void) =>
    apiClient.get<Blob>(`/resumes/${id}/export/pdf`, {
      responseType: "blob",
      onDownloadProgress: (event) => {
        if (onProgress && event.total) {
          onProgress(Math.round((event.loaded * 100) / event.total));
        }
      },
    }),

  uploadFile: (file: File, onProgress?: (progress: number) => void) => {
    const formData = new FormData();
    formData.append("file", file);
    return apiClient.post<ApiResponse<Resume>>("/resumes/upload", formData, {
      headers: { "Content-Type": "multipart/form-data" },
      onUploadProgress: (event) => {
        if (onProgress && event.total) {
          onProgress(Math.round((event.loaded * 100) / event.total));
        }
      },
    });
  },
};
