package org.sunbird.cb.hubservices.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class NotificationTemplate implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private Map<String, String> params = new HashMap<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}
}
