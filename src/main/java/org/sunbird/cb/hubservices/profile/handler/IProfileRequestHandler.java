package org.sunbird.cb.hubservices.profile.handler;

import java.util.List;
import java.util.Map;

public interface IProfileRequestHandler {

	public RegistryRequest createRequest(String uuid, Map<String, Object> request);

	public RegistryRequest updateRequest(String uuid, Map<String, Object> request);

	public Map<String, Object> updateRequestWithWF(String uuid, List<Map<String, Object>> requests);

	public RegistryRequest searchRequest(String uuid);

	public RegistryRequest readRequest(String id);

	public RegistryRequest searchRequest(Map<?, ?> params);
}
