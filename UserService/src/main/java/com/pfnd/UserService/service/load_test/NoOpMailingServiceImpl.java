package com.pfnd.UserService.service.load_test;

import com.pfnd.UserService.model.enums.EmailType;
import com.pfnd.UserService.service.MailingService;
import jakarta.mail.MessagingException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@Profile("load-test")
public class NoOpMailingServiceImpl implements MailingService {
    @Override
    public void sendEmail(EmailType emailType, Map<String, String> params) throws IOException, MessagingException {
        // No-op: email sending is disabled in this profile
        System.out.println("Email sending is disabled for profile 'test'");
    }
}