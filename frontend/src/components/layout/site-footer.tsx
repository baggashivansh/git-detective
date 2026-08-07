"use client";

export function SiteFooter() {
  return (
    <footer
      className="w-full border-t border-border/60 py-6"
      role="contentinfo"
      aria-label="Site footer"
    >
      <p className="text-center text-sm text-foreground/75">
        Made with{" "}
        <span aria-hidden="true">❤️</span>
        <span className="sr-only">love</span> by Shivansh Bagga
      </p>
    </footer>
  );
}
