package org.sunbird.cb.hubservices.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Node {

    private String identifier;
    private String name;
    private String department;
    private Date createdAt;
    private Date updatedAt;


    public Node(String identifier, String name, String department){
        this.identifier = identifier;
        this.name = name;
        this.department = department;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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

    public Date getCreatedAt() {
        return createdAt;
    }

    @JsonIgnore
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    //@JsonIgnore
    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
