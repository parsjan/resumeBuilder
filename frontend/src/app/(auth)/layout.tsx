import type { Metadata } from "next";
import Link from "next/link";
import { FileText } from "lucide-react";

export const metadata: Metadata = {
  title: {
    default: "AI Resume Builder",
    template: "%s | AI Resume Builder",
  },
};

export default function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="bg-background grid min-h-screen lg:grid-cols-2">
      {/* ── Left branding panel (desktop only) ── */}
      <div className="from-primary to-primary/80 relative hidden flex-col justify-between bg-gradient-to-br p-10 text-white lg:flex">
        {/* Logo */}
        <Link href="/" className="flex items-center gap-2 font-semibold">
          <FileText className="h-5 w-5" />
          <span>AI Resume Builder</span>
        </Link>

        {/* Illustration placeholder */}
        <div className="flex flex-col gap-4">
          <blockquote className="space-y-2">
            <p className="text-lg leading-relaxed opacity-90">
              &ldquo;Landed my dream job within 2 weeks. The AI suggestions made my
              experience stand out immediately.&rdquo;
            </p>
            <footer className="text-sm opacity-70">— Alex R., Software Engineer</footer>
          </blockquote>
        </div>

        {/* Feature list */}
        <ul className="space-y-2 text-sm opacity-80">
          {[
            "AI-powered bullet point suggestions",
            "ATS-optimized formatting",
            "Real-time job description matching",
            "One-click PDF export",
          ].map((f) => (
            <li key={f} className="flex items-center gap-2">
              <span className="h-1.5 w-1.5 rounded-full bg-white/70" />
              {f}
            </li>
          ))}
        </ul>
      </div>

      {/* ── Right form panel ── */}
      <div className="flex flex-col items-center justify-center p-6 sm:p-10">
        {/* Mobile logo */}
        <Link href="/" className="mb-8 flex items-center gap-2 font-semibold lg:hidden">
          <FileText className="text-primary h-5 w-5" />
          <span>AI Resume Builder</span>
        </Link>

        <div className="w-full max-w-sm">{children}</div>
      </div>
    </div>
  );
}
