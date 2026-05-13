"use client";

import { useState } from "react";

import { AlertTriangle, FileText, Plus, RefreshCw, Upload } from "lucide-react";

import { Button } from "@/components/ui/button";
import { useResumes } from "../hooks/useResumes";
import { CreateResumeModal } from "./CreateResumeModal";
import { DeleteResumeDialog } from "./DeleteResumeDialog";
import { ResumeCard } from "./ResumeCard";
import { UploadResumeModal } from "./UploadResumeModal";

// ─── Skeleton card ────────────────────────────────────────────────────────────

function ResumeCardSkeleton() {
  return (
    <div className="bg-card animate-pulse rounded-xl border p-5 shadow">
      <div className="mb-3 flex items-start justify-between">
        <div className="bg-muted h-9 w-9 rounded-lg" />
      </div>
      <div className="bg-muted mb-1.5 h-4 w-3/4 rounded" />
      <div className="bg-muted mb-3 h-3 w-1/3 rounded" />
      <div className="bg-muted mb-2 h-1.5 w-full rounded-full" />
      <div className="flex gap-1.5">
        <div className="bg-muted h-4 w-10 rounded-full" />
        <div className="bg-muted h-4 w-12 rounded-full" />
      </div>
      <div className="bg-muted mt-4 h-8 w-full rounded-md" />
    </div>
  );
}

// ─── Empty state ──────────────────────────────────────────────────────────────

function EmptyState({ onCreateClick }: { onCreateClick: () => void }) {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center">
      <div className="bg-muted mb-4 flex h-16 w-16 items-center justify-center rounded-full">
        <FileText className="text-muted-foreground h-7 w-7" />
      </div>
      <h3 className="mb-1.5 text-base font-semibold">No resumes yet</h3>
      <p className="text-muted-foreground mb-6 max-w-xs text-sm">
        Create your first resume to get started. It only takes a minute.
      </p>
      <Button onClick={onCreateClick}>
        <Plus className="mr-2 h-4 w-4" />
        Create your first resume
      </Button>
    </div>
  );
}

// ─── Error state ──────────────────────────────────────────────────────────────

function ErrorState({ onRetry }: { onRetry: () => void }) {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center">
      <div className="bg-destructive/10 mb-4 flex h-16 w-16 items-center justify-center rounded-full">
        <AlertTriangle className="text-destructive h-7 w-7" />
      </div>
      <h3 className="mb-1.5 text-base font-semibold">Failed to load resumes</h3>
      <p className="text-muted-foreground mb-6 max-w-xs text-sm">
        Something went wrong while fetching your resumes. Please try again.
      </p>
      <Button variant="outline" onClick={onRetry}>
        <RefreshCw className="mr-2 h-4 w-4" />
        Try again
      </Button>
    </div>
  );
}

// ─── Main component ───────────────────────────────────────────────────────────

export function ResumeList() {
  const [createOpen, setCreateOpen] = useState(false);
  const [uploadOpen, setUploadOpen] = useState(false);
  const { data, isLoading, isError, refetch } = useResumes();

  const resumes = data?.data?.content ?? [];
  const total = data?.data?.totalElements ?? 0;

  return (
    <>
      {/* ── Page header ── */}
      <div className="mb-6 flex items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">My Resumes</h1>
          {!isLoading && !isError && (
            <p className="text-muted-foreground mt-0.5 text-sm">
              {total === 0
                ? "No resumes yet"
                : `${total} resume${total === 1 ? "" : "s"}`}
            </p>
          )}
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            onClick={() => setUploadOpen(true)}
            disabled={isLoading}
          >
            <Upload className="mr-2 h-4 w-4" />
            Upload
          </Button>
          <Button onClick={() => setCreateOpen(true)} disabled={isLoading}>
            <Plus className="mr-2 h-4 w-4" />
            New Resume
          </Button>
        </div>
      </div>

      {/* ── Content ── */}
      {isLoading ? (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <ResumeCardSkeleton key={i} />
          ))}
        </div>
      ) : isError ? (
        <ErrorState onRetry={() => refetch()} />
      ) : resumes.length === 0 ? (
        <EmptyState onCreateClick={() => setCreateOpen(true)} />
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {resumes.map((resume) => (
            <ResumeCard key={resume.id} resume={resume} />
          ))}
        </div>
      )}

      {/* ── Modals (always mounted, controlled by state/uiStore) ── */}
      <CreateResumeModal open={createOpen} onOpenChange={setCreateOpen} />
      <UploadResumeModal open={uploadOpen} onOpenChange={setUploadOpen} />
      <DeleteResumeDialog />
    </>
  );
}
