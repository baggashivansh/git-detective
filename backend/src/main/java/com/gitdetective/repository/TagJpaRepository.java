package com.gitdetective.repository;

import com.gitdetective.entity.TagEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagJpaRepository extends JpaRepository<TagEntity, UUID> {

    List<TagEntity> findByRepositoryId(UUID repositoryId);

    Optional<TagEntity> findByRepositoryIdAndName(UUID repositoryId, String name);

    Optional<TagEntity> findByIdAndRepositoryId(UUID id, UUID repositoryId);

    void deleteByRepositoryId(UUID repositoryId);
}
