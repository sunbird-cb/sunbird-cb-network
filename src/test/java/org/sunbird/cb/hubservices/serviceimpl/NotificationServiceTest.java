package org.sunbird.cb.hubservices.serviceimpl;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.sunbird.cb.hubservices.model.NotificationEvent;
import org.sunbird.cb.hubservices.model.Response;
import org.sunbird.cb.hubservices.profile.handler.ProfileUtils;
import org.sunbird.cb.hubservices.util.ConnectionProperties;
import org.sunbird.cb.hubservices.util.Constants;

@RunWith(MockitoJUnitRunner.class)
class NotificationServiceTest {

	@InjectMocks
	NotificationService notificationService;
	@Mock
	ProfileService profileService;

	@Mock
	ProfileUtils profileUtils;

	@Mock
	ConnectionProperties connectionProperties;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	void buildEvent_all_values_set() throws Exception {

		final String sender = "#sender";
		final String targetUrl = "#targetUrl";
		final String urlValues = "#urlValues";
		final String status = "#status";
		final String id = "mockId";

		Map<String, Object> mockProfiles = new HashMap<>();
		Response response = new Response();
		response.put(Constants.ResponseStatus.DATA, mockProfiles);

		when(profileUtils.getUserProfiles(Arrays.asList(id))).thenReturn(Arrays.asList(mockProfiles));

		when(connectionProperties.getNotificationTemplateSender()).thenReturn(sender);
		when(connectionProperties.getNotificationTemplateTargetUrl()).thenReturn(targetUrl);
		when(connectionProperties.getNotificationTemplateTargetUrlValue()).thenReturn(urlValues);
		when(connectionProperties.getNotificationTemplateStatus()).thenReturn(status);
		when(connectionProperties.getNotificationTemplateReciepient()).thenReturn("#reciepient");

		/*
		 * UserConnection userConnection = mock(UserConnection.class,
		 * Mockito.RETURNS_DEEP_STUBS);
		 * when(userConnection.getUserConnectionPrimarykey().getUserId()).thenReturn(
		 * "uuid");
		 * when(userConnection.getUserConnectionPrimarykey().getConnectionId()).
		 * thenReturn("connect_id");
		 * when(userConnection.getConnectionStatus()).thenReturn("status");
		 */

		NotificationEvent notificationEvent = notificationService.buildEvent(id, "sender", "reciepient", "status");

		assertTrue(notificationEvent.getConfig().getSubject().equalsIgnoreCase(id));
		assertTrue(notificationEvent.getIds() != null);
		assertTrue(notificationEvent.getConfig() != null);
		assertTrue(notificationEvent.getTemplate() != null);

	}

	@Test
	void postEvent() {
		when(connectionProperties.getNotificationIp()).thenReturn("ipaddress");
		when(connectionProperties.getNotificationEventEndpoint()).thenReturn("endpoint");
		// when(new RestTemplate().exchange("ipaddressendpoint", HttpMethod.POST,
		// Mockito.any(), String.class)).thenReturn(Mockito.any());

		NotificationEvent notificationEvent = mock(NotificationEvent.class, Mockito.RETURNS_DEEP_STUBS);

		ResponseEntity entity = notificationService.postEvent(notificationEvent);
		assertTrue(entity != null);
		assertTrue(entity.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR));

	}
}