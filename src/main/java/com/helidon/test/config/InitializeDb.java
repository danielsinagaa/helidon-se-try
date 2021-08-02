package com.helidon.test.config;

import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;

import static com.helidon.test.Main.*;
import static io.helidon.config.ConfigSources.classpath;

public class InitializeDb {

    public static void init(){
        try {

            taskDB.execute(
                    exec -> exec.namedDml("create-task")
            ).await();

            roleDB.execute(
                    exec -> exec.namedDml("create-role-master")
            ).await();

            employeeDB.execute(
                    exec -> exec.namedDml("create-employee-master")
            ).await();

        } catch (Exception ex1) {
            System.out.printf("Could not create tables: %s", ex1.getMessage());
        }
    }



    private InitializeDb() {
        throw new UnsupportedOperationException("Instances of InitializeDb utility class are not allowed");
    }

    public static DbClient dbInit(String yaml){
        Config db = Config.builder()
                .disableEnvironmentVariablesSource()
                .sources(
                        classpath(yaml))
                .build().get("query");

        return DbClient.builder(db).build();
    }
}
