package org.sunbird.cb.hubservices.network.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.cb.hubservices.model.ConnectionRequest;
import org.sunbird.cb.hubservices.model.Response;
import org.sunbird.cb.hubservices.serviceimpl.ConnectionService;
import org.sunbird.cb.hubservices.util.Constants;

import java.util.Date;

@RestController
@RequestMapping(Constants.CONNECTIONS)
public class UserConnectionCrudController {

	@Autowired
	private ConnectionService connectionService;

	@PostMapping(Constants.ADD)
	public ResponseEntity<Response> add(@RequestBody ConnectionRequest request) {
		request.setStatus(Constants.Status.PENDING);
		request.setCreatedAt(new Date().toString());
		Response response = connectionService.upsert(request, Constants.ADD_OPERATION);
		if (response != null) {
			return new ResponseEntity<>(response, (HttpStatus) response.get(Constants.STATUS));
		}
		response.put(Constants.ResponseStatus.STATUS, HttpStatus.BAD_REQUEST);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	@PostMapping(Constants.UPDATE)
	public ResponseEntity<Response> update(@RequestBody ConnectionRequest request) {
		request.setUpdatedAt(new Date().toString());
		request.setUpdatedAt(new Date().toString());
		Response response = connectionService.upsert(request, Constants.UPDATE_OPERATION);
		if(response != null){
			return new ResponseEntity<>(response, (HttpStatus) response.get(Constants.STATUS));
		}
		response.put(Constants.ResponseStatus.STATUS, HttpStatus.BAD_REQUEST);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
}