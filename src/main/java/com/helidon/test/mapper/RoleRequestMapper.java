package com.helidon.test.mapper;

import com.helidon.test.entity.model.RoleRequest;
import io.helidon.dbclient.DbColumn;
import io.helidon.dbclient.DbMapper;
import io.helidon.dbclient.DbRow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoleRequestMapper implements DbMapper<RoleRequest> {
    @Override
    public RoleRequest read(DbRow dbRow) {
        DbColumn name = dbRow.column("name");

        return new RoleRequest(name.as(String.class));
    }

    @Override
    public Map<String, ?> toNamedParameters(RoleRequest role) {
        Map<String, Object> map = new HashMap<>(1);
        map.put("name", role.getName());
        return map;
    }

    @Override
    public List<?> toIndexedParameters(RoleRequest role) {
        List<Object> list = new ArrayList<>(1);
        list.add(role.getName());
        return list;
    }
}