package com.helidon.test.service;

import com.google.gson.Gson;
import com.helidon.test.dto.EmployeeLogin;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

import static com.helidon.test.Main.employeeDB;

public class FindEmployeeByUsername {
    public static EmployeeLogin execute(String username){
        List<EmployeeLogin> logins = new ArrayList<>();

        try {
            employeeDB.execute(exec -> exec
                    .createNamedGet("select-employee-by-username")
                    .addParam("username", username)
                    .execute())
                    .thenAccept(maybeRow -> maybeRow
                            .ifPresentOrElse(
                                    row -> {
                                        logins.add(new Gson().fromJson(row.as(JsonObject.class).toString(), EmployeeLogin.class));
                                    },
                                    () -> logins.add(new EmployeeLogin())))
                    .exceptionally(throwable -> ServiceHandler.sendError(throwable)).await();
        } catch (NumberFormatException ex) {
        }

        return logins.get(0);
    }
}
