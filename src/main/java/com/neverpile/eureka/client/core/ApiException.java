package com.neverpile.eureka.client.core;

public abstract class ApiException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final int code;

	public ApiException(final int code, final String msg) {
		super(msg);
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
