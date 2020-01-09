package com.neverpile.eureka.client.metadata;


import java.io.UnsupportedEncodingException;

public class AbstractMetadataElementBuilderImpl<P, I> {

  protected final P parent;
  
  protected final MetadataElement element;

  public AbstractMetadataElementBuilderImpl(final P parent, final String schema, final Metadata metadata) {
    this.parent = parent;

    element = new MetadataElement();
    element.setSchema(schema);
    
    metadata.elements().put(schema, element);
  }

  @SuppressWarnings("unchecked")
  public I content(final String s) {
    try {
      element.setContent(s.getBytes("utf-8"));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    return (I) this;
  }

  @SuppressWarnings("unchecked")
  public I content(final byte[] content) {
    element.setContent(content);
    return (I) this;
  }

  public P attach() {
    return parent;
  }

}