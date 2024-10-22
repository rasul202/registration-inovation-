package org.example.registration.controller;

import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.registration.entity.RoleEntity;
import org.example.registration.entity.UserEntity;
import org.example.registration.exception.NotFoundCompileTimeException;
import org.example.registration.exception.SamePasswordException;
import org.example.registration.exception.UserWithExistingEmail;
import org.example.registration.exception.WrongCridentialsException;
import org.example.registration.model.request.ChangePasswordRequest;
import org.example.registration.model.request.RoleRequest;
import org.example.registration.model.request.SaveUserToDbRequest;
import org.example.registration.model.response.GetUserByEmailAndPasswordResponse;
import org.example.registration.service.PasswordService;
import org.example.registration.service.TokenService;
import org.example.registration.service.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class UserController {

    UserService userService;
    TokenService tokenService;
    PasswordService passwordService;

    @GetMapping("/registration")
    public String registrationPage(Model model){
        SaveUserToDbRequest userEntity = new SaveUserToDbRequest();  //empty user for getting data from client
        model.addAttribute("user" , userEntity);
        return "registration";
    }

    @PostMapping("/registration/save")
    public String register(@ModelAttribute("user") SaveUserToDbRequest request,
                           Model model){
        request.setRoles(Arrays.asList(RoleRequest.builder().name("USER").build()));
        model.addAttribute("user" , request);
        try {
            userService.saveUserToDb(request);
        } catch (UserWithExistingEmail e) {
            model.addAttribute("existing_email" , e.getMessage() );
            return "registration";
        }
        return "successfulRegistration";
    }

    @GetMapping("/login")
    public String login(Model model){
        UserEntity userEntity = new UserEntity();
        model.addAttribute("user" , userEntity);
        return "login";
    }

    @PostMapping("/login/find")
    public String login(@RequestParam String email ,
                        @RequestParam String password,
                        Model model,
                        HttpSession session){

        GetUserByEmailAndPasswordResponse userResponse = null;
        try {
            userResponse = userService.getUserByEmailAndPassword(email , password);
        } catch (NotFoundCompileTimeException e) {//1. error case (no user with this email in DB)
            model.addAttribute("no_user" , e.getMessage());
        } catch (WrongCridentialsException e) { //2. error case (entered password don't match with stored one)
            model.addAttribute("wrong_credentials" , e.getMessage());
        }

        if(userResponse == null){ //if we will face first and second error cases this clause will work
            model.addAttribute("user" , new UserEntity());
            return "login";
        }

        List<String> roles = userResponse.getRoles().stream()
                .map(RoleRequest::getName)
                .toList();
        setUserToSecurityContext(email , passwordService.encodePassword(password) , roles);

        String jwtToken = tokenService.generateToken(userResponse.getEmail());
        tokenService.setTokenToSession(jwtToken ,session);

        model.addAttribute("user", userResponse);
        if(roles.contains("USER")){
            return "userPage";
        }else if (roles.contains("ADMIN")){
            return "adminPage";
        }else {
            return "mainPage";
        }
    }

    @GetMapping("/change-password")
    public String changePassword(Model model){
        model.addAttribute("change_password" , new ChangePasswordRequest());
        return "changePassword";
    }

    @PostMapping("/change-password")
    public String changePassword(@ModelAttribute("change_password") ChangePasswordRequest changePassword,
                                 Model model){
        String newPassword = changePassword.getNewPassword();

        //1. error case(repeat password and new password don't match)
        if( !newPassword.equals(changePassword.getRepeatPassword()) ){
            model.addAttribute("repeatedPasswordDontMachError" , "passwords dont match");
            return "changePassword";
        }


        UserEntity user = userService.getUserEntityFromSecurityContext();
        try {
            userService.updateUserPasswordByEmail(newPassword , user.getEmail());
        } catch (SamePasswordException e) { // 2. error case (new password is same with old one)
            model.addAttribute("sameWithOldPasswordError" , "you can not enter old password again");
            return "changePassword";
        }

        model.addAttribute("changePasswordSuccessMessage" , "password changed successfully");
        model.addAttribute("user" , user);
        return "mainPage";
    }

    @GetMapping("/main")
    public String mainPage(Model model){

        UserEntity user = userService.getUserEntityFromSecurityContext();

        model.addAttribute("user", user);
        return "mainPage";
    }


    @GetMapping("/user/page")
    public String userPage(Model model){

        UserEntity user = userService.getUserEntityFromSecurityContext();

        model.addAttribute("user", user);
        return "userPage";
    }

    @GetMapping("/admin/page")
    public String adminPage(Model model){
        UserEntity user = userService.getUserEntityFromSecurityContext();

        model.addAttribute("admin", user);
        return "adminPage";
    }


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // This will clear the SecurityContext as well
        return "redirect:/login";
    }

    private void setUserToSecurityContext(String email , String password , List<String> roles){
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new) // map role to a SimpleGrantedAuthority object
                .toList();

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(email , password , authorities));
    }

}
