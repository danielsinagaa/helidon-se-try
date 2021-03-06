package com.helidon.test.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class EmployeeRequest {
    private String username;
    private String password;
    private int roleId;
}
