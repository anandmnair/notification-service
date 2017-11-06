package com.anand.notification.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.anand.notification.config.NotificationProperties;
import com.anand.notification.config.TemplateProperties;
import com.anand.notification.core.NotificationMessage;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationService {

	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private TemplateEngine templateEngine;

	@Autowired
	private NotificationProperties notificationProperties;

	public String send(NotificationMessage notificationMessage) throws IOException, MessagingException {

		final MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();

		final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");

		Context context = new Context();

		if (notificationProperties.getNonProduction() != null
				&& notificationProperties.getNonProduction().isEnabled()) {
			message.setTo(notificationProperties.getNonProduction().getTo().toArray(new String[0]));
			message.setCc(notificationProperties.getNonProduction().getCc().toArray(new String[0]));
			message.setBcc(notificationProperties.getNonProduction().getBcc().toArray(new String[0]));
			context.setVariable("nonProduction", true);
			context.setVariable("actualTo", notificationMessage.getTo());
			context.setVariable("actualCc", notificationMessage.getCc());
			context.setVariable("actualBcc", notificationMessage.getBcc());

		} else {
			message.setTo(notificationMessage.getTo().toArray(new String[0]));
			message.setCc(notificationMessage.getCc().toArray(new String[0]));
			message.setBcc(notificationMessage.getBcc().toArray(new String[0]));
		}

		message.setFrom(notificationProperties.getFrom());
		message.setReplyTo(notificationProperties.getReplyTo());

		message.setSubject(notificationMessage.getSubject());

		String emailMessage;
		List<ClassPathResource> resources = new ArrayList<>();
		if (notificationMessage.getMessage() != null) {
			emailMessage = notificationMessage.getMessage();
		} else {
			context.getVariables().putAll(notificationMessage.getMessageParams());
			if(notificationProperties.getTemplates().containsKey(notificationMessage.getMessageTemplate())) {
				TemplateProperties templateProperties = notificationProperties.getTemplates().get(notificationMessage.getMessageTemplate());
				for (Map.Entry<String, String> entry : templateProperties.getInlineImages().entrySet()) {
					ClassPathResource resource = new ClassPathResource(entry.getValue());
					context.setVariable(entry.getKey(), resource.getFile().getName());
					resources.add(resource);
				}
			}
			log.info("context :: {}", context.getVariables());
			emailMessage = templateEngine.process(notificationMessage.getMessageTemplate(), context);
		}
		message.setText(emailMessage, true);
		for (ClassPathResource resource : resources) {
			message.addInline(resource.getFile().getName(), resource.getFile());
		}

		this.javaMailSender.send(mimeMessage);
		return emailMessage;
	}

}
