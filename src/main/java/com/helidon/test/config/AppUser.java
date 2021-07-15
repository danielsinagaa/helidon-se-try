package com.helidon.test.config;

import com.helidon.test.entity.model.EmployeeLogin;
import io.helidon.security.providers.httpauth.SecureUserStore;

import java.util.*;

public class AppUser implements SecureUserStore.User {
    private final String login;
    private final char[] password;
    private final Set<String> roles;

    public AppUser(EmployeeLogin login) {
        this.login = login.getUsername();
        this.password = login.getPassword().toCharArray();
        this.roles = Set.of(login.getRole());
    }

    private char[] password() {
        return password;
    }

    @Override
    public boolean isPasswordValid(char[] password) {
        return Arrays.equals(password(), password);
    }

    @Override
    public Set<String> roles() {
        return roles;
    }

    @Override
    public String login() {
        return login;
    }
}

