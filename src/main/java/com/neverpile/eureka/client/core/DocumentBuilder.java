package com.neverpile.eureka.client.core;

import com.neverpile.eureka.client.content.ContentElementBuilder;

public interface DocumentBuilder {
  DocumentBuilder id(String id);

  ContentElementBuilder<DocumentBuilder> contentElement();

  public <F extends DocumentFacetBuilder<DocumentBuilder>> F facet(final F facetBuilder);
  
  DocumentDto save();
}
