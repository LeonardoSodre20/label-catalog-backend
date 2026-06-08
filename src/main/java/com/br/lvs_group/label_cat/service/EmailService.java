package com.br.lvs_group.label_cat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetToken(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Reset - Label Cat");
        message.setText("Your password reset code is: " + token + "\n\nThis code is valid for 1 hour.");
        mailSender.send(message);
    }

    public void sendGeneratedPassword(String to, String password) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Welcome to Label Cat - Your Account");
        message.setText("Welcome to Label Cat!\n\nYour account has been created. Your temporary password is: " + password + "\n\nPlease log in and change your password on first access.\n\nThis is an automatically generated password, please change it after your first login.");
        mailSender.send(message);
    }
}
