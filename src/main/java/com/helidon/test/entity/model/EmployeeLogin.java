package com.helidon.test.entity.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class EmployeeLogin {
    private String username;
    private String password;
    private String role;
}
