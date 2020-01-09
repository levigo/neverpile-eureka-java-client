package com.neverpile.eureka.client.core;

import java.time.Instant;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class CreationDateFacet implements DocumentFacet<Instant> {


  @Override
  public String getName() {
    return "dateCreated";
  }

  @Override
  public JavaType getValueType(final TypeFactory f) {
    return f.constructType(Instant.class);
  }

}
