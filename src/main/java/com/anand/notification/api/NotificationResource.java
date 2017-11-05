package com.anand.notification.api;

import java.io.IOException;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.anand.notification.core.NotificationMessage;
import com.anand.notification.service.NotificationService;

@RestController
public class NotificationResource {
	
	@Autowired
	private NotificationService notificationService;
	
	@PostMapping(value = "/send-mail" , consumes=MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String sendMail(@RequestBody NotificationMessage notificationMessage) throws IOException, MessagingException {
		return notificationService.send(notificationMessage);
	}
	
}
