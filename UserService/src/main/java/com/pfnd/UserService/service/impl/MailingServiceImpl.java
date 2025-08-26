package com.pfnd.UserService.service.impl;

import com.pfnd.UserService.model.enums.EmailType;
import com.pfnd.UserService.service.MailingService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@Profile("!load-test")
public class MailingServiceImpl implements MailingService {
    @Autowired
    protected JavaMailSender javaMailSender;

    protected static String loadHtmlFile(EmailType emailType) throws IOException {
        String templateName = "";
        switch (emailType) {
            case RESET_PASSWORD -> templateName = "_reset_password_template.html";
            case NEW_ACCOUNT -> templateName = "_new_account_template.html";
            case null -> throw new FileNotFoundException("Template not found: " + templateName);
        }

        try (InputStream is = MailingServiceImpl.class.getResourceAsStream("/static/" + templateName)) {
            if (is == null) {
                throw new FileNotFoundException("Template not found: " + templateName);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String getCompletedHtmlContent(Map<String, String> placeholders, EmailType emailType) throws IOException {
        String htmlContent = loadHtmlFile(emailType);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            htmlContent = htmlContent.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return htmlContent;
    }

    @Override
    public void sendEmail(EmailType emailType, Map<String, String> params) throws IOException, MessagingException {
        String htmlContent = getCompletedHtmlContent(params, emailType);

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(params.get("emailAddress"));
        helper.setSubject(params.get("subject"));
        helper.setText(htmlContent, true);
        javaMailSender.send(message);
    }
}