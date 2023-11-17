package org.sunbird.cb.hubservices.cassandra;

import com.datastax.driver.core.Session;

public interface CassandraConnectionManager {
	/**
	 * Method to get the cassandra session oject on basis of keyspace name provided
	 * .
	 *
	 * @param keyspaceName
	 * @return Session
	 */
	Session getSession(String keyspaceName);
}
