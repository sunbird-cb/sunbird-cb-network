package org.sunbird.cb.hubservices.service;

import org.springframework.http.ResponseEntity;
import org.sunbird.cb.hubservices.model.NotificationEvent;

public interface INotificationService {

	/**
	 * Build the notification request
	 * 
	 * @param eventId
	 * @param userConnection
	 */
	NotificationEvent buildEvent(String eventId, String sender, String reciepient, String status);

	/**
	 * Sends notifications
	 *
	 * @param notificationEvent
	 * @return
	 */
	ResponseEntity postEvent(String rootOrg, NotificationEvent notificationEvent);

}
