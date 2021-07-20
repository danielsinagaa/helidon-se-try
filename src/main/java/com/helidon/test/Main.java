
package com.helidon.test;

import com.helidon.test.config.InitializeDb;
import com.helidon.test.dto.EmployeeRequest;
import com.helidon.test.dto.Login;
import com.helidon.test.dto.RoleRequest;
import com.helidon.test.service.*;
import io.helidon.common.LogConfig;
import io.helidon.common.configurable.Resource;
import io.helidon.common.reactive.Single;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.health.DbClientHealthCheck;
import io.helidon.health.HealthSupport;
import io.helidon.media.jsonb.JsonbSupport;
import io.helidon.media.jsonp.JsonpSupport;
import io.helidon.metrics.MetricsSupport;
import io.helidon.security.Security;
import io.helidon.security.integration.webserver.WebSecurity;
import io.helidon.security.providers.jwt.JwtProvider;
import io.helidon.webserver.Handler;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;

import java.nio.file.Paths;

public final class Main {

    public static void main(final String[] args) {
        startServer();
    }

    static Single<WebServer> startServer() {
        LogConfig.configureRuntime();

        Routing routing = createRouting(Config.create());
        WebServer server = WebServer.builder(routing)
                .addMediaSupport(JsonpSupport.create())
                .addMediaSupport(JsonbSupport.create())
                .config(Config.create().get("server"))
                .build();

        Single<WebServer> webserver = server.start();

        webserver.thenAccept(ws -> {
                    System.out.println("WEB server is up! http://localhost:" + ws.port());
                    ws.whenShutdown().thenRun(() -> System.out.println("WEB server is DOWN. Good bye!"));
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

        // Support for health
        HealthSupport health = HealthSupport.builder()
                .addLiveness(DbClientHealthCheck.create(dbClient, dbConfig.get("health-check")))
                .build();

        // Initialize database schema
        InitializeDb.init(dbClient);

        JwtProvider provider = JwtProvider.builder()
                .verifyJwk(Resource.create(Paths.get("conf/jwks.json")))
                .build();
        Security security = Security.builder()
                .addProvider(provider)
                .build();

        LoginService loginService = new LoginService(dbClient);
        CheckProfileService checkProfileService = new CheckProfileService();

        FindAllEmployeeService findAllEmployeeService = new FindAllEmployeeService(dbClient);
        FindAllRoleService findAllRoleService = new FindAllRoleService(dbClient);
        FindAllTaskService findAllTaskService = new FindAllTaskService(dbClient);

        FindByIdEmployeeService findByIdEmployeeService = new FindByIdEmployeeService(dbClient);
        FindByIdRoleService findByIdRoleService = new FindByIdRoleService(dbClient);
        FindByIdTaskService findByIdTaskService = new FindByIdTaskService(dbClient);

        PostEmployeeService postEmployeeService = new PostEmployeeService(dbClient);
        PostRoleService postRoleService = new PostRoleService(dbClient);
        PostTaskService postTaskService = new PostTaskService(dbClient);

        DeleteByIdEmployeeService deleteByIdEmployeeService = new DeleteByIdEmployeeService(dbClient);
        DeleteByIdRoleService deleteByIdRoleService = new DeleteByIdRoleService(dbClient);
        DeleteByIdTaskService deleteByIdTaskService = new DeleteByIdTaskService(dbClient);

        UpdateTaskService updateTaskService = new UpdateTaskService(dbClient);

        return Routing.builder()
                .register(WebSecurity.create(security))
                .register(health)                   // Health at "/health"
                .register(MetricsSupport.create())  // Metrics at "/metrics"
                //POST WITH REQUEST BODY WITH LAMBDA
                .get("/login", Handler.create(Login.class, (req, res, loginReq) ->
                        loginService.login(req, res, loginReq)))
                //GET WITH LAMBDA
                .get("/profile", WebSecurity.authenticate().rolesAllowed("master","spv", "staff"),
                        (req, res) -> checkProfileService.checkProfile(req, res))
                //GET WITH METHOD REFERENCE
                .get("/employee", WebSecurity.rolesAllowed("master", "spv"), findAllEmployeeService::findAll)
                .get("/employee/{id}", WebSecurity.rolesAllowed("master", "spv"), findByIdEmployeeService::findById)
                //POST WITH REQUEST BODY WITH METHOD REFERENCE
                .post("/employee", WebSecurity.rolesAllowed("master", "spv", "staff"),Handler.create(EmployeeRequest.class,postEmployeeService::post))
                .delete("/employee/{id}", WebSecurity.rolesAllowed("master"), deleteByIdEmployeeService::deleteById)
                .get("/role", WebSecurity.rolesAllowed("master"), findAllRoleService::findAll)
                .get("/role/{id}", WebSecurity.rolesAllowed("master"), findByIdRoleService::findById)
                .post("/role", WebSecurity.rolesAllowed("master"), Handler.create(RoleRequest.class, postRoleService::post))
                .delete("/role/{id}",  WebSecurity.rolesAllowed("master"), deleteByIdRoleService::deleteById)
                .get("/task", WebSecurity.rolesAllowed("master", "spv", "staff"), findAllTaskService::findAll)
                .get("/task/{id}", WebSecurity.rolesAllowed("master", "spv", "staff"), findByIdTaskService::findById)
                .get("/task/new/{task}", WebSecurity.rolesAllowed("master", "spv"), postTaskService::post)
                .get("/task/verified/{id}", WebSecurity.rolesAllowed("master", "spv" ), updateTaskService::updateVerifiedTask)
                .get("/task/finished/{id}", WebSecurity.rolesAllowed("master", "staff"), updateTaskService::updateFinishedTask)
                .delete("/task/{id}", WebSecurity.rolesAllowed("master"), deleteByIdTaskService::deleteById)
                .build();
    }


}
