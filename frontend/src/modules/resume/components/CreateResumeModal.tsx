"use client";

import { useState } from "react";

import { useRouter } from "next/navigation";
import { Loader2, Plus } from "lucide-react";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useCreateResume } from "../hooks/useResumes";

interface CreateResumeModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function CreateResumeModal({ open, onOpenChange }: CreateResumeModalProps) {
  const router = useRouter();
  const [title, setTitle] = useState("");
  const createResume = useCreateResume();

  function handleOpenChange(next: boolean) {
    if (createResume.isPending) return;
    if (!next) {
      setTitle("");
      createResume.reset();
    }
    onOpenChange(next);
  }

  function handleCreate() {
    const trimmed = title.trim() || "Untitled Resume";
    createResume.mutate(trimmed, {
      onSuccess: (resume) => {
        onOpenChange(false);
        setTitle("");
        router.push(`/resume/${resume.id}`);
      },
    });
  }

  function handleKeyDown(e: React.KeyboardEvent<HTMLInputElement>) {
    if (e.key === "Enter") handleCreate();
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-sm">
        <DialogHeader>
          <DialogTitle>New Resume</DialogTitle>
          <DialogDescription>
            Give your resume a name to get started. You can rename it anytime.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-1.5">
          <Label htmlFor="resume-title">Resume title</Label>
          <Input
            id="resume-title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="e.g. Software Engineer — Google"
            autoFocus
            disabled={createResume.isPending}
          />
        </div>

        <DialogFooter>
          <Button
            variant="outline"
            onClick={() => handleOpenChange(false)}
            disabled={createResume.isPending}
          >
            Cancel
          </Button>
          <Button onClick={handleCreate} disabled={createResume.isPending}>
            {createResume.isPending ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Creating…
              </>
            ) : (
              <>
                <Plus className="mr-2 h-4 w-4" />
                Create
              </>
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
