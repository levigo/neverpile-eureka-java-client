package com.neverpile.eureka.client;

import com.neverpile.eureka.client.impl.feign.EurekaClientBuilder;

public class EurekaClient {
  private EurekaClient() {
    // just static methods
  }
  
  public static EurekaClientBuilder builder() {
    return new EurekaClientBuilder();
  }
}
