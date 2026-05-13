"use client";

import { useRef, useEffect, useState } from "react";

import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import { GripVertical, Loader2, Sparkles, Trash2 } from "lucide-react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { cn } from "@/utils/cn";
import { useImproveBullet } from "@/modules/ai/hooks/useImproveBullet";
import type { ImproveBulletResponse } from "@/types";

interface BulletEditorProps {
  id: string;
  value: string;
  onChange: (value: string) => void;
  onRemove: () => void;
  canRemove?: boolean;
  placeholder?: string;
  /** e.g. "Software Engineer at Google" — sent to AI for context */
  context?: string;
}

export function BulletEditor({
  id,
  value,
  onChange,
  onRemove,
  canRemove = true,
  placeholder = "Describe a key achievement or responsibility…",
  context,
}: BulletEditorProps) {
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const [suggestions, setSuggestions] = useState<ImproveBulletResponse | null>(null);

  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id,
  });

  const style: React.CSSProperties = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.4 : 1,
  };

  const improveBullet = useImproveBullet();

  // Auto-resize textarea
  useEffect(() => {
    const el = textareaRef.current;
    if (!el) return;
    el.style.height = "auto";
    el.style.height = `${el.scrollHeight}px`;
  }, [value]);

  function handleImprove() {
    if (!value.trim()) {
      toast.error("Write something first before improving it.");
      return;
    }
    setSuggestions(null);
    improveBullet.mutate(
      { text: value.trim(), context },
      {
        onSuccess: (data) => setSuggestions(data),
        onError: () => toast.error("Failed to improve bullet. Please try again."),
      }
    );
  }

  function applyImprovement(text: string) {
    onChange(text);
    setSuggestions(null);
    improveBullet.reset();
  }

  function dismiss() {
    setSuggestions(null);
    improveBullet.reset();
  }

  return (
    <div ref={setNodeRef} style={style} className="space-y-1.5">
      {/* ── Main row ── */}
      <div className="flex items-start gap-1.5">
        {/* Drag handle */}
        <button
          {...attributes}
          {...listeners}
          className="text-muted-foreground hover:text-foreground mt-2 cursor-grab touch-none active:cursor-grabbing"
          tabIndex={-1}
          aria-label="Drag bullet"
        >
          <GripVertical className="h-3.5 w-3.5" />
        </button>

        {/* Bullet dot */}
        <span className="text-muted-foreground mt-2.5 text-xs select-none">•</span>

        {/* Auto-resize textarea */}
        <textarea
          ref={textareaRef}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder={placeholder}
          rows={1}
          className={cn(
            "border-input bg-background placeholder:text-muted-foreground focus-visible:ring-ring min-h-0 flex-1 resize-none rounded border px-2 py-1.5 text-sm leading-snug focus-visible:outline-none focus-visible:ring-1",
            isDragging && "cursor-grabbing"
          )}
        />

        {/* Improve with AI */}
        <Button
          variant="ghost"
          size="icon"
          className={cn(
            "mt-0.5 h-7 w-7 shrink-0",
            suggestions
              ? "text-violet-500"
              : "text-muted-foreground hover:text-violet-500"
          )}
          onClick={handleImprove}
          disabled={improveBullet.isPending}
          aria-label="Improve with AI"
          title="Improve with AI"
        >
          {improveBullet.isPending ? (
            <Loader2 className="h-3.5 w-3.5 animate-spin" />
          ) : (
            <Sparkles className="h-3.5 w-3.5" />
          )}
        </Button>

        {/* Remove */}
        <Button
          variant="ghost"
          size="icon"
          className={cn(
            "mt-0.5 h-7 w-7 shrink-0",
            canRemove
              ? "text-muted-foreground hover:text-destructive"
              : "text-muted-foreground/30 cursor-not-allowed"
          )}
          onClick={onRemove}
          disabled={!canRemove}
          aria-label="Remove bullet"
        >
          <Trash2 className="h-3.5 w-3.5" />
        </Button>
      </div>

      {/* ── AI suggestions panel ── */}
      {suggestions && (
        <div className="ml-6 rounded-md border bg-violet-50/60 p-3 dark:bg-violet-950/20 space-y-2.5">
          {/* Header */}
          <div className="flex items-center justify-between">
            <span className="flex items-center gap-1.5 text-xs font-semibold text-violet-700 dark:text-violet-300">
              <Sparkles className="h-3 w-3" />
              AI Suggestion
            </span>
            <button
              onClick={dismiss}
              className="text-muted-foreground hover:text-foreground text-xs underline underline-offset-2"
            >
              Dismiss
            </button>
          </div>

          {/* Improved version */}
          <SuggestionRow
            label="Improved"
            text={suggestions.improved}
            onApply={() => applyImprovement(suggestions.improved)}
          />

          {/* Alternatives */}
          {suggestions.suggestions.length > 0 && (
            <div className="space-y-1.5">
              <p className="text-muted-foreground text-[11px] font-medium uppercase tracking-wide">
                Alternatives
              </p>
              {suggestions.suggestions.map((s, i) => (
                <SuggestionRow key={i} text={s} onApply={() => applyImprovement(s)} />
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

// ─── Suggestion row ───────────────────────────────────────────────────────────

function SuggestionRow({
  label,
  text,
  onApply,
}: {
  label?: string;
  text: string;
  onApply: () => void;
}) {
  return (
    <div className="flex items-start gap-2 rounded border bg-white px-2.5 py-2 dark:bg-neutral-900">
      <p className="flex-1 text-[13px] leading-snug">
        {label && (
          <span className="mr-1 text-[11px] font-semibold text-violet-600 dark:text-violet-400">
            {label}
          </span>
        )}
        {text}
      </p>
      <Button
        size="sm"
        variant="outline"
        className="h-6 shrink-0 px-2 text-[11px]"
        onClick={onApply}
      >
        Apply
      </Button>
    </div>
  );
}
