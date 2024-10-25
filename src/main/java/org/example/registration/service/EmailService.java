package org.example.registration.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE )
public class EmailService {

    final JavaMailSender mailSender;
    final TemplateEngine templateEngine;

    @Value("${email.forgot-password.lifetime-minutes}")
    Integer forgotPasswordMailTimeToLive;

    public void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        // MimeMessageHelper allows sending HTML content
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        helper.setText(htmlBody, true);
        helper.setTo(to);
        helper.setSubject(subject);

        mailSender.send(mimeMessage);
    }

    public void sendForgotPasswordEmail(HttpSession session , String recipientEmail) throws MessagingException {
        String changePasswordUrl = "http://localhost:8080/change-password";

        //this content will be sent to toEmail address
        String htmlContent = loadHtmlFileAsString("forgotPasswordEmailMessage" , recipientEmail);

        sendHtmlEmail(recipientEmail, "Forgot Password", htmlContent);
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(forgotPasswordMailTimeToLive);

        session.setAttribute("EmailExpirationTime" , expirationTime);
        log.info("EmailExpirationTime attribute is added to  session {}" ,expirationTime);

    }

    private String loadHtmlFileAsString(String templateName , String recipientEmail) {

        Context context = new Context();
        context.setVariable("recipientEmail", recipientEmail);

        // Process the Thymeleaf template with the given context
        return templateEngine.process(templateName, context);
    }

    public boolean isMailMessageExpired(HttpSession session){
        LocalDateTime expirationTime = (LocalDateTime) session.getAttribute("EmailExpirationTime");

        if (expirationTime == null) {
            // If the attribute doesn't exist, assume it's expired or invalid
            log.info("there is no EmailExpirationTime attribute in session");
            return true;
        }

        LocalDateTime currentTime = LocalDateTime.now();

        return currentTime.isAfter(expirationTime);
    }

}