import type { Metadata } from "next";

import { ResumeBuilderShell } from "@/modules/resume/components/ResumeBuilderShell";

export const metadata: Metadata = { title: "Resume Builder" };

interface ResumeBuilderPageProps {
  params: Promise<{ id: string }>;
}

export default async function ResumeBuilderPage({ params }: ResumeBuilderPageProps) {
  const { id } = await params;

  return (
    <div className="h-screen overflow-hidden">
      <ResumeBuilderShell resumeId={id} />
    </div>
  );
}
