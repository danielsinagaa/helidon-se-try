package com.helidon.test.service;

import com.helidon.test.dto.RoleRequest;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import java.util.logging.Logger;
import static com.helidon.test.Main.dbClient;

public class PostRoleService {
    private static final Logger LOGGER = Logger.getLogger(PostRoleService.class.getName());

    public void post(ServerRequest request, ServerResponse response, RoleRequest roleRequest){
        dbClient.execute(exec -> exec
                .createNamedInsert("insert-role")
                .indexedParam(roleRequest)
                .execute())
                .thenAccept( count -> response.send("Inserted: " + count + " values\n"))
                .exceptionally(throwable -> ServiceHandler.sendError(throwable, response, LOGGER));
    }
}
