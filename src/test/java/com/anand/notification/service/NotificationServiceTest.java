package com.anand.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.anand.notification.config.NonProductionProperties;
import com.anand.notification.config.NotificationProperties;
import com.anand.notification.core.NotificationMessage;
import com.anand.notification.core.NotificationMessage.NotificationMessageBuilder;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = { "spring.mail.host=localhost", "spring.mail.port=3025" })
public class NotificationServiceTest {

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private NotificationProperties notificationProperties;

	private NotificationProperties originalNotificationProperties;

	private GreenMail mailServer;

	private NotificationMessageBuilder notificationMessageBuilder;

	@Before
	public void setUp() {
		originalNotificationProperties = new NotificationProperties();
		BeanUtils.copyProperties(notificationProperties, originalNotificationProperties);
		mailServer = new GreenMail(ServerSetupTest.SMTP);
		mailServer.start();
		notificationMessageBuilder = NotificationMessage.builder().to("actual-to@to-mail.com")
				.cc("actual-cc@cc-mail.com").bcc("actual-bcc@bcc-mail.com").subject("Test Email Subject")
				.message("Test Email Service");
	}

	@After
	public void cleanUp() {
		BeanUtils.copyProperties(originalNotificationProperties, notificationProperties);
		mailServer.stop();
	}

	@Test
	public void sendEmailWithMessageTest() throws IOException, MessagingException {
		NotificationMessage notificationMessage = notificationMessageBuilder.build();
		notificationProperties.getNonProduction().setEnabled(false);
		notificationService.send(notificationMessage);
		
		MimeMessage[] messages = mailServer.getReceivedMessages();
		assertThat(messages.length).isEqualTo(3);

		assertRecipients(messages,notificationMessage);

		assertThat(messages[0].getSubject()).isEqualTo(notificationMessage.getSubject());
		String body = GreenMailUtil.getBody(messages[0]);
		assertThat(body).contains(notificationMessage.getMessage());
	}

	@Test
	public void sendEmailWithMessageNonProductionModeTest() throws IOException, MessagingException {
		NotificationMessage notificationMessage = notificationMessageBuilder.build();
		notificationService.send(notificationMessage);
		
		MimeMessage[] messages = mailServer.getReceivedMessages();
		assertThat(messages.length).isEqualTo(3);

		assertRecipients(messages,notificationMessage);

		assertThat(messages[0].getSubject()).isEqualTo(notificationMessage.getSubject());
		String body = GreenMailUtil.getBody(messages[0]);
		assertThat(body).contains(notificationMessage.getMessage());
	}
	
	@Test
	public void sendEmailWithMessageTemplateTest() throws IOException, MessagingException {
		NotificationMessage notificationMessage = notificationMessageBuilder.message(null).messageTemplate("test-mail")
				.messageParam("name", "Anand").messageParam("message", "Test Email Message").build();
		notificationProperties.setNonProduction(null);
		notificationService.send(notificationMessage);

		MimeMessage[] messages = mailServer.getReceivedMessages();
		assertThat(messages.length).isEqualTo(3);

		assertRecipients(messages,notificationMessage);
		
		assertRecipients(messages[0].getRecipients(Message.RecipientType.TO),notificationMessage.getTo());
		assertRecipients(messages[0].getRecipients(Message.RecipientType.CC),notificationMessage.getCc());
		assertRecipients(messages[0].getRecipients(Message.RecipientType.BCC),notificationMessage.getBcc());

		assertThat(messages[0].getSubject()).isEqualTo(notificationMessage.getSubject());
		String body = GreenMailUtil.getBody(messages[0]);
		System.out.println(body);

		assertThat(body).contains("Hello <span>Anand</span>");
		assertThat(body).contains("Your message is <span>Test Email Message</span>");
		assertThat(body).contains("<img src=\"cid:user.png\" />");
	}

	
	
	@Test
	public void sendEmailWithMessageTemplateWithNonProductionModeTest() throws IOException, MessagingException {
		NotificationMessage notificationMessage = notificationMessageBuilder.message(null).messageTemplate("test-mail")
				.messageParam("name", "Anand").messageParam("message", "Test Email Message").build();
		notificationService.send(notificationMessage);

		MimeMessage[] messages = mailServer.getReceivedMessages();
		assertThat(messages.length).isEqualTo(3);

		assertRecipients(messages,notificationMessage);

		assertThat(messages[0].getSubject()).isEqualTo(notificationMessage.getSubject());
		String body = GreenMailUtil.getBody(messages[0]);
		assertThat(body).contains("<h3>Actual Mail Recipients</h3>");
		assertThat(body).contains("To : <span>[actual-to@to-mail.com]</span>");
		assertThat(body).contains("Cc : <span>[actual-cc@cc-mail.com]</span>");
		assertThat(body).contains("Bcc : <span>[actual-bcc@bcc-mail.com]</span>");
		assertThat(body).contains("Hello <span>Anand</span>");
		assertThat(body).contains("Your message is <span>Test Email Message</span>");
		assertThat(body).contains("<img src=\"cid:user.png\" />");
	}
	
	private void assertRecipients(MimeMessage[] messages, NotificationMessage notificationMessage) throws MessagingException {
		if(messages!=null) {
			for(MimeMessage message : messages) {
				assertRecipients(message.getRecipients(Message.RecipientType.TO),
						isNonProduction(notificationProperties.getNonProduction())? notificationProperties.getNonProduction().getTo() : notificationMessage.getTo());
				assertRecipients(message.getRecipients(Message.RecipientType.CC),
						isNonProduction(notificationProperties.getNonProduction())? notificationProperties.getNonProduction().getCc() : notificationMessage.getCc());
				assertRecipients(message.getRecipients(Message.RecipientType.BCC),
						isNonProduction(notificationProperties.getNonProduction())? notificationProperties.getNonProduction().getBcc() : notificationMessage.getBcc());
			}
		}
	}
	
	private void assertRecipients(Address[] actualAddresses, List<String> expectedAddress) {
		if (actualAddresses != null) {
			for (Address address : actualAddresses) {
				assertThat(address.toString()).contains((expectedAddress));
			}
		}
	}
	
	private boolean isNonProduction(NonProductionProperties nonProductionProperties) {
		return nonProductionProperties!=null && nonProductionProperties.isEnabled();
	}
}
