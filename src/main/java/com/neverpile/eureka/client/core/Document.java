package com.neverpile.eureka.client.core;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties("_links")
public class Document {

    private String documentId;
    
    private Instant versionTimestamp; 
    
    private final Map<String, Object> facets = new HashMap<>();

    public Document() {
    }

    public Document(final String newId) {
        this.documentId = newId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(final String documentId) {
        this.documentId = documentId;
    }

    @JsonAnyGetter
    public Map<String, Object> facets() {
        return facets;
    }

    @JsonIgnore // handled by custom deserializer
    public void facet(final String name, final Object value) {
        facets.put(name, value);
    }
    
    @JsonIgnore // handled by custom deserializer
    @SuppressWarnings("unchecked")
    public <V> Optional<V> facet(final Class<? extends DocumentFacet<V>> facet) {
      try {
        // FIXME: cache facet instances?
        return (Optional<V>) Optional.ofNullable(facets.get(facet.newInstance().getName()));
      } catch (InstantiationException | IllegalAccessException e) {
        throw new RuntimeException("Can't create facet instance", e);
      }
    }

    @JsonProperty(required = false)
    public Instant getVersionTimestamp() {
      return versionTimestamp;
    }

    public void setVersionTimestamp(final Instant versionTimestamp) {
      this.versionTimestamp = versionTimestamp;
    }
}
