package com.pfnd.UserService.service.impl;

import com.pfnd.UserService.model.enums.EmailType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class MailingServiceImplTest {

    private JavaMailSender javaMailSender;
    private MailingServiceImpl mailingService;

    @BeforeEach
    public void setup() {
        javaMailSender = mock(JavaMailSender.class);
        mailingService = new MailingServiceImpl();
        mailingService.javaMailSender = javaMailSender;
    }

    @Test
    public void testSendEmailResetPassword_success() throws IOException, MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        Map<String, String> params = new HashMap<>();
        params.put("emailAddress", "test@example.com");
        params.put("subject", "Test Subject");
        params.put("username", "John");

        mailingService.sendEmail(EmailType.RESET_PASSWORD, params);

        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    public void testSendEmailNewAccount_success() throws IOException, MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        Map<String, String> params = new HashMap<>();
        params.put("emailAddress", "test@example.com");
        params.put("subject", "Test Subject");
        params.put("username", "John");

        mailingService.sendEmail(EmailType.NEW_ACCOUNT, params);

        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    public void testLoadHtmlFile_templateNotFound_throwsFileNotFoundException() {
        // Passing null will cause no matching template, simulating missing template
        assertThrows(FileNotFoundException.class, () -> {
            MailingServiceImpl.loadHtmlFile(null);
        });
    }

    @Test
    public void testSendEmail_missingEmailAddress_throwsException() throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("subject", "Test Subject");
        // missing emailAddress

        // MimeMessage creation mock
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertThrows(IllegalArgumentException.class, () -> {
            mailingService.sendEmail(EmailType.RESET_PASSWORD, params);
        });
    }

    @Test
    public void testSendEmail_missingSubject_throwsException() throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("emailAddress", "test@example.com");
        // missing subject

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // The code will try to set subject with null; it might throw MessagingException or not.
        // Here we check if an exception occurs.
        assertThrows(Exception.class, () -> {
            mailingService.sendEmail(EmailType.RESET_PASSWORD, params);
        });
    }
}