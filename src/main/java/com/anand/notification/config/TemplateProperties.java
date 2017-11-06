package com.anand.notification.config;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;

@Data
public class TemplateProperties {

	private Map<String,String> inlineImages = new LinkedHashMap<String, String>();
	
}
