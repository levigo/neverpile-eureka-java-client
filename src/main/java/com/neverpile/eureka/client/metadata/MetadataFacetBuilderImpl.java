package com.neverpile.eureka.client.metadata;

import com.neverpile.eureka.client.core.Document;
import com.neverpile.eureka.client.core.DocumentFacetBuilder;

public class MetadataFacetBuilderImpl<P> implements MetadataFacetBuilder<P>, DocumentFacetBuilder<P> {

  private P parent;
  
  private final Metadata metadata = new Metadata();

  public MetadataFacetBuilderImpl() {
  }
  
  public void init(final P parent, final Document document) {
    this.parent = parent;
    document.facet("metadata", metadata);
  }
  
  @Override
  public P attach() {
    return parent;
  }

  @Override
  public GenericMetadataElementBuilder<P> genericMetadata(final String schema) {
    return new GenericMetadataElementBuilderImpl<>(parent, schema, metadata);
  }

  @Override
  public JsonMetadataElementBuilder<P> jsonMetadata(final String schema) {
    return new JsonMetadataElementBuilderImpl<>(parent, schema, metadata);
  }

  @Override
  public XmlMetadataElementBuilder<P> xmlMetadata(final String schema) {
    return new XmlMetadataElementBuilderImpl<>(parent, schema, metadata);
  }

}
