package com.neverpile.eureka.client.metadata;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface JsonMetadataElementBuilder<P> {

  JsonMetadataElementBuilder<P> content(ObjectNode node);
  
  JsonMetadataElementBuilder<P> content(String serializedJson);
  
  JsonMetadataElementBuilder<P> content(Object jsonSerializableObject);

  P attach();
  
}
