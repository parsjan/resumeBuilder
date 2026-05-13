"use client";

import { observer } from "mobx-react-lite";

import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useResumeStore } from "@/hooks/useStore";
import type { PersonalInfo } from "@/types";

export const PersonalInfoEditor = observer(function PersonalInfoEditor() {
  const resumeStore = useResumeStore();
  const info = resumeStore.resume?.sections.personalInfo;

  if (!info) return null;

  function update(field: keyof PersonalInfo, value: string) {
    resumeStore.updatePersonalInfoField(field, value);
  }

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-2 gap-3">
        <div className="col-span-2 space-y-1.5">
          <Label className="text-xs">Full Name</Label>
          <Input
            value={info.fullName}
            onChange={(e) => update("fullName", e.target.value)}
            placeholder="Jane Smith"
            autoComplete="name"
          />
        </div>

        <div className="space-y-1.5">
          <Label className="text-xs">Email Address</Label>
          <Input
            type="email"
            value={info.email}
            onChange={(e) => update("email", e.target.value)}
            placeholder="jane@example.com"
            autoComplete="email"
          />
        </div>

        <div className="space-y-1.5">
          <Label className="text-xs">Phone</Label>
          <Input
            type="tel"
            value={info.phone ?? ""}
            onChange={(e) => update("phone", e.target.value)}
            placeholder="+1 555 000 0000"
            autoComplete="tel"
          />
        </div>

        <div className="space-y-1.5">
          <Label className="text-xs">Location</Label>
          <Input
            value={info.location ?? ""}
            onChange={(e) => update("location", e.target.value)}
            placeholder="New York, NY"
          />
        </div>

        <div className="space-y-1.5">
          <Label className="text-xs">LinkedIn URL</Label>
          <Input
            type="url"
            value={info.linkedIn ?? ""}
            onChange={(e) => update("linkedIn", e.target.value)}
            placeholder="linkedin.com/in/janesmith"
          />
        </div>

        <div className="col-span-2 space-y-1.5">
          <Label className="text-xs">Website / Portfolio</Label>
          <Input
            type="url"
            value={info.website ?? ""}
            onChange={(e) => update("website", e.target.value)}
            placeholder="https://janesmith.dev"
          />
        </div>
      </div>
    </div>
  );
});
