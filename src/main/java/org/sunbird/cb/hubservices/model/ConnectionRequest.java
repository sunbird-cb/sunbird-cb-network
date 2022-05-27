package org.sunbird.cb.hubservices.model;

public class ConnectionRequest {

	private String userIdFrom;
	private String userIdTo;

	private String userNameFrom;
	private String userNameTo;
	private String userDepartmentFrom;
	private String userDepartmentTo;
	private String status;
	private String createdAt;
	private String updatedAt;

	public String getUserNameFrom() {
		return userNameFrom;
	}

	public void setUserNameFrom(String userNameFrom) {
		this.userNameFrom = userNameFrom;
	}

	public String getUserNameTo() {
		return userNameTo;
	}

	public void setUserNameTo(String userNameTo) {
		this.userNameTo = userNameTo;
	}

	public String getUserDepartmentFrom() {
		return userDepartmentFrom;
	}

	public void setUserDepartmentFrom(String userDepartmentFrom) {
		this.userDepartmentFrom = userDepartmentFrom;
	}

	public String getUserDepartmentTo() {
		return userDepartmentTo;
	}

	public void setUserDepartmentTo(String userDepartmentTo) {
		this.userDepartmentTo = userDepartmentTo;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getUserIdFrom() {
		return userIdFrom;
	}

	public void setUserIdFrom(String userIdFrom) {
		this.userIdFrom = userIdFrom;
	}

	public String getUserIdTo() {
		return userIdTo;
	}

	public void setUserIdTo(String userIdTo) {
		this.userIdTo = userIdTo;
	}

}
