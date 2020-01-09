package com.neverpile.eureka.client.core;

import java.io.IOException;
import java.io.InputStream;

public interface DocumentService {
  public interface ContentElementResponse {
    String getMediaType();
    
    InputStream getContent() throws IOException;
    
    Digest getDigest();
  }
  
  DocumentDto getDocument(String documentId);

  ContentElementResponse getContentElement(String documentId, String elementId) throws IOException;

  DocumentBuilder newDocument();
}
