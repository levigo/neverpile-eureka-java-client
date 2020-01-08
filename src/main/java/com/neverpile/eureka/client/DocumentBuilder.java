package com.neverpile.eureka.client;

import com.neverpile.eureka.client.model.DocumentDto;

public interface DocumentBuilder {
  DocumentBuilder id(String id);

  ContentElementBuilder<DocumentBuilder> contentElement(String id);

  public <F extends DocumentFacetBuilder<DocumentBuilder>> F facet(final F facetBuilder);
  
  DocumentDto save();
}
