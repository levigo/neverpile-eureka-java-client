package com.neverpile.eureka.client.impl.feign;

import com.neverpile.eureka.client.ApiException;

public class FeignServerException extends ApiException {

	private static final long serialVersionUID = 1L;

	public FeignServerException(final int code, final String msg) {
		super(code, msg);
	}
}

