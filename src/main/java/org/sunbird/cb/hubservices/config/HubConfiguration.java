package org.sunbird.cb.hubservices.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class HubConfiguration {

	@Value("${taskExecutor.connection.threadPoolName}")
	private String connectionThreadName;

	@Value("${taskExecutor.connection.corePoolSize}")
	private int connectionCorePoolSize;

	@Value("${taskExecutor.connection.maxPoolSize}")
	private int connectionMaxPoolSize;

	@Value("${taskExecutor.connection.queueCapacity}")
	private int connectionQueueCapacity;

	@Bean(name = "connectionExecutor")
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(connectionCorePoolSize);
		executor.setMaxPoolSize(connectionMaxPoolSize);
		executor.setQueueCapacity(connectionQueueCapacity);
		executor.setThreadNamePrefix(connectionThreadName);
		executor.initialize();
		return executor;
	}

}
