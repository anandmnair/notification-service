package com.anand.notification.core;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
	
	@NotNull
	@Singular("to")
	private List<String> to; 

	@Singular("cc")
	private List<String> cc;

	@Singular("bcc")
	private List<String> bcc;
	
	@NotNull
	private String subject;
 
	private String message;
	
	private String messageTemplate;
	
	@Singular
	private Map<String,Object> messageParams;
	
	@Singular
	private List<File> attchements;
	
}
