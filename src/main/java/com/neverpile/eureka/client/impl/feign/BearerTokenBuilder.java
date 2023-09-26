package com.neverpile.eureka.client.impl.feign;

import com.neverpile.eureka.client.impl.feign.interceptor.BearerAuthRequestInterceptor;

public class BearerTokenBuilder {

    private final EurekaClientBuilder parent;

    private String token;

    BearerTokenBuilder(final EurekaClientBuilder clientBuilder) {
        this.parent = clientBuilder;
    }

    public BearerTokenBuilder token(final String token) {
        this.token = token;
        return this;
    }

    public EurekaClientBuilder done() {
        parent.withInterceptor(new BearerAuthRequestInterceptor(token));
        return parent;
    }
}
