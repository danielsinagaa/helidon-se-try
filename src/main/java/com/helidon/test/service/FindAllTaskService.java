package com.helidon.test.service;

import io.helidon.dbclient.DbClient;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;

import javax.json.JsonObject;
import java.util.List;

public class FindAllTaskService {
    private final DbClient dbClient;

    public FindAllTaskService(DbClient dbClient) {
        this.dbClient = dbClient;
    }

    public void findAll(ServerRequest request, ServerResponse response) {
        List<JsonObject> json = dbClient.execute(exec -> exec.namedQuery("select-all-task"))
                .map(it -> it.as(JsonObject.class)).collectList().await();

        response.send(json);
    }
}
