package org.sunbird.cb.hubservices.cassandra;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.sunbird.cb.hubservices.util.Constants;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Select.Builder;
import com.datastax.driver.core.querybuilder.Select.Where;

@Component
public class CassandraOperationImpl implements CassandraOperation {

	private Logger logger = LoggerFactory.getLogger(getClass().getName());

	@Autowired
	CassandraConnectionManager connectionManager;

	@Override
	public List<Map<String, Object>> getRecordsByProperties(String keyspaceName, String tableName,
			Map<String, Object> propertyMap, List<String> fields) {
		Select selectQuery = null;
		List<Map<String, Object>> response = new ArrayList<>();
		try {
			selectQuery = processQuery(keyspaceName, tableName, propertyMap, fields);
			ResultSet results = connectionManager.getSession(keyspaceName).execute(selectQuery);
			response = CassandraUtil.createResponse(results);
		} catch (Exception e) {
			logger.error(Constants.EXCEPTION_MSG_FETCH + tableName + " : " + e.getMessage(), e);
		}
		return response;
	}

	private Select processQuery(String keyspaceName, String tableName, Map<String, Object> propertyMap,
			List<String> fields) {
		Select selectQuery = null;
		Builder selectBuilder;
		if (!CollectionUtils.isEmpty(fields)) {
			String[] dbFields = fields.toArray(new String[fields.size()]);
			selectBuilder = QueryBuilder.select(dbFields);
		} else {
			selectBuilder = QueryBuilder.select().all();
		}
		selectQuery = selectBuilder.from(keyspaceName, tableName);
		if (MapUtils.isNotEmpty(propertyMap)) {
			Where selectWhere = selectQuery.where();
			for (Entry<String, Object> entry : propertyMap.entrySet()) {
				if (entry.getValue() instanceof List) {
					List<Object> list = (List) entry.getValue();
					if (null != list) {
						Object[] propertyValues = list.toArray(new Object[list.size()]);
						Clause clause = QueryBuilder.in(entry.getKey(), propertyValues);
						selectWhere.and(clause);
					}
				} else {
					Clause clause = QueryBuilder.eq(entry.getKey(), entry.getValue());
					selectWhere.and(clause);
				}
			}
		}
		return selectQuery;
	}

}
