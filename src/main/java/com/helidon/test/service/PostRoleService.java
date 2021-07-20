package com.helidon.test.service;

import com.helidon.test.dto.RoleRequest;
import io.helidon.dbclient.DbClient;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;

import java.util.logging.Logger;

public class PostRoleService {
    private static final Logger LOGGER = Logger.getLogger(PostRoleService.class.getName());
    private final DbClient dbClient;

    public PostRoleService(DbClient dbClient) {
        this.dbClient = dbClient;
    }

    public void post(ServerRequest request, ServerResponse response, RoleRequest roleRequest){
        dbClient.execute(exec -> exec
                .createNamedInsert("insert-role")
                .indexedParam(roleRequest)
                .execute())
                .thenAccept( count -> response.send("Inserted: " + count + " values\n"))
                .exceptionally(throwable -> ServiceHandler.sendError(throwable, response, LOGGER));
    }
}
