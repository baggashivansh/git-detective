package com.gitdetective.repository;

import com.gitdetective.entity.AnnotationEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnotationJpaRepository extends JpaRepository<AnnotationEntity, UUID> {

    List<AnnotationEntity> findByOwnerKindAndOwnerId(String ownerKind, UUID ownerId);

    void deleteByOwnerKindAndOwnerId(String ownerKind, UUID ownerId);
}
