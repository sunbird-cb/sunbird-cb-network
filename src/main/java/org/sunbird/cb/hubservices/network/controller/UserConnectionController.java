package org.sunbird.cb.hubservices.network.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.cb.hubservices.model.Response;
import org.sunbird.cb.hubservices.serviceimpl.ConnectionService;

@RestController
@RequestMapping("/connections")
public class UserConnectionController {

	@Autowired
	private ConnectionService connectionService;

	@PostMapping("/find/recommended")
	public ResponseEntity<Response> findRecommendedConnection(@RequestHeader String rootOrg,
			@RequestHeader(required = false) String org,
			@RequestParam(defaultValue = "5", required = false, name = "pageSize") int pageSize,
			@RequestParam(defaultValue = "0", required = false, name = "pageNo") int pageNo,
			@RequestBody Map<String, Object> request) {

		Response response = null;
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@PostMapping("/find/common")
	public ResponseEntity<Response> findCommonConnections(@RequestHeader String rootOrg,
			@RequestHeader(required = false) String org, @RequestHeader String userId,
			@RequestParam(defaultValue = "5", required = false, name = "pageSize") int pageSize,
			@RequestParam(defaultValue = "0", required = false, name = "pageNo") int pageNo) {

		Response response = connectionService.findSuggestedConnections(rootOrg, userId, pageNo, pageSize);
		return new ResponseEntity<>(response, HttpStatus.OK);

	}
}
