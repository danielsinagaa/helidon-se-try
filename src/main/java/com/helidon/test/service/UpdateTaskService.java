package com.helidon.test.service;

import io.helidon.dbclient.DbClient;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;

import java.util.logging.Logger;

public class UpdateTaskService {

    private static final Logger LOGGER = Logger.getLogger(UpdateTaskService.class.getName());

    private final DbClient dbClient;

    public UpdateTaskService(DbClient dbClient) {
        this.dbClient = dbClient;
    }

    public void updateFinishedTask(ServerRequest request, ServerResponse response){
        try {
            int id = Integer.parseInt(request.path().param("id"));
            dbClient.execute(exec -> exec
                    .createNamedDelete("update-finished-task")
                    .addParam("id", id)
                    .execute())
                    .thenAccept(count -> response.send("Task now FINISHED\n WAITING TO VERIFIED"))
                    .exceptionally(throwable -> ServiceHandler.sendError(throwable, response, LOGGER));
        } catch (NumberFormatException ex) {
            ServiceHandler.sendError(ex, response, LOGGER);
        }
    }

    public void updateVerifiedTask(ServerRequest request, ServerResponse response){
        try {
            int id = Integer.parseInt(request.path().param("id"));
            dbClient.execute(exec -> exec
                    .createNamedDelete("update-verified-task")
                    .addParam("id", id)
                    .execute())
                    .thenAccept(count -> response.send("Task now already VERIFIED and FINISHED\n"))
                    .exceptionally(throwable -> ServiceHandler.sendError(throwable, response, LOGGER));
        } catch (NumberFormatException ex) {
            ServiceHandler.sendError(ex, response, LOGGER);
        }
    }
}
