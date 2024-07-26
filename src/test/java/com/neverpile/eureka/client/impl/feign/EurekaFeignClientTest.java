package com.neverpile.eureka.client.impl.feign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static com.github.tomakehurst.wiremock.client.WireMock.aMultipart;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.neverpile.eureka.client.EurekaClient;
import com.neverpile.eureka.client.content.ContentElementFacet;
import com.neverpile.eureka.client.core.ContentElement;
import com.neverpile.eureka.client.core.CreationDateFacet;
import com.neverpile.eureka.client.core.Document;
import com.neverpile.eureka.client.core.DocumentService.ContentElementResponse;
import com.neverpile.eureka.client.core.HashAlgorithm;
import com.neverpile.eureka.client.core.ModificationDateFacet;
import com.neverpile.eureka.client.core.NeverpileEurekaClient;
import com.neverpile.eureka.client.metadata.Metadata;
import com.neverpile.eureka.client.metadata.MetadataFacet;
import com.neverpile.eureka.client.metadata.MetadataFacetBuilder;

@WireMockTest
class EurekaFeignClientTest {

  private static NeverpileEurekaClient client;

  @BeforeAll
  public static void createClient(WireMockRuntimeInfo wmRuntimeInfo) {
    client = EurekaClient.builder().baseURL("http://localhost:" + wmRuntimeInfo.getHttpPort()).build();
  }

  @Test
  void testThat_documentCanBeRetrieved() throws Exception {
    stubFor( //
        get(urlEqualTo("/api/v1/documents/aDocument")) //
            .withHeader("Accept", equalTo("application/json")) //
            .willReturn( //
                aResponse() //
                    .withStatus(200) //
                    .withHeader("Content-Type", "application/json") //
                    .withBodyFile("exampleDocument.json")));

    Optional<Document> document = client.documentService().getDocument("aDocument");

    assertThat(document).isPresent();
    assertThat(document.get().getDocumentId()).isEqualTo("aDocument");
    assertThat(document.get().facet(CreationDateFacet.class)).isPresent().hasValue(Instant.parse("2019-12-09T14:25:53.747Z"));
    assertThat(document.get().facet(ModificationDateFacet.class)).isPresent().hasValue(
        Instant.parse("2019-12-09T14:25:53.747Z"));

    verify(getRequestedFor(urlMatching("/api/v1/documents/aDocument")));
  }

  @Test
  void testThat_metadataFacetIsSupported() throws Exception {
    stubFor( //
        get(urlEqualTo("/api/v1/documents/aDocument")) //
            .withHeader("Accept", equalTo("application/json")) //
            .willReturn( //
                aResponse() //
                    .withStatus(200) //
                    .withHeader("Content-Type", "application/json") //
                    .withBodyFile("exampleDocument.json")));

    Optional<Document> document = client.documentService().getDocument("aDocument");

    assertThat(document).isPresent();
    Metadata metadata = document.get().facet(MetadataFacet.class).get();
    assertThat(metadata).isNotNull();
    assertThat(metadata.elements()).containsKey("foo");
    assertThat(metadata.jsonElement("foo").get().asTree().path("foo").asText()).isEqualTo("bar2");
    assertThat(metadata.jsonElement("foo").get().asTree().path("bar").asText()).isEqualTo("baz2");

    verify(getRequestedFor(urlMatching("/api/v1/documents/aDocument")));
  }

  @Test
  void testThat_contentElementFacetIsSupported() throws Exception {
    stubFor( //
        get(urlEqualTo("/api/v1/documents/aDocument")) //
            .withHeader("Accept", equalTo("application/json")) //
            .willReturn( //
                aResponse() //
                    .withStatus(200) //
                    .withHeader("Content-Type", "application/json") //
                    .withBodyFile("exampleDocument.json")));

    Optional<Document> document = client.documentService().getDocument("aDocument");

    assertThat(document).isPresent();
    List<ContentElement> ce = document.get().facet(ContentElementFacet.class).get();
    assertThat(ce).isNotNull().hasSize(1);

    verify(getRequestedFor(urlMatching("/api/v1/documents/aDocument")));
  }

  @Test
  void testThat_documentCreationFromMultipartWorks() throws Exception {
    // We match the request parts in two identical calls due to limitations of WireMock's
    // .withMultipartRequestBody(aMultipart()...
    stubFor( //
        post(urlEqualTo("/api/v1/documents")) //
            .withMultipartRequestBody( //
                aMultipart() //
                    .withHeader("Content-Disposition", matching(".*name=\"__DOC\".*")) //
                    .withBody(equalToJson("{\"documentId\":\"aDocument\",\"versionTimestamp\":null}")) //
            ) //
            .willReturn( //
                aResponse() //
                    .withStatus(200) //
                    .withHeader("Content-Type", "application/json") //
                    .withBodyFile("exampleDocument.json")));

    Document document = client.documentService() //
        .newDocument() //
        .id("aDocument") //
        .contentElement() //
        .fileName("foo.txt") //
        .role("part") //
        .content(new ByteArrayInputStream("foo".getBytes())) //
        .mediaType("text/plain") //
        .attach() //
        .save();

    assertThat(document.getDocumentId()).isEqualTo("aDocument");
    assertThat(document.facet(CreationDateFacet.class)).isPresent().hasValue(Instant.parse("2019-12-09T14:25:53.747Z"));
    assertThat(document.facet(ModificationDateFacet.class)).isPresent().hasValue(
        Instant.parse("2019-12-09T14:25:53.747Z"));

    verify(postRequestedFor(urlMatching("/api/v1/documents")));

    // The second call
    stubFor( //
        post(urlEqualTo("/api/v1/documents")) //
            .withMultipartRequestBody( //
                aMultipart() //
                    .withHeader("Content-Disposition", matching(".*name=\"part\".*")) //
                    .withBody(equalTo("foo")) //
            ) //
            .willReturn( //
                aResponse() //
                    .withStatus(200) //
                    .withHeader("Content-Type", "application/json") //
                    .withBodyFile("exampleDocument.json")));

    document = client.documentService() //
        .newDocument() //
        .id("aDocument") //
        .contentElement() //
        .fileName("foo.txt") //
        .role("part") //
        .content(new ByteArrayInputStream("foo".getBytes())) //
        .mediaType("text/plain") //
        .attach() //
        .save();

    verify(postRequestedFor(urlMatching("/api/v1/documents")));
  }

  @Test
  void testThat_documentCreationWithMetadataWorks() throws Exception {
    // We match the request parts in two identical calls due to limitations of WireMock's
    // .withMultipartRequestBody(aMultipart()...
    stubFor( //
        post(urlEqualTo("/api/v1/documents")) //
            .withMultipartRequestBody( //
                aMultipart() //
                    .withHeader("Content-Disposition", matching(".*name=\"__DOC\".*")) //
                    .withBody(equalToJson("{\"documentId\":\"aDocument\",\"versionTimestamp\":null,\"metadata\" : {\r\n"
                        + "  \"foo\" : {\r\n" + //
                        "    \"schema\" : \"foo\",\r\n" + //
                        "    \"contentType\" : \"application/json\",\r\n" + //
                        "    \"content\" : \"ImZvbyI6ImJhciI=\",\r\n" + //
                        "    \"encryption\" : null,\r\n" + //
                        "    \"keyHint\" : null,\r\n" + //
                        "    \"dateCreated\" : null,\r\n" + //
                        "    \"dateModified\" : null\r\n" + //
                        "  }\r\n" + //
                        "}}")) //
            ) //
            .willReturn( //
                aResponse() //
                    .withStatus(200) //
                    .withHeader("Content-Type", "application/json") //
                    .withBodyFile("exampleDocument.json")));

    Document document = client.documentService() //
        .newDocument() //
        .id("aDocument") //
        .contentElement() //
        .fileName("foo.txt") //
        .role("part") //
        .content(new ByteArrayInputStream("foo".getBytes())) //
        .mediaType("text/plain") //
        .attach() //
        .facet(MetadataFacetBuilder.metadata()) //
        .jsonMetadata("foo") //
        .content("\"foo\":\"bar\"") //
        .attach() //
        .save();

    Metadata metadata = document.facet(MetadataFacet.class).get();
    assertThat(metadata).isNotNull();
    assertThat(metadata.elements()).containsKey("foo");

    verify(postRequestedFor(urlMatching("/api/v1/documents")));
  }
  
  @Test
  void testThat_contentElementCanBeAdded() throws Exception {
    // We match the request parts in two identical calls due to limitations of WireMock's
    // .withMultipartRequestBody(aMultipart()...
    stubFor( //
        post(urlEqualTo("/api/v1/documents/aDocument/content")) //
            .withMultipartRequestBody( //
                aMultipart() //
                    .withHeader("Content-Disposition", matching("form-data; name=\"part\";\\s*filename=\"foo.txt\"(;.*)?")) //
                    .withBody(equalTo("Hello, world!")) //
            ) //
            .willReturn( //
                aResponse() //
                    .withStatus(200) //
                    .withHeader("Content-Type", "application/json") //
                    .withBodyFile("exampleDocument.json")));

    ContentElement ce = client.documentService().addContentElement("aDocument", new ByteArrayInputStream("Hello, world!".getBytes()), "text/plain", "part", "foo.txt");

    assertThat(ce.getVersionTimestamp()).isEqualTo(Instant.ofEpochMilli(1));
    assertThat(ce.getId()).isEqualTo("6709b8b1-f9f2-4d2b-8db1-da14c6d7dfc4");
    assertThat(ce.getRole()).isEqualTo("part");
    assertThat(ce.getFileName()).isEqualTo("foo.txt");

    verify(postRequestedFor(urlMatching("/api/v1/documents/aDocument/content")));
  }
  
  @Test
  void testThat_contentElementCanBeAddedWithBuilder() throws Exception {
    // We match the request parts in two identical calls due to limitations of WireMock's
    // .withMultipartRequestBody(aMultipart()...
    stubFor( //
        post(urlEqualTo("/api/v1/documents/aDocument/content")) //
        .withMultipartRequestBody( //
            aMultipart() //
            .withHeader("Content-Disposition", matching("form-data; name=\"part\";\\s*filename=\"foo.txt\"(;.*)?")) //
            .withBody(equalTo("Hello, world!")) //
            ) //
        .willReturn( //
            aResponse() //
            .withStatus(200) //
            .withHeader("Content-Type", "application/json") //
            .withBodyFile("exampleDocument.json")));
    
    List<ContentElement> ces = client.documentService().addContent("aDocument") //
        .element() //
        .content("Hello, world!".getBytes()) //
        .role("part") //
        .mediaType("text/plain") //
        .fileName("foo.txt") //
        .attach() //
        .save();
    
    assertThat(ces).hasSize(1);
    
    ContentElement ce = ces.get(0);
    assertThat(ce.getVersionTimestamp()).isEqualTo(Instant.ofEpochMilli(1));
    assertThat(ce.getId()).isEqualTo("6709b8b1-f9f2-4d2b-8db1-da14c6d7dfc4");
    assertThat(ce.getRole()).isEqualTo("part");
    assertThat(ce.getFileName()).isEqualTo("foo.txt");
    
    verify(postRequestedFor(urlMatching("/api/v1/documents/aDocument/content")));
  }

  @Test
  void testThat_contentQueriesForFirstWork() throws Exception {
    stubFor( //
        get(urlMatching("/api/v1/documents/aDocument/content\\?.*")) //
            .withHeader("Accept", containing("text/plain")) //
            .withHeader("Accept", containing("application/x-something")) //
            .willReturn( //
                aResponse() //
                    .withStatus(200) //
                    .withHeader("Content-Type", "text/plain") //
                    .withHeader(DocumentServiceImpl.VERSION_TIMESTAMP_HEADER, "1970-01-01T00:00:00.003Z") //
                    .withBody("Hello, world!")));

    ContentElementResponse ce = client.documentService().queryContent("aDocument") //
        .withMediaType("text/plain") //
        .withMediaType("application/x-something") //
        .withRole("someRole") //
        .withRole("someOtherRole") //
        .getFirst();

    assertThat(ce.getMediaType()).isEqualTo("text/plain");
    assertThat(ce.getVersionTimestamp()).isEqualTo(Instant.ofEpochMilli(3));
    assertThat(new BufferedReader(new InputStreamReader(ce.getContent())).readLine()).isEqualTo("Hello, world!");

    verify(getRequestedFor(urlMatching("/api/v1/documents/aDocument/content\\?.*")) //
        .withQueryParam("role", containing("someRole")) //
        .withQueryParam("role", containing("someOtherRole")) //
        .withQueryParam("return", containing("first")) //
        .withHeader("Accept", containing("text/plain")) //
        .withHeader("Accept", containing("application/x-something")) //
    );
  }

  @Test
  void testThat_contentQueriesForOnlyWork() throws Exception {
    stubFor( //
        get(urlMatching("/api/v1/documents/aDocument/content\\?.*")) //
            .withHeader("Accept", containing("text/plain")) //
            .withHeader("Accept", containing("application/x-something")) //
            .willReturn( //
                aResponse() //
                    .withStatus(200) //
                    .withHeader("Content-Type", "text/plain") //
                    .withBody("Hello, world!")));

    ContentElementResponse ce = client.documentService().queryContent("aDocument") //
        .withMediaType("text/plain") //
        .withMediaType("application/x-something") //
        .withRole("someRole") //
        .withRole("someOtherRole") //
        .getOnly();

    assertThat(ce.getMediaType()).isEqualTo("text/plain");
    assertThat(new BufferedReader(new InputStreamReader(ce.getContent())).readLine()).isEqualTo("Hello, world!");

    verify(getRequestedFor(urlMatching("/api/v1/documents/aDocument/content\\?.*")) //
        .withQueryParam("role", containing("someRole")) //
        .withQueryParam("role", containing("someOtherRole")) //
        .withQueryParam("return", containing("only")) //
        .withHeader("Accept", containing("text/plain")) //
        .withHeader("Accept", containing("application/x-something")) //
    );
  }

  @Test
  void testThat_contentQueriesForAllWork() throws Exception {
    stubFor( //
        get(urlMatching("/api/v1/documents/aDocument/content\\?.*")) //
            .willReturn( //
                aResponse() //
                    .withStatus(200) //
                    .withHeader("Content-Type",
                        "multipart/mixed; boundary=QekfwgcG0Tam6ly0hQqL2JF6srHvBxdn;charset=UTF-8") //
                    .withBodyFile("multipartStream.txt")));

    ContentElementSequence mis = client.documentService().queryContent("aDocument") //
        .getAll();

    ContentElementResponse ce = mis.nextContentElement();
    assertThat(ce.getDigest().getAlgorithm()).isEqualTo(HashAlgorithm.SHA_256);
    assertThat(ce.getDigest().getBytes()).isEqualTo(
        Base64.getDecoder().decode("LCa0a2j/xo/5m0U8HTBBNBNCLXBkg7+g+YpeiGJm564="));
    assertThat(ce.getMediaType()).isEqualTo("text/plain");
    assertThat(ce.getVersionTimestamp()).isEqualTo(Instant.ofEpochMilli(1));
    assertThat(new BufferedReader(new InputStreamReader(ce.getContent())).readLine()).isEqualTo("foo");

    ce = mis.nextContentElement();
    assertThat(ce.getDigest().getAlgorithm()).isEqualTo(HashAlgorithm.SHA_256);
    assertThat(ce.getDigest().getBytes()).isEqualTo(
        Base64.getDecoder().decode("STjYc7Z1UJKRK1T5cDMFIgYZKk6q5c6aTyNaEGfQSw0="));
    assertThat(ce.getMediaType()).isEqualTo("application/xml");
    assertThat(ce.getVersionTimestamp()).isEqualTo(Instant.ofEpochMilli(2));
    assertThat(new BufferedReader(new InputStreamReader(ce.getContent())).readLine()).isEqualTo("<foo>foobar</foo>");

    ce = mis.nextContentElement();
    assertThat(ce.getDigest().getAlgorithm()).isEqualTo(HashAlgorithm.SHA_256);
    assertThat(ce.getDigest().getBytes()).isEqualTo(
        Base64.getDecoder().decode("7d38b5cd25a2baf85ad3bb5b9311383e671a8a142eb302b324d4a5fba8748c69"));
    assertThat(ce.getMediaType()).isEqualTo("application/octet-stream");
    assertThat(ce.getVersionTimestamp()).isEqualTo(Instant.ofEpochMilli(3));
    assertThat(new BufferedReader(new InputStreamReader(ce.getContent())).readLine()).isEqualTo(
        "The quick brown fox jumped over the lazy dog");

    assertThat(mis.nextContentElement()).isNull();

    verify(getRequestedFor(urlMatching("/api/v1/documents/aDocument/content\\?.*")) //
        .withQueryParam("return", containing("all")) //
    );
  }

  @Test
  void testThat_contentQueriesAgainstHistoryWork() throws Exception {
    stubFor( //
        get(urlMatching("/api/v1/documents/aDocument/history/1970-01-01T00:00:00.042Z/content\\?.*")) //
            .willReturn( //
                aResponse() //
                    .withStatus(200) //
                    .withHeader("Content-Type",
                        "multipart/mixed; boundary=QekfwgcG0Tam6ly0hQqL2JF6srHvBxdn;charset=UTF-8") //
                    .withBodyFile("multipartStream.txt")));

    ContentElementSequence mis = client.documentService().queryContent("aDocument", Instant.ofEpochMilli(42)) //
        .getAll();

    ContentElementResponse ce = mis.nextContentElement();
    assertThat(new BufferedReader(new InputStreamReader(ce.getContent())).readLine()).isEqualTo("foo");

    ce = mis.nextContentElement();
    assertThat(new BufferedReader(new InputStreamReader(ce.getContent())).readLine()).isEqualTo("<foo>foobar</foo>");

    ce = mis.nextContentElement();
    assertThat(new BufferedReader(new InputStreamReader(ce.getContent())).readLine()).isEqualTo(
        "The quick brown fox jumped over the lazy dog");

    assertThat(mis.nextContentElement()).isNull();

    verify(getRequestedFor(urlMatching("/api/v1/documents/aDocument/history/1970-01-01T00:00:00.042Z/content\\\\?.*")) //
        .withQueryParam("return", containing("all")) //
    );
  }

  @Test()
  void testThat_contentElementSequenceConsumptionworks() throws Exception {
    ContentElementSequence s = new ContentElementSequence(
        getClass().getResourceAsStream("/__files/multipartStream.txt"), "QekfwgcG0Tam6ly0hQqL2JF6srHvBxdn".getBytes());

    InputStream stream1 = s.nextContentElement().getContent();

    s.nextContentElement().getContent();
    IllegalStateException e = assertThrows(IllegalStateException.class, () -> stream1.read(),
        "should no longer be able to consume this");
    assertTrue(e.getMessage().contains("Already advanced"));
  }

  @Test
  void testThat_oldVersionCanBeRetrieved() throws Exception {
    stubFor( //
        get(urlEqualTo("/api/v1/documents/aDocument/history/1970-01-01T00:00:00.042Z")) //
            .withHeader("Accept", equalTo("application/json")) //
            .willReturn( //
                aResponse() //
                    .withStatus(200) //
                    .withHeader("Content-Type", "application/json") //
                    .withBodyFile("exampleDocument.json")));

    Optional<Document> document = client.documentService().getDocumentVersion("aDocument", Instant.ofEpochMilli(42L));

    assertThat(document).isPresent();
    assertThat(document.get().getDocumentId()).isEqualTo("aDocument");

    verify(getRequestedFor(urlMatching("/api/v1/documents/aDocument/history/1970-01-01T00:00:00.042Z")));
  }

  @Test
  void testThat_versionListCanBeRetrieved() throws Exception {
    stubFor( //
        get(urlEqualTo("/api/v1/documents/aDocument/history")) //
            .withHeader("Accept", equalTo("application/json")) //
            .willReturn( //
                aResponse() //
                    .withStatus(200) //
                    .withHeader("Content-Type", "application/json") //
                    .withBody(
                        "[\"1970-01-01T00:00:00.001Z\", \"1970-01-01T00:00:00.002Z\", \"1970-01-01T00:00:00.003Z\"]")));

    List<Instant> versions = client.documentService().getVersions("aDocument");

    assertThat(versions).hasSize(3).containsExactly(Instant.ofEpochMilli(1l), Instant.ofEpochMilli(2l),
        Instant.ofEpochMilli(3l));

    verify(getRequestedFor(urlMatching("/api/v1/documents/aDocument/history")));
  }

  @Test
  void testThat_contentElementCanBeUpdated() throws Exception {
    stubFor( //
        put(urlEqualTo("/api/v1/documents/aDocument/content/someContentElementId")) //
            .withHeader("Accept", equalTo("application/json")) //
            .withHeader("Content-Type", equalTo("text/plain")) //
            .withRequestBody(WireMock.equalTo("Hello, world!")) //
            .willReturn( //
                aResponse() //
                    .withStatus(200) //
                    .withHeader("Content-Type", "application/json") //
                    .withHeader(DocumentServiceImpl.VERSION_TIMESTAMP_HEADER, "1970-01-01T00:00:00.003Z") //
                    .withBodyFile("updatedContentElement.json")));

    ContentElement updated = client.documentService().updateContentElement("aDocument", "someContentElementId",
        "Hello, world!".getBytes(), "text/plain");

    assertThat(updated.getId()).isEqualTo("anUpdatedId");
    assertThat(updated.getType()).isEqualTo("text/plain");
    assertThat(updated.getFileName()).isEqualTo("aFileName.txt");
    assertThat(updated.getRole()).isEqualTo("part");
    assertThat(updated.getDigest().getAlgorithm()).isEqualTo(HashAlgorithm.SHA_256);
    assertThat(updated.getDigest().getBytes()).isEqualTo(
        Base64.getDecoder().decode("sWrtbVRuHGOC8ZCGUgSUN1Cv/sM8m40mgqRRW71154c="));
    assertThat(updated.getVersionTimestamp()).isEqualTo(Instant.ofEpochMilli(3));

    verify(putRequestedFor(urlMatching("/api/v1/documents/aDocument/content/someContentElementId")));
  }

  @Test
  void testThat_AcceptHeadersDoNotGetHtmlEncoded() throws Exception {
    stubFor( //
        get(urlMatching("/api/v1/documents/aDocument/content\\?.*")) //
            .withHeader("Accept", equalTo("*/*")) //
            .willReturn( //
                aResponse() //
                    .withStatus(200) //
                    .withHeader("Content-Type",
                        "multipart/mixed; boundary=QekfwgcG0Tam6ly0hQqL2JF6srHvBxdn;charset=UTF-8") //
                    .withBodyFile("multipartStream.txt")));

    ContentElementResponse contentElement = client.documentService().queryContent("aDocument") //
        .getFirst();

    assertNotNull(contentElement.getContent());


    verify(getRequestedFor(urlMatching("/api/v1/documents/aDocument/content\\?.*")));
  }
}
