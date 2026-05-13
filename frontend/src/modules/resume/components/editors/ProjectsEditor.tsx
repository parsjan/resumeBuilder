"use client";

import { useState } from "react";

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
import { observer } from "mobx-react-lite";
import { Plus, X } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { useResumeStore } from "@/hooks/useStore";

import { BulletList } from "../BulletList";
import { SectionCard } from "../SectionCard";

export const ProjectsEditor = observer(function ProjectsEditor() {
  const resumeStore = useResumeStore();
  const projects = resumeStore.resume?.sections.projects ?? [];

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 8 } }),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates })
  );

  function handleDragEnd(event: DragEndEvent) {
    const { active, over } = event;
    if (!over || active.id === over.id) return;
    const fromIndex = projects.findIndex((p) => p.id === active.id);
    const toIndex = projects.findIndex((p) => p.id === over.id);
    if (fromIndex !== -1 && toIndex !== -1) {
      resumeStore.reorderItems("projects", fromIndex, toIndex);
    }
  }

  return (
    <div className="space-y-3">
      <DndContext
        sensors={sensors}
        collisionDetection={closestCenter}
        onDragEnd={handleDragEnd}
      >
        <SortableContext
          items={projects.map((p) => p.id)}
          strategy={verticalListSortingStrategy}
        >
          {projects.map((item, index) => (
            <SectionCard
              key={item.id}
              id={item.id}
              title={item.name || "New Project"}
              subtitle={item.technologies.slice(0, 3).join(", ") || undefined}
              onRemove={() => resumeStore.removeItem("projects", item.id)}
            >
              <div className="space-y-3">
                <div className="grid grid-cols-2 gap-3">
                  <div className="space-y-1.5">
                    <Label className="text-xs">Project Name</Label>
                    <Input
                      value={item.name}
                      onChange={(e) =>
                        resumeStore.updateProjectItem(index, { name: e.target.value })
                      }
                      placeholder="My Awesome Project"
                    />
                  </div>
                  <div className="space-y-1.5">
                    <Label className="text-xs">URL (optional)</Label>
                    <Input
                      type="url"
                      value={item.url ?? ""}
                      onChange={(e) =>
                        resumeStore.updateProjectItem(index, {
                          url: e.target.value || undefined,
                        })
                      }
                      placeholder="https://github.com/…"
                    />
                  </div>
                </div>

                <div className="space-y-1.5">
                  <Label className="text-xs">Description</Label>
                  <Textarea
                    value={item.description}
                    onChange={(e) =>
                      resumeStore.updateProjectItem(index, { description: e.target.value })
                    }
                    placeholder="Brief one-sentence overview of the project…"
                    rows={2}
                    className="resize-none"
                  />
                </div>

                <div className="space-y-1.5">
                  <Label className="text-xs">Key Highlights</Label>
                  <BulletList
                    scopeKey={`proj-${item.id}`}
                    sectionType="projects"
                    itemIndex={index}
                    bullets={item.bullets}
                    onAdd={() => resumeStore.addBullet("projects", index)}
                    onRemove={(bi) => resumeStore.removeBullet("projects", index, bi)}
                    onChange={(bi, val) =>
                      resumeStore.updateBullet("projects", index, bi, val)
                    }
                    onReorder={(from, to) =>
                      resumeStore.reorderBullets("projects", index, from, to)
                    }
                    context={item.name || undefined}
                  />
                </div>

                <TechTagsInput
                  technologies={item.technologies}
                  onAdd={(tech) =>
                    resumeStore.updateProjectItem(index, {
                      technologies: [...item.technologies, tech],
                    })
                  }
                  onRemove={(i) =>
                    resumeStore.updateProjectItem(index, {
                      technologies: item.technologies.filter((_, idx) => idx !== i),
                    })
                  }
                />
              </div>
            </SectionCard>
          ))}
        </SortableContext>
      </DndContext>

      <Button
        type="button"
        variant="outline"
        className="w-full"
        onClick={() => resumeStore.addItem("projects")}
      >
        <Plus className="mr-2 h-4 w-4" />
        Add Project
      </Button>
    </div>
  );
});

// ─── Inline tech tags input ───────────────────────────────────────────────────

function TechTagsInput({
  technologies,
  onAdd,
  onRemove,
}: {
  technologies: string[];
  onAdd: (tech: string) => void;
  onRemove: (index: number) => void;
}) {
  const [value, setValue] = useState("");

  function commit(raw: string) {
    raw
      .split(",")
      .map((t) => t.trim())
      .filter(Boolean)
      .forEach(onAdd);
    setValue("");
  }

  function handleKeyDown(e: React.KeyboardEvent<HTMLInputElement>) {
    if (e.key === "Enter" || e.key === ",") {
      e.preventDefault();
      commit(value);
    }
  }

  return (
    <div className="space-y-1.5">
      <Label className="text-xs">Technologies</Label>
      <div className="flex flex-wrap gap-1.5">
        {technologies.map((tech, i) => (
          <span
            key={`${tech}-${i}`}
            className="bg-secondary text-secondary-foreground flex items-center gap-1 rounded-full px-2 py-0.5 text-xs"
          >
            {tech}
            <button type="button" onClick={() => onRemove(i)} aria-label={`Remove ${tech}`}>
              <X className="h-2.5 w-2.5" />
            </button>
          </span>
        ))}
        <Input
          value={value}
          onChange={(e) => setValue(e.target.value)}
          onKeyDown={handleKeyDown}
          onBlur={() => { if (value.trim()) commit(value); }}
          placeholder="React, Node.js…"
          className="h-6 w-32 border-dashed px-2 text-xs"
        />
      </div>
    </div>
  );
}
