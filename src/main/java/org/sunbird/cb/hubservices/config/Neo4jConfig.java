
package org.sunbird.cb.hubservices.config;

import java.util.concurrent.TimeUnit;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.exceptions.AuthenticationException;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sunbird.cb.hubservices.exception.GraphException;
import org.sunbird.cb.hubservices.util.GraphDbProperties;

@Configuration
public class Neo4jConfig {

	private Logger logger = LoggerFactory.getLogger(Neo4jConfig.class);

	@Autowired
	private GraphDbProperties graphDbProperties;

	@Bean
	public Driver Neo4jDriver() {

		try {
			if (Boolean.parseBoolean(graphDbProperties.getNeo4jAuthEnable())) {
				return GraphDatabase.driver(graphDbProperties.getNeo4jHost(),
						AuthTokens.basic(graphDbProperties.getNeo4jUserName(), graphDbProperties.getNeo4jPassword()));
			} else {
				Config config = Config.build()
						.withConnectionTimeout(graphDbProperties.getNeoTimeout(), TimeUnit.SECONDS)
						.withConnectionLivenessCheckTimeout(10L, TimeUnit.SECONDS).toConfig();
				logger.info("Using timeout config of : " + graphDbProperties.getNeoTimeout().toString());
				return GraphDatabase.driver(graphDbProperties.getNeo4jHost(), config);
			}

		} catch (AuthenticationException | ServiceUnavailableException e) {
			throw new GraphException(e.code(), e.getMessage());
		}

	}
}
