import Link from "next/link";
import { ThemeToggle } from "@/components/layout/theme-toggle";
import { Button } from "@/components/ui/button";

export function SiteHeader() {
  return (
    <header className="sticky top-0 z-40 border-b border-border/60 bg-background/80 backdrop-blur-md">
      <div className="mx-auto flex h-14 max-w-6xl items-center justify-between px-4 sm:px-6">
        <Link href="/" className="flex items-center gap-2 font-medium tracking-tight">
          <span className="inline-flex size-6 items-center justify-center rounded-md border border-border bg-card text-xs">
            GD
          </span>
          <span>Git Detective</span>
        </Link>

        <nav className="hidden items-center gap-6 text-sm text-muted-foreground md:flex">
          <Link href="/#features" className="transition-colors hover:text-foreground">
            Features
          </Link>
          <Link
            href="/#architecture"
            className="transition-colors hover:text-foreground"
          >
            Architecture
          </Link>
          <Link href="/dashboard" className="transition-colors hover:text-foreground">
            Dashboard
          </Link>
          <Link
            href="/repositories"
            className="transition-colors hover:text-foreground"
          >
            Repositories
          </Link>
        </nav>

        <div className="flex items-center gap-2">
          <ThemeToggle />
          <Button size="sm" render={<Link href="/dashboard" />}>
            Open app
          </Button>
        </div>
      </div>
    </header>
  );
}
