const ACCESS_TOKEN_KEY = "access_token";
const REFRESH_TOKEN_KEY = "refresh_token";

function setCookie(name: string, value: string) {
  // 7-day expiry; no HttpOnly so middleware can read it
  const expires = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toUTCString();
  document.cookie = `${name}=${value}; expires=${expires}; path=/; SameSite=Lax`;
}

function deleteCookie(name: string) {
  document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`;
}

export const tokenStorage = {
  getAccessToken: (): string | null =>
    typeof window !== "undefined" ? localStorage.getItem(ACCESS_TOKEN_KEY) : null,

  getRefreshToken: (): string | null =>
    typeof window !== "undefined" ? localStorage.getItem(REFRESH_TOKEN_KEY) : null,

  setTokens: (accessToken: string, refreshToken: string): void => {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
    // Cookie lets Next.js middleware see the auth state
    setCookie(ACCESS_TOKEN_KEY, accessToken);
  },

  clearTokens: (): void => {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    deleteCookie(ACCESS_TOKEN_KEY);
  },
};
