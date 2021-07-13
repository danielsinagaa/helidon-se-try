package com.helidon.test.entity.model;

import lombok.Data;

@Data
public class EmployeeResponse {
    private int id;
    private String username;
    private String role;

    public EmployeeResponse() {
    }

    public EmployeeResponse(int id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }
}
