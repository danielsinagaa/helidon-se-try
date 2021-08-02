package com.helidon.test.service;

import io.helidon.common.http.Http;
import io.helidon.security.Role;
import io.helidon.security.SecurityContext;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import java.util.HashMap;

public class CheckProfileService {

    public static void execute(ServerRequest req, ServerResponse res){
        req.context().get(SecurityContext.class).ifPresentOrElse(
                ctx -> ctx.atnClientBuilder().submit().whenComplete((result, error) -> {
                    if (error != null) {
                        res.status(Http.Status.UNAUTHORIZED_401).send();
                    }
                    else {
                        var user = result.user().get();
                        var response = new HashMap<String, Object>();
                        response.put("subject", user.principal().getName());
                        response.put("roles", user.grants(Role.class));
                        res.status(Http.Status.OK_200).send(response);
                    }
                }),
                () -> res.status(Http.Status.INTERNAL_SERVER_ERROR_500).send()
        );
    }
}
