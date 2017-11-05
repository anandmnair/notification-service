package com.anand.notification.config;

import java.util.List;

import org.springframework.util.Assert;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class NonProductionProperties {

	private boolean enabled;
	
	private List<String> to; 

	private List<String> cc;
	
	private List<String> bcc;
	
	public void validate(){
		if(!enabled) {
			Assert.notEmpty(to, "To address (notification.service.non-production.to) must not be null for non production env");
		}
		log.info("notification.service.non-production.enabled : {}",enabled);
		log.info("notification.service.non-production.to : {}",to);
		log.info("notification.service.non-production.cc : {}",cc);
		log.info("notification.service.non-production.bcc : {}",bcc);
	}
		    
}
