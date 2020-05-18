package com.neverpile.eureka.client.core;

public class ClientException extends ApiException {
  private static final long serialVersionUID = 1L;

  public ClientException(final int code, final String msg) {
    super(code, msg);
  }

}
