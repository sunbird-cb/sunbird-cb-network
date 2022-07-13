package org.sunbird.cb.hubservices.network.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.cb.hubservices.model.ConnectionRequest;
import org.sunbird.cb.hubservices.model.Node;
import org.sunbird.cb.hubservices.model.Response;
import org.sunbird.cb.hubservices.serviceimpl.ConnectionService;
import org.sunbird.cb.hubservices.util.Constants;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping(Constants.CONNECTIONS)
public class UserConnectionCrudController {

	@Autowired
	private ConnectionService connectionService;

	@PostMapping(Constants.ADD)
	public ResponseEntity<Response> add(@RequestBody ConnectionRequest request) {
		request.setStatus(Constants.Status.PENDING);
		request.setCreatedAt(new Date().toString());
		Response response = new Response();
		if(connectionService.validateRequest(request)) {
			Node from = new Node(request.getUserIdFrom());
			Node to = new Node(request.getUserIdTo());
			Map<String, String> relP = connectionService.setRelationshipProperties(request, from, to);
			response = connectionService.upsert(from,to,relP);
			return new ResponseEntity<>(response, (HttpStatus) response.get(Constants.STATUS));
		}
		response.put(Constants.ResponseStatus.STATUS, HttpStatus.BAD_REQUEST);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	@PostMapping(Constants.UPDATE)
	public ResponseEntity<Response> update(@RequestBody ConnectionRequest request) {
		request.setUpdatedAt(new Date().toString());
		Response response = new Response();
		if(connectionService.validateRequest(request)) {
			Node to = new Node(request.getUserIdFrom());
			Node from = new Node(request.getUserIdTo());
			request.setUpdatedAt(new Date().toString());
			Map<String, String> relP = connectionService.setRelationshipProperties(request, from, to);
			response = connectionService.upsert(from,to,relP);
			return new ResponseEntity<>(response, (HttpStatus) response.get(Constants.STATUS));
		}
		response.put(Constants.ResponseStatus.STATUS, HttpStatus.BAD_REQUEST);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
}