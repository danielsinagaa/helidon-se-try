package com.helidon.test.service;

import io.helidon.common.http.Http;
import io.helidon.dbclient.DbRow;
import io.helidon.webserver.ServerResponse;

import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceHandler {


    public static void sendNotFound(ServerResponse response, String message) {
        response.status(Http.Status.NOT_FOUND_404);
        response.send(message);
    }

    public static void sendRow(DbRow row, ServerResponse response) {
        response.send(row.as(javax.json.JsonObject.class));
    }

    public static <T> T sendError(Throwable throwable, ServerResponse response, Logger LOGGER) {
        Throwable realCause = throwable;
        if (throwable instanceof CompletionException) {
            realCause = throwable.getCause();
        }
        response.status(Http.Status.INTERNAL_SERVER_ERROR_500);
        response.send("Failed to process request: " + realCause.getClass().getName() + "(" + realCause.getMessage() + ")");
        LOGGER.log(Level.WARNING, "Failed to process request", throwable);
        return null;
    }
}
