package org.sunbird.cb.hubservices.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NotificationEvent implements Serializable {

	private static final long serialVersionUID = 1L;

	private String mode;
	private String deliveryType;
	// recipients ids
	private List<String> ids = new ArrayList<>();

	private NotificationConfig config = new NotificationConfig();
	private NotificationTemplate template = new NotificationTemplate();

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getDeliveryType() {
		return deliveryType;
	}

	public void setDeliveryType(String deliveryType) {
		this.deliveryType = deliveryType;
	}

	public List<String> getIds() {
		return ids;
	}

	public void setIds(List<String> ids) {
		this.ids = ids;
	}

	public NotificationConfig getConfig() {
		return config;
	}

	public void setConfig(NotificationConfig config) {
		this.config = config;
	}

	public NotificationTemplate getTemplate() {
		return template;
	}

	public void setTemplate(NotificationTemplate template) {
		this.template = template;
	}
}
