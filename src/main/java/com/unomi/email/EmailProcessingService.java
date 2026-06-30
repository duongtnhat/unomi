package com.unomi.email;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import com.samskivert.mustache.Mustache;
import com.unomi.action.messaging.ActionExecutionCommand;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailProcessingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailProcessingService.class);

    private final EmailTemplateRepository templateRepository;
    private final EmailCallRepository callRepository;

    public EmailProcessingService(
        EmailTemplateRepository templateRepository,
        EmailCallRepository callRepository
    ) {
        this.templateRepository = templateRepository;
        this.callRepository = callRepository;
    }

    public EmailCallEntity process(ActionExecutionCommand command) {
        String templateKey = templateKey(command);
        if (templateKey == null) {
            LOGGER.warn("Email action {} trackingId={} profileId={} does not contain payload.template",
                command.actionEventId(), trackingId(command), command.profileId());
            return null;
        }

        EmailTemplateEntity template = templateRepository.findByKeyAndActiveTrue(templateKey).orElse(null);
        if (template == null) {
            LOGGER.warn("Active email template not found for key {} actionEventId={} trackingId={} profileId={}",
                templateKey, command.actionEventId(), trackingId(command), command.profileId());
            return null;
        }

        EmailSmtpConfigEntity smtpConfig = template.getSmtpConfig();
        Map<String, Object> context = context(command);
        String toAddress = render(template.getToAddress(), context);
        String subject = render(template.getSubject(), context);
        String body = render(template.getBody(), context);
        String fromAddress = fromAddress(smtpConfig);

        EmailCallEntity call = new EmailCallEntity();
        call.setTemplate(template);
        call.setSmtpConfig(smtpConfig);
        call.setActionEventId(command.actionEventId());
        call.setTrackingId(trackingId(command));
        call.setMessageId(command.messageId());
        call.setProfileId(command.profileId());
        call.setRuleKey(command.ruleKey());
        call.setActionKey(command.actionKey());
        call.setFromAddress(fromAddress);
        call.setToAddress(toAddress);
        call.setSubject(subject);
        call.setBody(body);
        call = callRepository.save(call);

        try {
            send(smtpConfig, fromAddress, toAddress, subject, body, template.getContentType());
            call.setStatus("SUCCESS");
            LOGGER.info(
                "Email sent actionEventId={} trackingId={} profileId={} templateKey={} to={}",
                command.actionEventId(),
                trackingId(command),
                command.profileId(),
                template.getKey(),
                toAddress
            );
        } catch (Exception exception) {
            call.setStatus("FAILED");
            call.setErrorMessage(limit(exception.getMessage()));
            LOGGER.warn(
                "Email send failed actionEventId={} trackingId={} profileId={} templateKey={} to={}",
                command.actionEventId(),
                trackingId(command),
                command.profileId(),
                template.getKey(),
                toAddress,
                exception
            );
        }

        call.setCompletedAt(Instant.now());
        return callRepository.save(call);
    }

    private void send(
        EmailSmtpConfigEntity smtpConfig,
        String fromAddress,
        String toAddress,
        String subject,
        String body,
        String contentType
    ) throws Exception {
        JavaMailSenderImpl sender = mailSender(smtpConfig);
        MimeMessage message = sender.createMimeMessage();
        message.setFrom(new InternetAddress(fromAddress));
        message.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(toAddress));
        message.setSubject(subject);
        message.setContent(body, contentType == null || contentType.isBlank() ? "text/html; charset=UTF-8" : contentType);
        sender.send(message);
    }

    private JavaMailSenderImpl mailSender(EmailSmtpConfigEntity smtpConfig) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(smtpConfig.getHost());
        sender.setPort(smtpConfig.getPort());
        sender.setUsername(smtpConfig.getUsername());
        sender.setPassword(smtpConfig.getPassword());

        Properties properties = sender.getJavaMailProperties();
        properties.put("mail.smtp.auth", String.valueOf(smtpConfig.isAuthEnabled()));
        properties.put("mail.smtp.starttls.enable", String.valueOf(smtpConfig.isStartTlsEnabled()));
        properties.put("mail.smtp.connectiontimeout", "5000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");
        return sender;
    }

    private String templateKey(ActionExecutionCommand command) {
        Object value = command.payload() == null ? null : command.payload().get("template");
        if (value instanceof String text && !text.isBlank()) {
            return text;
        }
        return null;
    }

    private UUID trackingId(ActionExecutionCommand command) {
        return command.trackingId() == null ? command.actionEventId() : command.trackingId();
    }

    private String render(String template, Map<String, Object> context) {
        return Mustache.compiler()
            .escapeHTML(false)
            .compile(template)
            .execute(context);
    }

    private Map<String, Object> context(ActionExecutionCommand command) {
        Map<String, Object> context = new LinkedHashMap<>();
        if (command.payload() != null) {
            context.putAll(command.payload());
        }
        context.put("payload", command.payload() == null ? Map.of() : command.payload());
        context.put("actionEventId", command.actionEventId().toString());
        context.put("trackingId", trackingId(command).toString());
        context.put("messageId", command.messageId());
        context.put("requestedAt", command.requestedAt());
        context.put("profileId", command.profileId());
        context.put("ruleKey", command.ruleKey());
        context.put("actionKey", command.actionKey());
        context.put("actionType", command.actionType());
        return context;
    }

    private String fromAddress(EmailSmtpConfigEntity smtpConfig) {
        if (smtpConfig.getFromName() == null || smtpConfig.getFromName().isBlank()) {
            return smtpConfig.getFromAddress();
        }
        return smtpConfig.getFromName() + " <" + smtpConfig.getFromAddress() + ">";
    }

    private String limit(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= 2000 ? message : message.substring(0, 2000);
    }
}
