"use client";

import { observer } from "mobx-react-lite";

import { useResumeStore } from "@/hooks/useStore";
import { formatDateRange, formatMonthYear } from "@/utils/formatDate";
import type { ResumeSections } from "@/types";

// ─── Preview shell ────────────────────────────────────────────────────────────

export const ResumePreview = observer(function ResumePreview() {
  const { resume } = useResumeStore();

  if (!resume) {
    return (
      <div className="text-muted-foreground flex h-full items-center justify-center text-sm">
        Loading preview…
      </div>
    );
  }

  return (
    // Outer wrapper scrolls; inner paper stays A4-ish
    <div className="flex min-h-full justify-center p-6">
      <ResumePaper sections={resume.sections} />
    </div>
  );
});

// ─── Paper ────────────────────────────────────────────────────────────────────

function ResumePaper({ sections: s }: { sections: ResumeSections }) {
  const { personalInfo: pi } = s;

  return (
    <div
      className="w-full max-w-[680px] bg-white px-10 py-8 text-[#1a1a1a] shadow-md"
      style={{ fontFamily: "'Georgia', serif", minHeight: "860px" }}
    >
      {/* ── Header ── */}
      <div className="mb-5 text-center">
        <h1
          className="text-2xl font-bold tracking-wide"
          style={{ fontFamily: "'Arial', sans-serif" }}
        >
          {pi.fullName || (
            <span className="text-gray-300 italic">Your Name</span>
          )}
        </h1>

        <div className="mt-1.5 flex flex-wrap items-center justify-center gap-x-2 text-[13px] text-gray-600">
          {[
            pi.email,
            pi.phone,
            pi.location,
            pi.linkedIn,
            pi.website,
          ]
            .filter(Boolean)
            .map((val, i, arr) => (
              <span key={i} className="flex items-center gap-x-2">
                {val?.startsWith("http") ? (
                  <a href={val} className="text-blue-600 hover:underline">
                    {val.replace(/^https?:\/\//, "")}
                  </a>
                ) : (
                  <span>{val}</span>
                )}
                {i < arr.length - 1 && (
                  <span className="text-gray-300">|</span>
                )}
              </span>
            ))}
        </div>
      </div>

      {/* ── Summary ── */}
      {s.summary.trim() && (
        <PreviewSection title="Professional Summary">
          <p className="text-[13px] leading-relaxed text-gray-700">{s.summary}</p>
        </PreviewSection>
      )}

      {/* ── Experience ── */}
      {s.experience.length > 0 && (
        <PreviewSection title="Experience">
          {s.experience.map((exp) => (
            <div key={exp.id} className="mb-3 last:mb-0">
              <div className="flex items-start justify-between gap-2">
                <div>
                  <p className="text-[13.5px] font-bold" style={{ fontFamily: "Arial, sans-serif" }}>
                    {exp.role || <em className="text-gray-300">Role</em>}
                  </p>
                  <p className="text-[13px] text-gray-600">{exp.company}</p>
                </div>
                <p className="shrink-0 text-[12px] text-gray-500">
                  {formatDateRange(exp.startDate, exp.endDate, exp.current)}
                </p>
              </div>
              {exp.bullets.filter((b) => b.trim()).length > 0 && (
                <ul className="ml-4 mt-1 list-disc space-y-0.5">
                  {exp.bullets
                    .filter((b) => b.trim())
                    .map((bullet, i) => (
                      <li key={i} className="text-[13px] leading-snug text-gray-700">
                        {bullet}
                      </li>
                    ))}
                </ul>
              )}
            </div>
          ))}
        </PreviewSection>
      )}

      {/* ── Education ── */}
      {s.education.length > 0 && (
        <PreviewSection title="Education">
          {s.education.map((edu) => (
            <div key={edu.id} className="mb-2 flex items-start justify-between gap-2 last:mb-0">
              <div>
                <p className="text-[13.5px] font-bold" style={{ fontFamily: "Arial, sans-serif" }}>
                  {edu.institution}
                </p>
                <p className="text-[13px] text-gray-600">
                  {[edu.degree, edu.field].filter(Boolean).join(", ")}
                  {edu.gpa ? ` — GPA: ${edu.gpa}` : ""}
                </p>
              </div>
              <p className="shrink-0 text-[12px] text-gray-500">
                {formatDateRange(edu.startDate, edu.endDate)}
              </p>
            </div>
          ))}
        </PreviewSection>
      )}

      {/* ── Skills ── */}
      {s.skills.length > 0 && (
        <PreviewSection title="Skills">
          <p className="text-[13px] leading-relaxed text-gray-700">
            {s.skills.join(" • ")}
          </p>
        </PreviewSection>
      )}

      {/* ── Projects ── */}
      {s.projects.length > 0 && (
        <PreviewSection title="Projects">
          {s.projects.map((proj) => (
            <div key={proj.id} className="mb-3 last:mb-0">
              <div className="flex items-baseline gap-2">
                <p className="text-[13.5px] font-bold" style={{ fontFamily: "Arial, sans-serif" }}>
                  {proj.name || "Project"}
                </p>
                {proj.url && (
                  <a
                    href={proj.url}
                    className="text-[12px] text-blue-600 hover:underline"
                  >
                    {proj.url.replace(/^https?:\/\//, "")}
                  </a>
                )}
              </div>
              {proj.description && (
                <p className="text-[13px] text-gray-600">{proj.description}</p>
              )}
              {proj.bullets.filter((b) => b.trim()).length > 0 && (
                <ul className="ml-4 mt-1 list-disc space-y-0.5">
                  {proj.bullets
                    .filter((b) => b.trim())
                    .map((bullet, i) => (
                      <li key={i} className="text-[13px] leading-snug text-gray-700">
                        {bullet}
                      </li>
                    ))}
                </ul>
              )}
              {proj.technologies.length > 0 && (
                <p className="mt-1 text-[12px] text-gray-500">
                  <span className="font-semibold">Stack: </span>
                  {proj.technologies.join(", ")}
                </p>
              )}
            </div>
          ))}
        </PreviewSection>
      )}

      {/* ── Certifications ── */}
      {s.certifications.length > 0 && (
        <PreviewSection title="Certifications">
          {s.certifications.map((cert) => (
            <div key={cert.id} className="mb-1.5 flex items-start justify-between last:mb-0">
              <div>
                <span className="text-[13.5px] font-bold" style={{ fontFamily: "Arial, sans-serif" }}>
                  {cert.name}
                </span>
                {cert.issuer && (
                  <span className="text-[13px] text-gray-600"> — {cert.issuer}</span>
                )}
              </div>
              {cert.date && (
                <p className="shrink-0 text-[12px] text-gray-500">
                  {formatMonthYear(cert.date)}
                </p>
              )}
            </div>
          ))}
        </PreviewSection>
      )}
    </div>
  );
}

// ─── Section wrapper ──────────────────────────────────────────────────────────

function PreviewSection({
  title,
  children,
}: {
  title: string;
  children: React.ReactNode;
}) {
  return (
    <section className="mb-5">
      <div className="mb-2">
        <h2
          className="text-[11px] font-extrabold uppercase tracking-[0.12em] text-gray-800"
          style={{ fontFamily: "Arial, sans-serif" }}
        >
          {title}
        </h2>
        <div className="mt-0.5 h-px bg-gray-300" />
      </div>
      {children}
    </section>
  );
}
