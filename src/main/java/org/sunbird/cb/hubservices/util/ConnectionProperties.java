package org.sunbird.cb.hubservices.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConnectionProperties {

	@Value("${max.node.size}")
	private int maxNodeSize;

	@Value("${es.host}")
	private String esHost;

	@Value("${es.port}")
	private String esPort;

	@Value("${es.username}")
	private String esUser;

	@Value("${es.password}")
	private String esPassword;

	@Value("${es.profile.index}")
	private String esProfileIndex;

	@Value("${es.profile.index.type}")
	private String esProfileIndexType;

	@Value("${es.profile.source.fields}")
	private String[] esProfileSourceFields;

	@Value("${notification.service.host}")
	private String notificationIp;

	@Value("${notification.event.endpoint}")
	private String notificationEventEndpoint;

	@Value("${notification.template.targetUrl}")
	private String notificationTemplateTargetUrl;

	@Value("${notification.template.targetUrl.value}")
	private String notificationTemplateTargetUrlValue;

	@Value("${notification.template.sender}")
	private String notificationTemplateSender;

	@Value("${notification.template.reciepient}")
	private String notificationTemplateReciepient;

	@Value("${notification.template.request}")
	private String notificationTemplateRequest;

	@Value("${notification.template.response}")
	private String notificationTemplateResponse;

	@Value("${notification.template.status}")
	private String notificationTemplateStatus;

	@Value("${notification.template.v2.sender}")
	private String notificationv2Sender;

	@Value("${notification.template.v2.id}")
	private String notificationv2Id;

	@Value("${notification.template.v2.delivery.type}")
	private String notificationv2DeliveryType;

	@Value("${notification.template.v2.mode}")
	private String notificationv2Mode;

	@Value("${notification.template.v2.request.body}")
	private String notificationv2RequestBody;

	@Value("${notification.template.v2.response.body}")
	private String notificationv2ResponseBody;

	@Value("${notification.enabled}")
	private boolean notificationEnabled;

	@Value("${sunbird.learner.service.host}")
	private String learnerServiceHost;

	@Value("${sunbird.user.search.endpoint}")
	private String userSearchEndPoint;

	@Value("${sunbird.user.update.endpoint}")
	private String userUpdateEndPoint;

	@Value("${sunbird.user.read.endpoint}")
	private String userReadEndPoint;

	public String getEsProfileIndex() {
		return esProfileIndex;
	}

	public String getEsProfileIndexType() {
		return esProfileIndexType;
	}

	public String[] getEsProfileSourceFields() {
		return esProfileSourceFields;
	}

	public String getEsHost() {
		return esHost;
	}

	public String getEsPort() {
		return esPort;
	}

	public String getEsUser() {
		return esUser;
	}

	public String getEsPassword() {
		return esPassword;
	}

	public String getNotificationTemplateTargetUrlValue() {
		return notificationTemplateTargetUrlValue;
	}

	public String getNotificationTemplateSender() {
		return notificationTemplateSender;
	}

	public String getNotificationIp() {
		return notificationIp;
	}

	public String getNotificationEventEndpoint() {
		return notificationEventEndpoint;
	}

	public String getNotificationTemplateTargetUrl() {
		return notificationTemplateTargetUrl;
	}

	public String getNotificationTemplateReciepient() {
		return notificationTemplateReciepient;
	}

	public String getNotificationv2Sender() {
		return notificationv2Sender;
	}

	public String getNotificationv2Id() {
		return notificationv2Id;
	}

	public String getNotificationv2DeliveryType() {
		return notificationv2DeliveryType;
	}

	public String getNotificationv2RequestBody() {
		return notificationv2RequestBody;
	}

	public String getNotificationv2ResponseBody() {
		return notificationv2ResponseBody;
	}

	public String getNotificationv2Mode() {
		return notificationv2Mode;
	}

	public String getNotificationTemplateRequest() {
		return notificationTemplateRequest;
	}

	public String getLearnerServiceHost() {
		return learnerServiceHost;
	}

	public String getUserSearchEndPoint() {
		return userSearchEndPoint;
	}

	public String getUserUpdateEndPoint() {
		return userUpdateEndPoint;
	}

	public String getNotificationTemplateResponse() {
		return notificationTemplateResponse;
	}

	public String getNotificationTemplateStatus() {
		return notificationTemplateStatus;
	}

	public boolean isNotificationEnabled() {
		return notificationEnabled;
	}

	public String getUserReadEndPoint() {
		return userReadEndPoint;
	}

	public int getMaxNodeSize() {
		return maxNodeSize;
	}
}
