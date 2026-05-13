import type { ApiResponse, AuthTokens, LoginPayload, RegisterPayload, User } from "@/types";

import apiClient from "./apiClient";

export const authService = {
  login: (payload: LoginPayload) =>
    apiClient.post<ApiResponse<AuthTokens>>("/auth/login", payload),

  register: (payload: RegisterPayload) =>
    apiClient.post<ApiResponse<User>>("/auth/register", payload),

  logout: () => apiClient.post("/auth/logout"),

  getMe: () => apiClient.get<ApiResponse<User>>("/auth/me"),

  refreshToken: () => apiClient.post<ApiResponse<AuthTokens>>("/auth/refresh"),

  /**
   * Initiates Google OAuth flow.
   * Backend redirects the browser to Google, then back to /auth/callback
   * after successful authentication.
   */
  getGoogleOAuthUrl: (): string =>
    `${process.env.NEXT_PUBLIC_API_URL}/oauth2/authorize/google`,
};
