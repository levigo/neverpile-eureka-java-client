package com.neverpile.eureka.client.metadata;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonMetadataElement {

  private final MetadataElement element;

  public JsonMetadataElement(final MetadataElement e) {
    this.element = e;

    if (!e.getContentType().contains("application/json"))
      throw new IllegalArgumentException("Element isn't of type JSON");
  }

  /**
   * Unmarshal the JSON metadata as the given type using Jackson.
   * 
   * @param <T> the type
   * @param type the type's class
   * @return the unmarshaled element
   * @throws IOException in case of (Jackson) unmarshalling failures
   */
  @SuppressWarnings("unchecked")
  public <T> T as(final Class<T> type) throws IOException {
    return (T) new ObjectMapper().readerFor(type).readTree(element.getContent());
  }
  
  /**
   * Unmarshal the JSON metadata as a JSON tree using Jackson.
   * 
   * @return the unmarshaled tree
   * @throws IOException in case of (Jackson) unmarshalling failures
   */
  public JsonNode asTree() throws IOException {
    return new ObjectMapper().readTree(element.getContent());
  }

  /**
   * Update the content from the given JSON tree.
   * 
   * @param node the root of the JSON tree
   * @throws JsonProcessingException in case of (Jackson) unmarshalling failures
   */
  public void update(final ObjectNode node) throws JsonProcessingException {
    element.setContent(new ObjectMapper().writeValueAsBytes(node));
  }

  /**
   * Update the content from the given JSON-serializable object using Jackson.
   * 
   * @param jsonSerializableObject the object to be serialized
   * @throws JsonProcessingException in case of (Jackson) marshalling failures
   */
  public void update(final Object jsonSerializableObject) throws JsonProcessingException {
    element.setContent(new ObjectMapper().writeValueAsBytes(jsonSerializableObject));
  }
}
