package com.neverpile.eureka.client.core;

public class UnauthorizedException extends ApiException {
  private static final long serialVersionUID = 1L;

  public UnauthorizedException(final int code, final String msg) {
    super(code, msg);
  }

}
