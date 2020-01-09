package com.neverpile.eureka.client.metadata;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.neverpile.eureka.client.core.DocumentFacet;
import com.neverpile.eureka.client.core.Metadata;

public class MetadataFacet implements DocumentFacet<Metadata> {


  @Override
  public String getName() {
    return "metadata";
  }

  @Override
  public JavaType getValueType(final TypeFactory f) {
    return f.constructType(Metadata.class);
  }

}
