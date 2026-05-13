import type { Metadata } from "next";
import Link from "next/link";

import { RegisterForm } from "@/modules/auth/components/RegisterForm";

export const metadata: Metadata = { title: "Create Account" };

export default function RegisterPage() {
  return (
    <div className="space-y-6">
      {/* Heading */}
      <div className="space-y-1 text-center">
        <h1 className="text-2xl font-bold tracking-tight">Create your account</h1>
        <p className="text-muted-foreground text-sm">
          Start building your AI-powered resume for free
        </p>
      </div>

      {/* Form */}
      <RegisterForm />

      {/* Footer link */}
      <p className="text-muted-foreground text-center text-sm">
        Already have an account?{" "}
        <Link
          href="/login"
          className="text-foreground font-medium underline-offset-4 hover:underline"
        >
          Sign in
        </Link>
      </p>

      {/* Terms */}
      <p className="text-muted-foreground text-center text-xs">
        By creating an account you agree to our{" "}
        <Link href="/terms" className="underline underline-offset-4 hover:text-foreground">
          Terms of Service
        </Link>{" "}
        and{" "}
        <Link href="/privacy" className="underline underline-offset-4 hover:text-foreground">
          Privacy Policy
        </Link>
        .
      </p>
    </div>
  );
}
