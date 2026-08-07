"use client";

import Link from "next/link";
import { motion } from "framer-motion";
import { Button } from "@/components/ui/button";
import { usePrefersReducedMotion } from "@/lib/use-prefers-reduced-motion";

export function HeroSection() {
  const reduceMotion = usePrefersReducedMotion();
  const initial = reduceMotion ? false : { opacity: 0, y: 12 };

  return (
    <section className="relative overflow-hidden" aria-labelledby="hero-heading">
      <div className="site-grid absolute inset-0" aria-hidden="true" />
      <div className="relative mx-auto flex max-w-6xl flex-col items-start px-4 pb-20 pt-20 sm:px-6 sm:pt-28">
        <motion.p
          initial={initial}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: reduceMotion ? 0 : 0.35 }}
          className="mb-4 text-sm text-muted-foreground"
        >
          Software investigation platform
        </motion.p>
        <motion.h1
          id="hero-heading"
          initial={initial}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: reduceMotion ? 0 : 0.4, delay: reduceMotion ? 0 : 0.05 }}
          className="max-w-3xl text-4xl font-semibold tracking-tight text-foreground sm:text-6xl"
        >
          Git Detective
        </motion.h1>
        <motion.p
          initial={initial}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: reduceMotion ? 0 : 0.4, delay: reduceMotion ? 0 : 0.12 }}
          className="mt-5 max-w-2xl text-base leading-relaxed text-muted-foreground sm:text-lg"
        >
          Understand why code exists, who changed it, and what breaks next —
          with evidence-backed investigations built for engineering teams.
        </motion.p>
        <motion.div
          initial={initial}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: reduceMotion ? 0 : 0.4, delay: reduceMotion ? 0 : 0.18 }}
          className="mt-8 flex flex-wrap items-center gap-3"
        >
          <Button size="lg" render={<Link href="/dashboard" />}>
            Enter workspace
          </Button>
          <Button
            variant="outline"
            size="lg"
            render={<Link href="/#architecture" />}
          >
            View architecture
          </Button>
        </motion.div>
      </div>
    </section>
  );
}
