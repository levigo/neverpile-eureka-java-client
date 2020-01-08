package com.neverpile.eureka.client.impl.feign;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("client-context-oauth2")
public class ClientProperties {

    private String username = "admin";
    private String password = "admin";
    private String accountServiceUrl = "http://localhost:8080/oauth/token";
    private String clientId = "trusted-app";
    private String clientSecret = "secret";
    private String[] scope = new String[]{"document", "public"};

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccountServiceUrl() {
        return accountServiceUrl;
    }

    public void setAccountServiceUrl(String accountServiceUrl) {
        this.accountServiceUrl = accountServiceUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String[] getScope() {
        return scope;
    }

    public void setScope(String[] scope) {
        this.scope = scope;
    }
}