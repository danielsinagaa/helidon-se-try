package com.helidon.test.service;

import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import java.util.logging.Logger;
import static com.helidon.test.Main.taskDB;

public class DeleteByIdTaskService {
    private static final Logger LOGGER = Logger.getLogger(DeleteByIdTaskService.class.getName());

    public static void deleteById(ServerRequest request, ServerResponse response) {
        try {
            int id = Integer.parseInt(request.path().param("id"));
            taskDB.execute(exec -> exec
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
