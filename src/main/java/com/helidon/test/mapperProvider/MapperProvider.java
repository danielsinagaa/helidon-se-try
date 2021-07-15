package com.helidon.test.mapperProvider;

import com.helidon.test.entity.Employee;
import com.helidon.test.entity.Role;
import com.helidon.test.entity.model.EmployeeRequest;
import com.helidon.test.entity.model.EmployeeResponse;
import com.helidon.test.entity.model.RoleRequest;
import com.helidon.test.mapper.*;
import io.helidon.dbclient.DbMapper;
import io.helidon.dbclient.spi.DbMapperProvider;
import javax.annotation.Priority;
import java.util.Optional;

@Priority(1000)
public class MapperProvider implements DbMapperProvider {
    private static final RoleMapper ROLE_MAPPER = new RoleMapper();
    private static final RoleRequestMapper ROLE_REQUEST_MAPPER = new RoleRequestMapper();
    private static final EmployeeMapper EMPLOYEE_MAPPER = new EmployeeMapper();
    private static final EmployeeRequestMapper EMPLOYEE_REQUEST_MAPPER = new EmployeeRequestMapper();
    private static final EmployeeResponseMapper EMPLOYEE_RESPONSE_MAPPER = new EmployeeResponseMapper();

    @Override
    public <T> Optional<DbMapper<T>> mapper(Class<T> type) {
        if (type.equals(Role.class)){
            return Optional.of((DbMapper<T>) ROLE_MAPPER);
        }
        else if (type.equals(RoleRequest.class)){
            return Optional.of((DbMapper<T>) ROLE_REQUEST_MAPPER);
        }
        else if (type.equals(Employee.class)){
            return Optional.of((DbMapper<T>) EMPLOYEE_MAPPER);
        }
        else if (type.equals(EmployeeRequest.class)){
            return Optional.of((DbMapper<T>) EMPLOYEE_REQUEST_MAPPER);
        }
        else if (type.equals(EmployeeResponse.class)){
            return Optional.of((DbMapper<T>) EMPLOYEE_RESPONSE_MAPPER);
        }
        return Optional.empty();
    }
}
