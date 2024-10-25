package org.example.registration.service;

import io.jsonwebtoken.RequiredTypeException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.registration.entity.RoleEntity;
import org.example.registration.enums.RoleStatusEnum;
import org.example.registration.exception.NotFoundCompileTimeException;
import org.example.registration.mapper.RoleMapper;
import org.example.registration.model.request.RoleRequest;
import org.example.registration.model.request.SaveRoleToDbRequest;
import org.example.registration.repository.RoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class RoleService {

    RoleRepository roleRepository;
    RoleMapper roleMapper;

    public void saveRoleToDb(SaveRoleToDbRequest request){

        Optional<RoleEntity> optionalRole = roleRepository.findByName(request.getName());
        if (optionalRole.isPresent()){
            RoleEntity roleEntity = optionalRole.get();
            if (roleEntity.getStatus() == RoleStatusEnum.DE_ACTIVE.getStatus()){
                roleEntity.setStatus(RoleStatusEnum.ACTIVE.getStatus());
                roleRepository.save(roleEntity);
            }else throw new RequiredTypeException("there is already a role with " + request.getName() + " name");
        }

    }

    public RoleEntity getRoleByName(String name) throws NotFoundCompileTimeException {
        return fetchRoleByName(name , RoleStatusEnum.ACTIVE.getStatus()).
        orElseThrow(() -> new NotFoundCompileTimeException(
                "there is no Role with" + name + " name",
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now()
        ));
    }

    //this method will receive List that contains RoleDto and convert them to RoleEntities (for getting entities in Detached State)
    public List<RoleEntity> getRoleEntitiesFromDbByNames(List<RoleRequest> requestRoles) throws NotFoundCompileTimeException {
        ArrayList<RoleEntity> roles = new ArrayList<>();
        for (RoleRequest roleRequest :  requestRoles){
            roles.add(getRoleByName(roleRequest.getName()));
        }
        return roles;
    }

    public Optional<RoleEntity> fetchRoleByName(String name , Integer status ){
        return roleRepository.findByNameAndStatus(name , status);
    }


}
