package org.sunbird.cb.hubservices.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sunbird.hubservices.dao.IGraphDao;
import org.sunbird.hubservices.daoimpl.GraphDao;

@Configuration
public class GraphConfg {

	@Bean
	public IGraphDao userGraphDao() {
		return new GraphDao("userV2");
	}
}
