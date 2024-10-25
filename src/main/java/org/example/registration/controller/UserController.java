package org.example.registration.controller;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.registration.entity.RoleEntity;
import org.example.registration.entity.UserEntity;
import org.example.registration.exception.*;
import org.example.registration.model.request.ChangePasswordIfForgotRequest;
import org.example.registration.model.request.ChangePasswordIfLoggedInRequest;
import org.example.registration.model.request.RoleRequest;
import org.example.registration.model.request.SaveUserToDbRequest;
import org.example.registration.model.response.GetUserByEmailAndPasswordResponse;
import org.example.registration.service.*;
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
    EmailService emailService;

    @GetMapping("/registration")
    public String registrationPage(Model model){
        model.addAttribute("user" , new SaveUserToDbRequest()); //pass empty object for getting data
        return "registration";
    }


    @PostMapping("/registration")
    public String register(@ModelAttribute("user") SaveUserToDbRequest request,
                           Model model){
        request.setRoles(Arrays.asList(RoleRequest.builder().name("USER").build()));//this is USER registration form , for registering as admin we must create another page or logic
        try {
            userService.saveUserToDb(request);
        } catch (UserWithExistingEmail e) {  // error case (if the client tries to register with existing email)
            model.addAttribute("existing_email" , e.getMessage() );
            return "registration";
        }
        model.addAttribute("user" , request);
        return "successfulRegistration";
    }


    @GetMapping("/login")
    public String login(Model model){
        model.addAttribute("user" , new UserEntity());
        return "login";
    }


    @PostMapping("/login")
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
        userService.setUserToSecurityContext(email , password, roles);

        String jwtToken = tokenService.generateToken(userResponse.getEmail());
        tokenService.setTokenToSession(jwtToken ,session);

        model.addAttribute("user", userResponse);
        return redirectUserToPageAccordingRole(roles);
    }

    private String redirectUserToPageAccordingRole(List<String> roles){
        if(roles.contains("USER")){
            return "userPage";
        }else if (roles.contains("ADMIN")){
            return "adminPage";
        }else {
            return "mainPage";
        }
    }


    @GetMapping("/main")
    public String mainPage(Model model){

        UserEntity user = userService.getUserEntityFromSecurityContext();

        List<String> roles = user.getRoles().stream()
                .map(RoleEntity::getName)
                .toList();

        model.addAttribute("user", user);
        return redirectUserToPageAccordingRole(roles);
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
        session.removeAttribute("jwtToken");
        log.info("jwtToken attribute is removed from session");
        return "redirect:/login";
    }


    @GetMapping("/forgot-password")
    public String forgotPassword(Model model){
        model.addAttribute("email" , "");
        return "forgotPassword";
    }


    @PostMapping("/forgot-password")
    public String forgotPassword(@ModelAttribute("email") String email,
                                 Model model,
                                 HttpSession session){
        try {
            //this method first will send the email to given email and add expiration date of this email to session
            emailService.sendForgotPasswordEmail(session , email);
        } catch (NotFoundRunTimeException e) { // error case (there is no user in DB with this email)
            model.addAttribute("noUser" , true);
            return "forgotPassword";
        } catch (MessagingException e) {
            throw new RuntimeException("problem occurred while sending email", e);
        }
        model.addAttribute("messageSentSuccess" , true);
        return "forgotPassword";
    }

    @GetMapping("/change-password-if-forget")
    public String changePassword(Model model,
                                 @RequestParam(name = "email") String email, //if we receive this parameter it means this request has come from forgot password
                                 HttpSession session
    ){
        if (emailService.isMailMessageExpired(session))
            return "mailMessageExpired";

        session.removeAttribute("mailMessageExpired");
        log.info("mailMessageExpired attribute is removed from session");

        model.addAttribute("change_password" , ChangePasswordIfForgotRequest.builder().email(email).build());
        return "changePasswordIfForget";
    }


    @PostMapping("/change-password-if-forget")
    public String changePassword(@ModelAttribute("change_password") ChangePasswordIfForgotRequest changePassword,
                                 Model model){
        String newPassword = changePassword.getNewPassword();
        String repeatPassword = changePassword.getRepeatPassword();
        String email = changePassword.getEmail();

        try {
            userService.updateUserPasswordByEmail(newPassword, repeatPassword , email);
        } catch (SamePasswordException e) { // 1. error case (new password is same with old one)
            model.addAttribute("sameWithOldPasswordError", "you can not enter old password");
            return "changePasswordIfForget";
        } catch (RepeatedPasswordDontMatch e) { //2. error case(repeat password and new password don't match)
            model.addAttribute("repeatedPasswordDontMachError" , "passwords dont match");
            return "changePasswordIfForget";
        }

        return "passwordChangedSuccess";
    }

    @GetMapping("/change-password-if-logged-in")
    public String changePasswordIfLoggedIn(Model model){
        model.addAttribute("change_password" , new ChangePasswordIfLoggedInRequest());
        return "changePasswordIfLoggedIn";
    }


    @PostMapping("/change-password-if-logged-in")
    public String changePassword(@ModelAttribute("change_password") ChangePasswordIfLoggedInRequest changePassword,
                                 Model model){
        String newPassword = changePassword.getNewPassword();
        String repeatPassword = changePassword.getRepeatPassword();
        String oldPassword = changePassword.getOldPassword();

        UserEntity userEntity = userService.getUserEntityFromSecurityContext();
        if (userService.verifyUserPassword(userEntity , oldPassword)){
            try {
                userService.updateUserPasswordByEmail(newPassword , repeatPassword , userEntity.getEmail());
            } catch (SamePasswordException e) { // 1. error case (new password is same with old one)
                model.addAttribute("sameWithOldPasswordError", "you can not enter old password again");
                return "changePasswordIfLoggedIn";
            } catch (RepeatedPasswordDontMatch e) { //2. error case(repeat password and new password don't match)
                model.addAttribute("repeatedPasswordDontMachError" , "passwords dont match");
                return "changePasswordIfLoggedIn";
            }
        }else {
            model.addAttribute("wrongPassword" , "old password is incorrect");
            return "changePasswordIfLoggedIn";
        }
        model.addAttribute("success" , true);
        return "changePasswordIfLoggedIn";
    }

}
