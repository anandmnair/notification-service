package com.anand.notification.config;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@EnableConfigurationProperties
@Component
@ConfigurationProperties(value="notification.service")
@Data
@Slf4j
public class NotificationProperties {

    @NestedConfigurationProperty
	private NonProductionProperties nonProduction;
	
	private String from; 

	private String replyTo;
	
	private Map<String,TemplateProperties> templates;
		
	@PostConstruct
	public void validate(){
		nonProduction.validate();
		log.info("notification.service.from : {}",from);
		log.info("notification.service.reply-to : {}",replyTo);
	}
}
