package com.helidon.test.service;

import com.helidon.test.entity.model.RoleRequest;
import io.helidon.dbclient.DbClient;
import io.helidon.security.integration.webserver.WebSecurity;
import io.helidon.webserver.*;
import javax.json.JsonObject;
import java.util.logging.Logger;

public class RoleService implements Service{
    private final DbClient dbClient;

    private static final Logger LOGGER = Logger.getLogger(RoleService.class.getName());

    public RoleService(DbClient dbClient) {
        this.dbClient = dbClient;
    }

    @Override
    public void update(Routing.Rules rules) {
        rules
                .get("/", WebSecurity.rolesAllowed("master", "spv", "staff"), this::findAll)
                .get("/{id}", WebSecurity.rolesAllowed("master", "spv", "staff"), this::findById)
                .post("/", WebSecurity.rolesAllowed("master", "spv"), Handler.create(RoleRequest.class, this::add))
                .delete("/{id}", WebSecurity.rolesAllowed("master"), this::deleteById);

    }

    public void findAll(ServerRequest request, ServerResponse response) {
        response.send(dbClient.execute(exec -> exec.namedQuery("select-all-role"))
                .map(it -> it.as(JsonObject.class)), JsonObject.class);
    }

    public void findById(ServerRequest request, ServerResponse response) {
        try {
            int id = Integer.parseInt(request.path().param("id"));
            dbClient.execute(exec -> exec
                    .createNamedGet("select-role-by-id")
                    .addParam("id", id)
                    .execute())
                    .thenAccept(maybeRow -> maybeRow
                            .ifPresentOrElse(
                                    row -> ServiceHandler.sendRow(row, response),
                                    () -> ServiceHandler.sendNotFound(response, "Role " + id + " not found")))
                    .exceptionally(throwable -> ServiceHandler.sendError(throwable, response, LOGGER));
        } catch (NumberFormatException ex) {
            ServiceHandler.sendError(ex, response, LOGGER);
        }
    }

    public void add(ServerRequest request, ServerResponse response, RoleRequest roleRequest){
        dbClient.execute(exec -> exec
                .createNamedInsert("insert-role")
                .indexedParam(roleRequest)
                .execute())
                .thenAccept( count -> response.send("Inserted: " + count + " values\n"))
                .exceptionally(throwable -> ServiceHandler.sendError(throwable, response, LOGGER));
    }

    public void deleteById(ServerRequest request, ServerResponse response) {
        try {
            int id = Integer.parseInt(request.path().param("id"));
            dbClient.execute(exec -> exec
                    .createNamedDelete("delete-role-by-id")
                    .addParam("id", id)
                    .execute())
                    .thenAccept(count -> response.send("Deleted: " + count + " values\n"))
                    .exceptionally(throwable -> ServiceHandler.sendError(throwable, response, LOGGER));
        } catch (NumberFormatException ex) {
            ServiceHandler.sendError(ex, response, LOGGER);
        }
    }
}
