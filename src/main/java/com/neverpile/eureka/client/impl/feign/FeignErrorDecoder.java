package com.neverpile.eureka.client.impl.feign;

import feign.Response;
import feign.codec.ErrorDecoder;

public class FeignErrorDecoder implements ErrorDecoder {
	private final ErrorDecoder defaultErrorDecoder = new Default();

	@Override
	public Exception decode(final String methodKey, final Response response) {
		if(response.status() == 401){
		  // FIXME
//			FeignNeverpileClient.getInstance().renewToken();
			System.out.println("new Token");
			return new FeignClientException(response.status(), response.reason());
		}
		else if (response.status() >= 400 && response.status() <= 499) {
			return new FeignClientException(response.status(), response.reason());
		}
		else if (response.status() >= 500 && response.status() <= 599) {
			return new FeignServerException(response.status(), response.reason());
		}
		return defaultErrorDecoder.decode(methodKey, response);
	}
}