package com.gitdetective.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "type_interfaces")
@IdClass(TypeInterfaceId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypeInterfaceEntity {

    @Id
    @Column(name = "type_id", nullable = false)
    private UUID typeId;

    @Id
    @Column(name = "interface_name", nullable = false)
    private String interfaceName;
}
