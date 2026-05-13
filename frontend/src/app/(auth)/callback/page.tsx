"use client";

import { Suspense, useEffect, useRef } from "react";

import { Loader2 } from "lucide-react";
import { useRouter, useSearchParams } from "next/navigation";
import { toast } from "sonner";

import { useAuthStore } from "@/hooks/useStore";
import { authService } from "@/services/authService";
import { tokenStorage } from "@/utils/tokenStorage";

function OAuthCallbackContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const authStore = useAuthStore();
  const called = useRef(false);

  useEffect(() => {
    if (called.current) return;
    called.current = true;

    const error = searchParams.get("error");

    if (error) {
      toast.error("Google sign-in was cancelled or failed. Please try again.");
      router.replace("/login");
      return;
    }

    const accessToken = searchParams.get("accessToken");
    const refreshToken = searchParams.get("refreshToken");

    if (!accessToken || !refreshToken) {
      toast.error("Authentication failed. Missing tokens.");
      router.replace("/login");
      return;
    }

    tokenStorage.setTokens(accessToken, refreshToken);

    authService
      .getMe()
      .then((res) => {
        authStore.setUser(res.data.data);
        authStore.setInitialized();
        toast.success(`Welcome, ${res.data.data.fullName.split(" ")[0]}!`);
        const redirect = searchParams.get("redirect") ?? "/dashboard";
        router.replace(redirect);
      })
      .catch(() => {
        toast.error("Authentication failed. Please try again.");
        router.replace("/login");
      });
  }, [authStore, router, searchParams]);

  return null;
}

function Spinner() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-3">
      <Loader2 className="text-primary h-8 w-8 animate-spin" />
      <p className="text-muted-foreground text-sm">Completing sign-in…</p>
    </div>
  );
}

export default function OAuthCallbackPage() {
  return (
    <Suspense fallback={<Spinner />}>
      <Spinner />
      <OAuthCallbackContent />
    </Suspense>
  );
}
