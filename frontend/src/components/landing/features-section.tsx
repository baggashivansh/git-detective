import { Card, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";

const features = [
  {
    title: "Evidence timeline",
    description:
      "Trace commits, pull requests, and ownership signals as a coherent investigation trail.",
  },
  {
    title: "Impact awareness",
    description:
      "Surface blast radius before changes land — files, services, and dependent flows.",
  },
  {
    title: "Architecture memory",
    description:
      "Retain how systems evolved so institutional knowledge survives team turnover.",
  },
] as const;

export function FeaturesSection() {
  return (
    <section id="features" className="mx-auto max-w-6xl px-4 py-16 sm:px-6">
      <div className="mb-10 max-w-2xl">
        <h2 className="text-2xl font-semibold tracking-tight sm:text-3xl">
          Built for investigation, not summaries
        </h2>
        <p className="mt-3 text-muted-foreground">
          Analyze repositories, run deterministic investigations, and ask an
          evidence-backed assistant — without invented answers.
        </p>
      </div>
      <div className="grid gap-4 md:grid-cols-3">
        {features.map((feature) => (
          <Card key={feature.title} className="border-border/70 bg-card/60">
            <CardHeader>
              <CardTitle className="text-base">{feature.title}</CardTitle>
              <CardDescription className="leading-relaxed">
                {feature.description}
              </CardDescription>
            </CardHeader>
          </Card>
        ))}
      </div>
    </section>
  );
}
