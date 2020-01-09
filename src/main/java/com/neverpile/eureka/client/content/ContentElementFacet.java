package com.neverpile.eureka.client.content;

import java.util.List;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.neverpile.eureka.client.core.ContentElement;
import com.neverpile.eureka.client.core.DocumentFacet;

public class ContentElementFacet implements DocumentFacet<List<ContentElement>> {
  @Override
  public String getName() {
    return "contentElements";
  }

  @Override
  public JavaType getValueType(final TypeFactory f) {
    return f.constructCollectionType(List.class, ContentElement.class);
  }

}
