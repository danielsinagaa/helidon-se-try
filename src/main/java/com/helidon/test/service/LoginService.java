package com.helidon.test.service;

import com.helidon.test.config.InitializeDb;
import com.helidon.test.dto.EmployeeLogin;
import com.helidon.test.dto.Login;
import com.helidon.test.dto.LoginResponse;
import io.helidon.common.configurable.Resource;
import io.helidon.common.http.Http;
import io.helidon.security.jwt.Jwt;
import io.helidon.security.jwt.SignedJwt;
import io.helidon.security.jwt.jwk.JwkKeys;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;

import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

public class LoginService {

    public static void login(ServerRequest req, ServerResponse res, Login loginReq){
        JwkKeys keySet = JwkKeys.builder().resource(Resource.create(Paths.get("conf/jwks.json"))).build();

        EmployeeLogin data = InitializeDb.findByUsername(loginReq.getUsername());
        Jwt.Builder jwt;

        if (verifyLogin(loginReq)){
            jwt = Jwt
                    .builder()
                    .keyId("blog-app")
                    .subject(data.getUsername())
                    .notBefore(Instant.now())
                    .expirationTime(Instant.now().plus(Duration.ofHours(1)))
                    .addUserGroup(data.getRole());


            SignedJwt signed = SignedJwt.sign(jwt.build(), keySet);

            LoginResponse loginData = new LoginResponse(data, signed.tokenContent());
            res.status(Http.Status.OK_200).send(loginData);
        } else {
            res.status(Http.Status.UNAUTHORIZED_401);
            res.send("WRONG PASSWORD OR USERNAME");
        }
    }

    private static Boolean verifyLogin(Login login){
        EmployeeLogin data = InitializeDb.findByUsername(login.getUsername());

        if (!data.getUsername().equals(login.getUsername()) || !data.getPassword().equals(login.getPassword())){
            return false;
        }

        return true;
    }
}
