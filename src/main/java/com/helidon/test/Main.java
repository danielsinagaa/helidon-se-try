
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
import io.helidon.openapi.OpenAPISupport;
import io.helidon.security.Security;
import io.helidon.security.integration.webserver.WebSecurity;
import io.helidon.security.providers.jwt.JwtProvider;
import io.helidon.webserver.Handler;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;

import java.nio.file.Paths;

public final class Main {

    private static final String EMPLOYEE_YAML = "employee.yaml";
    private static final String ROLE_YAML = "role.yaml";
    private static final String TASK_YAML = "task.yaml";

    public static final Config dbConfig = Config.create().get("db");
    public static final DbClient dbClient = DbClient.builder(dbConfig).build();

    public static final DbClient employeeDB = InitializeDb.dbInit(EMPLOYEE_YAML);
    public static final DbClient roleDB = InitializeDb.dbInit(ROLE_YAML);
    public static final DbClient taskDB = InitializeDb.dbInit(TASK_YAML);

    public static void main(final String[] args) {
        startServer();
    }

    static Single<WebServer> startServer() {
        LogConfig.configureRuntime();

        Routing routing = createRouting();
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

    private static Routing createRouting() {

        // Support for health
        HealthSupport health = HealthSupport.builder()
                .addLiveness(DbClientHealthCheck.create(dbClient, dbConfig.get("health-check")))
                .build();

        // Initialize database schema
        InitializeDb.init();

        JwtProvider provider = JwtProvider.builder()
                .verifyJwk(Resource.create(Paths.get("conf/jwks.json")))
                .build();
        Security security = Security.builder()
                .addProvider(provider)
                .build();

        return Routing.builder()
                .register(OpenAPISupport.create(Config.create()))
                .register(WebSecurity.create(security))
                .register(health)                   // Health at "/health"
                .register(MetricsSupport.create())  // Metrics at "/metrics"
                //POST WITH REQUEST BODY WITH LAMBDA
                .get("/login", Handler.create(Login.class, (req, res, loginReq) ->
                        LoginService.login(req, res, loginReq)))
                //GET WITH LAMBDA
                .get("/profile", WebSecurity.authenticate().rolesAllowed("master","spv", "staff"),
                        (req, res) -> CheckProfileService.checkProfile(req, res))
                //GET WITH METHOD REFERENCE
                .get("/employee", WebSecurity.rolesAllowed("master", "spv"), FindAllEmployeeService::findAll)
                .get("/employee/{id}", WebSecurity.rolesAllowed("master", "spv"), FindByIdEmployeeService::findById)
                //POST WITH REQUEST BODY WITH METHOD REFERENCE
                .post("/employee", WebSecurity.rolesAllowed("master", "spv", "staff"),Handler.create(EmployeeRequest.class,PostEmployeeService::post))
                .delete("/employee/{id}", WebSecurity.rolesAllowed("master"), DeleteByIdEmployeeService::deleteById)
                .get("/role", WebSecurity.rolesAllowed("master"), FindAllRoleService::findAll)
                .get("/role/{id}", WebSecurity.rolesAllowed("master"), FindByIdRoleService::findById)
                .post("/role", WebSecurity.rolesAllowed("master"), Handler.create(RoleRequest.class, PostRoleService::post))
                .delete("/role/{id}",  WebSecurity.rolesAllowed("master"), DeleteByIdRoleService::deleteById)
                .get("/task", WebSecurity.rolesAllowed("master", "spv", "staff"), FindAllTaskService::findAll)
                .get("/task/{id}", WebSecurity.rolesAllowed("master", "spv", "staff"), FindByIdTaskService::findById)
                .get("/task/new/{task}", WebSecurity.rolesAllowed("master", "spv"), PostTaskService::post)
                .get("/task/verified/{id}", WebSecurity.rolesAllowed("master", "spv" ), UpdateTaskService::updateVerifiedTask)
                .get("/task/finished/{id}", WebSecurity.rolesAllowed("master", "staff"), UpdateTaskService::updateFinishedTask)
                .delete("/task/{id}", WebSecurity.rolesAllowed("master"), DeleteByIdTaskService::deleteById)
                .build();
    }


}
