package org.sunbird.cb.hubservices.profile.handler;

public interface IProducerService {

	/**
	 * Produces any kafka event
	 * 
	 * @param topic   name of topic
	 * @param message message to push into the topic
	 */

	public void sendMessage(String topic, Object message);
}
