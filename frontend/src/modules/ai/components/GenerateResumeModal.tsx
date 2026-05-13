"use client";

import { useEffect, useState } from "react";

import { observer } from "mobx-react-lite";
import { Loader2, Sparkles } from "lucide-react";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Textarea } from "@/components/ui/textarea";
import { useUiStore } from "@/hooks/useStore";

import { useGenerateResume } from "../hooks/useGenerateResume";

// ─── Constants ────────────────────────────────────────────────────────────────

const PLACEHOLDER =
  "I'm a software engineer with 5 years of experience specialising in React and Node.js. " +
  "I worked at Acme Corp as a Senior Frontend Engineer where I led a team of 4 to redesign " +
  "the customer portal, reducing load time by 40%. Before that I was a junior developer at " +
  "StartupXYZ building REST APIs. I hold a B.Sc. in Computer Science from State University " +
  "and I'm AWS certified.";

const PHASES = [
  "Analysing your background…",
  "Structuring experience section…",
  "Crafting bullet points…",
  "Polishing the summary…",
  "Almost done…",
];

// ─── Loading indicator ────────────────────────────────────────────────────────

function GeneratingIndicator({ isPending }: { isPending: boolean }) {
  const [index, setIndex] = useState(0);

  useEffect(() => {
    if (!isPending) {
      setIndex(0);
      return;
    }
    const id = setInterval(
      () => setIndex((i) => Math.min(i + 1, PHASES.length - 1)),
      2_500
    );
    return () => clearInterval(id);
  }, [isPending]);

  if (!isPending) return null;

  return (
    <div className="flex items-center gap-2 text-sm">
      <Loader2 className="text-primary h-4 w-4 shrink-0 animate-spin" />
      <span className="text-muted-foreground">{PHASES[index]}</span>
    </div>
  );
}

// ─── Modal ────────────────────────────────────────────────────────────────────

export const GenerateResumeModal = observer(function GenerateResumeModal() {
  const uiStore = useUiStore();
  const [prompt, setPrompt] = useState("");
  const generate = useGenerateResume();

  const isOpen = uiStore.isModalOpen("generateResume");

  function handleOpenChange(open: boolean) {
    if (!open && !generate.isPending) {
      uiStore.closeModal("generateResume");
      generate.reset();
      setPrompt("");
    }
  }

  function handleGenerate() {
    const trimmed = prompt.trim();
    if (!trimmed) return;
    generate.mutate(trimmed, {
      onSuccess: () => setPrompt(""),
    });
  }

  return (
    <Dialog open={isOpen} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-xl">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Sparkles className="h-4 w-4 text-violet-500" />
            Generate Resume with AI
          </DialogTitle>
          <DialogDescription>
            Describe your background and experience in plain text. AI will fill in all resume
            sections — you can edit them afterwards.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-3">
          <Textarea
            value={prompt}
            onChange={(e) => setPrompt(e.target.value)}
            placeholder={PLACEHOLDER}
            rows={9}
            className="resize-none text-sm"
            disabled={generate.isPending}
            aria-label="Describe your background"
          />

          <GeneratingIndicator isPending={generate.isPending} />

          <p className="text-muted-foreground text-xs">
            Be as detailed as possible — include job titles, companies, dates, key achievements,
            education, and skills.
          </p>
        </div>

        <DialogFooter>
          <Button
            variant="outline"
            onClick={() => handleOpenChange(false)}
            disabled={generate.isPending}
          >
            Cancel
          </Button>
          <Button onClick={handleGenerate} disabled={!prompt.trim() || generate.isPending}>
            {generate.isPending ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Generating…
              </>
            ) : (
              <>
                <Sparkles className="mr-2 h-4 w-4" />
                Generate
              </>
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
});
