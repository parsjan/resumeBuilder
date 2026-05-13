"use client";

import { useState } from "react";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

import { resumeService } from "@/services/resumeService";
import type { Resume } from "@/types";
import { RESUME_KEYS } from "./useResumes";

/**
 * Uploads a resume file to POST /resumes/upload.
 * Tracks upload progress in local state so callers can render a progress bar.
 */
export function useUploadResume() {
  const queryClient = useQueryClient();
  const [progress, setProgress] = useState(0);

  const mutation = useMutation<Resume, Error, File>({
    onMutate: () => setProgress(0),
    mutationFn: (file: File) =>
      resumeService.uploadFile(file, setProgress).then((r) => r.data.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: RESUME_KEYS.all });
    },
    onError: () => {
      toast.error("Upload failed. Please try again.");
    },
  });

  return { ...mutation, progress };
}
