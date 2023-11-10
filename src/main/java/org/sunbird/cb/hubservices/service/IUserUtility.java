package org.sunbird.cb.hubservices.service;

import org.sunbird.cb.hubservices.model.MultiSearch;

import java.util.List;
import java.util.Map;

public interface IUserUtility {

    Map<String, Object> getUserInfoFromRedish(MultiSearch multiSearch, String[] sourceField, List<String> connectionIdsToExclude);
}
