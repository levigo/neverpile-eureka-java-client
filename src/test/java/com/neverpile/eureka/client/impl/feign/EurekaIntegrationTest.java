package com.neverpile.eureka.client.impl.feign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.neverpile.eureka.client.EurekaClient;
import com.neverpile.eureka.client.core.ContentElement;
import com.neverpile.eureka.client.core.DocumentBuilder;
import com.neverpile.eureka.client.core.DocumentService;
import com.neverpile.eureka.client.core.DocumentService.ContentElementResponse;
import com.neverpile.eureka.client.core.NeverpileEurekaClient;
import com.neverpile.eureka.client.core.NotFoundException;

import feign.RetryableException;

class EurekaIntegrationTest {

  private NeverpileEurekaClient client;

  @BeforeEach
  public void createClient() {
    client = EurekaClient.builder().baseURL("http://localhost:8080").withBasicAuth().username("admin").password(
        "admin").done().build();
  }

  @Test
  @Disabled("Enable manually")
  void testThat_saveLoadDeleteRoundtripWorks() throws Exception {
    // Create document
    String docID = UUID.randomUUID().toString();

    String filename = "foo.txt";
    String role = "part";

    String text = "Hello, world!";

    DocumentBuilder db = buildEurekaDocument(docID);
    db.save();

    final DocumentService documentService = client.documentService();
    ContentElement ce = documentService.addContentElement(docID, new ByteArrayInputStream(text.getBytes()),
        "text/plain", role, filename);

    assertThat(ce.getRole()).isEqualTo(role);
    assertThat(ce.getFileName()).isEqualTo(filename);
    String ceID = ce.getId();

    // Check document contents
    ContentElementResponse cer = documentService.getContentElement(docID, ceID);
    try (InputStream is = cer.getContent()) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      IOUtils.copy(is, baos);

      assertThat(new String(baos.toByteArray())).isEqualTo(text);
    }

    // Delete document
    documentService.deleteDocument(docID);

    // Check document delete
    assertThrows(NotFoundException.class, () -> documentService.getContentElement(docID, ceID),"Did not receive NotFoundException for docID: " + docID);
  }

  @Test
  void test_url_not_absolute() {
    final NeverpileEurekaClient clientWithIncorrectURL = EurekaClient.builder().baseURL(
        "http://somehost:1234").withBasicAuth().username("admin").password("admin").done().build();
    final String docID = UUID.randomUUID().toString();
    final String text = "Hello, world!";
    DocumentService documentService = client.documentService();
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(text.getBytes());
    assertThrows(RetryableException.class,
        () -> documentService.addContentElement(docID, byteArrayInputStream, "text/plain", "role", "filename"));
  }

  private DocumentBuilder buildEurekaDocument(String docId) {
    if (null == docId) {
      docId = UUID.randomUUID().toString();
    }
    return client.documentService().newDocument().id(docId);
  }
}
