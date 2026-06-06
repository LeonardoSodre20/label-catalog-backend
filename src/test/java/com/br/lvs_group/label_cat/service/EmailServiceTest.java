package com.br.lvs_group.label_cat.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    @Test
    void shouldSendPasswordResetTokenEmail() {
        String to = "user@example.com";
        String token = "123456";

        emailService.sendPasswordResetToken(to, token);

        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sent = messageCaptor.getValue();
        assertThat(sent.getTo()).containsExactly(to);
        assertThat(sent.getSubject()).isEqualTo("Password Reset - Label Cat");
        assertThat(sent.getText()).contains(token);
    }
}
