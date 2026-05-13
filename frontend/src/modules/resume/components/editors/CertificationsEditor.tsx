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

export const CertificationsEditor = observer(function CertificationsEditor() {
  const resumeStore = useResumeStore();
  const certifications = resumeStore.resume?.sections.certifications ?? [];

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 8 } }),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates })
  );

  function handleDragEnd(event: DragEndEvent) {
    const { active, over } = event;
    if (!over || active.id === over.id) return;
    const fromIndex = certifications.findIndex((c) => c.id === active.id);
    const toIndex = certifications.findIndex((c) => c.id === over.id);
    if (fromIndex !== -1 && toIndex !== -1) {
      resumeStore.reorderItems("certifications", fromIndex, toIndex);
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
          items={certifications.map((c) => c.id)}
          strategy={verticalListSortingStrategy}
        >
          {certifications.map((item, index) => (
            <SectionCard
              key={item.id}
              id={item.id}
              title={item.name || "New Certification"}
              subtitle={item.issuer || undefined}
              onRemove={() => resumeStore.removeItem("certifications", item.id)}
            >
              <div className="grid grid-cols-2 gap-3">
                <div className="col-span-2 space-y-1.5">
                  <Label className="text-xs">Certification Name</Label>
                  <Input
                    value={item.name}
                    onChange={(e) =>
                      resumeStore.updateCertificationItem(index, { name: e.target.value })
                    }
                    placeholder="AWS Certified Solutions Architect"
                  />
                </div>
                <div className="space-y-1.5">
                  <Label className="text-xs">Issuing Organisation</Label>
                  <Input
                    value={item.issuer}
                    onChange={(e) =>
                      resumeStore.updateCertificationItem(index, { issuer: e.target.value })
                    }
                    placeholder="Amazon Web Services"
                  />
                </div>
                <div className="space-y-1.5">
                  <Label className="text-xs">Date Issued</Label>
                  <Input
                    type="month"
                    value={toMonthInputValue(item.date)}
                    onChange={(e) =>
                      resumeStore.updateCertificationItem(index, { date: e.target.value })
                    }
                  />
                </div>
                <div className="col-span-2 space-y-1.5">
                  <Label className="text-xs">Credential URL (optional)</Label>
                  <Input
                    type="url"
                    value={item.url ?? ""}
                    onChange={(e) =>
                      resumeStore.updateCertificationItem(index, {
                        url: e.target.value || undefined,
                      })
                    }
                    placeholder="https://verify.example.com/…"
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
        onClick={() => resumeStore.addItem("certifications")}
      >
        <Plus className="mr-2 h-4 w-4" />
        Add Certification
      </Button>
    </div>
  );
});
