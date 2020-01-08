package com.neverpile.eureka.client.metadata;

import org.springframework.http.MediaType;

public interface GenericMetadataElementBuilder<P> {
  
  GenericMetadataElementBuilder<P> mediaType(String mediaType);
  
  GenericMetadataElementBuilder<P> mediaType(MediaType mediaType);
  
  GenericMetadataElementBuilder<P> content(byte[] content);
  
  P attach();
  
}
