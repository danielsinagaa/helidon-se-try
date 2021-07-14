
package com.helidon.test;

import com.helidon.test.config.InitializeDb;
import com.helidon.test.config.SignatureUtil;
import com.helidon.test.entity.model.EmployeeLogin;
import com.helidon.test.entity.model.Entity;
import com.helidon.test.service.EmployeeService;
import com.helidon.test.service.RoleService;
import com.helidon.test.service.TaskService;
import io.helidon.common.configurable.Resource;
import io.helidon.common.http.MediaType;
import io.helidon.common.pki.KeyConfig;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.health.DbClientHealthCheck;
import io.helidon.health.HealthSupport;
import io.helidon.metrics.MetricsSupport;
import io.helidon.security.*;
import io.helidon.security.integration.webserver.WebSecurity;
import io.helidon.security.providers.httpauth.HttpBasicAuthProvider;
import io.helidon.security.providers.httpauth.SecureUserStore;
import io.helidon.security.providers.httpsign.HttpSignProvider;
import io.helidon.security.providers.httpsign.InboundClientDefinition;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;

import java.nio.file.Paths;
import java.util.*;

public final class Main {

    private static WebServer serviceServer;

    public static Entity ENTITY = new Entity();

    private static final Map<String, SecureUserStore.User> USERS = new HashMap<>();

    private static void addUser(String user, String password, List<String> roles) {
        USERS.put(user, new SecureUserStore.User() {
            @Override
            public String login() {
                return user;
            }

            char[] password() {
                return password.toCharArray();
            }

            @Override
            public boolean isPasswordValid(char[] password) {
                return Arrays.equals(password(), password);
            }

            @Override
            public Collection<String> roles() {
                return roles;
            }
        });
    }

    private Main() {
    }

    public static void main(final String[] args) {
        serviceServer = SignatureUtil.startServer(createRouting());
    }

    private static Routing createRouting() {
        Config config = Config.create();
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
                .register(WebSecurity.create(security(dbClient)).securityDefaults(WebSecurity.authenticate()))
                .get("/service", WebSecurity.rolesAllowed("master","spv","staff"))
                .get("/2", WebSecurity.rolesAllowed("master","spv","staff"))
                .register(health)                   // Health at "/health"
                .register(MetricsSupport.create())  // Metrics at "/metrics"
                .register("/role", new RoleService(dbClient))
                .register("/employee", new EmployeeService(dbClient))
                .get("/task", (req, res) -> {
                    new TaskService(dbClient).findAll(req,res);
                }, WebSecurity.rolesAllowed("master","spv","staff"))
                .get("/task/{id}", (req, res) -> {
                    new TaskService(dbClient).findById(req,res);
                })
                .get("/task/new/{task}", (req, res) -> {
                    new TaskService(dbClient).add(req,res);
                }, WebSecurity.rolesAllowed("master", "spv"))
                .get("/task/verified/{id}", (req, res) -> {
                    new TaskService(dbClient).updateVerifiedTask(req,res);
                }, WebSecurity.rolesAllowed("master", "spv"))
                .get("/task/finished/{id}", (req, res) -> {
                    new TaskService(dbClient).updateFinishedTask(req,res);
                }, WebSecurity.rolesAllowed("master", "staff"))
                .delete("/task/{id}", (req, res) -> {
                    new TaskService(dbClient).deleteById(req,res);
                }, WebSecurity.rolesAllowed("master"))

                .build();
    }

    public static Security security(DbClient dbClient) {
        return Security.builder()
                .providerSelectionPolicy(CompositeProviderSelectionPolicy.builder()
                        .addAuthenticationProvider("http-signatures", CompositeProviderFlag.OPTIONAL)
                        .addAuthenticationProvider("basic-auth")
                        .build())
                .addProvider(HttpBasicAuthProvider.builder()
                                .realm("mic")
                                .userStore(users(dbClient)),
                        "basic-auth")
                .addProvider(HttpSignProvider.builder()
                                .addInbound(InboundClientDefinition.builder("service1-hmac")
                                        .principalName("Service1 - HMAC signature")
                                        .hmacSecret("somePasswordForHmacShouldBeEncrypted")
                                        .build())
                                .addInbound(InboundClientDefinition.builder("service1-rsa")
                                        .principalName("Service1 - RSA signature")
                                        .publicKeyConfig(KeyConfig.keystoreBuilder()
                                                .keystore(Resource.create(Paths.get(
                                                        "src/main/resources/keystore.p12")))
                                                .keystorePassphrase("password".toCharArray())
                                                .certAlias("service_cert")
                                                .build())
                                        .build())
                                .build(),
                        "http-signatures")
                .build();
    }

    static WebServer getServiceServer() {
        return serviceServer;
    }

    private static SecureUserStore users(DbClient dbClient) {
        for (EmployeeLogin e: InitializeDb.findAllUsers(dbClient)){
            addUser(e.getUsername(), e.getPassword(), List.of(e.getRole()));
        }

        return login -> Optional.ofNullable(USERS.get(login));
    }
}
