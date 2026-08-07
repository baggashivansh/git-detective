package com.gitdetective.evidence.model;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable, traceable evidence record. Anonymous evidence is forbidden — every item must carry
 * ids, provenance, confidence, and verification status.
 */
public final class EvidenceRecord {

    private final UUID evidenceId;
    private final EvidenceCategory evidenceType;
    private final EvidenceProvenance source;
    private final String sourceIdentifier;
    private final UUID repositoryId;
    private final UUID investigationId;
    private final Instant timestamp;
    private final int confidence;
    private final String description;
    private final Map<String, String> supportingMetadata;
    private final EvidenceVerificationStatus verificationStatus;

    private EvidenceRecord(Builder builder) {
        this.evidenceId = Objects.requireNonNull(builder.evidenceId, "evidenceId");
        this.evidenceType = Objects.requireNonNull(builder.evidenceType, "evidenceType");
        this.source = Objects.requireNonNull(builder.source, "source");
        this.sourceIdentifier =
                Objects.requireNonNull(builder.sourceIdentifier, "sourceIdentifier");
        this.repositoryId = Objects.requireNonNull(builder.repositoryId, "repositoryId");
        this.investigationId = Objects.requireNonNull(builder.investigationId, "investigationId");
        this.timestamp = Objects.requireNonNull(builder.timestamp, "timestamp");
        this.confidence = builder.confidence;
        this.description = Objects.requireNonNull(builder.description, "description");
        this.supportingMetadata =
                Collections.unmodifiableMap(new LinkedHashMap<>(builder.metadata));
        this.verificationStatus =
                Objects.requireNonNull(builder.verificationStatus, "verificationStatus");
    }

    public UUID evidenceId() {
        return evidenceId;
    }

    public EvidenceCategory evidenceType() {
        return evidenceType;
    }

    public EvidenceProvenance source() {
        return source;
    }

    public String sourceIdentifier() {
        return sourceIdentifier;
    }

    public UUID repositoryId() {
        return repositoryId;
    }

    public UUID investigationId() {
        return investigationId;
    }

    public Instant timestamp() {
        return timestamp;
    }

    public int confidence() {
        return confidence;
    }

    public String description() {
        return description;
    }

    public Map<String, String> supportingMetadata() {
        return supportingMetadata;
    }

    public EvidenceVerificationStatus verificationStatus() {
        return verificationStatus;
    }

    /** Stable key used for duplicate elimination. */
    public String dedupeKey() {
        return evidenceType.name()
                + '|'
                + source.name()
                + '|'
                + sourceIdentifier
                + '|'
                + description;
    }

    public EvidenceRecord withVerificationStatus(EvidenceVerificationStatus status) {
        return builder()
                .evidenceId(evidenceId)
                .evidenceType(evidenceType)
                .source(source)
                .sourceIdentifier(sourceIdentifier)
                .repositoryId(repositoryId)
                .investigationId(investigationId)
                .timestamp(timestamp)
                .confidence(confidence)
                .description(description)
                .supportingMetadata(supportingMetadata)
                .verificationStatus(status)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UUID evidenceId = UUID.randomUUID();
        private EvidenceCategory evidenceType;
        private EvidenceProvenance source;
        private String sourceIdentifier;
        private UUID repositoryId;
        private UUID investigationId;
        private Instant timestamp = Instant.now();
        private int confidence = EvidenceConfidenceRules.REPOSITORY_METADATA;
        private String description;
        private final Map<String, String> metadata = new LinkedHashMap<>();
        private EvidenceVerificationStatus verificationStatus = EvidenceVerificationStatus.PENDING;

        public Builder evidenceId(UUID evidenceId) {
            this.evidenceId = evidenceId;
            return this;
        }

        public Builder evidenceType(EvidenceCategory evidenceType) {
            this.evidenceType = evidenceType;
            return this;
        }

        public Builder source(EvidenceProvenance source) {
            this.source = source;
            if (source != null) {
                this.confidence = EvidenceConfidenceRules.forProvenance(source);
            }
            return this;
        }

        public Builder sourceIdentifier(String sourceIdentifier) {
            this.sourceIdentifier = sourceIdentifier;
            return this;
        }

        public Builder repositoryId(UUID repositoryId) {
            this.repositoryId = repositoryId;
            return this;
        }

        public Builder investigationId(UUID investigationId) {
            this.investigationId = investigationId;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder confidence(int confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder supportingMetadata(Map<String, String> supportingMetadata) {
            this.metadata.clear();
            if (supportingMetadata != null) {
                this.metadata.putAll(supportingMetadata);
            }
            return this;
        }

        public Builder meta(String key, String value) {
            if (key != null && value != null) {
                this.metadata.put(key, value);
            }
            return this;
        }

        public Builder verificationStatus(EvidenceVerificationStatus verificationStatus) {
            this.verificationStatus = verificationStatus;
            return this;
        }

        public EvidenceRecord build() {
            return new EvidenceRecord(this);
        }
    }
}
