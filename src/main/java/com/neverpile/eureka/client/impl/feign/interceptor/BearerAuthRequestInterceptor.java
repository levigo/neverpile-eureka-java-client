package com.neverpile.eureka.client.impl.feign.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class BearerAuthRequestInterceptor implements RequestInterceptor {

    private final String bearerToken;

    public BearerAuthRequestInterceptor(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header("Authorization", "Bearer " + bearerToken);
    }
}
