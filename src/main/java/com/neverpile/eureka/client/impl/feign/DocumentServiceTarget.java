package com.neverpile.eureka.client.impl.feign;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neverpile.eureka.client.content.MultipartFile;
import com.neverpile.eureka.client.core.Document;

import feign.HeaderMap;
import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;
import feign.Response;

public interface DocumentServiceTarget {
  Logger LOGGER = LoggerFactory.getLogger(DocumentServiceTarget.class);

  String url = "/api/v1/documents";

  @RequestLine("DELETE " + url + "/{documentID}")
  Object deleteDocument(@Param("documentID") String documentId);

  @RequestLine("GET " + url + "/{documentID}")
  @Headers("Accept: application/json")
  Document getDocument(@Param("documentID") String documentId);

  @RequestLine("GET " + url + "/{documentID}/history/{versionTimestamp}")
  @Headers("Accept: application/json")
  Document getDocumentVersion(@Param("documentID") String documentId,
      @Param("versionTimestamp") Instant versionTimestamp);

  @RequestLine("GET " + url + "/{documentID}/history")
  @Headers("Accept: application/json")
  List<Instant> getVersions(@Param("documentID") String documentId);

  @RequestLine("POST " + url)
  Object uploadDocument(Document doc);

  @RequestLine("POST " + url)
  @Headers("Content-Type: multipart/form-data")
  Document uploadDocumentWithContent(@Param("__DOC") Document doc, @Param("*") MultipartFile content[]);

  @RequestLine("POST " + url + "/{documentID}/content")
  @Headers("Content-Type: multipart/form-data")
  Document addContentElement(@Param("documentID") String documentId, @Param("*") MultipartFile content[]);

  @RequestLine("GET " + url + "/{documentID}/content/{elementID}")
  Response getContentElement(@Param("documentID") String documentId, @Param("elementID") String elementId);

  @RequestLine("GET " + url + "/{documentID}/content")
  Response queryContent(@Param("documentID") String documentId, @QueryMap Map<String, Object> queryMap,
                        @HeaderMap Map<String, Object> headerMap);

  @RequestLine("GET " + url + "/{documentID}/history/{versionTimestamp}/content")
  Response queryContent(@Param("documentID") String documentId, @Param("versionTimestamp") Instant versionTimestamp,
      @QueryMap Map<String, Object> queryMap, @HeaderMap Map<String, Object> headerMap);

  @Headers({"Content-Type: {contentType}", "Accept: application/json"})
  @RequestLine("PUT " + url + "/{documentID}/content/{elementID}")
  Response updateContentElement(InputStream body, @Param("documentID") String documentId,
      @Param("elementID") String contentElementId, @Param("contentType") String mediaType);

}
