package org.sunbird.cb.hubservices.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NotificationEventV2 implements Serializable {

	private static final long serialVersionUID = 1L;

	private String mode;
	private String deliveryType;
	//recipients ids
	private List<String> ids = new ArrayList<>();

	private NotificationConfigV2 config = new NotificationConfigV2();
	private NotificationTemplateV2 template = new NotificationTemplateV2();


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

	public NotificationConfigV2 getConfig() {
		return config;
	}

	public void setConfig(NotificationConfigV2 config) {
		this.config = config;
	}

	public NotificationTemplateV2 getTemplate() {
		return template;
	}

	public void setTemplate(NotificationTemplateV2 template) {
		this.template = template;
	}
}
