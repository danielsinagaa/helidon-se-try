package com.helidon.test.service;

import com.helidon.test.entity.model.EmployeeRequest;
import io.helidon.dbclient.DbClient;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;

import javax.json.JsonObject;
import java.util.logging.Logger;

public class TaskService implements Service {
    private final DbClient dbClient;

    private static final Logger LOGGER = Logger.getLogger(TaskService.class.getName());

    public TaskService(DbClient dbClient) {
        this.dbClient = dbClient;
    }

    @Override
    public void update(Routing.Rules rules) {
        rules
                .get("/", this::findAll)
                .get("/{id}", this::findById)
                .get("/new/{task}", this::add)
                .get("/verified/{id}", this::updateVerifiedTask)
                .get("/finished/{id}", this::updateFinishedTask)
                .delete("/{id}", this::deleteById);
    }

    private void findAll(ServerRequest request, ServerResponse response) {
        response.send(dbClient.execute(exec -> exec.namedQuery("select-all-task"))
                .map(it -> it.as(JsonObject.class)), JsonObject.class);
    }

    private void findById(ServerRequest request, ServerResponse response) {
        try {
            int id = Integer.parseInt(request.path().param("id"));
            dbClient.execute(exec -> exec
                    .createNamedGet("select-task-by-id")
                    .addParam("id", id)
                    .execute())
                    .thenAccept(maybeRow -> maybeRow
                            .ifPresentOrElse(
                                    row -> ServiceHandler.sendRow(row, response),
                                    () -> ServiceHandler.sendNotFound(response, "Task " + id + " not found")))
                    .exceptionally(throwable -> ServiceHandler.sendError(throwable, response, LOGGER));
        } catch (NumberFormatException ex) {
            ServiceHandler.sendError(ex, response, LOGGER);
        }
    }

    private void add(ServerRequest request, ServerResponse response){

        String task = request.path().param("task");

        dbClient.execute(exec -> exec
                .createNamedInsert("insert-new-task")
                .addParam("task", task)
                .execute())
                .thenAccept( count -> response.send("Inserted: " + count + " values\n"))
                .exceptionally(throwable -> ServiceHandler.sendError(throwable, response, LOGGER));
    }

    private void updateFinishedTask(ServerRequest request, ServerResponse response){

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

    private void updateVerifiedTask(ServerRequest request, ServerResponse response){

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

    private void deleteById(ServerRequest request, ServerResponse response) {
        try {
            int id = Integer.parseInt(request.path().param("id"));
            dbClient.execute(exec -> exec
                    .createNamedDelete("delete-task-by-id")
                    .addParam("id", id)
                    .execute())
                    .thenAccept(count -> response.send("Deleted: " + count + " values\n"))
                    .exceptionally(throwable -> ServiceHandler.sendError(throwable, response, LOGGER));
        } catch (NumberFormatException ex) {
            ServiceHandler.sendError(ex, response, LOGGER);
        }
    }
}
