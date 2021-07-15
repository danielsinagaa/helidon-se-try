package com.helidon.test.entity.model;

import lombok.Data;

@Data
public class EmployeeLogin {

    private String username;
    private String password;
    private String role;
}
