package org.example.registration.security.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.registration.entity.UserEntity;
import org.example.registration.exception.NotFoundCompileTimeException;
import org.example.registration.security.model.CustomUserDetails;
import org.example.registration.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class CustomUserDetailsService implements UserDetailsService {

    UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email)  {
        UserEntity user = userService.fetchUserByEmail(email);
        return new CustomUserDetails(user);
    }

}
