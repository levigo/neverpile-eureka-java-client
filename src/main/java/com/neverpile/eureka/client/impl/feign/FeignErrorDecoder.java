package com.neverpile.eureka.client.impl.feign;

import com.neverpile.eureka.client.core.ClientException;
import com.neverpile.eureka.client.core.ForbiddenException;
import com.neverpile.eureka.client.core.NotFoundException;
import com.neverpile.eureka.client.core.ServerException;
import com.neverpile.eureka.client.core.UnauthorizedException;

import feign.Response;
import feign.codec.ErrorDecoder;

public class FeignErrorDecoder implements ErrorDecoder {
  private final ErrorDecoder defaultErrorDecoder = new Default();

  @Override
  public Exception decode(final String methodKey, final Response response) {
    switch (response.status()){
      case 401 :
        return new UnauthorizedException(response.status(), response.reason());
      case 403 :
        return new ForbiddenException(response.status(), response.reason());
      case 404 :
        return new NotFoundException(response.status(), response.reason());
      default :
        if (response.status() >= 400 && response.status() <= 499)
          return new ClientException(response.status(), response.reason());
        if (response.status() >= 500 && response.status() <= 599)
          return new ServerException(response.status(), response.reason());
        return defaultErrorDecoder.decode(methodKey, response);
    }
  }
}