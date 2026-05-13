"use client";

import { useState } from "react";

import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import { ChevronDown, GripVertical, Trash2 } from "lucide-react";

import { Button } from "@/components/ui/button";
import { cn } from "@/utils/cn";

interface SectionCardProps {
  id: string;
  title: string;
  subtitle?: string;
  defaultExpanded?: boolean;
  onRemove: () => void;
  children: React.ReactNode;
}

export function SectionCard({
  id,
  title,
  subtitle,
  defaultExpanded = true,
  onRemove,
  children,
}: SectionCardProps) {
  const [expanded, setExpanded] = useState(defaultExpanded);

  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id,
  });

  const style: React.CSSProperties = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.45 : 1,
    zIndex: isDragging ? 10 : undefined,
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={cn(
        "bg-card rounded-lg border transition-shadow",
        isDragging && "shadow-lg"
      )}
    >
      {/* ── Header ─────────────────────────────────────────────────────────── */}
      <div className="flex items-center gap-1.5 px-3 py-2.5">
        {/* Drag handle */}
        <button
          {...attributes}
          {...listeners}
          className="text-muted-foreground hover:text-foreground cursor-grab touch-none active:cursor-grabbing"
          aria-label="Drag to reorder"
          tabIndex={-1}
        >
          <GripVertical className="h-4 w-4" />
        </button>

        {/* Collapse toggle */}
        <button
          className="flex min-w-0 flex-1 items-center gap-2 text-left"
          onClick={() => setExpanded((v) => !v)}
        >
          <div className="min-w-0 flex-1">
            <p className="truncate text-sm font-medium">{title || "Untitled"}</p>
            {subtitle && (
              <p className="text-muted-foreground truncate text-xs">{subtitle}</p>
            )}
          </div>
          <ChevronDown
            className={cn(
              "text-muted-foreground h-4 w-4 shrink-0 transition-transform duration-150",
              expanded && "rotate-180"
            )}
          />
        </button>

        {/* Delete */}
        <Button
          variant="ghost"
          size="icon"
          className="text-muted-foreground hover:text-destructive h-7 w-7 shrink-0"
          onClick={onRemove}
          aria-label="Remove item"
        >
          <Trash2 className="h-3.5 w-3.5" />
        </Button>
      </div>

      {/* ── Body ───────────────────────────────────────────────────────────── */}
      {expanded && (
        <div className="border-t px-3 pb-3 pt-3">{children}</div>
      )}
    </div>
  );
}
