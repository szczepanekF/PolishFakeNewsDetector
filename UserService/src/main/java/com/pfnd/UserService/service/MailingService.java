package com.pfnd.UserService.service;

import com.pfnd.UserService.model.enums.EmailType;
import jakarta.mail.MessagingException;

import java.io.IOException;
import java.util.Map;

public interface MailingService {
    void sendEmail(EmailType emailType, Map<String, String> params) throws IOException, MessagingException;
}
