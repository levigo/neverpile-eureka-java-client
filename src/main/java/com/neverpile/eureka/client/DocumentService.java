package com.neverpile.eureka.client;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.MediaType;

import com.neverpile.eureka.client.model.Digest;
import com.neverpile.eureka.client.model.DocumentDto;

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
