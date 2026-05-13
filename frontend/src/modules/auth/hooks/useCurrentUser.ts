"use client";

import { useQuery } from "@tanstack/react-query";
import { observer } from "mobx-react-lite";

import { useAuthStore } from "@/hooks/useStore";
import { authService } from "@/services/authService";

export const AUTH_KEYS = {
  me: ["auth", "me"] as const,
};

/**
 * TanStack Query wrapper around GET /auth/me.
 *
 * Only runs when the user is already authenticated (avoids unnecessary
 * network requests on public pages). Syncs the result back to authStore
 * so MobX-observed components stay in sync with any server-side changes
 * (e.g. profile update from another tab).
 */
export function useCurrentUser() {
  const authStore = useAuthStore();

  return useQuery({
    queryKey: AUTH_KEYS.me,
    queryFn: async () => {
      const res = await authService.getMe();
      const user = res.data.data;
      authStore.setUser(user); // keep MobX store in sync
      return user;
    },
    enabled: authStore.isAuthenticated,
    staleTime: 5 * 60 * 1_000, // 5 min — user profile rarely changes mid-session
  });
}

// Suppress lint warning: observer is intentionally unused here.
// Components that consume this hook must wrap themselves with observer()
// if they read authStore values directly.
void observer;
