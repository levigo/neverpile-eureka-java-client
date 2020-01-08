package com.neverpile.eureka.client.model;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public interface DocumentFacet {
  String getName();

  JavaType getValueType(TypeFactory f);
}
