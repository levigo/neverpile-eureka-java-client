package com.neverpile.eureka.client.core;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.neverpile.eureka.client.content.ContentElementListBuilder;

public interface DocumentService {
  public interface ContentElementResponse {
    /**
     * The element's media type.
     * 
     * @return the media type
     */
    String getMediaType();

    /**
     * The element's digest.
     * 
     * @return the digest
     */
    Digest getDigest();

    /**
     * Return the element payload stream.
     * 
     * @return the payload stream
     * @throws IOException
     */
    InputStream getContent() throws IOException;

    /**
     * Return the enclosing document's version timestamp.
     * 
     * @return the version timestamp
     */
    Instant getVersionTimestamp();
  }

  /**
   * Create a {@link DocumentBuilder} which can be used to create a new document.
   * 
   * @return a DocumentBuilder
   */
  DocumentBuilder newDocument();

  /**
   * Get the current version of the document with the given id.
   * 
   * @param documentId the document's id
   * @return the current version or the empty optional if there is no such document
   */
  Optional<Document> getDocument(String documentId);

  /**
   * Get a particular version of the document with the given id.
   * 
   * @param documentId the document's id
   * @param versionTimestamp the version's timestamp
   * @return the current version or the empty optional if there is no such document
   */
  Optional<Document> getDocumentVersion(String documentId, Instant versionTimestamp);

  /**
   * Get the version timestamps of all versions of the document with the given id.
   * 
   * @param documentId the document's id
   * @return the list of version timestamps or the empty list, if the document does not exist
   */
  List<Instant> getVersions(String documentId);

  /**
   * Retrieve a content element by document id and content element id
   * 
   * @param documentId the document id
   * @param elementId the content element id
   * @return a content element response
   * @throws IOException
   */
  ContentElementResponse getContentElement(String documentId, String elementId) throws IOException;

  /**
   * Create a {@link ContentQueryBuilder} which can be used to query for content elements of the
   * document with the given id.
   * 
   * @param documentId the id of the document for which to query for content elements
   * @return a ContentQueryBuilder
   */
  ContentQueryBuilder queryContent(String documentId);

  /**
   * Create a {@link ContentQueryBuilder} which can be used to query for content elements of the
   * document with the given id and version timestamp.
   * 
   * @param documentId the id of the document for which to query for content elements
   * @param versionTimestamp the version timestamp of the document
   * @return a ContentQueryBuilder
   */
  ContentQueryBuilder queryContent(String documentId, Instant versionTimestamp);

  /**
   * Add an content element to an existing document using the contents of the given input stream.
   * 
   * @param documentId the id of the document to update
   * @param is the input stream from which to update the content element
   * @param mediaType the media type of the supplied content
   * @param role the element role
   * @param filename TODO
   * @return the {@link ContentElement} descriptor of the updated element
   */
  ContentElement addContentElement(String documentId, InputStream is, String mediaType, String role, String filename);

  /**
   * Add multiple content elements in one go.
   * 
   * @param documentId the document to add content to.
   * @return a builder used to attach the content elements to be added
   */
  ContentElementListBuilder addContent(String documentId);

  /**
   * Update an existing content element by replacing its content stream with the contents of the
   * given input stream. Updating a content element may, depending on the configuration of the
   * server, change the content element's id. The new id will be returned as part of the returned
   * content element descriptor.
   * 
   * @param documentId the id of the document to update
   * @param contentElementId the id of the content element to update
   * @param is the input stream from which to update the content element
   * @param mediaType the media type of the supplied content
   * @return the {@link ContentElement} descriptor of the updated element
   */
  ContentElement updateContentElement(String documentId, String contentElementId, InputStream is, String mediaType);

  /**
   * Update an existing content element by replacing its content stream with the contents of the
   * input stream supplied by the given supplier. Updating a content element may, depending on the
   * configuration of the server, change the content element's id. The new id will be returned as
   * part of the returned content element descriptor.
   * 
   * @param documentId the id of the document to update
   * @param contentElementId the id of the content element to update
   * @param iss the input stream supplier from which to update the content element
   * @param mediaType the media type of the supplied content
   * @return the {@link ContentElement} descriptor of the updated element
   */
  ContentElement updateContentElement(String documentId, String contentElementId, Supplier<InputStream> iss,
      String mediaType);

  /**
   * Update an existing content element by replacing its content stream with the contents of the
   * given byte array. Updating a content element may, depending on the configuration of the server,
   * change the content element's id. The new id will be returned as part of the returned content
   * element descriptor.
   * 
   * @param documentId the id of the document to update
   * @param contentElementId the id of the content element to update
   * @param data the byte array from which to update the content element
   * @param mediaType the media type of the supplied content
   * @return the {@link ContentElement} descriptor of the updated element
   */
  ContentElement updateContentElement(String documentId, String contentElementId, byte[] data, String mediaType);

  /**
   * Delete a Document via its ID.
   * 
   * @param documentID the document ID
   */
  void deleteDocument(String documentID);
}
