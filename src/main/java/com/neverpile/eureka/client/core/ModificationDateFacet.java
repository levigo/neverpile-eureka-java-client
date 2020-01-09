package com.neverpile.eureka.client.core;

import java.time.Instant;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ModificationDateFacet implements DocumentFacet<Instant> {


  @Override
  public String getName() {
    return "dateModified";
  }

  @Override
  public JavaType getValueType(final TypeFactory f) {
    return f.constructType(Instant.class);
  }

}
