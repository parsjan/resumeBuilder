"use client";

import {
  DndContext,
  KeyboardSensor,
  PointerSensor,
  closestCenter,
  useSensor,
  useSensors,
  type DragEndEvent,
} from "@dnd-kit/core";
import {
  SortableContext,
  sortableKeyboardCoordinates,
  verticalListSortingStrategy,
} from "@dnd-kit/sortable";
import { Plus } from "lucide-react";

import { Button } from "@/components/ui/button";

import { BulletEditor } from "./BulletEditor";

type BulletSection = "experience" | "projects";

interface BulletListProps {
  /** Used to generate stable dnd IDs scoped per section+item. */
  scopeKey: string;
  sectionType: BulletSection;
  itemIndex: number;
  bullets: string[];
  onAdd: () => void;
  onRemove: (bulletIndex: number) => void;
  onChange: (bulletIndex: number, value: string) => void;
  onReorder: (fromIndex: number, toIndex: number) => void;
  /** Sent to AI for context, e.g. "Software Engineer at Google" */
  context?: string;
}

export function BulletList({
  scopeKey,
  bullets,
  onAdd,
  onRemove,
  onChange,
  onReorder,
  context,
}: BulletListProps) {
  // Stable IDs scoped per item to avoid cross-list drag collisions
  const ids = bullets.map((_, i) => `${scopeKey}-b${i}`);

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } }),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates })
  );

  function handleDragEnd(event: DragEndEvent) {
    const { active, over } = event;
    if (!over || active.id === over.id) return;
    const fromIndex = ids.indexOf(active.id as string);
    const toIndex = ids.indexOf(over.id as string);
    if (fromIndex !== -1 && toIndex !== -1) {
      onReorder(fromIndex, toIndex);
    }
  }

  return (
    <div className="space-y-1.5">
      <DndContext
        sensors={sensors}
        collisionDetection={closestCenter}
        onDragEnd={handleDragEnd}
      >
        <SortableContext items={ids} strategy={verticalListSortingStrategy}>
          {bullets.map((bullet, bulletIndex) => (
            <BulletEditor
              key={ids[bulletIndex]}
              id={ids[bulletIndex]}
              value={bullet}
              onChange={(val) => onChange(bulletIndex, val)}
              onRemove={() => onRemove(bulletIndex)}
              canRemove={bullets.length > 1}
              context={context}
            />
          ))}
        </SortableContext>
      </DndContext>

      <Button
        type="button"
        variant="ghost"
        size="sm"
        onClick={onAdd}
        className="text-muted-foreground hover:text-foreground w-full border border-dashed"
      >
        <Plus className="mr-1.5 h-3.5 w-3.5" />
        Add bullet
      </Button>
    </div>
  );
}
