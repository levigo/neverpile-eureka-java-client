package com.neverpile.eureka.client.impl.feign;

import com.neverpile.eureka.client.DocumentService;
import com.neverpile.eureka.client.NeverpileClient;

import feign.Feign;
import feign.Feign.Builder;


public class FeignNeverpileClient implements NeverpileClient {
  public static ClientBuilder builder() {
    return new ClientBuilder();
  }

  private final Builder builder;
  private final String baseURI;

  FeignNeverpileClient(final Feign.Builder builder, final String baseURI) {
    this.builder = builder;
    this.baseURI = baseURI;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.neverpile.eureka.client.impl.NeverpileClient#documentService()
   */
  @Override
  public DocumentService documentService() {
    return new DocumentServiceImpl(builder, baseURI);
  }
}
