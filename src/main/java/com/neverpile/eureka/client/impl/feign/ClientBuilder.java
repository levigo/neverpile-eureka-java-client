package com.neverpile.eureka.client.impl.feign;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.Module;
import com.neverpile.eureka.client.NeverpileClient;
import com.neverpile.eureka.client.content.ContentElementFacet;
import com.neverpile.eureka.client.metadata.MetadataFacet;
import com.neverpile.eureka.client.model.DocumentFacet;

import feign.Feign;
import feign.RequestInterceptor;
import feign.jackson.JacksonDecoder;

public class ClientBuilder {
  private String baseURL;
  
  private final Feign.Builder builder;
  
  ClientBuilder() {
    builder = Feign.builder()
        .errorDecoder(new FeignErrorDecoder())
        .decoder(createDecoder())
        .encoder(createEncoder());
  }

  private JacksonDecoder createDecoder() {
    return new JacksonDecoder(jacksonModules());
  }

  private FeignFormEncoder createEncoder() {
    return new FeignFormEncoder(jacksonModules());
  }

  private List<Module> jacksonModules() {
    // FIXME: improve discovery mechanism
    List<DocumentFacet> facets = Arrays.asList(new MetadataFacet(), new ContentElementFacet());
    List<Module> modules = Arrays.asList(new FacetedDocumentDtoModule(facets));
    return modules;
  }

  public ClientBuilder baseURL(final String baseURL) {
    this.baseURL = baseURL;
    return this;
  }
  
  public OAuth2ClientBuilder withOAuth2() {
    return new OAuth2ClientBuilder(this);
  }

  public ClientBuilder requestInterceptor(final RequestInterceptor i) {
    builder.requestInterceptor(i);
    return this;
  }
  
  public NeverpileClient build() {
    return new FeignNeverpileClient(builder, baseURL);
  }

  public BasicAuthBuilder withBasicAuth() {
    return new BasicAuthBuilder(this);
  }
}
