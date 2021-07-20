package com.helidon.test.mapperProvider;

import com.helidon.test.dto.EmployeeRequest;
import com.helidon.test.dto.RoleRequest;
import com.helidon.test.mapper.*;
import io.helidon.dbclient.DbMapper;
import io.helidon.dbclient.spi.DbMapperProvider;
import javax.annotation.Priority;
import java.util.Optional;

@Priority(1000)
public class MapperProvider implements DbMapperProvider {
    private static final RoleRequestMapper ROLE_REQUEST_MAPPER = new RoleRequestMapper();
    private static final EmployeeRequestMapper EMPLOYEE_REQUEST_MAPPER = new EmployeeRequestMapper();

    @Override
    public <T> Optional<DbMapper<T>> mapper(Class<T> type) {
        if (type.equals(RoleRequest.class)){
            return Optional.of((DbMapper<T>) ROLE_REQUEST_MAPPER);
        }
        else if (type.equals(EmployeeRequest.class)){
            return Optional.of((DbMapper<T>) EMPLOYEE_REQUEST_MAPPER);
        }
        return Optional.empty();
    }
}
