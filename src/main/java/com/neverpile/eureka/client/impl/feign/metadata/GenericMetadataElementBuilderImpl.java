package com.neverpile.eureka.client.impl.feign.metadata;

import com.neverpile.eureka.client.core.Metadata;
import com.neverpile.eureka.client.impl.feign.AbstractMetadataElementBuilderImpl;
import com.neverpile.eureka.client.metadata.GenericMetadataElementBuilder;

public class GenericMetadataElementBuilderImpl<P>
    extends
      AbstractMetadataElementBuilderImpl<P, GenericMetadataElementBuilderImpl<P>>
    implements
      GenericMetadataElementBuilder<P> {

  public GenericMetadataElementBuilderImpl(final P parent, final String schema, final Metadata metadata) {
    super(parent, schema, metadata);
  }

  @Override
  public GenericMetadataElementBuilder<P> mediaType(final String mediaType) {
    element.setContentType(mediaType);
    return this;
  }

  @Override
  public P attach() {
    return parent;
  }

}
