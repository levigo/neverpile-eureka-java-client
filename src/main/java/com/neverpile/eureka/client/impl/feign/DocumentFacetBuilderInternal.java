package com.neverpile.eureka.client.impl.feign;

import com.neverpile.eureka.client.core.DocumentDto;
import com.neverpile.eureka.client.core.DocumentFacetBuilder;

public interface DocumentFacetBuilderInternal<P> extends DocumentFacetBuilder<P> {
  void init(P parent, DocumentDto document);
}
