package com.helidon.test.service;

import com.helidon.test.entity.model.EmployeeRequest;
import io.helidon.dbclient.DbClient;
import io.helidon.security.integration.webserver.WebSecurity;
import io.helidon.webserver.*;

import javax.json.JsonObject;
import java.util.logging.Logger;

public class EmployeeService implements Service {
    private final DbClient dbClient;

    private static final Logger LOGGER = Logger.getLogger(EmployeeService.class.getName());

    public EmployeeService(DbClient dbClient) {
        this.dbClient = dbClient;
    }

    @Override
    public void update(Routing.Rules rules) {
        rules
            .get("/", WebSecurity.rolesAllowed("master"), this::findAll)
                .get("/{id}", WebSecurity.rolesAllowed("master"), this::findById)
                .post("/", WebSecurity.rolesAllowed("master"), Handler.create(EmployeeRequest.class, this::add))
                .delete("/{id}",WebSecurity.rolesAllowed("master"), this::deleteById);
    }

    private void findAll(ServerRequest request, ServerResponse response) {
        response.send(dbClient.execute(exec -> exec.namedQuery("select-all-employee"))
                .map(it -> it.as(JsonObject.class)), JsonObject.class);
    }

    private void findById(ServerRequest request, ServerResponse response) {
        try {
            int id = Integer.parseInt(request.path().param("id"));
            dbClient.execute(exec -> exec
                    .createNamedGet("select-employee-by-id")
                    .addParam("id", id)
                    .execute())
                    .thenAccept(maybeRow -> maybeRow
                            .ifPresentOrElse(
                                    row -> ServiceHandler.sendRow(row, response),
                                    () -> ServiceHandler.sendNotFound(response, "Employee " + id + " not found")))
                    .exceptionally(throwable -> ServiceHandler.sendError(throwable, response, LOGGER));
        } catch (NumberFormatException ex) {
            ServiceHandler.sendError(ex, response, LOGGER);
        }
    }

    private void add(ServerRequest request, ServerResponse response, EmployeeRequest employeeRequest){
        dbClient.execute(exec -> exec
                .createNamedInsert("insert-employee")
                .indexedParam(employeeRequest)
                .execute())
                .thenAccept( count -> response.send("Inserted: " + count + " values\n"))
                .exceptionally(throwable -> ServiceHandler.sendError(throwable, response, LOGGER));
    }

    private void deleteById(ServerRequest request, ServerResponse response) {
        try {
            int id = Integer.parseInt(request.path().param("id"));
            dbClient.execute(exec -> exec
                    .createNamedDelete("delete-employee-by-id")
                    .addParam("id", id)
                    .execute())
                    .thenAccept(count -> response.send("Deleted: " + count + " values\n"))
                    .exceptionally(throwable -> ServiceHandler.sendError(throwable, response, LOGGER));
        } catch (NumberFormatException ex) {
            ServiceHandler.sendError(ex, response, LOGGER);
        }
    }
}
