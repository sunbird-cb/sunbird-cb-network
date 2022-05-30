package org.sunbird.cb.hubservices.network.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.cb.hubservices.model.MultiSearch;
import org.sunbird.cb.hubservices.model.Response;
import org.sunbird.cb.hubservices.serviceimpl.ProfileService;
import org.sunbird.cb.hubservices.util.Constants;

import java.io.IOException;

@RestController
@RequestMapping(Constants.CONNECTIONS_PROFILE)
public class ConnectionProfileController {

	@Autowired
	private ProfileService profileService;

	@PostMapping(Constants.FIND_RECOMMENDED)
	public ResponseEntity<Response> findRecommendedConnections(@RequestHeader String userId,
			@RequestParam(required = false, name = Constants.INCLUDE_SOURCES) String[] includeSources,
			@RequestBody MultiSearch multiSearch) {

		Response response = profileService.multiSearchProfiles(userId, multiSearch, includeSources);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping(Constants.FIND_SUGGESTS)
	public ResponseEntity<Response> findSuggests(@RequestHeader String rootOrg, @RequestHeader String userId,
			@RequestParam(defaultValue = "50", required = false, name = Constants.PAGE_SIZE) int pageSize,
			@RequestParam(defaultValue = "0", required = false, name = Constants.PAGE_NO) int pageNo) throws IOException {

		Response response = profileService.findCommonProfile(rootOrg, userId, pageNo, pageSize);
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@GetMapping(Constants.FETCH_REQUESTED)
	public ResponseEntity<Response> findRequests(@RequestHeader(required = true) String rootOrg,
												 @RequestHeader String userId,
												 @RequestParam(defaultValue = "50", required = false, name = Constants.PAGE_SIZE) int pageSize,
												 @RequestParam(defaultValue = "0", required = false, name = Constants.PAGE_NO) int pageNo) {

		Response response = profileService.findProfileRequested(rootOrg, userId, pageNo, pageSize,
				Constants.DIRECTION.OUT);
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@GetMapping(Constants.FETCH_REQUESTS_RECEIVED)
	public ResponseEntity<Response> findRequestsReceived(@RequestHeader(required = true) String rootOrg, @RequestHeader String userId,
														 @RequestParam(defaultValue = "50", required = false, name = Constants.PAGE_SIZE) int pageSize,
														 @RequestParam(defaultValue = "0", required = false, name = Constants.PAGE_NO) int pageNo) {

		Response response = profileService.findProfileRequested(rootOrg, userId, pageNo, pageSize,
				Constants.DIRECTION.IN);
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@GetMapping(Constants.FETCH_ESTABLISHED)
	public ResponseEntity<Response> findEstablished(@RequestHeader(required = true) String rootOrg,
													@RequestHeader String userId,
													@RequestParam(defaultValue = "50", required = false, name = Constants.PAGE_SIZE) int pageSize,
													@RequestParam(defaultValue = "0", required = false, name = Constants.PAGE_NO) int pageNo) {

		Response response = profileService.findProfiles(rootOrg, userId, pageNo, pageSize);
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

}
