package com.neverpile.eureka.client.impl.feign;

import com.neverpile.eureka.client.DocumentFacetBuilder;
import com.neverpile.eureka.client.model.DocumentDto;

public interface DocumentFacetBuilderInternal<P> extends DocumentFacetBuilder<P> {
  void init(P parent, DocumentDto document);
}
