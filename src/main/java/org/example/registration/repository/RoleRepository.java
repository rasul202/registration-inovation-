package org.example.registration.repository;

import org.example.registration.entity.RoleEntity;
import org.example.registration.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.print.DocFlavor;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity , Long> {

    Optional<RoleEntity> findByNameAndStatus(String name , Integer status);

    Optional<RoleEntity> findByName(String name);

}
