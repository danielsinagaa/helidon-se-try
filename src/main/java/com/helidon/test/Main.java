
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
import io.helidon.security.integration.webserver.SecurityHandler;
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

        SecurityHandler allRoles = WebSecurity.authenticate().rolesAllowed("master","spv", "staff");
        SecurityHandler masterAndSupervisor = WebSecurity.authenticate().rolesAllowed("master","spv");
        SecurityHandler masterRole = WebSecurity.authenticate().rolesAllowed("master");

        return Routing.builder()
                .register(WebSecurity.create(security))
                .register(health)                   // Health at "/health"
                .register(MetricsSupport.create())  // Metrics at "/metrics"
                //POST WITH REQUEST BODY WITH LAMBDA
                .get("/login", Handler.create(Login.class, (req, res, loginReq) ->
                        LoginService.execute(req, res, loginReq)))
                //GET WITH LAMBDA
                .get("/profile", allRoles,
                        (req, res) -> CheckProfileService.execute(req, res))
                //GET WITH METHOD REFERENCE
                .get("/employee", masterAndSupervisor, FindAllEmployeeService::execute)
                .get("/employee/{id}", masterAndSupervisor, FindByIdEmployeeService::execute)
                //POST WITH REQUEST BODY WITH METHOD REFERENCE
                .post("/employee", allRoles,Handler.create(EmployeeRequest.class,PostEmployeeService::execute))
                .delete("/employee/{id}", masterRole, DeleteByIdEmployeeService::execute)
                .get("/role", masterRole, FindAllRoleService::execute)
                .get("/role/{id}", masterRole, FindByIdRoleService::execute)
                .post("/role", masterRole, Handler.create(RoleRequest.class, PostRoleService::execute))
                .delete("/role/{id}",  masterRole, DeleteByIdRoleService::execute)
                .get("/task", allRoles, FindAllTaskService::execute)
                .get("/task/{id}", allRoles, FindByIdTaskService::findById)
                .get("/task/new/{task}", masterAndSupervisor, PostTaskService::execute)
                .get("/task/verified/{id}", masterAndSupervisor, UpdateTaskService::updateVerifiedTask)
                .get("/task/finished/{id}", WebSecurity.rolesAllowed("master", "staff"), UpdateTaskService::updateFinishedTask)
                .delete("/task/{id}", masterRole, DeleteByIdTaskService::deleteById)
                .build();
    }


}
