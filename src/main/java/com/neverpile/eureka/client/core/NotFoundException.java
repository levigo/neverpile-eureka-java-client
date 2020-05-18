package com.neverpile.eureka.client.core;

public class NotFoundException extends ApiException {
  private static final long serialVersionUID = 1L;

  public NotFoundException(final int code, final String msg) {
    super(code, msg);
  }

}
