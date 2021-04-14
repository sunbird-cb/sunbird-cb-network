package org.sunbird.cb.hubservices.profile.handler;

import org.springframework.kafka.annotation.KafkaListener;

public class ConsumerService implements IConsumerService {
	@Override
	@KafkaListener(topics = "${add.response.topic.name}", groupId = "${add.topic.group.id}", containerFactory = "customKafkaListenerContainerFactory")
	public void consumeCreateProfile(Object message) {

		// TODO: with handling validation in producer
	}

	@Override
	public void consumeUpdateProfile(Object message) {
		// TODO

	}
}
