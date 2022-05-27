package org.sunbird.cb.hubservices.model;

public class Node {

	private String id;
	private String name;
	private String departmentName;
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public Node(String id) {
		this.id = id;
	}

	public Node(String id, String name, String departmentName) {
		this.id = id;
		this.name = name;
		this.departmentName = departmentName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
