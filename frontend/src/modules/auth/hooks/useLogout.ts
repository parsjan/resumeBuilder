"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { toast } from "sonner";

import { useAuthStore } from "@/hooks/useStore";
import { authService } from "@/services/authService";
import { tokenStorage } from "@/utils/tokenStorage";

export function useLogout() {
  const router = useRouter();
  const authStore = useAuthStore();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => authService.logout(),
    onSettled: () => {
      // Always clear local state regardless of API success/failure
      tokenStorage.clearTokens(); // clears localStorage + access_token cookie
      authStore.logout();
      queryClient.clear(); // purge all cached server state
      toast.success("You've been signed out.");
      router.push("/login");
      router.refresh();
    },
  });
}
