package com.neverpile.eureka.client.core;

public class ForbiddenException extends ApiException {
  private static final long serialVersionUID = 1L;

  public ForbiddenException(final int code, final String msg) {
    super(code, msg);
  }

}
