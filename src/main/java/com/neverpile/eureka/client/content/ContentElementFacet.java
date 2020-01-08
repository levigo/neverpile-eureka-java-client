package com.neverpile.eureka.client.content;

import java.util.List;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.neverpile.eureka.client.model.ContentElementDto;
import com.neverpile.eureka.client.model.DocumentFacet;

public class ContentElementFacet implements DocumentFacet {
  @Override
  public String getName() {
    return "contentElements";
  }

  @Override
  public JavaType getValueType(final TypeFactory f) {
    return f.constructCollectionType(List.class, ContentElementDto.class);
  }

}
