package com.neverpile.eureka.client.core;

public interface DocumentFacetBuilderInternal<P> extends DocumentFacetBuilder<P> {
  void init(P parent, Document document);
}
