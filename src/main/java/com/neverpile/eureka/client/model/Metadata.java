package com.neverpile.eureka.client.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Metadata {
  public static Metadata with(final String name, final MetadataElement metadata) {
    return new Metadata().set(name, metadata);
  }
  
  private final Map<String, MetadataElement> elements = new HashMap<>();
  
  @JsonAnyGetter
  public Map<String, MetadataElement> get() {
    return elements;
  }
  
  @JsonAnySetter
  private Metadata set(final String name, final MetadataElement element) {
    elements.put(name, element);
    return this;
  }

  @JsonIgnore
  public Map<String, MetadataElement> getElements() {
    return elements;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + elements.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Metadata other = (Metadata) obj;
    if (!elements.equals(other.elements))
      return false;
    return true;
  }
}
