package com.neverpile.eureka.client.core;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public interface DocumentFacet<T> {
  String getName();

  JavaType getValueType(TypeFactory f);
}
