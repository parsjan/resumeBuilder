"use client";

import { useMutation } from "@tanstack/react-query";
import { toast } from "sonner";

import { resumeService } from "@/services/resumeService";

interface ExportPdfArgs {
  resumeId: string;
  /** Used as the downloaded file name. */
  title?: string;
}

/**
 * Calls GET /resumes/{id}/export/pdf (blob), then triggers a browser download.
 * Returns the TanStack mutation so callers can read `isPending`.
 */
export function useExportPdf() {
  return useMutation({
    mutationFn: async ({ resumeId, title }: ExportPdfArgs) => {
      const response = await resumeService.exportPdf(resumeId);
      const blob = new Blob([response.data], { type: "application/pdf" });
      const url = URL.createObjectURL(blob);

      const anchor = document.createElement("a");
      anchor.href = url;
      anchor.download = `${(title ?? "resume").replace(/[/\\?%*:|"<>]/g, "-")}.pdf`;
      document.body.appendChild(anchor);
      anchor.click();
      document.body.removeChild(anchor);

      // Small delay so the browser finishes initiating the download before we revoke
      setTimeout(() => URL.revokeObjectURL(url), 1_000);
    },
    onSuccess: () => toast.success("PDF downloaded!"),
    onError: () => toast.error("Failed to export PDF. Please try again."),
  });
}
