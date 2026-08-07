"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { LayoutDashboard, Search, GitBranch, Settings, Bot } from "lucide-react";
import { cn } from "@/lib/utils";

const navigationItems = [
  { href: "/dashboard", label: "Overview", icon: LayoutDashboard },
  { href: "/investigations", label: "Investigations", icon: Search },
  { href: "/repositories", label: "Repositories", icon: GitBranch },
  { href: "/assistant", label: "Assistant", icon: Bot },
  { href: "/dashboard", label: "Settings", icon: Settings, disabled: true },
] as const;

export function AppSidebar() {
  const pathname = usePathname();

  return (
    <aside className="hidden w-60 shrink-0 border-r border-border/60 bg-sidebar text-sidebar-foreground md:flex md:flex-col">
      <div className="flex h-14 items-center border-b border-sidebar-border px-4">
        <Link href="/" className="flex items-center gap-2 text-sm font-medium">
          <span className="inline-flex size-6 items-center justify-center rounded-md border border-sidebar-border bg-background text-xs">
            GD
          </span>
          Git Detective
        </Link>
      </div>

      <nav className="flex flex-1 flex-col gap-1 p-3">
        {navigationItems.map((item) => {
          const Icon = item.icon;
          const isActive =
            pathname === item.href ||
            (item.label === "Investigations" &&
              pathname.startsWith("/investigations")) ||
            (item.label === "Repositories" &&
              pathname.startsWith("/repositories")) ||
            (item.label === "Assistant" && pathname.startsWith("/assistant"));
          const isDisabled = "disabled" in item && item.disabled;

          return (
            <Link
              key={item.label}
              href={isDisabled ? "#" : item.href}
              aria-disabled={isDisabled}
              tabIndex={isDisabled ? -1 : 0}
              className={cn(
                "flex items-center gap-2 rounded-md px-3 py-2 text-sm transition-colors",
                isActive
                  ? "bg-sidebar-accent text-sidebar-accent-foreground"
                  : "text-muted-foreground hover:bg-sidebar-accent/70 hover:text-foreground",
                isDisabled && "pointer-events-none opacity-40",
              )}
            >
              <Icon className="size-4" />
              {item.label}
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}
