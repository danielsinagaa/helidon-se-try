package com.helidon.test.service;

import com.google.gson.Gson;
import com.helidon.test.entity.Employee;
import com.helidon.test.entity.model.EmployeeRequest;
import io.helidon.common.http.MediaType;
import io.helidon.dbclient.DbClient;
import io.helidon.webserver.*;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class EmployeeService implements Service {
    private final DbClient dbClient;

    private static final Logger LOGGER = Logger.getLogger(EmployeeService.class.getName());

    public EmployeeService(DbClient dbClient) {
        this.dbClient = dbClient;
    }

    @Override
    public void update(Routing.Rules rules) {
        rules
            .get("/", this::findAll)
                .get("/{id}", this::findById)
                .post("/", Handler.create(EmployeeRequest.class, this::add))
                .post("/login", this::login)
                .delete("/{id}",this::deleteById);
    }

    private void findAll(ServerRequest request, ServerResponse response) {
        List<Employee> employees = new ArrayList<>();

        dbClient.execute(exec -> exec.namedQuery("select-all-employee"))
                .map(it -> it.as(JsonObject.class)).forEach(it -> {
                    employees.add(new Gson().fromJson(it.toString(), Employee.class));
        }).await();

        response.send(new Gson().toJson(employees));
    }

    private void findById(ServerRequest request, ServerResponse response) {
        try {
            int id = Integer.parseInt(request.path().param("id"));
            dbClient.execute(exec -> exec
                    .createNamedGet("select-employee-by-id")
                    .addParam("id", id)
                    .execute())
                    .thenAccept(maybeRow -> maybeRow
                            .ifPresentOrElse(
                                    row -> ServiceHandler.sendRow(row, response),
                                    () -> ServiceHandler.sendNotFound(response, "Employee " + id + " not found")))
                    .exceptionally(throwable -> ServiceHandler.sendError(throwable, response, LOGGER));
        } catch (NumberFormatException ex) {
            ServiceHandler.sendError(ex, response, LOGGER);
        }
    }

    private void add(ServerRequest request, ServerResponse response, EmployeeRequest employeeRequest){
        dbClient.execute(exec -> exec
                .createNamedInsert("insert-employee")
                .indexedParam(employeeRequest)
                .execute())
                .thenAccept( count -> response.send("Inserted: " + count + " values\n"))
                .exceptionally(throwable -> ServiceHandler.sendError(throwable, response, LOGGER));
    }

    private void login(ServerRequest request, ServerResponse response) {
        String username = request.queryParams().put("username").get(0);
        String password = request.queryParams().put("password").get(0);

        try {

            dbClient.execute(exec -> exec
                    .createNamedGet("select-employee-by-login")
                    .addParam("username", username)
                    .addParam("password", password)
                    .execute())
                    .thenAccept(result -> result.ifPresentOrElse(
                            row -> ServiceHandler.sendRow(row, response),
                            () -> ServiceHandler.sendNotFound(response, "WRONG USERNAME OR PASSWORD")
                    ))
                    .exceptionally(throwable -> ServiceHandler.sendError(throwable, response, LOGGER));
        } catch (NumberFormatException ex) {
            ServiceHandler.sendError(ex, response, LOGGER);
        }
    }

        private void deleteById(ServerRequest request, ServerResponse response) {
        try {
            int id = Integer.parseInt(request.path().param("id"));
            dbClient.execute(exec -> exec
                    .createNamedDelete("delete-employee-by-id")
                    .addParam("id", id)
                    .execute())
                    .thenAccept(count -> response.send("Deleted: " + count + " values\n"))
                    .exceptionally(throwable -> ServiceHandler.sendError(throwable, response, LOGGER));
        } catch (NumberFormatException ex) {
            ServiceHandler.sendError(ex, response, LOGGER);
        }
    }
}
