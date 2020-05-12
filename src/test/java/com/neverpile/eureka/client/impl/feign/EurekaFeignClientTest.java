package com.neverpile.eureka.client.impl.feign;
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
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import org.apache.commons.fileupload.util.Streams;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
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

public class EurekaFeignClientTest {
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

  private NeverpileEurekaClient client;

  @Before
  public void createClient() {
    client = EurekaClient.builder().baseURL("http://localhost:" + wireMockRule.port()).build();
  }

  @Test
  public void testThat_documentCanBeRetrieved() throws Exception {
    stubFor( //
        get(urlEqualTo("/api/v1/documents/aDocument")) //
            .withHeader("Accept", equalTo("application/json")) //
            .willReturn( //
                aResponse() //
                    .withStatus(200) //
                    .withHeader("Content-Type", "application/json") //
                    .withBodyFile("exampleDocument.json")));

    Document document = client.documentService().getDocument("aDocument");

    assertThat(document.getDocumentId()).isEqualTo("aDocument");
    assertThat(document.facet(CreationDateFacet.class)).isPresent().hasValue(Instant.parse("2019-12-09T14:25:53.747Z"));
    assertThat(document.facet(ModificationDateFacet.class)).isPresent().hasValue(Instant.parse("2019-12-09T14:25:53.747Z"));

    verify(getRequestedFor(urlMatching("/api/v1/documents/aDocument")));
  }

  @Test
  public void testThat_metadataFacetIsSupported() throws Exception {
    stubFor( //
        get(urlEqualTo("/api/v1/documents/aDocument")) //
            .withHeader("Accept", equalTo("application/json")) //
            .willReturn( //
                aResponse() //
                    .withStatus(200) //
                    .withHeader("Content-Type", "application/json") //
                    .withBodyFile("exampleDocument.json")));

    Document document = client.documentService().getDocument("aDocument");

    Metadata metadata = document.facet(MetadataFacet.class).get();
    assertThat(metadata).isNotNull();
    assertThat(metadata.elements()).containsKey("foo");
    assertThat(metadata.jsonElement("foo").get().asTree().path("foo").asText()).isEqualTo("bar2");
    assertThat(metadata.jsonElement("foo").get().asTree().path("bar").asText()).isEqualTo("baz2");

    verify(getRequestedFor(urlMatching("/api/v1/documents/aDocument")));
  }

  @Test
  public void testThat_contentElementFacetIsSupported() throws Exception {
    stubFor( //
        get(urlEqualTo("/api/v1/documents/aDocument")) //
            .withHeader("Accept", equalTo("application/json")) //
            .willReturn( //
                aResponse() //
                    .withStatus(200) //
                    .withHeader("Content-Type", "application/json") //
                    .withBodyFile("exampleDocument.json")));

    Document document = client.documentService().getDocument("aDocument");

    List<ContentElement> ce = document.facet(ContentElementFacet.class).get();
    assertThat(ce).isNotNull();
    assertThat(ce).hasSize(1);

    verify(getRequestedFor(urlMatching("/api/v1/documents/aDocument")));
  }

  @Test
  public void testThat_documentCreationFromMultipartWorks() throws Exception {
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
    assertThat(document.facet(ModificationDateFacet.class)).isPresent().hasValue(Instant.parse("2019-12-09T14:25:53.747Z"));

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
  public void testThat_documentCreationWithMetadataWorks() throws Exception {
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
  public void testThat_contentQueriesForFirstWork() throws Exception {
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
        .getFirst();

    assertThat(ce.getMediaType()).isEqualTo("text/plain");
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
  public void testThat_contentQueriesForOnlyWork() throws Exception {
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
  public void testThat_contentQueriesForAllWork() throws Exception {
    String body = Streams.asString(getClass().getResourceAsStream("multipartStream.txt"));
    stubFor( //
        get(urlMatching("/api/v1/documents/aDocument/content\\?.*")) //
        .willReturn( //
            aResponse() //
            .withStatus(200) //
            .withHeader("Content-Type", "multipart/mixed; boundary=QekfwgcG0Tam6ly0hQqL2JF6srHvBxdn;charset=UTF-8") //
            .withBody(body)));
    
    MultipartInputStream mis = client.documentService().queryContent("aDocument") //
        .getAll();
    
    ContentElementResponse ce = mis.nextContentElement();
    assertThat(ce.getDigest().getAlgorithm()).isEqualTo(HashAlgorithm.SHA_256);
    assertThat(ce.getDigest().getBytes()).isEqualTo(Base64.getDecoder().decode("LCa0a2j/xo/5m0U8HTBBNBNCLXBkg7+g+YpeiGJm564="));
    assertThat(ce.getMediaType()).isEqualTo("text/plain");
    assertThat(new BufferedReader(new InputStreamReader(ce.getContent())).readLine()).isEqualTo("foo");
    
    ce = mis.nextContentElement();
    assertThat(ce.getDigest().getAlgorithm()).isEqualTo(HashAlgorithm.SHA_256);
    assertThat(ce.getDigest().getBytes()).isEqualTo(Base64.getDecoder().decode("STjYc7Z1UJKRK1T5cDMFIgYZKk6q5c6aTyNaEGfQSw0="));
    assertThat(ce.getMediaType()).isEqualTo("application/xml");
    assertThat(new BufferedReader(new InputStreamReader(ce.getContent())).readLine()).isEqualTo("<foo>foobar</foo>");
    
    ce = mis.nextContentElement();
    assertThat(ce.getDigest().getAlgorithm()).isEqualTo(HashAlgorithm.SHA_256);
    assertThat(ce.getDigest().getBytes()).isEqualTo(Base64.getDecoder().decode("7d38b5cd25a2baf85ad3bb5b9311383e671a8a142eb302b324d4a5fba8748c69"));
    assertThat(ce.getMediaType()).isEqualTo("application/octet-stream");
    assertThat(new BufferedReader(new InputStreamReader(ce.getContent())).readLine()).isEqualTo("The quick brown fox jumped over the lazy dog");
    
    verify(getRequestedFor(urlMatching("/api/v1/documents/aDocument/content\\?.*")) //
        .withQueryParam("return", containing("all")) //
        );
  }
}
