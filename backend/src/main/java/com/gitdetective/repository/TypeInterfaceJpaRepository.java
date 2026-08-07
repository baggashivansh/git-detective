package com.gitdetective.repository;

import com.gitdetective.entity.TypeInterfaceEntity;
import com.gitdetective.entity.TypeInterfaceId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TypeInterfaceJpaRepository
        extends JpaRepository<TypeInterfaceEntity, TypeInterfaceId> {

    List<TypeInterfaceEntity> findByTypeId(UUID typeId);

    void deleteByTypeId(UUID typeId);
}
