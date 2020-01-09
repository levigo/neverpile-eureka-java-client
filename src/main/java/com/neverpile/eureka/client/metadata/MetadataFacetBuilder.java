package com.neverpile.eureka.client.metadata;

import com.neverpile.eureka.client.core.DocumentFacetBuilder;

public interface MetadataFacetBuilder<P> extends DocumentFacetBuilder<P> {
  GenericMetadataElementBuilder<P> genericMetadata(String schema);
  
  JsonMetadataElementBuilder<P> jsonMetadata(String schema);

  XmlMetadataElementBuilder<P> xmlMetadata(String schema);

  static <P> MetadataFacetBuilder<P> metadata() {
    return new MetadataFacetBuilderImpl<P>();
  }
}
