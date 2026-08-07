package com.gitdetective.repository;

import com.gitdetective.entity.CommitTagEntity;
import com.gitdetective.entity.CommitTagId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitTagJpaRepository extends JpaRepository<CommitTagEntity, CommitTagId> {

    List<CommitTagEntity> findByCommitId(UUID commitId);

    void deleteByCommitId(UUID commitId);
}
