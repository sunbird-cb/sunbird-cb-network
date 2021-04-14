package org.sunbird.cb.hubservices.model;

import java.util.Date;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConnectionRequest {

	@NotNull
	@JsonProperty("userIdFrom")
	private String userId;

	@NotNull
	@JsonProperty("userIdTo")
	private String connectionId;

	// name - department = label
	@JsonProperty("userNameFrom")
	private String userName;

	@JsonProperty("userDepartmentFrom")
	private String userDepartment;

	public Integer getUserNodeId() {
		return userNodeId;
	}

	public void setUserNodeId(Integer userNodeId) {
		this.userNodeId = userNodeId;
	}

	public Integer getConnectionNodeId() {
		return connectionNodeId;
	}

	public void setConnectionNodeId(Integer connectionNodeId) {
		this.connectionNodeId = connectionNodeId;
	}

	@JsonProperty("userNameTo")
	private String connectionName;
	@JsonProperty("userDepartmentTo")
	private String connectionDepartment;
	private String status;
	private String type;
	private Date endDate;
	private Integer userNodeId;
	private Integer connectionNodeId;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserDepartment() {
		return userDepartment;
	}

	public void setUserDepartment(String userDepartment) {
		this.userDepartment = userDepartment;
	}

	public String getConnectionName() {
		return connectionName;
	}

	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

	public String getConnectionDepartment() {
		return connectionDepartment;
	}

	public void setConnectionDepartment(String connectionDepartment) {
		this.connectionDepartment = connectionDepartment;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
