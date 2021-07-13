
package com.helidon.test;

import com.helidon.test.config.InitializeDb;
import com.helidon.test.entity.model.Entity;
import com.helidon.test.service.EmployeeService;
import com.helidon.test.service.RoleService;
import com.helidon.test.service.TaskService;
import io.helidon.common.LogConfig;
import io.helidon.common.reactive.Single;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.health.DbClientHealthCheck;
import io.helidon.health.HealthSupport;
import io.helidon.media.jsonb.JsonbSupport;
import io.helidon.media.jsonp.JsonpSupport;
import io.helidon.metrics.MetricsSupport;
import io.helidon.webclient.WebClient;
import io.helidon.webclient.security.WebClientSecurity;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class Main {

    private static final WebClient CLIENT = WebClient.builder()
            .addService(WebClientSecurity.create())
            .build();

    private static final int START_TIMEOUT_SECONDS = 10;

    public static Entity ENTITY = new Entity();

    private Main() {
    }

    public static void main(final String[] args) {
        startServer();
    }

    static Single<WebServer> startServer() {
        LogConfig.configureRuntime();

        Config config = Config.create();

        Routing routing = createRouting(config);
        WebServer server = WebServer.builder(routing)
                .addMediaSupport(JsonpSupport.create())
                .addMediaSupport(JsonbSupport.create())
                .config(config.get("server"))
                .build();

        Single<WebServer> webserver = server.start();

        webserver.thenAccept(ws -> {
                    System.out.println(
                            "WEB server is up! http://localhost:" + ws.port() );
                    ws.whenShutdown().thenRun(()
                            -> System.out.println("WEB server is DOWN. Good bye!"));
                })
                .exceptionallyAccept(t -> {
                    System.err.println("Startup failed: " + t.getMessage());
                    t.printStackTrace(System.err);
                });

        return webserver;
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
