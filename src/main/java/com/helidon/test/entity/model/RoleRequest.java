package com.helidon.test.entity.model;

public class RoleRequest {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RoleRequest(String name) {
        this.name = name;
    }

    public RoleRequest() {
    }
}
