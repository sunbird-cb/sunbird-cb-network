package org.sunbird.cb.hubservices.profile.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.sunbird.cb.hubservices.exception.BadRequestException;
import org.sunbird.cb.hubservices.util.ConnectionProperties;

@Service
public class AutoCompleteService {

	@Autowired
	private ConnectionProperties connectionProperties;

	@Autowired
	private RestHighLevelClient esClient;

	final String[] includeFields = { "employmentDetails.departmentName", "personalDetails.firstname",
			"personalDetails.surname", "personalDetails.primaryEmail","personalDetails.mobile", "id", "professionalDetails.name" };

	public List<Map<String, Object>> getUserSearchData(String searchTerm) throws IOException {
		if (StringUtils.isEmpty(searchTerm))
			throw new BadRequestException("Search term should not be empty!");
		List<Map<String, Object>> resultArray = new ArrayList<>();
		Map<String, Object> result;
		String depName;
		final BoolQueryBuilder query = QueryBuilders.boolQuery();
		query.should(QueryBuilders.matchPhrasePrefixQuery("personalDetails.primaryEmail", searchTerm))
				.should(QueryBuilders.matchPhrasePrefixQuery("personalDetails.mobile", searchTerm))
				.should(QueryBuilders.matchPhrasePrefixQuery("personalDetails.firstname", searchTerm))
				.should(QueryBuilders.matchPhrasePrefixQuery("personalDetails.surname", searchTerm));
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().query(query);
		sourceBuilder.fetchSource(includeFields, new String[] {});
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices(connectionProperties.getEsProfileIndex());
		searchRequest.types(connectionProperties.getEsProfileIndexType());
		searchRequest.source(sourceBuilder);
		SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
		for (SearchHit hit : searchResponse.getHits()) {
			Map<String, Object> searObjectMap = hit.getSourceAsMap();
			Map<String, Object> personalDetails = (Map<String, Object>) searObjectMap.get("personalDetails");
			Map<String, Object> employmentDetails = (Map<String, Object>) searObjectMap.get("employmentDetails");
			depName = "";
			if (!CollectionUtils.isEmpty(employmentDetails)) {
				depName = StringUtils.isEmpty(employmentDetails.get("departmentName")) ? ""
						: (String) employmentDetails.get("departmentName");
			}
			result = new HashMap<>();
			result.put("first_name", personalDetails.get("firstname"));
			result.put("last_name", personalDetails.get("surname"));
			if (!ObjectUtils.isEmpty(personalDetails.get("primaryEmail")))
				result.put("email", personalDetails.get("primaryEmail"));
			result.put("wid", searObjectMap.get("id"));
			result.put("department_name", depName);
			result.put("rank", hit.getScore());
			resultArray.add(result);
		}
		return resultArray;
	}
}
