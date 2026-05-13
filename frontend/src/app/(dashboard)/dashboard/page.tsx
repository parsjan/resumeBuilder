import type { Metadata } from "next";

import { ResumeList } from "@/modules/resume/components/ResumeList";

export const metadata: Metadata = { title: "Dashboard — AI Resume Builder" };

export default function DashboardPage() {
  return <ResumeList />;
}
