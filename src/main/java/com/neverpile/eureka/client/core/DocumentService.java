package com.neverpile.eureka.client.core;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.MediaType;

public interface DocumentService {
  public interface ContentElementResponse {
    MediaType getMediaType();
    
    InputStream getContent() throws IOException;
    
    Digest getDigest();
  }
  
  DocumentDto getDocument(String documentId);

  ContentElementResponse getContentElement(String documentId, String elementId) throws IOException;

  DocumentBuilder newDocument();
}
