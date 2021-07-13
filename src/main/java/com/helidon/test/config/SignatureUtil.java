package com.helidon.test.config;

import io.helidon.common.LogConfig;
import io.helidon.config.Config;
import io.helidon.media.jsonb.JsonbSupport;
import io.helidon.media.jsonp.JsonpSupport;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SignatureUtil {

    private static final int START_TIMEOUT_SECONDS = 10;

    public static WebServer startServer(Routing routing) {
        LogConfig.configureRuntime();

        Config config = Config.create();

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
}
