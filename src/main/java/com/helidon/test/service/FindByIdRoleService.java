package com.helidon.test.service;

import io.helidon.dbclient.DbClient;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;

import java.util.logging.Logger;

public class FindByIdRoleService {

    private static final Logger LOGGER = Logger.getLogger(FindByIdRoleService.class.getName());

    private final DbClient dbClient;

    public FindByIdRoleService(DbClient dbClient) {
        this.dbClient = dbClient;
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

}