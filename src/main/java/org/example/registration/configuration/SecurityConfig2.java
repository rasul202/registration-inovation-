package org.example.registration.configuration;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.registration.security.filter.CustomRegistrationFilter;
import org.example.registration.security.filter.JWTAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class SecurityConfig2 {

    CustomRegistrationFilter customRegistrationFilter;
    JWTAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .csrf().disable()
                .addFilterBefore(customRegistrationFilter , UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter , CustomRegistrationFilter.class)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().authorizeHttpRequests()
                .requestMatchers( "/registration/**" , "/login/**").permitAll()
                .requestMatchers("/main" , "/change-password").authenticated()
                .requestMatchers("/user/**").hasAuthority("USER")
                .requestMatchers("/admin/**").hasAuthority("ADMIN")
                .and().formLogin().disable()  // Disable default form login
                .httpBasic().disable()
                .build();
    }

}
