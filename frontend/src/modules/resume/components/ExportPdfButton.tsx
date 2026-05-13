"use client";

import { Download, Loader2 } from "lucide-react";

import { Button } from "@/components/ui/button";
import { cn } from "@/utils/cn";
import { useExportPdf } from "../hooks/useExportPdf";

interface ExportPdfButtonProps {
  resumeId: string;
  title?: string;
  /** "icon" renders a square icon-only button; "default" includes a label. */
  variant?: "icon" | "default";
  className?: string;
}

export function ExportPdfButton({
  resumeId,
  title,
  variant = "default",
  className,
}: ExportPdfButtonProps) {
  const exportPdf = useExportPdf();

  const pending = exportPdf.isPending;

  if (variant === "icon") {
    return (
      <Button
        size="icon"
        variant="outline"
        className={cn("h-7 w-7", className)}
        onClick={() => exportPdf.mutate({ resumeId, title })}
        disabled={pending}
        aria-label="Download as PDF"
        title="Download as PDF"
      >
        {pending ? (
          <Loader2 className="h-3.5 w-3.5 animate-spin" />
        ) : (
          <Download className="h-3.5 w-3.5" />
        )}
      </Button>
    );
  }

  return (
    <Button
      size="sm"
      variant="outline"
      className={cn("h-8 gap-1.5", className)}
      onClick={() => exportPdf.mutate({ resumeId, title })}
      disabled={pending}
      title="Download as PDF"
    >
      {pending ? (
        <Loader2 className="h-3.5 w-3.5 animate-spin" />
      ) : (
        <Download className="h-3.5 w-3.5" />
      )}
      {pending ? "Exporting…" : "Export PDF"}
    </Button>
  );
}
