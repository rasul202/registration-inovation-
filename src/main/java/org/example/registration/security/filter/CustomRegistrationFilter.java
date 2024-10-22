package org.example.registration.security.filter;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class CustomRegistrationFilter extends OncePerRequestFilter {

    //1. if our request uri is not registration or login request , it will redirect our request to /registration
    //2. also , as all filters it will check is there authentication in context ( previous JwtAuthenticationFilter can add authentication )
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(uri.equals("/registration") || uri.equals("/registration/save") || uri.equals("/login") || uri.equals("/login/find")){
            filterChain.doFilter(request , response);
        }else{
            if( authentication != null && authentication.isAuthenticated()){
                filterChain.doFilter(request,response);
            }else
                response.sendRedirect("/registration");
        }

    }
}
