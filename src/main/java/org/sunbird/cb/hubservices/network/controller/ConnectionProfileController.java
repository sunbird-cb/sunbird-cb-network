package org.sunbird.cb.hubservices.network.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.cb.hubservices.model.MultiSearch;
import org.sunbird.cb.hubservices.model.Response;
import org.sunbird.cb.hubservices.serviceimpl.ProfileService;
import org.sunbird.cb.hubservices.util.Constants;

@RestController
@RequestMapping(Constants.CONNECTIONS_PROFILE)
public class ConnectionProfileController {

	@Autowired
	private ProfileService profileService;

	@PostMapping(Constants.FIND_RECOMMENDED)
	public ResponseEntity<Response> findRecommendedConnections(@RequestHeader String userId,
			@RequestParam(required = false, name = "includeSources") String[] includeSources,
			@RequestBody MultiSearch multiSearch) {

		Response response = profileService.multiSearchProfiles(userId, multiSearch, includeSources);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping(Constants.FIND_SUGGESTS)
	public ResponseEntity<Response> findSuggests(@RequestHeader(required = false) String org, @RequestHeader String userId,
			@RequestParam(defaultValue = "50", required = false, name = "pageSize") int pageSize,
			@RequestParam(defaultValue = "0", required = false, name = "pageNo") int pageNo) {

		Response response = profileService.findCommonProfileV2(userId, pageNo, pageSize);
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@GetMapping(Constants.FETCH_REQUESTED)
	public ResponseEntity<Response> findRequests(@RequestHeader(required = false) String org, @RequestHeader String userId,
			@RequestParam(defaultValue = "50", required = false, name = "pageSize") int pageSize,
			@RequestParam(defaultValue = "0", required = false, name = "pageNo") int pageNo) {

		Response response = profileService.findProfileRequestedV2(userId, pageNo, pageSize, Constants.DIRECTION.OUT);
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@GetMapping(Constants.FETCH_REQUESTS_RECEIVED)
	public ResponseEntity<Response> findRequestsRecieved(@RequestHeader(required = false) String org, @RequestHeader String userId,
			@RequestParam(defaultValue = "50", required = false, name = "pageSize") int pageSize,
			@RequestParam(defaultValue = "0", required = false, name = "pageNo") int pageNo) {

		Response response = profileService.findProfileRequestedV2(userId, pageNo, pageSize, Constants.DIRECTION.IN);
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@GetMapping(Constants.FETCH_ESTABLISHED)
	public ResponseEntity<Response> findEstablished(@RequestHeader(required = false) String org, @RequestHeader String userId,
			@RequestParam(defaultValue = "50", required = false, name = "pageSize") int pageSize,
			@RequestParam(defaultValue = "0", required = false, name = "pageNo") int pageNo) {

		Response response = profileService.findProfilesV2(userId, pageNo, pageSize);
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

}
