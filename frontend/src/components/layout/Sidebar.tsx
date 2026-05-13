"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { observer } from "mobx-react-lite";
import {
  LayoutDashboard,
  LogOut,
  ChevronLeft,
} from "lucide-react";

import { cn } from "@/utils/cn";
import { useAuthStore, useUiStore } from "@/hooks/useStore";
import { authService } from "@/services/authService";
import { Button } from "@/components/ui/button";
import { useRouter } from "next/navigation";
// ─── Nav items ────────────────────────────────────────────────────────────────

const NAV_ITEMS = [
  { label: "Dashboard", href: "/dashboard", icon: LayoutDashboard },
] as const;

// ─── Component ────────────────────────────────────────────────────────────────

export const Sidebar = observer(function Sidebar() {
  const pathname = usePathname();
  const authStore = useAuthStore();
  const uiStore = useUiStore();
  const router=useRouter();

  async function handleLogout() {
    router.push("/login")
    try {
      await authService.logout();
    } finally {
      authStore.logout();
    }
  }

  return (
    <aside
      className={cn(
        "bg-card border-border flex h-full flex-col border-r transition-all duration-200",
        uiStore.sidebarOpen ? "w-56" : "w-14"
      )}
    >
      {/* Logo + collapse toggle */}
      <div className="border-border flex h-14 items-center justify-between border-b px-3">
        {uiStore.sidebarOpen && (
          <span className="text-sm font-semibold tracking-tight">AI Resume</span>
        )}
        <Button
          variant="ghost"
          size="icon"
          className="ml-auto"
          onClick={() => uiStore.toggleSidebar()}
          aria-label={uiStore.sidebarOpen ? "Collapse sidebar" : "Expand sidebar"}
        >
          <ChevronLeft
            className={cn("h-4 w-4 transition-transform", !uiStore.sidebarOpen && "rotate-180")}
          />
        </Button>
      </div>

      {/* Navigation */}
      <nav className="flex flex-1 flex-col gap-1 overflow-y-auto p-2">
        {NAV_ITEMS.map(({ label, href, icon: Icon }) => {
          const isActive = pathname === href || pathname.startsWith(`${href}/`);
          return (
            <Link
              key={href}
              href={href}
              className={cn(
                "flex items-center gap-3 rounded-md px-2 py-2 text-sm font-medium transition-colors",
                isActive
                  ? "bg-primary text-primary-foreground"
                  : "text-muted-foreground hover:bg-accent hover:text-accent-foreground"
              )}
            >
              <Icon className="h-4 w-4 shrink-0" />
              {uiStore.sidebarOpen && <span>{label}</span>}
            </Link>
          );
        })}
      </nav>

      {/* User + logout */}
      <div className="border-border border-t p-2">
        <button
          onClick={handleLogout}
          className={cn(
            "text-muted-foreground hover:bg-accent hover:text-accent-foreground flex w-full items-center gap-3 rounded-md px-2 py-2 text-sm font-medium transition-colors"
          )}
        >
          <LogOut className="h-4 w-4 shrink-0" />
          {uiStore.sidebarOpen && <span>Logout</span>}
        </button>

        {uiStore.sidebarOpen && authStore.user && (
          <div className="text-muted-foreground mt-2 truncate px-2 text-xs">
            {authStore.user.email}
          </div>
        )}
      </div>
    </aside>
  );
});
