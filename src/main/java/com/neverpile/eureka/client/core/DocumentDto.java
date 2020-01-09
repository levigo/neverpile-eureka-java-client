package com.neverpile.eureka.client.core;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class DocumentDto {

    private String documentId;
    
    private Instant versionTimestamp; 
    
    private final Map<String, Object> facets = new HashMap<>();

    public DocumentDto() {
    }

    public DocumentDto(final String newId) {
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
    public <V> Optional<V> facet(final DocumentFacet<V> facet) {
      return (Optional<V>) Optional.ofNullable(facets.get(facet.getName()));
    }

    public Instant getVersionTimestamp() {
      return versionTimestamp;
    }

    public void setVersionTimestamp(final Instant versionTimestamp) {
      this.versionTimestamp = versionTimestamp;
    }
}
