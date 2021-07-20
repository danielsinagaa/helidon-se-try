package com.helidon.test.mapper;

import com.helidon.test.dto.EmployeeRequest;
import io.helidon.dbclient.DbColumn;
import io.helidon.dbclient.DbMapper;
import io.helidon.dbclient.DbRow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeRequestMapper implements DbMapper<EmployeeRequest> {
    @Override
    public EmployeeRequest read(DbRow dbRow) {
        DbColumn username = dbRow.column("username");
        DbColumn password = dbRow.column("password");
        DbColumn roleId = dbRow.column("role_id");

        return new EmployeeRequest(username.as(String.class), password.as(String.class), roleId.as(Integer.class));
    }

    @Override
    public Map<String, ?> toNamedParameters(EmployeeRequest employeeRequest) {
        Map<String, Object> map = new HashMap<>(3);
        map.put("username", employeeRequest.getUsername());
        map.put("password", employeeRequest.getPassword());
        map.put("role_id", employeeRequest.getRoleId());
        return map;
    }

    @Override
    public List<?> toIndexedParameters(EmployeeRequest employeeRequest) {
        List<Object> list = new ArrayList<>(3);
        list.add(employeeRequest.getUsername());
        list.add(employeeRequest.getPassword());
        list.add(employeeRequest.getRoleId());
        return list;
    }
}
