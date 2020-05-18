package com.neverpile.eureka.client.impl.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neverpile.eureka.client.core.DocumentService;
import com.neverpile.eureka.client.core.NeverpileEurekaClient;

import feign.Feign;


public class FeignNeverpileClient implements NeverpileEurekaClient {
  public static EurekaClientBuilder builder() {
    return new EurekaClientBuilder();
  }

  private final Feign feign;
  private final String baseURI;
  private final ObjectMapper objectMapper;

  FeignNeverpileClient(final Feign feign, final ObjectMapper objectMapper, final String baseURI) {
    this.feign = feign;
    this.objectMapper = objectMapper;
    this.baseURI = baseURI;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.neverpile.eureka.client.impl.NeverpileClient#documentService()
   */
  @Override
  public DocumentService documentService() {
    return new DocumentServiceImpl(feign, baseURI, objectMapper);
  }
}
