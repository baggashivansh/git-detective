package com.gitdetective.repository;

import com.gitdetective.entity.FieldEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FieldJpaRepository extends JpaRepository<FieldEntity, UUID> {

    List<FieldEntity> findByTypeId(UUID typeId);

    void deleteByTypeId(UUID typeId);
}
