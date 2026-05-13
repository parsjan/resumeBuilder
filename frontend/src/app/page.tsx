import Link from "next/link";

export default function HomePage() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center gap-6 p-8">
      <h1 className="text-4xl font-bold tracking-tight">AI Resume Builder</h1>
      <p className="text-muted-foreground text-lg">
        Build a professional resume in minutes with AI assistance.
      </p>
      <div className="flex gap-4">
        <Link
          href="/login"
          className="bg-primary text-primary-foreground hover:bg-primary/90 rounded-md px-6 py-2.5 text-sm font-medium transition-colors"
        >
          Get Started
        </Link>
        <Link
          href="/dashboard"
          className="border-border hover:bg-accent rounded-md border px-6 py-2.5 text-sm font-medium transition-colors"
        >
          Dashboard
        </Link>
      </div>
    </main>
  );
}
