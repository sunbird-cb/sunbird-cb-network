package org.sunbird.cb.hubservices.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

public class NodeV2 {

    private String identifier;

    public NodeV2(String identifier){
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

}
