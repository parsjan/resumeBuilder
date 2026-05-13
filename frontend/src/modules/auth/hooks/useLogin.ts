"use client";

import { useMutation } from "@tanstack/react-query";
import { useRouter, useSearchParams } from "next/navigation";
import { toast } from "sonner";

import { useAuthStore } from "@/hooks/useStore";
import { authService } from "@/services/authService";
import type { LoginPayload } from "@/types";
import { tokenStorage } from "@/utils/tokenStorage";

export function useLogin() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const authStore = useAuthStore();

  return useMutation({
    mutationFn: (payload: LoginPayload) => authService.login(payload),
    onSuccess: async (loginResponse) => {
      // Store tokens so subsequent requests attach the Authorization header
      const { accessToken, refreshToken } = loginResponse.data.data;
      tokenStorage.setTokens(accessToken, refreshToken);

      const { data } = await authService.getMe();
      authStore.setUser(data.data);
      authStore.setInitialized();

      toast.success(`Welcome back, ${data.data.fullName.split(" ")[0]}!`);

      // Honour the ?redirect= param set by middleware
      const redirect = searchParams.get("redirect") ?? "/dashboard";
      router.push(redirect);
      router.refresh(); // revalidate server components
    },
    // ⚠️  onError is intentionally absent here.
    // The calling form component handles field-level 422 errors via
    // applyServerErrors(form.setError, error) and shows a generic toast
    // for credential errors (401/400) itself.
  });
}
