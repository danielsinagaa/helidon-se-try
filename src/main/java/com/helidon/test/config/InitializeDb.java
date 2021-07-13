package com.helidon.test.config;

import io.helidon.dbclient.DbClient;

public class InitializeDb {

    public static void init(DbClient dbClient){
        try {
            dbClient.execute(
                    exec -> exec
                    .namedDml("create-role")
                    .flatMapSingle(result -> exec.namedDml("create-employee")))
                    .await();

            dbClient.execute(
                    exec -> exec.namedDml("create-task")
            ).await();

            dbClient.execute(
                    exec -> exec.namedDml("create-role-master")
            ).await();

            dbClient.execute(
                    exec -> exec.namedDml("create-employee-master")
            ).await();
        } catch (Exception ex1) {
            System.out.printf("Could not create tables: %s", ex1.getMessage());
        }
    }

    private InitializeDb() {
        throw new UnsupportedOperationException("Instances of InitializeDb utility class are not allowed");
    }
}
