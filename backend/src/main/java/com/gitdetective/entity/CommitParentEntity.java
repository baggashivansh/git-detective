package com.gitdetective.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "commit_parents")
@IdClass(CommitParentId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommitParentEntity {

    @Id
    @Column(name = "commit_id", nullable = false)
    private UUID commitId;

    @Id
    @Column(name = "parent_sha", nullable = false, length = 64)
    private String parentSha;

    @Column(name = "parent_order", nullable = false)
    private int parentOrder;
}
