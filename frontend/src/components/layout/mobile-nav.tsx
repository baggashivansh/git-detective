"use client";

import Link from "next/link";
import { Menu } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from "@/components/ui/sheet";

const links = [
  { href: "/dashboard", label: "Overview" },
  { href: "/investigations", label: "Investigations" },
  { href: "/repositories", label: "Repositories" },
  { href: "/assistant", label: "Assistant" },
  { href: "/dashboard", label: "Settings", disabled: true },
] as const;

export function MobileNav() {
  return (
    <Sheet>
      <SheetTrigger
        render={
          <Button
            variant="ghost"
            size="icon"
            className="md:hidden"
            aria-label="Open navigation"
          >
            <Menu className="size-4" />
          </Button>
        }
      />
      <SheetContent side="left" className="w-72">
        <SheetHeader>
          <SheetTitle>Git Detective</SheetTitle>
        </SheetHeader>
        <nav className="mt-6 flex flex-col gap-2 px-2">
          {links.map((link) => (
            <Link
              key={link.label}
              href={"disabled" in link && link.disabled ? "#" : link.href}
              className="rounded-md px-3 py-2 text-sm text-muted-foreground transition-colors hover:bg-accent hover:text-foreground aria-disabled:pointer-events-none aria-disabled:opacity-40"
              aria-disabled={"disabled" in link && link.disabled}
            >
              {link.label}
            </Link>
          ))}
        </nav>
      </SheetContent>
    </Sheet>
  );
}
