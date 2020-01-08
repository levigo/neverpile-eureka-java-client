package com.neverpile.eureka.client.impl.feign.metadata;

import com.neverpile.eureka.client.core.DocumentDto;
import com.neverpile.eureka.client.core.Metadata;
import com.neverpile.eureka.client.impl.feign.DocumentFacetBuilderInternal;
import com.neverpile.eureka.client.metadata.GenericMetadataElementBuilder;
import com.neverpile.eureka.client.metadata.JsonMetadataElementBuilder;
import com.neverpile.eureka.client.metadata.MetadataFacetBuilder;
import com.neverpile.eureka.client.metadata.XmlMetadataElementBuilder;

public class MetadataFacetBuilderImpl<P> implements MetadataFacetBuilder<P>, DocumentFacetBuilderInternal<P> {

  private P parent;
  
  private final Metadata metadata = new Metadata();

  public MetadataFacetBuilderImpl() {
  }
  
  public void init(final P parent, final DocumentDto document) {
    this.parent = parent;
    document.facet("metadata", metadata);
  }
  
  @Override
  public P attach() {
    return parent;
  }

  @Override
  public GenericMetadataElementBuilder<P> genericMetadata(final String schema) {
    return new GenericMetadataElementBuilderImpl<P>(parent, schema, metadata);
  }

  @Override
  public JsonMetadataElementBuilder<P> jsonMetadata(final String schema) {
    return new JsonMetadataElementBuilderImpl<P>(parent, schema, metadata);
  }

  @Override
  public XmlMetadataElementBuilder<P> xmlMetadata(final String schema) {
    return new XmlMetadataElementBuilderImpl<P>(parent, schema, metadata);
  }

}
