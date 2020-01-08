package com.neverpile.eureka.client.metadata;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.neverpile.eureka.client.model.DocumentFacet;
import com.neverpile.eureka.client.model.Metadata;

public class MetadataFacet implements DocumentFacet {


  @Override
  public String getName() {
    return "metadata";
  }

  @Override
  public JavaType getValueType(final TypeFactory f) {
    return f.constructType(Metadata.class);
  }

}
