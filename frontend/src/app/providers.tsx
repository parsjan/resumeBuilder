"use client";

import { useEffect, useRef, useState } from "react";

import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
import { Toaster } from "sonner";

import { registerLogoutHandler } from "@/services/apiClient";
import { authService } from "@/services/authService";
import { useAuthStore } from "@/hooks/useStore";
import { RootStoreProvider } from "@/store/rootStore";

// ─── QueryClient factory ──────────────────────────────────────────────────────

function makeQueryClient(): QueryClient {
  return new QueryClient({
    defaultOptions: {
      queries: {
        staleTime: 60 * 1_000,        // 1 min before refetch
        gcTime: 5 * 60 * 1_000,       // 5 min cache retention
        retry: 1,
        refetchOnWindowFocus: false,
      },
      mutations: {
        retry: 0,
      },
    },
  });
}

// Singleton for the browser so hot-reloads don't create a new client
let browserQueryClient: QueryClient | undefined;

function getQueryClient(): QueryClient {
  if (typeof window === "undefined") return makeQueryClient();
  browserQueryClient ??= makeQueryClient();
  return browserQueryClient;
}

// ─── AppInitializer ───────────────────────────────────────────────────────────
// Runs once on mount: bootstraps auth + registers the apiClient logout hook.

function AppInitializer() {
  const authStore = useAuthStore();
  const initialized = useRef(false);

  useEffect(() => {
    if (initialized.current) return;
    initialized.current = true;

    // Register logout callback for the apiClient interceptor
    // tokenStorage.clearTokens() is called inside triggerLogout() before this runs
    registerLogoutHandler(() => authStore.logout());

    // Bootstrap: check whether the user has an active session
    authService
      .getMe()
      .then((res) => authStore.setUser(res.data.data))
      .catch(() => authStore.setUser(null))
      .finally(() => authStore.setInitialized());
  }, [authStore]);

  return null;
}

// ─── Providers ────────────────────────────────────────────────────────────────

export function Providers({ children }: { children: React.ReactNode }) {
  const [queryClient] = useState(getQueryClient);

  return (
    <QueryClientProvider client={queryClient}>
      <RootStoreProvider>
        <AppInitializer />
        {children}
        <Toaster richColors closeButton position="top-right" duration={4_000} />
      </RootStoreProvider>
      {process.env.NODE_ENV === "development" && (
        <ReactQueryDevtools initialIsOpen={false} buttonPosition="bottom-left" />
      )}
    </QueryClientProvider>
  );
}
