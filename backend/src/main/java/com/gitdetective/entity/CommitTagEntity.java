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
@Table(name = "commit_tags")
@IdClass(CommitTagId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommitTagEntity {

    @Id
    @Column(name = "commit_id", nullable = false)
    private UUID commitId;

    @Id
    @Column(name = "tag_name", nullable = false)
    private String tagName;
}
