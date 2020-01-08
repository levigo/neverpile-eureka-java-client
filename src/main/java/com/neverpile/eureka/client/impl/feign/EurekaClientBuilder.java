package com.neverpile.eureka.client.impl.feign;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.Module;
import com.neverpile.eureka.client.content.ContentElementFacet;
import com.neverpile.eureka.client.core.DocumentFacet;
import com.neverpile.eureka.client.core.NeverpileClient;
import com.neverpile.eureka.client.metadata.MetadataFacet;

import feign.Feign;
import feign.RequestInterceptor;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

public class EurekaClientBuilder {
  private String baseURL;
  
  private final Feign.Builder builder;
  
  public EurekaClientBuilder() {
    builder = Feign.builder()
        .errorDecoder(new FeignErrorDecoder())
        .decoder(createDecoder())
        .encoder(createEncoder());
  }

  private JacksonDecoder createDecoder() {
    return new JacksonDecoder(jacksonModules());
  }

  private FormEncoder createEncoder() {
    return new FormEncoder(new JacksonEncoder(jacksonModules()));
  }

  private List<Module> jacksonModules() {
    // FIXME: improve discovery mechanism
    List<DocumentFacet> facets = Arrays.asList(new MetadataFacet(), new ContentElementFacet());
    List<Module> modules = Arrays.asList(new FacetedDocumentDtoModule(facets));
    return modules;
  }

  public EurekaClientBuilder baseURL(final String baseURL) {
    this.baseURL = baseURL;
    return this;
  }
  
  public OAuth2ClientBuilder withOAuth2() {
    return new OAuth2ClientBuilder(this);
  }

  public EurekaClientBuilder requestInterceptor(final RequestInterceptor i) {
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
