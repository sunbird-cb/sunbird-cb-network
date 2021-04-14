
package org.sunbird.cb.hubservices.service;

import org.sunbird.cb.hubservices.model.MultiSearch;
import org.sunbird.cb.hubservices.model.Response;
import org.sunbird.cb.hubservices.util.Constants;

public interface IProfileService {

	/**
	 * Find related profile from existing connections
	 * 
	 * @param userId
	 * @param offset
	 * @param limit
	 * @return
	 */
	Response findCommonProfile(String rootOrg, String userId, int offset, int limit);

	/**
	 * Find profile for which connections are established Accepted connection
	 * 
	 * @param userId
	 * @return
	 */
	Response findProfiles(String rootOrg, String userId, int offset, int limit);

	/**
	 * Find profiles for which is not established/pending for approval
	 * 
	 * @param userId
	 * @return
	 */
	Response findProfileRequested(String rootOrg, String userId, int offset, int limit, Constants.DIRECTION direction);

	Response multiSearchProfiles(String rootOrg, String userId, MultiSearch multiSearchRequest, String[] sourceFields);

}
