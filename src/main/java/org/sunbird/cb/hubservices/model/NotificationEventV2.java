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

	private NotificationConfigV2 configV2 = new NotificationConfigV2();
	private NotificationTemplateV2 templateV2 = new NotificationTemplateV2();


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

	public NotificationConfigV2 getConfigV2() {
		return configV2;
	}

	public void setConfigV2(NotificationConfigV2 configV2) {
		this.configV2 = configV2;
	}

	public NotificationTemplateV2 getTemplateV2() {
		return templateV2;
	}

	public void setTemplateV2(NotificationTemplateV2 templateV2) {
		this.templateV2 = templateV2;
	}
}
