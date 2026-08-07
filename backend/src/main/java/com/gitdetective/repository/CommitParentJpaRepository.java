package com.gitdetective.repository;

import com.gitdetective.entity.CommitParentEntity;
import com.gitdetective.entity.CommitParentId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitParentJpaRepository
        extends JpaRepository<CommitParentEntity, CommitParentId> {

    List<CommitParentEntity> findByCommitId(UUID commitId);

    void deleteByCommitId(UUID commitId);
}
