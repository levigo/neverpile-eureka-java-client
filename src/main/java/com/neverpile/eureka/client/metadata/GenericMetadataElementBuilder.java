package com.neverpile.eureka.client.metadata;

public interface GenericMetadataElementBuilder<P> {
  
  GenericMetadataElementBuilder<P> mediaType(String mediaType);
  
  GenericMetadataElementBuilder<P> content(byte[] content);
  
  P attach();
  
}
