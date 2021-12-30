package org.sunbird.cb.hubservices.profile.handler;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/user")
public class UserProfileController {

	@Autowired
	private ProfileUtils profileUtils;

	@Autowired
	IProfileRequestHandler profileRequestHandler;

	@PostMapping("/create/profile")
	public ResponseEntity createProfile(@RequestParam String userId, @RequestBody Map<String, Object> request) {

		RegistryRequest registryRequest = profileRequestHandler.createRequest(userId, request);
		return profileUtils.getResponseEntity(ProfileUtils.URL.CREATE.getValue(), registryRequest);
	}

	@PostMapping("/update/profile")
	public ResponseEntity updateProfile(@RequestParam String userId, @RequestBody Map<String, Object> request) {

		RegistryRequest registryRequest = profileRequestHandler.updateRequest(userId, request);
		return profileUtils.getResponseEntity(ProfileUtils.URL.UPDATE.getValue(), registryRequest);
	}

	@PostMapping("/update/workflow/profile")
	public ResponseEntity updateProfileWithWF(@RequestParam String userId,
			@RequestBody List<Map<String, Object>> requests) {
		Map<String, Object> registryRequest = profileRequestHandler.updateRequestWithWF(userId, requests);
		return profileUtils.updateProfile(userId, registryRequest);
	}

	@GetMapping("/search/profile")
	public ResponseEntity searchProfileById(@RequestParam String userId) {

		RegistryRequest registryRequest = profileRequestHandler.searchRequest(userId);
		return profileUtils.getResponseEntity(ProfileUtils.URL.SEARCH.getValue(), registryRequest);
	}

	@PostMapping("/search/profile")
	public ResponseEntity searchProfile(@RequestBody Map request) {
		RegistryRequest registryRequest = profileRequestHandler.searchRequest(request);
		return profileUtils.getResponseEntity(ProfileUtils.URL.SEARCH.getValue(), registryRequest);
	}

	@GetMapping("/get/profile")
	public ResponseEntity getProfile(@RequestParam String osid) {

		RegistryRequest registryRequest = profileRequestHandler.readRequest(osid);
		return profileUtils.getResponseEntity(ProfileUtils.URL.READ.getValue(), registryRequest);

	}

}
