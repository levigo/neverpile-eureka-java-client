package com.neverpile.eureka.client.metadata;

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
