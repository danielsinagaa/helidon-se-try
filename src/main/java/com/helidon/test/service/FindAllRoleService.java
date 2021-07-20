package com.helidon.test.service;

import com.google.gson.Gson;
import io.helidon.dbclient.DbClient;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import javax.json.JsonObject;
import java.util.List;

public class FindAllRoleService {
    private final DbClient dbClient;

    public FindAllRoleService(DbClient dbClient) {
        this.dbClient = dbClient;
    }

    public void findAll(ServerRequest request, ServerResponse response) {
        List<JsonObject> json = dbClient.execute(exec -> exec.namedQuery("select-all-role"))
                .map(it -> it.as(JsonObject.class)).collectList().await();

        response.send(json);
    }
}
