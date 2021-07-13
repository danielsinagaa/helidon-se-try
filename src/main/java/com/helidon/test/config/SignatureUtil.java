package com.helidon.test.config;

import com.helidon.test.entity.model.Entity;
import com.helidon.test.service.EmployeeService;
import com.helidon.test.service.RoleService;
import com.helidon.test.service.TaskService;
import io.helidon.common.LogConfig;
import io.helidon.common.http.Http;
import io.helidon.common.http.MediaType;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.health.DbClientHealthCheck;
import io.helidon.health.HealthSupport;
import io.helidon.media.jsonb.JsonbSupport;
import io.helidon.media.jsonp.JsonpSupport;
import io.helidon.metrics.MetricsSupport;
import io.helidon.security.SecurityContext;
import io.helidon.webclient.WebClient;
import io.helidon.webclient.security.WebClientSecurity;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.WebServer;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SignatureUtil {
    private static final WebClient CLIENT = WebClient.builder()
            .addService(WebClientSecurity.create())
            .build();

    private static final int START_TIMEOUT_SECONDS = 10;

    public static Entity ENTITY = new Entity();

    static WebServer startServer() {
        LogConfig.configureRuntime();

        Config config = Config.create();

        Routing routing = createRouting(config);
        WebServer server = WebServer.builder(routing)
                .addMediaSupport(JsonpSupport.create())
                .addMediaSupport(JsonbSupport.create())
                .config(config.get("server"))
                .build();

        long t = System.nanoTime();

        CountDownLatch cdl = new CountDownLatch(1);

        server.start().thenAccept(webServer -> {
            long time = System.nanoTime() - t;

            System.out.printf("Server started in %d ms ms%n", TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS));
            System.out.printf("Started server on localhost:%d%n", webServer.port());
            System.out.println();
            cdl.countDown();
        }).exceptionally(throwable -> {
            throw new RuntimeException("Failed to start server", throwable);
        });

        try {
            cdl.await(START_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to start server within defined timeout: " + START_TIMEOUT_SECONDS + " seconds");
        }
        return server;
    }

    static void processServiceRequest(ServerRequest req, ServerResponse res, String path, int svc2port) {
        Optional<SecurityContext> securityContext = req.context().get(SecurityContext.class);

        res.headers().contentType(MediaType.TEXT_PLAIN.withCharset("UTF-8"));

        securityContext.ifPresentOrElse(context -> {
            CLIENT.get()
                    .uri("http://localhost:" + svc2port + path)
                    .request()
                    .thenAccept(it -> {
                        if (it.status() == Http.Status.OK_200) {
                            it.content().as(String.class)
                                    .thenAccept(res::send)
                                    .exceptionally(throwable -> {
                                        res.send("Getting server response failed!");
                                        return null;
                                    });
                        } else {
                            res.send("Request failed, status: " + it.status());
                        }
                    });

        }, () -> res.send("Security context is null"));
    }

    private static Routing createRouting(Config config) {
        Config dbConfig = config.get("db");

        // Client services are added through a service loader
        DbClient dbClient = DbClient.builder(dbConfig).build();

        // Support for health
        HealthSupport health = HealthSupport.builder()
                .addLiveness(DbClientHealthCheck.create(dbClient, dbConfig.get("health-check")))
                .build();

        // Initialize database schema
        InitializeDb.init(dbClient);

        return Routing.builder()
                .register(health)                   // Health at "/health"
                .register(MetricsSupport.create())  // Metrics at "/metrics"
                .register("/role", new RoleService(dbClient))
                .register("/employee", new EmployeeService(dbClient))
                .register("/task", new TaskService(dbClient))
                .build();
    }
}
