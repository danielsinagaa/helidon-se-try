package com.helidon.test.mapper;

import com.helidon.test.entity.Role;
import io.helidon.dbclient.DbColumn;
import io.helidon.dbclient.DbMapper;
import io.helidon.dbclient.DbRow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoleMapper implements DbMapper<Role> {
    @Override
    public Role read(DbRow dbRow) {
        DbColumn id = dbRow.column("id");
        DbColumn name = dbRow.column("name");

        return new Role(id.as(Integer.class), name.as(String.class));
    }

    @Override
    public Map<String, ?> toNamedParameters(Role role) {
        Map<String, Object> map = new HashMap<>(2);
        map.put("id", role.getId());
        map.put("name", role.getName());
        return map;
    }

    @Override
    public List<?> toIndexedParameters(Role role) {
        List<Object> list = new ArrayList<>(2);
        list.add(role.getId());
        list.add(role.getName());
        return list;
    }
}
