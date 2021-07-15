package com.helidon.test.entity.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class EmployeeResponse {
    private int id;
    private String username;
    private String role;
}
