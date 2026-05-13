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
import { observer } from "mobx-react-lite";
import { Plus } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useResumeStore } from "@/hooks/useStore";
import { toMonthInputValue } from "@/utils/formatDate";

import { BulletList } from "../BulletList";
import { SectionCard } from "../SectionCard";

export const ExperienceEditor = observer(function ExperienceEditor() {
  const resumeStore = useResumeStore();
  const experience = resumeStore.resume?.sections.experience ?? [];

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 8 } }),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates })
  );

  function handleDragEnd(event: DragEndEvent) {
    const { active, over } = event;
    if (!over || active.id === over.id) return;
    const fromIndex = experience.findIndex((e) => e.id === active.id);
    const toIndex = experience.findIndex((e) => e.id === over.id);
    if (fromIndex !== -1 && toIndex !== -1) {
      resumeStore.reorderItems("experience", fromIndex, toIndex);
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
          items={experience.map((e) => e.id)}
          strategy={verticalListSortingStrategy}
        >
          {experience.map((item, index) => (
            <SectionCard
              key={item.id}
              id={item.id}
              title={item.company || item.role || "New Position"}
              subtitle={item.role || undefined}
              onRemove={() => resumeStore.removeItem("experience", item.id)}
            >
              {/* ── Fields ── */}
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1.5">
                  <Label className="text-xs">Company</Label>
                  <Input
                    value={item.company}
                    onChange={(e) =>
                      resumeStore.updateExperienceItem(index, { company: e.target.value })
                    }
                    placeholder="Acme Corp"
                  />
                </div>
                <div className="space-y-1.5">
                  <Label className="text-xs">Job Title</Label>
                  <Input
                    value={item.role}
                    onChange={(e) =>
                      resumeStore.updateExperienceItem(index, { role: e.target.value })
                    }
                    placeholder="Software Engineer"
                  />
                </div>
                <div className="space-y-1.5">
                  <Label className="text-xs">Start Date</Label>
                  <Input
                    type="month"
                    value={toMonthInputValue(item.startDate)}
                    onChange={(e) =>
                      resumeStore.updateExperienceItem(index, { startDate: e.target.value })
                    }
                  />
                </div>
                <div className="space-y-1.5">
                  <Label className="text-xs">End Date</Label>
                  <Input
                    type="month"
                    value={item.current ? "" : toMonthInputValue(item.endDate)}
                    disabled={item.current}
                    onChange={(e) =>
                      resumeStore.updateExperienceItem(index, { endDate: e.target.value })
                    }
                  />
                  <label className="text-muted-foreground flex cursor-pointer items-center gap-1.5 text-xs">
                    <input
                      type="checkbox"
                      checked={item.current}
                      onChange={(e) =>
                        resumeStore.updateExperienceItem(index, { current: e.target.checked })
                      }
                    />
                    Currently working here
                  </label>
                </div>
              </div>

              {/* ── Bullets ── */}
              <div className="mt-3 space-y-1.5">
                <Label className="text-xs">Key Achievements</Label>
                <BulletList
                  scopeKey={`exp-${item.id}`}
                  sectionType="experience"
                  itemIndex={index}
                  bullets={item.bullets}
                  onAdd={() => resumeStore.addBullet("experience", index)}
                  onRemove={(bi) => resumeStore.removeBullet("experience", index, bi)}
                  onChange={(bi, val) => resumeStore.updateBullet("experience", index, bi, val)}
                  onReorder={(from, to) => resumeStore.reorderBullets("experience", index, from, to)}
                  context={[item.role, item.company].filter(Boolean).join(" at ") || undefined}
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
        onClick={() => resumeStore.addItem("experience")}
      >
        <Plus className="mr-2 h-4 w-4" />
        Add Experience
      </Button>
    </div>
  );
});
