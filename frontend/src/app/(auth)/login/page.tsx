import type { Metadata } from "next";
import Link from "next/link";

import { LoginForm } from "@/modules/auth/components/LoginForm";

export const metadata: Metadata = { title: "Sign In" };

interface LoginPageProps {
  searchParams: Promise<{ registered?: string }>;
}

export default async function LoginPage({ searchParams }: LoginPageProps) {
  const { registered } = await searchParams;

  return (
    <div className="space-y-6">
      {/* Heading */}
      <div className="space-y-1 text-center">
        <h1 className="text-2xl font-bold tracking-tight">Welcome back</h1>
        <p className="text-muted-foreground text-sm">Sign in to your account to continue</p>
      </div>

      {/* Success banner after registration */}
      {registered === "true" && (
        <div className="bg-green-50 text-green-800 rounded-md border border-green-200 px-4 py-3 text-sm">
          Account created successfully — please sign in.
        </div>
      )}

      {/* Form */}
      <LoginForm />

      {/* Footer link */}
      <p className="text-muted-foreground text-center text-sm">
        Don&apos;t have an account?{" "}
        <Link
          href="/register"
          className="text-foreground font-medium underline-offset-4 hover:underline"
        >
          Sign up for free
        </Link>
      </p>
    </div>
  );
}
