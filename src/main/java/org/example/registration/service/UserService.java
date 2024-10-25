package org.example.registration.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.example.registration.entity.UserEntity;
import org.example.registration.enums.UserStatusEnum;
import org.example.registration.exception.*;
import org.example.registration.mapper.UserMapper;
import org.example.registration.model.request.SaveUserToDbRequest;
import org.example.registration.model.request.UpdateUserEntityByEmailRequest;
import org.example.registration.model.response.GetUserByEmailAndPasswordResponse;
import org.example.registration.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class UserService {

    RoleService roleService;
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordService passwordService;

    public UserService(UserRepository userRepository, RoleService roleService, UserMapper userMapper, @Lazy PasswordService passwordService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.userMapper = userMapper;
        this.passwordService = passwordService;
    }

    public void saveUserToDb(SaveUserToDbRequest request ) {

        UserEntity userEntity = userMapper.toUserEntity(request);

        try {
            userEntity.setRoles(roleService.getRoleEntitiesFromDbByNames(request.getRoles()));
        } catch (NotFoundCompileTimeException e) {
            throw new RuntimeException(e.getMessage());
        }

        Optional<UserEntity> optionalUser = userRepository.findByEmail(userEntity.getEmail());
        if(optionalUser.isPresent())
            throw new UserWithExistingEmail(
                    "there is already a user with " + userEntity.getEmail() + " email " ,
                    500 ,
                    LocalDateTime.now()
            );

        userEntity.setPassword(passwordService.encodePassword(userEntity.getPassword()));
        userRepository.save(userEntity);
    }


    public GetUserByEmailAndPasswordResponse getUserByEmailAndPassword(String email , String password) throws NotFoundCompileTimeException, WrongCridentialsException {

        UserEntity userEntity = null;
        try {
            userEntity = fetchUserByEmail(email);
        } catch (RuntimeException e) {
            throw new NotFoundCompileTimeException(
                    e.getMessage(),
                    500,
                    LocalDateTime.now()
            );
        }

        if(verifyUserPassword(userEntity , password)){
            return userMapper.toGetUserByEmailAndPasswordResponse(userEntity);
        }else
            throw new WrongCridentialsException(
                    "wrong email or password",
                    500,
                    LocalDateTime.now()
            );
    }

    public UserEntity fetchUserByEmail(String email)  {
        return userRepository.findByEmailAndStatus(email , UserStatusEnum.ACTIVE.getStatus())
                .orElseThrow(() -> new NotFoundRunTimeException(
                        "there is no user with " + email + " email",
                        404 ,
                        LocalDateTime.now()
                ));
    }

    public boolean verifyUserPassword(UserEntity user , String password){
        return passwordService.isMatches(password , user.getPassword());
    }

    public UserEntity getUserEntityFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object object = authentication.getPrincipal();
        UserDetails userDetails;
        if(object instanceof UserDetails) {
            userDetails = (UserDetails) authentication.getPrincipal();
        }else return null;

        return fetchUserByEmail(userDetails.getUsername());
    }

    public void updateUserEntityByEmail(UpdateUserEntityByEmailRequest request, String email) {

        UserEntity user = fetchUserByEmail(email);
        userMapper.updateUserEntity(user , request );

        userRepository.save(user);
    }

    public void updateUserPasswordByEmail(String newPassword, String repeatPassword ,  String email) throws SamePasswordException, RepeatedPasswordDontMatch {

        if( !newPassword.equals(repeatPassword))
                throw new RepeatedPasswordDontMatch(
                        "new password and repeated password don't match",
                        500,
                        LocalDateTime.now()
                );

        UserEntity user = fetchUserByEmail(email);

        if(passwordService.isMatches(newPassword , user.getPassword())){
            throw new SamePasswordException(
                    "entered new password and old password is same",
                    500,
                    LocalDateTime.now()
            );
        }

        newPassword = passwordService.encodePassword(newPassword);

        updateUserEntityByEmail(
                UpdateUserEntityByEmailRequest.builder().password(newPassword).build(),
                email
        );
    }


    public void setUserToSecurityContext(String email, String password, List<String> roles){
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new) // map role to a SimpleGrantedAuthority object
                .toList();

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(email , password , authorities));
    }


}
