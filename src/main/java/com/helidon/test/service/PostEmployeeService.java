package com.helidon.test.service;

import com.helidon.test.dto.EmployeeRequest;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import java.util.logging.Logger;
import static com.helidon.test.Main.employeeDB;

public class PostEmployeeService {
    private static final Logger LOGGER = Logger.getLogger(PostEmployeeService.class.getName());
    public static void execute(ServerRequest request, ServerResponse response, EmployeeRequest employeeRequest){
        employeeDB.execute(exec -> exec
                .createNamedInsert("insert-employee")
                .indexedParam(employeeRequest)
                .execute())
                .thenAccept( count -> {
                    response.send("Inserted: " + count + " values\n");

                })
                .exceptionally(throwable -> ServiceHandler.sendError(throwable, response, LOGGER));
    }
}
