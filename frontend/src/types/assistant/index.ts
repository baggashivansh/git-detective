export interface EvidenceCitation {
  evidenceId: string;
  evidenceType: string;
  provenance: string;
  sourceIdentifier: string;
  confidence: number;
  description: string;
}

export interface SupportingArtifacts {
  files: string[];
  commits: string[];
  contributors: string[];
  packages: string[];
}

export interface AssistantAnswer {
  messageId: string;
  answer: string;
  evidenceUsed: EvidenceCitation[];
  confidence: number;
  supportingArtifacts: SupportingArtifacts;
  referencedFiles: string[];
  referencedCommits: string[];
  referencedContributors: string[];
  referencedPackages: string[];
  suggestedFollowUpQuestions: string[];
  intent: string;
  insufficientEvidence: boolean;
}

export interface AssistantMessage {
  id: string;
  role: "USER" | "ASSISTANT" | string;
  content: string;
  intent?: string | null;
  confidence?: number | null;
  createdAt: string;
  answer?: AssistantAnswer | null;
}

export interface AssistantConversation {
  id: string;
  repositoryId: string;
  investigationId: string;
  title?: string | null;
  createdAt: string;
  updatedAt: string;
  messages: AssistantMessage[];
  suggestedQuestions: string[];
}

export interface ConversationExport {
  format: string;
  content: string;
}
