package org.sunbird.cb.hubservices.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConnectionProperties {

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

	@Value("${notification.ip}")
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

	@Value("${notification.enabled}")
	private boolean notificationEnabled;

	@Value("${sunbird.learner.service.host}")
	private String learnerServiceHost;

	@Value("${sunbird.user.search.endpoint}")
	private String userSearchEndPoint;


	@Value("${update.profile.connections}")
	private boolean updateProfileConnections;

	public void setEsPort(String esPort) {
		this.esPort = esPort;
	}

	public void setEsUser(String esUser) {
		this.esUser = esUser;
	}

	public void setEsPassword(String esPassword) {
		this.esPassword = esPassword;
	}

	public void setEsProfileIndex(String esProfileIndex) {
		this.esProfileIndex = esProfileIndex;
	}

	public void setEsProfileIndexType(String esProfileIndexType) {
		this.esProfileIndexType = esProfileIndexType;
	}

	public void setEsProfileSourceFields(String[] esProfileSourceFields) {
		this.esProfileSourceFields = esProfileSourceFields;
	}

	public void setNotificationIp(String notificationIp) {
		this.notificationIp = notificationIp;
	}

	public void setNotificationEventEndpoint(String notificationEventEndpoint) {
		this.notificationEventEndpoint = notificationEventEndpoint;
	}

	public void setNotificationTemplateTargetUrl(String notificationTemplateTargetUrl) {
		this.notificationTemplateTargetUrl = notificationTemplateTargetUrl;
	}

	public void setNotificationTemplateTargetUrlValue(String notificationTemplateTargetUrlValue) {
		this.notificationTemplateTargetUrlValue = notificationTemplateTargetUrlValue;
	}

	public void setNotificationTemplateSender(String notificationTemplateSender) {
		this.notificationTemplateSender = notificationTemplateSender;
	}

	public void setNotificationTemplateReciepient(String notificationTemplateReciepient) {
		this.notificationTemplateReciepient = notificationTemplateReciepient;
	}

	public void setNotificationTemplateRequest(String notificationTemplateRequest) {
		this.notificationTemplateRequest = notificationTemplateRequest;
	}

	public void setNotificationTemplateResponse(String notificationTemplateResponse) {
		this.notificationTemplateResponse = notificationTemplateResponse;
	}

	public void setNotificationTemplateStatus(String notificationTemplateStatus) {
		this.notificationTemplateStatus = notificationTemplateStatus;
	}

	public void setNotificationEnabled(boolean notificationEnabled) {
		this.notificationEnabled = notificationEnabled;
	}

	public void setLearnerServiceHost(String learnerServiceHost) {
		this.learnerServiceHost = learnerServiceHost;
	}

	public void setUserSearchEndPoint(String userSearchEndPoint) {
		this.userSearchEndPoint = userSearchEndPoint;
	}

	public boolean isUpdateProfileConnections() {
		return updateProfileConnections;
	}

	public void setUpdateProfileConnections(boolean updateProfileConnections) {
		this.updateProfileConnections = updateProfileConnections;
	}

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

	public String getNotificationTemplateRequest() {
		return notificationTemplateRequest;
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

	public String getLearnerServiceHost() {
		return learnerServiceHost;
	}

	public String getUserSearchEndPoint() {
		return userSearchEndPoint;
	}
}
