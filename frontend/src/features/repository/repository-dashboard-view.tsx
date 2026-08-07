"use client";

import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { AnalysisProgress } from "@/components/repository/analysis-progress";
import { ClassesList } from "@/components/repository/classes-list";
import { CommitsTimeline } from "@/components/repository/commits-timeline";
import { ContributorsTable } from "@/components/repository/contributors-table";
import { EmptyState } from "@/components/repository/empty-state";
import { LanguagesDistribution } from "@/components/repository/languages-distribution";
import { PackagesList } from "@/components/repository/packages-list";
import { RepositoryDashboardSkeleton } from "@/components/repository/repository-skeletons";
import { RepositoryOverview } from "@/components/repository/repository-overview";
import { RepositorySearchPanel } from "@/components/repository/repository-search-panel";
import { RepositoryTree } from "@/components/repository/repository-tree";
import { StatisticsCards } from "@/components/repository/statistics-cards";
import { StatusBadge } from "@/components/repository/status-badge";
import { isAnalysisComplete } from "@/features/repository/constants";
import {
  useRepositoryClasses,
  useRepositoryCommits,
  useRepositoryContributors,
  useRepositoryLanguages,
  useRepositoryPackages,
  useRepositoryStatistics,
  useRepositoryTree,
} from "@/features/repository/hooks/use-repository-dashboard";
import { useRepository } from "@/features/repository/hooks/use-repository";

interface RepositoryDashboardViewProps {
  repositoryId: string;
}

export function RepositoryDashboardView({
  repositoryId,
}: RepositoryDashboardViewProps) {
  const { data: repository, isLoading, isError, error } =
    useRepository(repositoryId);

  const status = repository?.status;
  const analysisComplete = status ? isAnalysisComplete(status) : false;

  const tree = useRepositoryTree(repositoryId, status);
  const contributors = useRepositoryContributors(repositoryId, status);
  const languages = useRepositoryLanguages(repositoryId, status);
  const commits = useRepositoryCommits(repositoryId, status);
  const statistics = useRepositoryStatistics(repositoryId, status);
  const packages = useRepositoryPackages(repositoryId, status);
  const classes = useRepositoryClasses(repositoryId, status);

  if (isLoading) {
    return <RepositoryDashboardSkeleton />;
  }

  if (isError || !repository) {
    return (
      <EmptyState
        title="Repository not found"
        description={
          error instanceof Error
            ? error.message
            : "Unable to load this repository."
        }
      />
    );
  }

  return (
    <div className="mx-auto flex max-w-6xl flex-col gap-6">
      <div className="flex flex-col gap-4">
        <Button
          variant="ghost"
          size="sm"
          className="w-fit px-0 text-muted-foreground hover:text-foreground"
          render={<Link href="/repositories" />}
        >
          <ArrowLeft className="size-4" />
          Back to repositories
        </Button>

        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <div className="flex flex-wrap items-center gap-2">
              <h2 className="text-2xl font-semibold tracking-tight">
                {repository.name}
              </h2>
              <StatusBadge status={repository.status} />
            </div>
            <p className="mt-1 break-all text-sm text-muted-foreground">
              {repository.sourceUri}
            </p>
          </div>
        </div>
      </div>

      <AnalysisProgress repository={repository} />

      {!analysisComplete ? (
        <EmptyState
          title="Analysis in progress"
          description="Detailed sections will unlock once repository analysis completes."
        />
      ) : null}

      <Tabs defaultValue="overview" className="gap-4">
        <TabsList variant="line" className="w-full justify-start overflow-x-auto">
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="tree">Tree</TabsTrigger>
          <TabsTrigger value="contributors">Contributors</TabsTrigger>
          <TabsTrigger value="languages">Languages</TabsTrigger>
          <TabsTrigger value="commits">Commits</TabsTrigger>
          <TabsTrigger value="packages">Packages</TabsTrigger>
          <TabsTrigger value="classes">Classes</TabsTrigger>
          <TabsTrigger value="statistics">Statistics</TabsTrigger>
          <TabsTrigger value="search">Search</TabsTrigger>
        </TabsList>

        <TabsContent value="overview">
          <RepositoryOverview repository={repository} />
        </TabsContent>

        <TabsContent value="tree">
          <RepositoryTree tree={tree.data} isLoading={tree.isLoading} />
        </TabsContent>

        <TabsContent value="contributors">
          <ContributorsTable
            contributors={contributors.data}
            isLoading={contributors.isLoading}
          />
        </TabsContent>

        <TabsContent value="languages">
          <LanguagesDistribution
            languages={languages.data}
            isLoading={languages.isLoading}
          />
        </TabsContent>

        <TabsContent value="commits">
          <CommitsTimeline
            commits={commits.data}
            isLoading={commits.isLoading}
          />
        </TabsContent>

        <TabsContent value="packages">
          <PackagesList
            packages={packages.data}
            isLoading={packages.isLoading}
          />
        </TabsContent>

        <TabsContent value="classes">
          <ClassesList classes={classes.data} isLoading={classes.isLoading} />
        </TabsContent>

        <TabsContent value="statistics">
          <StatisticsCards
            statistics={statistics.data}
            isLoading={statistics.isLoading}
          />
        </TabsContent>

        <TabsContent value="search">
          <RepositorySearchPanel
            repositoryId={repositoryId}
            status={status}
          />
        </TabsContent>
      </Tabs>
    </div>
  );
}
