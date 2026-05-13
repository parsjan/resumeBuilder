"use client";

import { useState, useRef } from "react";

import { observer } from "mobx-react-lite";
import { Loader2, Sparkles, X } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useResumeStore } from "@/hooks/useStore";
import { useSuggestSkills } from "@/modules/ai/hooks/useSuggestSkills";
import { cn } from "@/utils/cn";

export const SkillsEditor = observer(function SkillsEditor() {
  const resumeStore = useResumeStore();
  const skills = resumeStore.resume?.sections.skills ?? [];
  const [inputValue, setInputValue] = useState("");
  const inputRef = useRef<HTMLInputElement>(null);
  const suggestSkills = useSuggestSkills();

  function commitSkill(raw: string) {
    // Support comma-separated entry ("React, Node.js, TypeScript")
    raw
      .split(",")
      .map((s) => s.trim())
      .filter(Boolean)
      .forEach((skill) => resumeStore.addSkill(skill));
    setInputValue("");
  }

  function handleKeyDown(e: React.KeyboardEvent<HTMLInputElement>) {
    if (e.key === "Enter" || e.key === ",") {
      e.preventDefault();
      commitSkill(inputValue);
    }
    if (e.key === "Backspace" && inputValue === "" && skills.length > 0) {
      resumeStore.removeSkill(skills.length - 1);
    }
  }

  function handleBlur() {
    if (inputValue.trim()) commitSkill(inputValue);
  }

  return (
    <div className="space-y-3">
      {/* Tag input */}
      <div
        className="border-input bg-background focus-within:ring-ring flex min-h-20 w-full cursor-text flex-wrap gap-1.5 rounded-md border px-3 py-2 text-sm focus-within:ring-2"
        onClick={() => inputRef.current?.focus()}
      >
        {skills.map((skill, i) => (
          <span
            key={`${skill}-${i}`}
            className="bg-secondary text-secondary-foreground flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-medium"
          >
            {skill}
            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                resumeStore.removeSkill(i);
              }}
              className="hover:text-destructive ml-0.5"
              aria-label={`Remove ${skill}`}
            >
              <X className="h-3 w-3" />
            </button>
          </span>
        ))}

        <Input
          ref={inputRef}
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          onKeyDown={handleKeyDown}
          onBlur={handleBlur}
          placeholder={skills.length === 0 ? "Type a skill and press Enter…" : "Add more…"}
          className={cn(
            "h-auto min-w-30 flex-1 border-0 p-0 shadow-none focus-visible:ring-0"
          )}
        />
      </div>

      {/* Footer row */}
      <div className="flex items-center justify-between gap-2">
        <p className="text-muted-foreground text-xs">
          Press <kbd className="bg-muted rounded px-1 py-0.5 font-mono text-xs">Enter</kbd> or{" "}
          <kbd className="bg-muted rounded px-1 py-0.5 font-mono text-xs">,</kbd> to add.
          Backspace removes the last one.
        </p>

        <Button
          type="button"
          size="sm"
          variant="outline"
          onClick={() => suggestSkills.mutate()}
          disabled={suggestSkills.isPending || !resumeStore.resume}
          className="shrink-0 gap-1.5 text-xs"
        >
          {suggestSkills.isPending ? (
            <Loader2 className="h-3.5 w-3.5 animate-spin" />
          ) : (
            <Sparkles className="h-3.5 w-3.5 text-violet-500" />
          )}
          {suggestSkills.isPending ? "Suggesting…" : "Suggest Skills"}
        </Button>
      </div>
    </div>
  );
});
