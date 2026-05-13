"use client";

import { useMutation } from "@tanstack/react-query";

import { aiService } from "@/services/aiService";
import type { ImproveBulletPayload, ImproveBulletResponse } from "@/types";

/**
 * Fires POST /ai/improve-bullet and returns the mutation.
 * The calling component is responsible for handling `data` (applying or dismissing suggestions).
 */
export function useImproveBullet() {
  return useMutation<ImproveBulletResponse, Error, ImproveBulletPayload>({
    mutationFn: (payload) => aiService.improveBullet(payload).then((r) => r.data.data),
  });
}
