"use client";

import { useMutation } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { toast } from "sonner";

import { useAuthStore } from "@/hooks/useStore";
import { authService } from "@/services/authService";
import type { RegisterPayload } from "@/types";

export function useRegister() {
  const router = useRouter();
  const authStore = useAuthStore();

  return useMutation({
    mutationFn: (payload: RegisterPayload) => authService.register(payload),
    onSuccess: async (response) => {
      // Backend returns the created user. If it also sets the session cookie
      // (auto-login on register) we can go straight to the dashboard.
      // If not, redirect to /login with a success param.
      const user = response.data.data;

      // Optimistically try to verify the session via /auth/me
      try {
        const meRes = await authService.getMe();
        authStore.setUser(meRes.data.data);
        authStore.setInitialized();
        toast.success(`Account created! Welcome, ${user.fullName.split(" ")[0]}.`);
        router.push("/dashboard");
        router.refresh();
      } catch {
        // Backend didn't auto-login — redirect to login with success notice
        toast.success("Account created! Please sign in.");
        router.push("/login?registered=true");
      }
    },
    // Field-level 422 errors handled in RegisterForm via applyServerErrors
  });
}
