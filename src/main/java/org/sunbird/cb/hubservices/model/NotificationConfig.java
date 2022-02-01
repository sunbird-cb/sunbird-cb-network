package org.sunbird.cb.hubservices.model;

import java.io.Serializable;

public class NotificationConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	private String sender;
	private String subject;

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
}
