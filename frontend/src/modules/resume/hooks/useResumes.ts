"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

import { resumeService } from "@/services/resumeService";
import type { Resume } from "@/types";

export const RESUME_KEYS = {
  all: ["resumes"] as const,
  detail: (id: string) => ["resumes", id] as const,
};

export function useResumes() {
  return useQuery({
    queryKey: RESUME_KEYS.all,
    queryFn: () => resumeService.getAll().then((r) => r.data),
  });
}

export function useResume(id: string) {
  return useQuery({
    queryKey: RESUME_KEYS.detail(id),
    queryFn: () => resumeService.getById(id).then((r) => r.data.data),
    enabled: !!id,
    staleTime: 30_000, // don't refetch mid-edit unless 30s have passed
  });
}

export function useCreateResume() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (title: string) => resumeService.create({ title }).then((r) => r.data.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: RESUME_KEYS.all });
      toast.success("Resume created.");
    },
  });
}

export function useUpdateResume() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: Partial<Resume> }) =>
      resumeService.update(id, data).then((r) => r.data.data),
    onSuccess: (updatedResume) => {
      // Sync the detail cache with the saved version
      queryClient.setQueryData(RESUME_KEYS.detail(updatedResume.id), updatedResume);
    },
  });
}

export function useDeleteResume() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => resumeService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: RESUME_KEYS.all });
      toast.success("Resume deleted.");
    },
  });
}
