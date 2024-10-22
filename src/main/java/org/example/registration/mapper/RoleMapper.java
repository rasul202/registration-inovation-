package org.example.registration.mapper;

import org.example.registration.entity.UserEntity;
import org.example.registration.model.request.SaveUserToDbRequest;
import org.example.registration.model.response.GetUserByEmailAndPasswordResponse;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring" ,nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RoleMapper {



}
