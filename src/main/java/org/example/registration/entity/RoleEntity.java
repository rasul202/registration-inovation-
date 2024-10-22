package org.example.registration.entity;

import jakarta.persistence.*;
import liquibase.license.LicenseService;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.registration.enums.RoleStatusEnum;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "roles")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;
    Integer status;

    @ManyToMany(mappedBy = "roles")
    List<UserEntity> users;

    @PrePersist
    public void setDefaults(){
        this.status = RoleStatusEnum.ACTIVE.getStatus();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleEntity that = (RoleEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
