package com.neverpile.eureka.client.impl.feign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.neverpile.eureka.client.EurekaClient;
import com.neverpile.eureka.client.core.ContentElement;
import com.neverpile.eureka.client.core.DocumentBuilder;
import com.neverpile.eureka.client.core.DocumentService.ContentElementResponse;
import com.neverpile.eureka.client.core.NeverpileEurekaClient;
import com.neverpile.eureka.client.core.NotFoundException;

import feign.RetryableException;

public class EurekaIntegrationTest {

  private NeverpileEurekaClient client;

  @Before
  public void createClient() {
    client = EurekaClient.builder().baseURL("http://localhost:8080").withBasicAuth().username("admin").password(
        "admin").done().build();
  }

  @Test
  @Ignore // TODO Enable manually
  public void testThat_saveLoadDeleteRoundtripWorks() throws Exception {
    // Create document
    String docID = UUID.randomUUID().toString();

    String filename = "foo.txt";
    String role = "part";

    String text = "Hello, world!";

    DocumentBuilder db = buildEurekaDocument(docID);
    db.save();

    ContentElement ce = client.documentService().addContentElement(docID, new ByteArrayInputStream(text.getBytes()),
        "text/plain", role, filename);

    assertThat(ce.getRole()).isEqualTo(role);
    assertThat(ce.getFileName()).isEqualTo(filename);
    String ceID = ce.getId();

    // Check document contents
    ContentElementResponse cer = client.documentService().getContentElement(docID, ceID);
    try (InputStream is = cer.getContent()) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      IOUtils.copy(is, baos);

      assertThat(new String(baos.toByteArray())).isEqualTo(text);
    }

    // Delete document
    client.documentService().deleteDocument(docID);

    // Check document delete
    try {
      cer = client.documentService().getContentElement(docID, ceID);
      fail("Did not receive NotFoundException for docID: " + docID);
    } catch (NotFoundException e) {
      // expected
    }
  }

  @Test(expected = RetryableException.class)
  public void test_url_not_absolute(){
    final NeverpileEurekaClient clientWithIncorrectURL = EurekaClient.builder().baseURL("http://somehost:1234").withBasicAuth().username("admin").password(
        "admin").done().build();
    final String docID = UUID.randomUUID().toString();
    final String text = "Hello, world!";
    ContentElement ce = clientWithIncorrectURL.documentService().addContentElement(docID, new ByteArrayInputStream(text.getBytes()),
        "text/plain", "role", "filename");
  }

  private DocumentBuilder buildEurekaDocument(String docId) {
    if (null == docId) {
      docId = UUID.randomUUID().toString();
    }
    return client.documentService().newDocument().id(docId);
  }
}
