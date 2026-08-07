"use client";

import * as React from "react";
import {
  Background,
  BackgroundVariant,
  Controls,
  MiniMap,
  ReactFlow,
  type Edge,
  type Node,
  useEdgesState,
  useNodesState,
} from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { EmptyState } from "@/components/investigation/empty-state";
import { GraphSkeleton } from "@/components/investigation/investigation-skeletons";
import { formatTargetType } from "@/features/investigation/utils/format";
import type { RelationshipItem } from "@/types/investigation";

interface RelationshipsGraphProps {
  relationships: RelationshipItem[] | undefined;
  isLoading: boolean;
}

interface NodeMeta {
  label: string;
  type: string;
}

function buildGraphElements(relationships: RelationshipItem[]): {
  nodes: Node[];
  edges: Edge[];
} {
  const nodeMap = new Map<string, NodeMeta>();

  for (const relationship of relationships) {
    nodeMap.set(relationship.sourceKey, {
      label: relationship.sourceLabel,
      type: relationship.sourceType,
    });
    nodeMap.set(relationship.targetKey, {
      label: relationship.targetLabel,
      type: relationship.targetType,
    });
  }

  const entries = Array.from(nodeMap.entries());
  const columns = Math.max(3, Math.ceil(Math.sqrt(entries.length)));

  const nodes: Node[] = entries.map(([id, meta], index) => ({
    id,
    data: {
      label: (
        <div className="px-1 text-center">
          <p className="text-xs font-medium leading-tight">{meta.label}</p>
          <p className="mt-0.5 text-[10px] text-muted-foreground">
            {meta.type}
          </p>
        </div>
      ),
    },
    position: {
      x: (index % columns) * 240,
      y: Math.floor(index / columns) * 120,
    },
    style: {
      background: "hsl(var(--card))",
      border: "1px solid hsl(var(--border))",
      borderRadius: 8,
      color: "hsl(var(--foreground))",
      fontSize: 12,
      padding: 8,
      width: 180,
    },
  }));

  const edges: Edge[] = relationships.map((relationship) => ({
    id: relationship.id,
    source: relationship.sourceKey,
    target: relationship.targetKey,
    label: formatTargetType(relationship.relationshipType),
    animated: relationship.relationshipType === "CALLS",
    style: { stroke: "hsl(var(--muted-foreground))" },
    labelStyle: {
      fill: "hsl(var(--foreground))",
      fontSize: 10,
    },
  }));

  return { nodes, edges };
}

export function RelationshipsGraph({
  relationships,
  isLoading,
}: RelationshipsGraphProps) {
  const [nodes, setNodes, onNodesChange] = useNodesState<Node>([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState<Edge>([]);

  React.useEffect(() => {
    if (!relationships?.length) {
      setNodes([]);
      setEdges([]);
      return;
    }

    const graph = buildGraphElements(relationships);
    setNodes(graph.nodes);
    setEdges(graph.edges);
  }, [relationships, setEdges, setNodes]);

  return (
    <Card>
      <CardHeader>
        <CardTitle>Relationships</CardTitle>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <GraphSkeleton />
        ) : !relationships?.length ? (
          <EmptyState
            title="No relationships"
            description="Relationship graph data will appear once the investigation completes."
          />
        ) : (
          <div className="h-[480px] overflow-hidden rounded-xl border border-border/60 bg-muted/10">
            <ReactFlow
              nodes={nodes}
              edges={edges}
              onNodesChange={onNodesChange}
              onEdgesChange={onEdgesChange}
              fitView
              fitViewOptions={{ padding: 0.2 }}
              minZoom={0.2}
              maxZoom={1.5}
              proOptions={{ hideAttribution: true }}
            >
              <Background
                variant={BackgroundVariant.Dots}
                gap={16}
                size={1}
                color="hsl(var(--muted-foreground) / 0.3)"
              />
              <Controls
                className="!rounded-lg !border-border/60 !bg-card !shadow-sm [&>button]:!border-border/60 [&>button]:!bg-card [&>button]:!text-foreground [&>button:hover]:!bg-muted"
              />
              <MiniMap
                nodeColor="hsl(var(--primary))"
                maskColor="hsl(var(--background) / 0.7)"
                className="!rounded-lg !border-border/60 !bg-card"
              />
            </ReactFlow>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
