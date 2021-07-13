package com.helidon.test;

import com.helidon.test.config.SignatureUtil;
import com.helidon.test.service.TaskService;
import io.helidon.common.http.MediaType;
import io.helidon.config.Config;
import io.helidon.config.ConfigSources;
import io.helidon.dbclient.DbClient;
import io.helidon.security.SecurityContext;
import io.helidon.security.Subject;
import io.helidon.security.integration.webserver.WebSecurity;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;

import java.util.Optional;

public class SignatureConfigMain {
    private static WebServer service2Server;

    public SignatureConfigMain() {
    }

    public static void main(String[] args) {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        service2Server = SignatureUtil.startServer(routing());
    }

    private static Routing routing() {
        Config config = config("service.yaml");

        Config configuration = Config.create();
        Config dbConfig = configuration.get("db");

        DbClient dbClient = DbClient.builder(dbConfig).build();

        return Routing.builder()
                .register(WebSecurity.create(config.get("security")))
                .get("/{*}", (req, res) -> {
                    Optional<SecurityContext> securityContext = req.context().get(SecurityContext.class);
                    res.headers().contentType(MediaType.TEXT_PLAIN.withCharset("UTF-8"));
                    res.send("Response from service2, you are: \n" + securityContext
                            .flatMap(SecurityContext::user)
                            .map(Subject::toString)
                            .orElse("Security context is null") + ", service: " + securityContext
                            .flatMap(SecurityContext::service)
                            .map(Subject::toString));
                })
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

    private static Config config(String confFile) {
        return Config.builder()
                .sources(ConfigSources.classpath(confFile))
                .build();
    }
}
