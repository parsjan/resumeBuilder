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

import { SectionCard } from "../SectionCard";

export const EducationEditor = observer(function EducationEditor() {
  const resumeStore = useResumeStore();
  const education = resumeStore.resume?.sections.education ?? [];

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 8 } }),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates })
  );

  function handleDragEnd(event: DragEndEvent) {
    const { active, over } = event;
    if (!over || active.id === over.id) return;
    const fromIndex = education.findIndex((e) => e.id === active.id);
    const toIndex = education.findIndex((e) => e.id === over.id);
    if (fromIndex !== -1 && toIndex !== -1) {
      resumeStore.reorderItems("education", fromIndex, toIndex);
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
          items={education.map((e) => e.id)}
          strategy={verticalListSortingStrategy}
        >
          {education.map((item, index) => (
            <SectionCard
              key={item.id}
              id={item.id}
              title={item.institution || "New Education"}
              subtitle={
                item.degree && item.field
                  ? `${item.degree}, ${item.field}`
                  : item.degree || item.field || undefined
              }
              onRemove={() => resumeStore.removeItem("education", item.id)}
            >
              <div className="grid grid-cols-2 gap-3">
                <div className="col-span-2 space-y-1.5">
                  <Label className="text-xs">Institution</Label>
                  <Input
                    value={item.institution}
                    onChange={(e) =>
                      resumeStore.updateEducationItem(index, { institution: e.target.value })
                    }
                    placeholder="MIT"
                  />
                </div>
                <div className="space-y-1.5">
                  <Label className="text-xs">Degree</Label>
                  <Input
                    value={item.degree}
                    onChange={(e) =>
                      resumeStore.updateEducationItem(index, { degree: e.target.value })
                    }
                    placeholder="Bachelor of Science"
                  />
                </div>
                <div className="space-y-1.5">
                  <Label className="text-xs">Field of Study</Label>
                  <Input
                    value={item.field}
                    onChange={(e) =>
                      resumeStore.updateEducationItem(index, { field: e.target.value })
                    }
                    placeholder="Computer Science"
                  />
                </div>
                <div className="space-y-1.5">
                  <Label className="text-xs">Start Date</Label>
                  <Input
                    type="month"
                    value={toMonthInputValue(item.startDate)}
                    onChange={(e) =>
                      resumeStore.updateEducationItem(index, { startDate: e.target.value })
                    }
                  />
                </div>
                <div className="space-y-1.5">
                  <Label className="text-xs">End Date</Label>
                  <Input
                    type="month"
                    value={toMonthInputValue(item.endDate)}
                    onChange={(e) =>
                      resumeStore.updateEducationItem(index, { endDate: e.target.value })
                    }
                  />
                </div>
                <div className="space-y-1.5">
                  <Label className="text-xs">GPA (optional)</Label>
                  <Input
                    value={item.gpa ?? ""}
                    onChange={(e) =>
                      resumeStore.updateEducationItem(index, { gpa: e.target.value || undefined })
                    }
                    placeholder="3.8 / 4.0"
                  />
                </div>
              </div>
            </SectionCard>
          ))}
        </SortableContext>
      </DndContext>

      <Button
        type="button"
        variant="outline"
        className="w-full"
        onClick={() => resumeStore.addItem("education")}
      >
        <Plus className="mr-2 h-4 w-4" />
        Add Education
      </Button>
    </div>
  );
});
