package com.neverpile.eureka.client.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

//TODO - erneut mehr einbinden
public class DocumentDto {

    private String documentId;
    
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

    public void setFacet(final String name, final Object value) {
      facets.put(name, value);
    }
}
