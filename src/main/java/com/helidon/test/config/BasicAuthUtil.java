package com.helidon.test.config;

import io.helidon.webserver.WebServer;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class BasicAuthUtil {
    public BasicAuthUtil() {
    }

    public static void startAndPrintEndpoints(Supplier<WebServer> startMethod) {
        long t = System.nanoTime();

        WebServer webServer = startMethod.get();

        long time = System.nanoTime() - t;
        System.out.printf("Server started in %d ms ms%n", TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS));
        System.out.printf("Started server on localhost:%d%n", webServer.port());
        System.out.println();
    }
}
