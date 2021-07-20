package com.helidon.test.service;

import com.helidon.test.config.InitializeDb;
import com.helidon.test.dto.EmployeeLogin;
import com.helidon.test.dto.Login;
import com.helidon.test.dto.LoginData;
import io.helidon.common.configurable.Resource;
import io.helidon.common.http.Http;
import io.helidon.dbclient.DbClient;
import io.helidon.security.jwt.Jwt;
import io.helidon.security.jwt.SignedJwt;
import io.helidon.security.jwt.jwk.JwkKeys;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;

import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

import static com.helidon.test.Main.dbClient;

public class LoginService {
    private final JwkKeys keySet = JwkKeys.builder().resource(Resource.create(Paths.get("conf/jwks.json"))).build();

    public void login(ServerRequest req, ServerResponse res, Login loginReq){

        EmployeeLogin data = InitializeDb.findByUsername(dbClient, loginReq.getUsername());
        Jwt.Builder jwt;

        if (verifyLogin(loginReq, dbClient)){
            jwt = Jwt
                    .builder()
                    .keyId("blog-app")
                    .subject(data.getUsername())
                    .notBefore(Instant.now())
                    .expirationTime(Instant.now().plus(Duration.ofHours(1)))
                    .addUserGroup(data.getRole());


            SignedJwt signed = SignedJwt.sign(jwt.build(), keySet);

            LoginData loginData = new LoginData(data, signed.tokenContent());
            res.status(Http.Status.OK_200).send(loginData);
        } else {
            res.status(Http.Status.UNAUTHORIZED_401);
            res.send("WRONG PASSWORD OR USERNAME");
        }
    }

    private static Boolean verifyLogin(Login login, DbClient dbClient){
        EmployeeLogin data = InitializeDb.findByUsername(dbClient, login.getUsername());

        if (!data.getUsername().equals(login.getUsername()) || !data.getPassword().equals(login.getPassword())){
            return false;
        }

        return true;
    }
}
