package com.helidon.test.service;

import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import javax.json.JsonObject;
import java.util.List;
import static com.helidon.test.Main.roleDB;

public class FindAllRoleService {

    public static void execute(ServerRequest request, ServerResponse response) {
        List<JsonObject> json = roleDB.execute(exec -> exec.namedQuery("select-all-role"))
                .map(it -> it.as(JsonObject.class)).collectList().await();

        response.send(json);
    }
}
