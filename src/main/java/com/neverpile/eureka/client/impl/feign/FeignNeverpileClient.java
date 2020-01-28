package com.neverpile.eureka.client.impl.feign;

import com.neverpile.eureka.client.core.DocumentService;
import com.neverpile.eureka.client.core.NeverpileEurekaClient;

import feign.Feign;


public class FeignNeverpileClient implements NeverpileEurekaClient {
  public static EurekaClientBuilder builder() {
    return new EurekaClientBuilder();
  }

  private final Feign feign;
  private final String baseURI;

  FeignNeverpileClient(final Feign feign, final String baseURI) {
    this.feign = feign;
    this.baseURI = baseURI;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.neverpile.eureka.client.impl.NeverpileClient#documentService()
   */
  @Override
  public DocumentService documentService() {
    return new DocumentServiceImpl(feign, baseURI);
  }
}
