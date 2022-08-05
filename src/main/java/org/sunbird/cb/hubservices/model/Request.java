package org.sunbird.cb.hubservices.model;

import java.util.HashMap;
import java.util.Map;

public class Request {

    private String id;

    private Map<String, Object> request = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object> getRequest() {
        return request;
    }

    public void setRequest(Map<String, Object> request) {
        this.request = request;
    }
}