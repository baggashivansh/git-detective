import Link from "next/link";
import { Button } from "@/components/ui/button";

export function CtaSection() {
  return (
    <section className="mx-auto max-w-6xl px-4 pb-20 sm:px-6">
      <div className="rounded-2xl border border-border/70 bg-gradient-to-b from-card/80 to-background px-6 py-12 text-center sm:px-12">
        <h2 className="text-2xl font-semibold tracking-tight sm:text-3xl">
          Ask a question. Get evidence.
        </h2>
        <p className="mx-auto mt-3 max-w-xl text-muted-foreground">
          Point Git Detective at a public GitHub repository and an investigation
          question. The report stays inside the Evidence Engine.
        </p>
        <div className="mt-8">
          <Button size="lg" render={<Link href="/investigate" />}>
            Start an investigation
          </Button>
        </div>
      </div>
    </section>
  );
}
