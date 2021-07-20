package com.helidon.test.service;

import io.helidon.dbclient.DbClient;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;

import javax.json.JsonObject;
import java.util.List;
import java.util.logging.Logger;

public class FindAllEmployeeService {
    private static final Logger LOGGER = Logger.getLogger(TaskService.class.getName());

    private final DbClient dbClient;

    public FindAllEmployeeService(DbClient dbClient) {
        this.dbClient = dbClient;
    }

    public void findAll(ServerRequest request, ServerResponse response) {
        List<JsonObject> json = dbClient.execute(exec -> exec.namedQuery("select-all-employee"))
                .map(it -> it.as(JsonObject.class)).collectList().await();

        response.send(json);
    }
}
