package com.neverpile.eureka.client.core;

public class ServerException extends ApiException {
  private static final long serialVersionUID = 1L;

  public ServerException(final int code, final String msg) {
    super(code, msg);
  }

}
