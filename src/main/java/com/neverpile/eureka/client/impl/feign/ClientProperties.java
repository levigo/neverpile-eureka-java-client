package com.neverpile.eureka.client.impl.feign;

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

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getAccountServiceUrl() {
        return accountServiceUrl;
    }

    public void setAccountServiceUrl(final String accountServiceUrl) {
        this.accountServiceUrl = accountServiceUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(final String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String[] getScope() {
        return scope;
    }

    public void setScope(final String[] scope) {
        this.scope = scope;
    }
}