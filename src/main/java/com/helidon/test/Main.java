
package com.helidon.test;

import com.helidon.test.config.AppUser;
import com.helidon.test.config.BasicAuthUtil;
import com.helidon.test.config.InitializeDb;
import com.helidon.test.entity.model.EmployeeLogin;
import com.helidon.test.entity.model.Entity;
import com.helidon.test.entity.model.RoleRequest;
import com.helidon.test.service.EmployeeService;
import com.helidon.test.service.RoleService;
import com.helidon.test.service.TaskService;
import io.helidon.common.LogConfig;
import io.helidon.common.http.MediaType;
import io.helidon.common.reactive.Single;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.health.DbClientHealthCheck;
import io.helidon.health.HealthSupport;
import io.helidon.media.jsonb.JsonbSupport;
import io.helidon.media.jsonp.JsonpSupport;
import io.helidon.metrics.MetricsSupport;
import io.helidon.security.Security;
import io.helidon.security.SecurityContext;
import io.helidon.security.integration.webserver.WebSecurity;
import io.helidon.security.providers.httpauth.HttpBasicAuthProvider;
import io.helidon.security.providers.httpauth.SecureUserStore;
import io.helidon.webclient.WebClient;
import io.helidon.webclient.security.WebClientSecurity;
import io.helidon.webserver.Handler;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;

import javax.json.JsonObject;
import java.util.*;
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
        DbClient dbClient = DbClient.builder(dbConfig).build();

        RoleService roleService = new RoleService(dbClient);

        // Support for health
        HealthSupport health = HealthSupport.builder()
                .addLiveness(DbClientHealthCheck.create(dbClient, dbConfig.get("health-check")))
                .build();

        // Initialize database schema
        InitializeDb.init(dbClient);

//        Routing routing = Routing.builder()
//                .register(buildWebSecurity(dbClient).securityDefaults(WebSecurity.authenticate()))
//                .get("/role", WebSecurity.rolesAllowed("master", "spv", "staff"), (req, res) -> {
//                    roleService.findAll(req, res);
//                })
//                .post("/role", WebSecurity.rolesAllowed("master", "spv"), (req, res) -> {
//                    Handler.create(RoleRequest.class, roleService::add);
//                })
//                .build();
//
//        return routing;

        return Routing.builder()
                .register(buildWebSecurity(dbClient).securityDefaults(WebSecurity.authenticate()))
                .register(health)                   // Health at "/health"
                .register(MetricsSupport.create())  // Metrics at "/metrics"
                .register("/role", new RoleService(dbClient))
                .register("/employee", new EmployeeService(dbClient))
                .register("/task", new TaskService(dbClient))
                .get("/{*}", (req, res) -> {
                    Optional<SecurityContext> securityContext = req.context().get(SecurityContext.class);
                    res.headers().contentType(MediaType.TEXT_PLAIN.withCharset("UTF-8"));
                    res.send("Hello, you are: \n" + securityContext
                            .map(ctx -> ctx.user().orElse(SecurityContext.ANONYMOUS).toString())
                            .orElse("Security context is null"));
                })
                .build();
    }

    private static WebSecurity buildWebSecurity(DbClient dbClient) {
        Security security = Security.builder()
                .addAuthenticationProvider(
                        HttpBasicAuthProvider.builder()
                                .realm("helidon")
                                .userStore(buildUserStore(dbClient)),
                        "http-basic-auth")
                .build();
        return WebSecurity.create(security);
    }

    private static SecureUserStore buildUserStore(DbClient dbClient) {
        Map<String, AppUser> USERS = new HashMap<>();

        for (EmployeeLogin employee : InitializeDb.findAllEmployee(dbClient)){
            USERS.put(employee.getUsername(), new AppUser(employee));
            System.out.println(employee);
        }

        return login -> Optional.ofNullable(USERS.get(login));
    }
}
