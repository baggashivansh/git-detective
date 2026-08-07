export function ArchitecturePreview() {
  return (
    <section id="architecture" className="mx-auto max-w-6xl px-4 py-16 sm:px-6">
      <div className="mb-10 max-w-2xl">
        <h2 className="text-2xl font-semibold tracking-tight sm:text-3xl">
          Architecture preview
        </h2>
        <p className="mt-3 text-muted-foreground">
          A production monorepo with a clean Spring Boot backend and a Next.js
          investigation workspace.
        </p>
      </div>

      <div className="overflow-hidden rounded-xl border border-border/70 bg-card/40">
        <div className="grid divide-y divide-border/60 md:grid-cols-3 md:divide-x md:divide-y-0">
          {[
            {
              layer: "Presentation",
              detail: "Next.js App Router, design system, workspace shell",
            },
            {
              layer: "Application",
              detail: "Spring Boot APIs, security filter chain, validation",
            },
            {
              layer: "Infrastructure",
              detail: "PostgreSQL, Docker Compose, Zerops-ready deploy",
            },
          ].map((item) => (
            <div key={item.layer} className="space-y-2 p-6">
              <p className="text-xs uppercase tracking-[0.16em] text-muted-foreground">
                {item.layer}
              </p>
              <p className="text-sm leading-relaxed text-foreground/90">
                {item.detail}
              </p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
