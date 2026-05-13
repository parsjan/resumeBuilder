import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

// Routes accessible without authentication
const PUBLIC_PATHS = new Set(["/", "/login", "/register"]);

// Auth-flow routes that should never redirect (OAuth callback, etc.)
const AUTH_FLOW_PREFIXES = ["/auth/", "/api/", "/callback"];

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // Never intercept auth-flow or Next.js internal routes
  if (AUTH_FLOW_PREFIXES.some((p) => pathname.startsWith(p))) {
    return NextResponse.next();
  }

  const token = request.cookies.get("access_token")?.value;
  const isPublic = PUBLIC_PATHS.has(pathname);

  // ── Unauthenticated user on protected route ──
  if (!token && !isPublic) {
    const loginUrl = new URL("/login", request.url);
    loginUrl.searchParams.set("redirect", pathname);
    return NextResponse.redirect(loginUrl);
  }

  // ── Authenticated user hitting auth pages ──
  if (token && (pathname === "/login" || pathname === "/register")) {
    return NextResponse.redirect(new URL("/dashboard", request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    /*
     * Match all paths except:
     * - _next/static (static assets)
     * - _next/image (image optimisation)
     * - favicon.ico
     * - Files with an extension (e.g. .png, .svg)
     */
    "/((?!_next/static|_next/image|favicon.ico|.*\\..*).*)",
  ],
};
