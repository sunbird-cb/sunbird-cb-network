package org.sunbird.cb.hubservices.cassandra;

import java.util.List;
import java.util.Map;

public interface CassandraOperation {

	/**
	 * Fetch records with specified columns (select all if null) for given column
	 * map (name, value pairs).
	 *
	 * @param keyspaceName Keyspace name
	 * @param tableName    Table name
	 * @param propertyMap  Map describing columns to be used in where clause of
	 *                     select query.
	 * @param fields       List of columns to be returned in each record
	 * @return List consisting of fetched records
	 */
	List<Map<String, Object>> getRecordsByProperties(String keyspaceName, String tableName,
			Map<String, Object> propertyMap, List<String> fields);
}
