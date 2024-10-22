package org.example.registration.mapper;

import org.example.registration.entity.UserEntity;
import org.example.registration.model.request.SaveUserToDbRequest;
import org.example.registration.model.request.UpdateUserEntityByEmailRequest;
import org.example.registration.model.response.GetUserByEmailAndPasswordResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring" ,nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    GetUserByEmailAndPasswordResponse toGetUserByEmailAndPasswordResponse(UserEntity userEntity);

    UserEntity toUserEntity(SaveUserToDbRequest request);

    void updateUserEntity(@MappingTarget UserEntity user, UpdateUserEntityByEmailRequest request);
}
