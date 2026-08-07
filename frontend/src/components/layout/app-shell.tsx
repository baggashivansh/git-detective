import { AppSidebar } from "@/components/layout/app-sidebar";
import { MobileNav } from "@/components/layout/mobile-nav";
import { SiteFooter } from "@/components/layout/site-footer";
import { ThemeToggle } from "@/components/layout/theme-toggle";

export function AppShell({
  children,
  title,
}: {
  children: React.ReactNode;
  title: string;
}) {
  return (
    <div className="flex min-h-screen bg-background">
      <a
        href="#main-content"
        className="sr-only focus:not-sr-only focus:absolute focus:left-4 focus:top-4 focus:z-50 focus:rounded-md focus:bg-background focus:px-3 focus:py-2 focus:text-sm focus:shadow-md focus:ring-2 focus:ring-ring"
      >
        Skip to main content
      </a>
      <AppSidebar />
      <div className="flex min-h-screen flex-1 flex-col">
        <header className="flex h-14 items-center justify-between border-b border-border/60 px-4 sm:px-6">
          <div className="flex items-center gap-2">
            <MobileNav />
            <h1 className="text-sm font-medium tracking-tight">{title}</h1>
          </div>
          <ThemeToggle />
        </header>
        <main id="main-content" tabIndex={-1} className="flex-1 px-4 py-6 sm:px-6 outline-none">
          {children}
        </main>
        <SiteFooter />
      </div>
    </div>
  );
}
