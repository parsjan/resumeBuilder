"use client";

import { useRef, useState } from "react";

import { useRouter } from "next/navigation";
import { CheckCircle2, FileUp, Upload, X } from "lucide-react";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { cn } from "@/utils/cn";
import { formatBytes } from "@/utils/formatters";
import { useUploadResume } from "../hooks/useUploadResume";
import type { Resume } from "@/types";

// ─── Constants ────────────────────────────────────────────────────────────────

const ACCEPTED_TYPES = [".pdf", ".doc", ".docx"];
const ACCEPTED_MIME = [
  "application/pdf",
  "application/msword",
  "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
];
const MAX_BYTES = 5 * 1024 * 1024; // 5 MB

// ─── Helpers ──────────────────────────────────────────────────────────────────

function isValidFile(file: File): string | null {
  if (!ACCEPTED_MIME.includes(file.type)) {
    return "Only PDF, DOC, and DOCX files are supported.";
  }
  if (file.size > MAX_BYTES) {
    return `File is too large. Maximum size is ${formatBytes(MAX_BYTES)}.`;
  }
  return null;
}

// ─── Drop zone ────────────────────────────────────────────────────────────────

interface DropZoneProps {
  onFile: (file: File) => void;
  disabled?: boolean;
}

function DropZone({ onFile, disabled }: DropZoneProps) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [isDragOver, setIsDragOver] = useState(false);
  const [fileError, setFileError] = useState<string | null>(null);

  function handleDrop(e: React.DragEvent) {
    e.preventDefault();
    setIsDragOver(false);
    if (disabled) return;

    const file = e.dataTransfer.files[0];
    if (!file) return;

    const error = isValidFile(file);
    if (error) {
      setFileError(error);
      return;
    }
    setFileError(null);
    onFile(file);
  }

  function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;

    const error = isValidFile(file);
    if (error) {
      setFileError(error);
      return;
    }
    setFileError(null);
    onFile(file);
    // Reset input so the same file can be re-selected after an error
    e.target.value = "";
  }

  return (
    <div className="space-y-2">
      <div
        role="button"
        tabIndex={disabled ? -1 : 0}
        aria-label="Upload file drop zone"
        className={cn(
          "flex cursor-pointer flex-col items-center justify-center gap-3 rounded-lg border-2 border-dashed px-6 py-10 text-center transition-colors",
          isDragOver
            ? "border-primary bg-primary/5"
            : "border-muted-foreground/25 hover:border-primary/50 hover:bg-muted/40",
          disabled && "pointer-events-none opacity-50"
        )}
        onDragOver={(e) => { e.preventDefault(); setIsDragOver(true); }}
        onDragLeave={() => setIsDragOver(false)}
        onDrop={handleDrop}
        onClick={() => inputRef.current?.click()}
        onKeyDown={(e) => e.key === "Enter" && inputRef.current?.click()}
      >
        <div className="bg-muted flex h-12 w-12 items-center justify-center rounded-full">
          <FileUp className="text-muted-foreground h-5 w-5" />
        </div>
        <div>
          <p className="text-sm font-medium">
            {isDragOver ? "Drop it here!" : "Drag & drop or click to browse"}
          </p>
          <p className="text-muted-foreground mt-0.5 text-xs">
            PDF, DOC, DOCX · max {formatBytes(MAX_BYTES)}
          </p>
        </div>
      </div>

      {fileError && (
        <p className="text-destructive flex items-center gap-1.5 text-xs">
          <X className="h-3.5 w-3.5 shrink-0" />
          {fileError}
        </p>
      )}

      <input
        ref={inputRef}
        type="file"
        accept={ACCEPTED_TYPES.join(",")}
        className="sr-only"
        onChange={handleChange}
        disabled={disabled}
      />
    </div>
  );
}

// ─── Upload progress view ─────────────────────────────────────────────────────

function UploadingView({ file, progress }: { file: File; progress: number }) {
  return (
    <div className="space-y-3 rounded-lg border bg-muted/30 p-4">
      <div className="flex items-center justify-between gap-2">
        <div className="flex min-w-0 items-center gap-2">
          <FileUp className="text-primary h-4 w-4 shrink-0" />
          <span className="truncate text-sm font-medium">{file.name}</span>
        </div>
        <span className="text-muted-foreground shrink-0 text-xs">{formatBytes(file.size)}</span>
      </div>

      {/* Progress bar */}
      <div className="space-y-1">
        <div className="bg-muted h-2 w-full overflow-hidden rounded-full">
          <div
            className="bg-primary h-full rounded-full transition-all duration-300 ease-out"
            style={{ width: `${progress}%` }}
          />
        </div>
        <div className="flex justify-between text-xs text-muted-foreground">
          <span>Uploading…</span>
          <span>{progress}%</span>
        </div>
      </div>
    </div>
  );
}

// ─── Success view ─────────────────────────────────────────────────────────────

function SuccessView({ resume, onOpenEditor }: { resume: Resume; onOpenEditor: () => void }) {
  return (
    <div className="flex flex-col items-center gap-4 py-4 text-center">
      <div className="flex h-14 w-14 items-center justify-center rounded-full bg-green-100 dark:bg-green-900/30">
        <CheckCircle2 className="h-7 w-7 text-green-600 dark:text-green-400" />
      </div>
      <div>
        <p className="text-base font-semibold">Upload successful!</p>
        <p className="text-muted-foreground mt-0.5 text-sm">
          <span className="text-foreground font-medium">&ldquo;{resume.title}&rdquo;</span> is ready
          to edit.
        </p>
      </div>
      <Button className="w-full" onClick={onOpenEditor}>
        Open Editor
      </Button>
    </div>
  );
}

// ─── Modal ────────────────────────────────────────────────────────────────────

interface UploadResumeModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function UploadResumeModal({ open, onOpenChange }: UploadResumeModalProps) {
  const router = useRouter();
  const upload = useUploadResume();
  const [selectedFile, setSelectedFile] = useState<File | null>(null);

  const isUploading = upload.isPending;
  const uploadedResume = upload.data ?? null;

  function handleOpenChange(next: boolean) {
    if (isUploading) return;
    if (!next) resetState();
    onOpenChange(next);
  }

  function resetState() {
    setSelectedFile(null);
    upload.reset();
  }

  function handleFile(file: File) {
    setSelectedFile(file);
    upload.mutate(file);
  }

  function handleOpenEditor() {
    if (!uploadedResume) return;
    onOpenChange(false);
    resetState();
    router.push(`/resume/${uploadedResume.id}`);
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-md">
        {/* Don't show header/footer in success state — the SuccessView is self-contained */}
        {uploadedResume ? (
          <>
            <DialogHeader className="sr-only">
              <DialogTitle>Upload successful</DialogTitle>
            </DialogHeader>
            <SuccessView resume={uploadedResume} onOpenEditor={handleOpenEditor} />
          </>
        ) : (
          <>
            <DialogHeader>
              <DialogTitle className="flex items-center gap-2">
                <Upload className="h-4 w-4" />
                Upload Resume
              </DialogTitle>
              <DialogDescription>
                Import an existing PDF or Word document. We&apos;ll parse it into an editable
                resume.
              </DialogDescription>
            </DialogHeader>

            {isUploading && selectedFile ? (
              <UploadingView file={selectedFile} progress={upload.progress} />
            ) : upload.isError ? (
              <div className="space-y-3">
                <div className="text-destructive rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm">
                  Upload failed. Please check your file and try again.
                </div>
                <DropZone onFile={handleFile} />
              </div>
            ) : (
              <DropZone onFile={handleFile} />
            )}

            <DialogFooter>
              <Button
                variant="outline"
                onClick={() => handleOpenChange(false)}
                disabled={isUploading}
              >
                Cancel
              </Button>
            </DialogFooter>
          </>
        )}
      </DialogContent>
    </Dialog>
  );
}
