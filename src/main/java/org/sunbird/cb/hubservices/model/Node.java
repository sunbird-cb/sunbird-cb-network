package org.sunbird.cb.hubservices.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Node {

	private String id;
	private String createdAt;
	private String updatedAt;

	public Node(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	@JsonIgnore
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	//@JsonIgnore
	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

}
