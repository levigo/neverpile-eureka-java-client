package com.neverpile.eureka.client.core;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

public interface DocumentService {
  public interface ContentElementResponse {
    String getMediaType();
    
    InputStream getContent() throws IOException;
    
    Digest getDigest();
  }
  
  /**
   * Get the current version of the document with the given id.
   * 
   * @param documentId the document's id
   * @return the current version or the empty optional if there is no such document
   */
  Document getDocument(String documentId);

  /**
   * Get a particular version of the document with the given id.
   * 
   * @param documentId the document's id
   * @param versionTimestamp the version's timestamp
   * @return the current version or the empty optional if there is no such document
   */
  Document getDocumentVersion(String documentId, Instant versionTimestamp);
  
  /**
   * Get the version timestamps of all versions of the document with the given id.
   * 
   * @param documentId the document's id
   * @return the list of version timestamps or the empty list, if the document does not exist
   */
  List<Instant> getVersions(String documentId);
  
  ContentElementResponse getContentElement(String documentId, String elementId) throws IOException;

  DocumentBuilder newDocument();

  ContentQueryBuilder queryContent(String documentId);

}
