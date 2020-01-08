package com.neverpile.eureka.client.impl.feign;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

import com.neverpile.eureka.client.core.Digest;
import com.neverpile.eureka.client.core.DocumentBuilder;
import com.neverpile.eureka.client.core.DocumentDto;
import com.neverpile.eureka.client.core.DocumentService;
import com.neverpile.eureka.client.core.HashAlgorithm;

import feign.Feign;
import feign.Response;

public class DocumentServiceImpl implements DocumentService {
  private final DocumentServiceTarget documentServiceTarget;

  public DocumentServiceImpl(final Feign.Builder builder, final String baseURI) {
    documentServiceTarget = builder.target(DocumentServiceTarget.class, baseURI);
  }

  @Override
  public DocumentDto getDocument(final String documentId) {
    return documentServiceTarget.getDocument(documentId);
  }

  @Override
  public DocumentBuilder newDocument() {
    return new DocumentBuilderImpl(documentServiceTarget);
  }

  @Override
  public ContentElementResponse getContentElement(final String documentId, final String elementId) throws IOException {
    Response response = documentServiceTarget.getContentElement(documentId, elementId);
    Digest digest = createDigest(response);

    return new ContentElementResponse() {
      @Override
      public String getMediaType() {
        return response.headers().getOrDefault("content-type", Arrays.asList("application/octet-stream")) //
            .stream().findFirst().orElse("application/octet-stream") //
        ;
      }

      @Override
      public Digest getDigest() {
        return digest;
      }

      @Override
      public InputStream getContent() throws IOException {
        return response.body().asInputStream();
      }
    };
  }

  private Digest createDigest(final Response response) throws IOException {
    Optional<String> etagHeader = response.headers().getOrDefault("etag", Arrays.asList()) //
        .stream().findFirst();

    Digest digest = null;
    if (etagHeader.isPresent()) {
      String[] split = etagHeader.get().split(":");
      if (split.length == 2) {
        try {
          HashAlgorithm algorithm = HashAlgorithm.fromValue(split[0]);
          digest = new Digest(algorithm, Hex.decode(split[1]));
        } catch (Exception e) {
          throw new IOException("invalid ETag received: " + etagHeader, e);
        }
      }
    }
    return digest;
  }
}
