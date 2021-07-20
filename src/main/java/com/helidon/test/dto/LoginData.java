package com.helidon.test.dto;

import lombok.Data;

@Data
public class LoginData {
    private String username;
    private String role;
    private String jwt;

    public LoginData(EmployeeLogin login, String jwt) {
        this.username = login.getUsername();
        this.role = login.getRole();
        this.jwt = jwt;
    }
}
