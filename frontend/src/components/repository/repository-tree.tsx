"use client";

import * as React from "react";
import {
  ChevronDown,
  ChevronRight,
  ChevronsDownUp,
  File,
  FileCode2,
  Folder,
  FolderOpen,
  Search,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { EmptyState } from "@/components/repository/empty-state";
import { TreeSkeleton } from "@/components/repository/repository-skeletons";
import { formatBytes } from "@/features/repository/utils/format";
import type { RepositoryTreeNode } from "@/types/repository";
import { cn } from "@/lib/utils";

function collectDirectoryPaths(nodes: RepositoryTreeNode[]): string[] {
  const paths: string[] = [];

  for (const node of nodes) {
    if (node.directory) {
      paths.push(node.path);
      paths.push(...collectDirectoryPaths(node.children));
    }
  }

  return paths;
}

function filterTree(
  nodes: RepositoryTreeNode[],
  query: string,
): RepositoryTreeNode[] {
  const normalized = query.trim().toLowerCase();
  if (!normalized) return nodes;

  return nodes
    .map((node) => {
      const matches =
        node.name.toLowerCase().includes(normalized) ||
        node.path.toLowerCase().includes(normalized) ||
        node.language?.toLowerCase().includes(normalized);

      if (node.directory) {
        const children = filterTree(node.children, query);
        if (matches || children.length > 0) {
          return { ...node, children };
        }
        return null;
      }

      return matches ? node : null;
    })
    .filter((node): node is RepositoryTreeNode => node !== null);
}

function NodeIcon({ node }: { node: RepositoryTreeNode }) {
  if (node.directory) {
    return <Folder className="size-4 text-amber-500/80" />;
  }

  if (node.language) {
    return <FileCode2 className="size-4 text-sky-500/80" />;
  }

  return <File className="size-4 text-muted-foreground" />;
}

function TreeNodeRow({
  node,
  depth,
  expanded,
  onToggle,
}: {
  node: RepositoryTreeNode;
  depth: number;
  expanded: Set<string>;
  onToggle: (path: string) => void;
}) {
  const isExpanded = expanded.has(node.path);
  const hasChildren = node.directory && node.children.length > 0;

  return (
    <button
      type="button"
      onClick={() => hasChildren && onToggle(node.path)}
      className={cn(
        "flex w-full items-center gap-2 rounded-md px-2 py-1.5 text-left text-sm transition-colors hover:bg-muted/60",
        hasChildren ? "cursor-pointer" : "cursor-default",
      )}
      style={{ paddingLeft: `${depth * 16 + 8}px` }}
    >
      <span className="flex size-4 shrink-0 items-center justify-center">
        {hasChildren ? (
          isExpanded ? (
            <ChevronDown className="size-3.5 text-muted-foreground" />
          ) : (
            <ChevronRight className="size-3.5 text-muted-foreground" />
          )
        ) : null}
      </span>
      <NodeIcon node={node} />
      <span className="min-w-0 flex-1 truncate">{node.name}</span>
      {!node.directory ? (
        <span className="shrink-0 text-xs text-muted-foreground">
          {node.language ?? node.extension ?? formatBytes(node.sizeBytes)}
        </span>
      ) : (
        <span className="shrink-0 text-xs text-muted-foreground">
          {node.children.length} items
        </span>
      )}
    </button>
  );
}

function TreeBranch({
  nodes,
  depth,
  expanded,
  onToggle,
}: {
  nodes: RepositoryTreeNode[];
  depth: number;
  expanded: Set<string>;
  onToggle: (path: string) => void;
}) {
  return (
    <>
      {nodes.map((node) => (
        <React.Fragment key={node.id}>
          <TreeNodeRow
            node={node}
            depth={depth}
            expanded={expanded}
            onToggle={onToggle}
          />
          {node.directory && expanded.has(node.path) ? (
            <TreeBranch
              nodes={node.children}
              depth={depth + 1}
              expanded={expanded}
              onToggle={onToggle}
            />
          ) : null}
        </React.Fragment>
      ))}
    </>
  );
}

interface RepositoryTreeProps {
  tree: RepositoryTreeNode[] | undefined;
  isLoading: boolean;
}

export function RepositoryTree({ tree, isLoading }: RepositoryTreeProps) {
  const [filter, setFilter] = React.useState("");
  const [expanded, setExpanded] = React.useState<Set<string>>(new Set());

  const filteredTree = React.useMemo(
    () => (tree ? filterTree(tree, filter) : []),
    [tree, filter],
  );

  function toggle(path: string) {
    setExpanded((current) => {
      const next = new Set(current);
      if (next.has(path)) {
        next.delete(path);
      } else {
        next.add(path);
      }
      return next;
    });
  }

  function expandAll() {
    if (tree) {
      setExpanded(new Set(collectDirectoryPaths(tree)));
    }
  }

  function collapseAll() {
    setExpanded(new Set());
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex flex-wrap items-center justify-between gap-3">
          <CardTitle className="flex items-center gap-2">
            <FolderOpen className="size-4" />
            Repository tree
          </CardTitle>
          <div className="flex items-center gap-2">
            <Button type="button" variant="outline" size="sm" onClick={expandAll}>
              Expand all
            </Button>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={collapseAll}
            >
              <ChevronsDownUp className="size-3.5" />
              Collapse
            </Button>
          </div>
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="relative">
          <Search className="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            value={filter}
            onChange={(event) => setFilter(event.target.value)}
            placeholder="Filter files and folders…"
            className="pl-9"
          />
        </div>

        {isLoading ? (
          <TreeSkeleton />
        ) : !tree?.length ? (
          <EmptyState
            title="No files indexed"
            description="The repository tree will appear once analysis completes."
          />
        ) : filteredTree.length === 0 ? (
          <EmptyState
            title="No matches"
            description="Try a different search term."
          />
        ) : (
          <ScrollArea className="h-[420px] rounded-lg border border-border/60">
            <div className="p-2">
              <TreeBranch
                nodes={filteredTree}
                depth={0}
                expanded={expanded}
                onToggle={toggle}
              />
            </div>
          </ScrollArea>
        )}
      </CardContent>
    </Card>
  );
}
