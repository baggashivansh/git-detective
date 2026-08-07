import Link from "next/link";
import { Button } from "@/components/ui/button";

export function CtaSection() {
  return (
    <section className="mx-auto max-w-6xl px-4 pb-20 sm:px-6">
      <div className="rounded-2xl border border-border/70 bg-gradient-to-b from-card/80 to-background px-6 py-12 text-center sm:px-12">
        <h2 className="text-2xl font-semibold tracking-tight sm:text-3xl">
          Ready when the foundation is solid
        </h2>
        <p className="mx-auto mt-3 max-w-xl text-muted-foreground">
          Explore the empty workspace shell. Repository analysis and AI-assisted
          investigation arrive in later phases.
        </p>
        <div className="mt-8">
          <Button size="lg" render={<Link href="/dashboard" />}>
            Open dashboard
          </Button>
        </div>
      </div>
    </section>
  );
}
