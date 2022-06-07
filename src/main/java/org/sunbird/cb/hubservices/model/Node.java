package org.sunbird.cb.hubservices.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Node {

	private String id;
	private String name;
	private String department;
	private String createdAt;
	private String updatedAt;

	public Node(String id) {
		this.id = id;
	}

	public Node(String id, String department){
		this.id = id;
		this.department = department;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
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