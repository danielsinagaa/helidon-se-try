package com.helidon.test.entity;

import io.helidon.common.Reflected;
import lombok.Data;

@Reflected @Data
public class Employee {
    private int id;
    private String username;
    private String password;
    private int roleId;

    public Employee(int id, String username, String password, int roleId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.roleId = roleId;
    }
}
