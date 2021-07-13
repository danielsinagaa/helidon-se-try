package com.helidon.test.entity.model;

import com.helidon.test.entity.Employee;
import com.helidon.test.entity.Role;

import java.util.List;

public class Entity {
    private Role role;
    private List<Role> allRoles;
    private Employee employee;
    private List<Employee> allEmployees;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public List<Employee> getAllEmployees() {
        return allEmployees;
    }

    public void setAllEmployees(List<Employee> allEmployees) {
        this.allEmployees = allEmployees;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<Role> getAllRoles() {
        return allRoles;
    }

    public void setAllRoles(List<Role> allRoles) {
        this.allRoles = allRoles;
    }
}
