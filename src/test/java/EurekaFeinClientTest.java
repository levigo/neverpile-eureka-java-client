import static com.github.tomakehurst.wiremock.client.WireMock.aMultipart;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
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

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.neverpile.eureka.client.EurekaClient;
import com.neverpile.eureka.client.content.ContentElementFacet;
import com.neverpile.eureka.client.core.ContentElementDto;
import com.neverpile.eureka.client.core.CreationDateFacet;
import com.neverpile.eureka.client.core.DocumentDto;
import com.neverpile.eureka.client.core.Metadata;
import com.neverpile.eureka.client.core.ModificationDateFacet;
import com.neverpile.eureka.client.core.NeverpileClient;
import com.neverpile.eureka.client.metadata.MetadataFacet;
import com.neverpile.eureka.client.metadata.MetadataFacetBuilder;

public class EurekaFeinClientTest {
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

  private NeverpileClient client;

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

    DocumentDto document = client.documentService().getDocument("aDocument");

    assertThat(document.getDocumentId()).isEqualTo("aDocument");
    assertThat(document.facet(new CreationDateFacet()).get()).isEqualTo(Instant.parse("2019-12-09T14:25:53.747Z"));
    assertThat(document.facet(new ModificationDateFacet()).get()).isEqualTo(Instant.parse("2019-12-09T14:25:53.747Z"));

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

    DocumentDto document = client.documentService().getDocument("aDocument");

    Metadata metadata = document.facet(new MetadataFacet()).get();
    assertThat(metadata).isNotNull();
    assertThat(metadata.get()).containsKey("foo");

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

    DocumentDto document = client.documentService().getDocument("aDocument");

    List<ContentElementDto> ce = document.facet(new ContentElementFacet()).get();
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

    DocumentDto document = client.documentService() //
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
    assertThat(document.facet(new CreationDateFacet()).get()).isEqualTo(Instant.parse("2019-12-09T14:25:53.747Z"));
    assertThat(document.facet(new ModificationDateFacet()).get()).isEqualTo(Instant.parse("2019-12-09T14:25:53.747Z"));

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

    DocumentDto document = client.documentService() //
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
    
    Metadata metadata = document.facet(new MetadataFacet()).get();
    assertThat(metadata).isNotNull();
    assertThat(metadata.get()).containsKey("foo");
    
    verify(postRequestedFor(urlMatching("/api/v1/documents")));
  }
}
