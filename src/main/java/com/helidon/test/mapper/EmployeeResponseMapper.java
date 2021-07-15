package com.helidon.test.mapper;

import com.helidon.test.entity.model.EmployeeResponse;
import io.helidon.dbclient.DbColumn;
import io.helidon.dbclient.DbMapper;
import io.helidon.dbclient.DbRow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeResponseMapper implements DbMapper<EmployeeResponse> {
    @Override
    public EmployeeResponse read(DbRow dbRow) {
        DbColumn id = dbRow.column("id");
        DbColumn username = dbRow.column("username");
        DbColumn role = dbRow.column("role");

        return new EmployeeResponse(id.as(Integer.class), username.as(String.class), role.as(String.class));
    }

    @Override
    public Map<String, ?> toNamedParameters(EmployeeResponse employee) {
        Map<String, Object> map = new HashMap<>(3);
        map.put("id", employee.getId());
        map.put("username", employee.getUsername());
        map.put("role", employee.getRole());
        return map;
    }

    @Override
    public List<?> toIndexedParameters(EmployeeResponse employee) {
        List<Object> list = new ArrayList<>(3);
        list.add(employee.getId());
        list.add(employee.getUsername());
        list.add(employee.getRole());
        return list;
    }
}
