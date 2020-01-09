package com.neverpile.eureka.client.impl.feign;

import java.util.ArrayList;
import java.util.List;

import feign.RequestInterceptor;

@SuppressWarnings("unused")
public class OAuth2ClientBuilder {
  private final EurekaClientBuilder parent;
  private String username;
  private String password;
  private String clientId;
  private String clientSecret;
  private final List<String> scopes = new ArrayList<>();
  private String accessTokenUri;

  OAuth2ClientBuilder(final EurekaClientBuilder clientBuilder) {
    this.parent = clientBuilder;
  }

  public OAuth2ClientBuilder accessTokenUri(final String accessTokenUri) {
    this.accessTokenUri = accessTokenUri;
    return this;
  }
  
  public OAuth2ClientBuilder username(final String username) {
    this.username = username;
    return this;
  }

  public OAuth2ClientBuilder password(final String password) {
    this.password = password;
    return this;
  }

  public OAuth2ClientBuilder clientId(final String clientId) {
    this.clientId = clientId;
    return this;
  }

  public OAuth2ClientBuilder clientSecret(final String clientSecret) {
    this.clientSecret = clientSecret;
    return this;
  }

  public OAuth2ClientBuilder scope(final String scope) {
    scopes.add(scope);
    return this;
  }

  private RequestInterceptor oauth2FeignRequestInterceptor() {
    // FIXME
    return null;
  }

  public EurekaClientBuilder done() {
    parent.withInterceptor(oauth2FeignRequestInterceptor());
    return parent;
  }
}
