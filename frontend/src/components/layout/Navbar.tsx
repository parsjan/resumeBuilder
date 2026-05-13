"use client";

import { observer } from "mobx-react-lite";
import { Bell } from "lucide-react";

import { useAuthStore } from "@/hooks/useStore";
import { Button } from "@/components/ui/button";
import { cn } from "@/utils/cn";

interface NavbarProps {
  title?: string;
  className?: string;
}

export const Navbar = observer(function Navbar({ title, className }: NavbarProps) {
  const authStore = useAuthStore();

  return (
    <header
      className={cn(
        "bg-card border-border flex h-14 items-center justify-between border-b px-4",
        className
      )}
    >
      {/* Page title (injected by child layouts) */}
      <div className="text-sm font-medium">{title}</div>

      {/* Right actions */}
      <div className="flex items-center gap-2">
        <Button variant="ghost" size="icon" aria-label="Notifications">
          <Bell className="h-4 w-4" />
        </Button>

        {/* Avatar / initials */}
        <div
          className="bg-primary text-primary-foreground flex h-8 w-8 items-center justify-center rounded-full text-xs font-semibold"
          title={authStore.displayName}
        >
          {authStore.initials}
        </div>
      </div>
    </header>
  );
});
