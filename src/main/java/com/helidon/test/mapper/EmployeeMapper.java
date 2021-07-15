package com.helidon.test.mapper;

import com.helidon.test.entity.Employee;
import io.helidon.dbclient.DbColumn;
import io.helidon.dbclient.DbMapper;
import io.helidon.dbclient.DbRow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeMapper implements DbMapper<Employee> {
    @Override
    public Employee read(DbRow dbRow) {
        DbColumn id = dbRow.column("id");
        DbColumn username = dbRow.column("username");
        DbColumn password = dbRow.column("password");
        DbColumn roleId = dbRow.column("role_id");

        return new Employee(id.as(Integer.class), username.as(String.class), password.as(String.class), roleId.as(Integer.class));
    }

    @Override
    public Map<String, ?> toNamedParameters(Employee employee) {
        Map<String, Object> map = new HashMap<>(4);
        map.put("id", employee.getId());
        map.put("username", employee.getUsername());
        map.put("role_id", employee.getRoleId());
        return map;
    }

    @Override
    public List<?> toIndexedParameters(Employee employee) {
        List<Object> list = new ArrayList<>(4);
        list.add(employee.getId());
        list.add(employee.getUsername());
        list.add(employee.getRoleId());
        return list;
    }
}
