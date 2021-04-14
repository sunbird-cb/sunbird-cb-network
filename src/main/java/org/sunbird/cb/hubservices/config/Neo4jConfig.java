
package org.sunbird.cb.hubservices.config;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sunbird.cb.hubservices.util.GraphDbProperties;

import java.util.concurrent.TimeUnit;

@Configuration
public class Neo4jConfig {

	@Autowired
	private GraphDbProperties graphDbProperties;

	@Bean
	public Driver Neo4jDriver() {

		if (Boolean.parseBoolean(graphDbProperties.getNeo4jAuthEnable())) {
			return GraphDatabase.driver(graphDbProperties.getNeo4jHost(),
					AuthTokens.basic(graphDbProperties.getNeo4jUserName(), graphDbProperties.getNeo4jPassword()));
		} else {
			Config config = Config.build().withConnectionTimeout(graphDbProperties.getNeoTimeout(), TimeUnit.SECONDS)
					.withConnectionLivenessCheckTimeout(10L, TimeUnit.SECONDS).toConfig();
			System.out.println("Using timeout config of : " + graphDbProperties.getNeoTimeout().toString());
			return GraphDatabase.driver(graphDbProperties.getNeo4jHost(), config);
		}
	}
}
