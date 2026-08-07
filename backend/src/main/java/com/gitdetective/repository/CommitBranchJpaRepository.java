package com.gitdetective.repository;

import com.gitdetective.entity.CommitBranchEntity;
import com.gitdetective.entity.CommitBranchId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitBranchJpaRepository
        extends JpaRepository<CommitBranchEntity, CommitBranchId> {

    List<CommitBranchEntity> findByCommitId(UUID commitId);

    void deleteByCommitId(UUID commitId);
}
