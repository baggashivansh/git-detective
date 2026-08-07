package com.gitdetective.repository;

import com.gitdetective.entity.AnalysisSession;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisSessionJpaRepository extends JpaRepository<AnalysisSession, UUID> {

    List<AnalysisSession> findByRepositoryIdOrderByStartedAtDesc(UUID repositoryId);

    void deleteByRepositoryId(UUID repositoryId);
}
