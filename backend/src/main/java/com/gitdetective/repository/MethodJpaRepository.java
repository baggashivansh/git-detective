package com.gitdetective.repository;

import com.gitdetective.entity.MethodEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MethodJpaRepository extends JpaRepository<MethodEntity, UUID> {

    List<MethodEntity> findByTypeId(UUID typeId);

    List<MethodEntity> findByTypeIdIn(Collection<UUID> typeIds);

    void deleteByTypeId(UUID typeId);
}
