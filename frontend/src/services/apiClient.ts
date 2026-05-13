import axios, {
  type AxiosError,
  type AxiosRequestConfig,
  type InternalAxiosRequestConfig,
} from "axios";
import { toast } from "sonner";

import type { ApiError } from "@/types";
import { tokenStorage } from "@/utils/tokenStorage";

// ─── Logout event bus ─────────────────────────────────────────────────────────
// apiClient has no direct access to MobX stores (would create circular imports).
// Instead, the AppInitializer registers a logout handler once on mount.

type LogoutHandler = () => void;
let _logoutHandler: LogoutHandler | null = null;

export function registerLogoutHandler(fn: LogoutHandler) {
  _logoutHandler = fn;
}

function triggerLogout() {
  tokenStorage.clearTokens();
  _logoutHandler?.();
  if (typeof window !== "undefined") {
    window.location.href = "/login";
  }
}

// ─── Token refresh queue ──────────────────────────────────────────────────────
// When the access token expires all in-flight requests receive a 401.
// We pause them, refresh once, then replay or reject the queue.

interface QueueItem {
  resolve: (value: unknown) => void;
  reject: (reason: unknown) => void;
}

let isRefreshing = false;
let failedQueue: QueueItem[] = [];

function processQueue(error: AxiosError | null) {
  failedQueue.forEach((item) => {
    if (error) {
      item.reject(error);
    } else {
      item.resolve(undefined);
    }
  });
  failedQueue = [];
}

// ─── Axios instance ───────────────────────────────────────────────────────────

const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  withCredentials: true,
  headers: { "Content-Type": "application/json" },
  timeout: 15_000,
});

// ─── Request interceptor ──────────────────────────────────────────────────────

apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = tokenStorage.getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => Promise.reject(error)
);

// ─── Response interceptor ─────────────────────────────────────────────────────

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiError>) => {
    const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean };
    const status = error.response?.status;
    const message = error.response?.data?.message ?? "An unexpected error occurred.";

    // ── 401 Unauthorized ────────────────────────────────────────────────────
    if (status === 401) {
      // These endpoints return 401 for their own reasons — never attempt refresh.
      const skipRefreshPaths = ["/auth/login", "/auth/register", "/auth/refresh"];
      const isAuthEndpoint = skipRefreshPaths.some((p) =>
        originalRequest.url?.includes(p)
      );

      if (isAuthEndpoint) {
        return Promise.reject(error);
      }

      if (originalRequest._retry) {
        // Refresh itself returned 401 — session fully expired.
        processQueue(error);
        triggerLogout();
        return Promise.reject(error);
      }

      // No refresh token stored → user is simply not logged in.
      const refreshToken = tokenStorage.getRefreshToken();
      if (!refreshToken) {
        return Promise.reject(error);
      }

      if (isRefreshing) {
        // Queue this request until the ongoing refresh resolves.
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then(() => apiClient(originalRequest));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const { data } = await apiClient.post(
          "/auth/refresh",
          null,
          { headers: { "X-Refresh-Token": refreshToken } }
        );
        const newAccessToken: string = data.data.accessToken;
        const newRefreshToken: string = data.data.refreshToken;
        tokenStorage.setTokens(newAccessToken, newRefreshToken);
        processQueue(null);
        return apiClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError as AxiosError);
        triggerLogout();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    // ── 403 Forbidden ───────────────────────────────────────────────────────
    if (status === 403) {
      toast.error("You don't have permission to do that.");
      return Promise.reject(error);
    }

    // ── 422 Unprocessable ───────────────────────────────────────────────────
    if (status === 422) {
      return Promise.reject(error);
    }

    // ── 404 Not Found ───────────────────────────────────────────────────────
    if (status === 404) {
      return Promise.reject(error);
    }

    // ── 5xx Server errors ───────────────────────────────────────────────────
    if (status && status >= 500) {
      toast.error("Server error. Please try again later.");
      return Promise.reject(error);
    }

    // ── Network / timeout ───────────────────────────────────────────────────
    if (!error.response) {
      toast.error("Network error. Check your connection.");
      return Promise.reject(error);
    }

    // ── Everything else ─────────────────────────────────────────────────────
    toast.error(message);
    return Promise.reject(error);
  }
);

export default apiClient;
