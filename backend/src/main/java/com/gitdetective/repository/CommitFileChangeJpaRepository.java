package com.gitdetective.repository;

import com.gitdetective.entity.CommitFileChangeEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitFileChangeJpaRepository extends JpaRepository<CommitFileChangeEntity, UUID> {

    List<CommitFileChangeEntity> findByCommitId(UUID commitId);

    List<CommitFileChangeEntity> findByCommitIdIn(Collection<UUID> commitIds);

    void deleteByCommitId(UUID commitId);
}
