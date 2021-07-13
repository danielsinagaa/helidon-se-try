package com.helidon.test.config;

import com.google.gson.Gson;
import com.helidon.test.entity.model.EmployeeLogin;
import io.helidon.dbclient.DbClient;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

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
                    exec -> exec.namedDml("primary-key-seq")
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

    public static List<EmployeeLogin> findAllUsers(DbClient dbClient){
        List<EmployeeLogin> employees = new ArrayList<>();

        dbClient.execute(exec -> exec.namedQuery("find-all-employee"))
                .map(it -> it.as(JsonObject.class)).forEach(it -> {
                    EmployeeLogin employee = new Gson().fromJson(it.toString(), EmployeeLogin.class);
                    employees.add(employee);
        }).await();

        return employees;
    }

    private InitializeDb() {
        throw new UnsupportedOperationException("Instances of InitializeDb utility class are not allowed");
    }
}
