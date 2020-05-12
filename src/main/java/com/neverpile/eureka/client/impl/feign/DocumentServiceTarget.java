package com.neverpile.eureka.client.impl.feign;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neverpile.eureka.client.content.MultipartFile;
import com.neverpile.eureka.client.core.Document;

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
  Document getDocumentVersion(@Param("documentID") String documentId, @Param("versionTimestamp") Instant versionTimestamp);
  
  @RequestLine("GET " + url + "/{documentID}/history")
  @Headers("Accept: application/json")
  List<Instant> getVersions(@Param("documentID") String documentId);
  
  @RequestLine("POST " + url)
  Object uploadDocument(Document doc);

  @RequestLine("POST " + url)
  @Headers("Content-Type: multipart/form-data")
  Document uploadDocumentWithContent(@Param("__DOC") Document doc, @Param("part") MultipartFile content[]);

  @RequestLine("POST " + url + "/{documentID}/content")
  Object uploadContent(@Param("documentID") String documentId, @Param("part") MultipartFile content);

  @RequestLine("GET " + url + "/{documentID}/content/{elementID}")
  Response getContentElement(@Param("documentID") String documentId, @Param("elementID") String elementId);
  
  @Headers("Accept: {accept}")
  @RequestLine("GET " + url + "/{documentID}/content")
  Response queryContent(@Param("documentID") String documentId, @QueryMap Map<String, Object> queryMap, @Param("accept") List<String> acceptHeaders);

}
