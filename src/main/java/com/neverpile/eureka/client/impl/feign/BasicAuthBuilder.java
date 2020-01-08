package com.neverpile.eureka.client.impl.feign;

import feign.auth.BasicAuthRequestInterceptor;

public class BasicAuthBuilder {
  private final ClientBuilder parent;
  private String username;
  private String password;

  BasicAuthBuilder(final ClientBuilder clientBuilder) {
    this.parent = clientBuilder;
  }
  
  public BasicAuthBuilder username(final String username) {
    this.username = username;
    return this;
  }

  public BasicAuthBuilder password(final String password) {
    this.password = password;
    return this;
  }

  public ClientBuilder done() {
    parent.requestInterceptor(new BasicAuthRequestInterceptor(username, password));
    return parent;
  }
}
