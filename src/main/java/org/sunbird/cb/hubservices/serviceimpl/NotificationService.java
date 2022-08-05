package org.sunbird.cb.hubservices.serviceimpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.sunbird.cb.hubservices.model.NotificationConfig;
import org.sunbird.cb.hubservices.model.NotificationEvent;
import org.sunbird.cb.hubservices.model.NotificationTemplate;
import org.sunbird.cb.hubservices.profile.handler.ProfileUtils;
import org.sunbird.cb.hubservices.service.INotificationService;
import org.sunbird.cb.hubservices.util.ConnectionProperties;
import org.sunbird.cb.hubservices.util.Constants;

import java.util.*;

@Service
public class NotificationService implements INotificationService {

	private Logger logger = LoggerFactory.getLogger(NotificationService.class);
	@Autowired
	private ObjectMapper mapper;

	@Autowired
	ConnectionProperties connectionProperties;

	@Autowired
	private ProfileService profileService;

	@Autowired
	private ProfileUtils profileUtils;

	@Override
	public NotificationEvent buildEvent(String eventId, String sender, String reciepient, String status) {

		NotificationEvent notificationEvent = new NotificationEvent();

		if (eventId != null && sender != null && reciepient != null) {

			String fromUUID = sender;

			Map<String, List<String>> recipients = new HashMap<>();
			List<String> toList = Arrays.asList(reciepient);
			recipients.put(connectionProperties.getNotificationTemplateReciepient(), toList);

			logger.info("Notification sender --> {}", fromUUID);
			logger.info("Notification recipients --> {}", recipients);
			// values in body of notification template
			Map<String, Object> tagValues = new HashMap<>();
			tagValues.put(connectionProperties.getNotificationTemplateSender(), getUserName(fromUUID));
			tagValues.put(connectionProperties.getNotificationTemplateTargetUrl(),
					connectionProperties.getNotificationTemplateTargetUrlValue());
			tagValues.put(connectionProperties.getNotificationTemplateStatus(), status);

			notificationEvent.setMode(connectionProperties.getNotificationv2Mode());
			notificationEvent.setDeliveryType(connectionProperties.getNotificationv2DeliveryType());
			// replace recipient ids to email ids
			List<Map<String, Object>> profiles = profileUtils.getUserProfiles(toList);
			List<String> toListMails = new ArrayList<>();
			profiles.forEach(profile -> {
				toListMails.add(((Map<String, Object>) profile.get(Constants.Profile.PERSONAL_DETAILS))
						.get("primaryEmail").toString());
			});
			notificationEvent.setIds(toListMails);

			NotificationConfig configV2 = new NotificationConfig();
			configV2.setSender(connectionProperties.getNotificationv2Sender());
			configV2.setSubject(eventId);
			notificationEvent.setConfig(configV2);

			NotificationTemplate templateV2 = new NotificationTemplate();
			templateV2.setId(connectionProperties.getNotificationv2Id());
			Map<String, String> params = new HashMap<>();
			if (eventId.equals(connectionProperties.getNotificationTemplateRequest()))
				params.put("body", replaceWith(connectionProperties.getNotificationv2RequestBody(), tagValues));
			else if (eventId.equals(connectionProperties.getNotificationTemplateResponse()))
				params.put("body", replaceWith(connectionProperties.getNotificationv2ResponseBody(), tagValues));

			templateV2.setParams(params);
			notificationEvent.setTemplate(templateV2);

		}
		return notificationEvent;

	}

	private String getUserName(String uuid) {

		String fromName = null;
		try {

			Map<String, Object> profile = profileUtils.getUserProfiles(Arrays.asList(uuid)).get(0);

			if (profile != null) {

				JsonNode dataNode = mapper.convertValue(profile, JsonNode.class);
				logger.info("profile dataNode :-{}", dataNode);

				JsonNode profilePersonalDetails = dataNode.get(Constants.Profile.PERSONAL_DETAILS);
				fromName = profilePersonalDetails.get(Constants.Profile.FIRST_NAME).asText().concat(" ")
						.concat(profilePersonalDetails.get(Constants.Profile.SUR_NAME).asText());

			} else {
				fromName = Constants.Profile.HUB_MEMBER;
			}
		} catch (Exception e) {
			logger.error("Profile name could not be extracted :-{}", e.getMessage());
			fromName = Constants.Profile.HUB_MEMBER;

		}

		return fromName;
	}

	@Override
	public ResponseEntity postEvent(NotificationEvent notificationEventv2) {

		ResponseEntity<?> response = null;
		try {
			final String uri = connectionProperties.getNotificationIp()
					.concat(connectionProperties.getNotificationEventEndpoint());
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.set("Content-Type", "application/json");
			logger.info(String.format("Notification event v2 value :: %s", notificationEventv2));
			Map<String, List<NotificationEvent>> notifications = new HashMap<>();
			notifications.put("notifications", Arrays.asList(notificationEventv2));
			Map<String, Object> nrequest = new HashMap<>();
			nrequest.put("request", notifications);
			logger.info(String.format("Notification event v2 value :: %s", nrequest));
			HttpEntity request = new HttpEntity<>(nrequest, headers);
			response = restTemplate.exchange(uri, HttpMethod.POST, request, String.class);

			logger.info(Constants.Message.SENT_NOTIFICATION_SUCCESS, response.getStatusCode());

		} catch (Exception e) {
			logger.error(Constants.Message.SENT_NOTIFICATION_ERROR + ":{}", e);
			return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);

		}
		return response;

	}

	private String replaceWith(String templateStr, Map<String, Object> tagValues) {
		for (Map.Entry entry : tagValues.entrySet()) {
			if (templateStr.contains(entry.getKey().toString())) {
				templateStr = templateStr.replace(entry.getKey().toString(), entry.getValue().toString());
			}
		}
		logger.info(String.format("replaceWith value ::%s", templateStr));

		return templateStr;
	}
}
