package org.sunbird.cb.hubservices.cache.service;


import org.sunbird.cb.hubservices.model.SBApiResponse;

public interface RedisCacheService {

    public SBApiResponse deleteCache() throws Exception;

    public SBApiResponse getKeys() throws Exception;

    public SBApiResponse getKeysAndValues() throws Exception;

}
