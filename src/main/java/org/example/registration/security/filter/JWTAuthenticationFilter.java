package org.example.registration.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.registration.service.TokenService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    UserDetailsService userDetailsService;
    TokenService tokenService;

    // 1. add Authorization header to the request
    // 2. get this Authorization header
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String jwtToken = null;
        HttpSession session = request.getSession(false); // Get the current session, if it exists
        if (session != null) {
            jwtToken = (String) session.getAttribute("jwtToken"); // Retrieve the token from the session
        }

        if(jwtToken != null) { //this condition is crucial ,bec if we don't enter bearer token this filter must not work in this request
            String userNameFromToken = tokenService.extractUserName(jwtToken);

            if (SecurityContextHolder.getContext().getAuthentication() == null && userNameFromToken != null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(userNameFromToken);

                if (tokenService.validateToken(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }else
                    throw new RuntimeException("token is not valid");
            }

        }else log.info("this is not bearer token based request");


        filterChain.doFilter(request , response);
    }



    }


