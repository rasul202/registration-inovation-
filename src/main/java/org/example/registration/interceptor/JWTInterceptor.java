//package org.example.registration.interceptor;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpSession;
//import lombok.extern.slf4j.Slf4j;
//import org.example.registration.HeaderRequestWrapper;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//@Slf4j
//@Component
//public class JWTInterceptor implements HandlerInterceptor {
//
//    @Override
//    public boolean preHandle(
//            HttpServletRequest request,
//            HttpServletResponse response,
//            Object handler) throws Exception {
//        HttpSession session = request.getSession(false); // Get the current session, if it exists
//        if (session != null) {
//            String jwtToken = (String) session.getAttribute("jwtToken"); // Retrieve the token from the session
//            if (jwtToken != null) {
//                // Set the Authorization header for the response
//                response.addHeader("Authorization", "Bearer " + jwtToken);
//                log.info("jwt token from session is added to request : {}", jwtToken);
//            }
//        }
//        return true; // Continue with the request processing
//    }
//}
