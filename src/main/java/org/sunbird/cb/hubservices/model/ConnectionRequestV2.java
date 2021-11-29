package org.sunbird.cb.hubservices.model;

public class ConnectionRequestV2 {

	private String userIdFrom;
	private String userIdTo;
	private String userNameFrom;
	private String userDepartmentFrom;
	private String userNameTo;
	private String userDepartmentTo;
	private String status;

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

	public String getUserNameFrom() {
		return userNameFrom;
	}

	public void setUserNameFrom(String userNameFrom) {
		this.userNameFrom = userNameFrom;
	}

	public String getUserDepartmentFrom() {
		return userDepartmentFrom;
	}

	public void setUserDepartmentFrom(String userDepartmentFrom) {
		this.userDepartmentFrom = userDepartmentFrom;
	}

	public String getUserNameTo() {
		return userNameTo;
	}

	public void setUserNameTo(String userNameTo) {
		this.userNameTo = userNameTo;
	}

	public String getUserDepartmentTo() {
		return userDepartmentTo;
	}

	public void setUserDepartmentTo(String userDepartmentTo) {
		this.userDepartmentTo = userDepartmentTo;
	}


}
