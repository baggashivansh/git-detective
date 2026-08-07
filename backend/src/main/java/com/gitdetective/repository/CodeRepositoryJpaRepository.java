package com.gitdetective.repository;

import com.gitdetective.entity.CodeRepository;
import com.gitdetective.entity.RepositorySourceType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeRepositoryJpaRepository extends JpaRepository<CodeRepository, UUID> {

    Optional<CodeRepository> findBySourceTypeAndSourceUri(
            RepositorySourceType sourceType, String sourceUri);

    List<CodeRepository> findAllByOrderByUpdatedAtDesc();
}
