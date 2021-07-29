package com.helidon.test.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String username;
    private String role;
    private String jwt;

    public LoginResponse(EmployeeLogin login, String jwt) {
        this.username = login.getUsername();
        this.role = login.getRole();
        this.jwt = jwt;
    }
}
