package com.helidon.test.service;

import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import javax.json.JsonObject;
import java.util.List;
import static com.helidon.test.Main.employeeDB;

public class FindAllEmployeeService {

    public static void findAll(ServerRequest request, ServerResponse response) {
        List<JsonObject> json = employeeDB.execute(exec -> exec.namedQuery("select-all-employee"))
                .map(it -> it.as(JsonObject.class)).collectList().await();

        response.send(json);
    }
}
