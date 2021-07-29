package com.helidon.test.service;

import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;
import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

public class CounterService implements Service {

    private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());
    private final LongAdder allAccessCounter = new LongAdder();
    private final AtomicInteger apiAccessCounter = new AtomicInteger();

    @Override
    public void update(Routing.Rules routingRules) {
        routingRules.any(this::handleAny)
                .get("/api/counter", this::handleGet);
    }

    private void handleAny(ServerRequest request, ServerResponse response) {
        allAccessCounter.increment();
        request.next();
    }

    private void handleGet(ServerRequest request, ServerResponse response) {
        int apiAcc = apiAccessCounter.incrementAndGet();
        JsonObject result = JSON.createObjectBuilder()
                .add("all", allAccessCounter.longValue())
                .add("api", apiAcc)
                .build();
        response.send(result);
    }
}
