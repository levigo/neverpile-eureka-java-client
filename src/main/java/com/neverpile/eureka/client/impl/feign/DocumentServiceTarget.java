package com.neverpile.eureka.client.impl.feign;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neverpile.eureka.client.core.DocumentDto;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.Response;

public interface DocumentServiceTarget {
  Logger LOGGER = LoggerFactory.getLogger(DocumentServiceTarget.class);

  String url = "/api/v1/documents";

  @RequestLine("DELETE " + url + "/{documentID}")
  Object deleteDocument(@Param("documentID") String documentId);

  @RequestLine("GET " + url + "/{documentID}")
  @Headers("Accept: application/json")
  DocumentDto getDocument(@Param("documentID") String documentId);

  @RequestLine("POST " + url)
  Object uploadDocument(DocumentDto doc);

  @RequestLine("POST " + url)
  @Headers("Content-Type: multipart/form-data")
  DocumentDto uploadDocumentWithContent(@Param("__DOC") DocumentDto doc, @Param("part") MultipartFile content[]);

  @RequestLine("POST " + url + "/{documentID}/content")
  Object uploadContent(@Param("documentID") String documentId, @Param("part") MultipartFile content);

  @RequestLine("GET " + url + "/{documentID}/content/{elementID}")
  Response getContentElement(@Param("documentID") String documentId, @Param("elementID") String elementId);
}
