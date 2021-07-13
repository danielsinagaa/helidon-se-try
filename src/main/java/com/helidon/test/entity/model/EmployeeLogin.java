package com.helidon.test.entity.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class EmployeeLogin {

    private String username;
    private String password;
    private String role;
}
