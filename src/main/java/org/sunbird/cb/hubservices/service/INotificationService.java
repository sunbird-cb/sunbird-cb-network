package org.sunbird.cb.hubservices.service;

import org.springframework.http.ResponseEntity;
import org.sunbird.cb.hubservices.model.NotificationEvent;

public interface INotificationService {

	NotificationEvent buildEvent(String eventId, String sender, String reciepient, String status);

	ResponseEntity postEvent(NotificationEvent notificationEventV2);

}
