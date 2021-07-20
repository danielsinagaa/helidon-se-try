package com.helidon.test.service;

import io.helidon.dbclient.DbClient;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;

import java.util.logging.Logger;

public class PostTaskService {
    private static final Logger LOGGER = Logger.getLogger(PostTaskService.class.getName());
    private final DbClient dbClient;

    public PostTaskService(DbClient dbClient) {
        this.dbClient = dbClient;
    }

    public void post(ServerRequest request, ServerResponse response){

        String task = request.path().param("task");

        dbClient.execute(exec -> exec
                .createNamedInsert("insert-new-task")
                .addParam("task", task)
                .execute())
                .thenAccept( count -> response.send("Inserted: " + count + " values\n"))
                .exceptionally(throwable -> ServiceHandler.sendError(throwable, response, LOGGER));
    }
}
