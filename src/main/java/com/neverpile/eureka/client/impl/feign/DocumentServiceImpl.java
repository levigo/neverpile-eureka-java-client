package com.neverpile.eureka.client.impl.feign;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.neverpile.eureka.client.core.ContentQueryBuilder;
import com.neverpile.eureka.client.core.Digest;
import com.neverpile.eureka.client.core.Document;
import com.neverpile.eureka.client.core.DocumentBuilder;
import com.neverpile.eureka.client.core.DocumentService;
import com.neverpile.eureka.client.core.HashAlgorithm;

import feign.Feign;
import feign.Response;
import feign.Target;

public class DocumentServiceImpl implements DocumentService {
  private final DocumentServiceTarget documentServiceTarget;

  private class ContentQueryBuilderImpl implements ContentQueryBuilder {
    private final List<String> roles = new ArrayList<>();
    private final List<String> mediaTypes = new ArrayList<>();
    private final String documentId;
    
    private ContentQueryBuilderImpl(final String documentId) {
      this.documentId = documentId;
    }
    
    @Override
    public ContentQueryBuilder withRole(final String role) {
      roles.add(role);
      return this;
    }
    
    @Override
    public ContentQueryBuilder withMediaType(final String mediaType) {
      mediaTypes.add(mediaType);
      return this;
    }
    
    @Override
    public ContentElementResponse getFirst() throws IOException {
      Map<String, Object> queryMap = queryMap();
      queryMap.put("return", "first");
      
      return contentElementResponse(documentServiceTarget.queryContent(documentId, queryMap));
    }

    private Map<String, Object> queryMap() {
      Map<String,Object> queryMap = new HashMap<>();
      queryMap.put("role", roles);
      queryMap.put("mediaType", mediaTypes);
      return queryMap;
    }
    
    @Override
    public ContentElementResponse getOnly() throws IOException {
      Map<String, Object> queryMap = queryMap();
      queryMap.put("return", "first");
      return contentElementResponse(documentServiceTarget.queryContent(documentId, queryMap));
    }
  }
  
  public DocumentServiceImpl(final Feign feign, final String baseURI) {
    documentServiceTarget = feign.newInstance(new Target.HardCodedTarget<>(DocumentServiceTarget.class, baseURI));
  }

  @Override
  public Document getDocument(final String documentId) {
    return documentServiceTarget.getDocument(documentId);
  }

  @Override
  public DocumentBuilder newDocument() {
    return new DocumentBuilderImpl(documentServiceTarget);
  }

  @Override
  public ContentElementResponse getContentElement(final String documentId, final String elementId) throws IOException {
    return contentElementResponse(documentServiceTarget.getContentElement(documentId, elementId));
  }

  private ContentElementResponse contentElementResponse(final Response response) throws IOException {
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
  
  @Override
  public ContentQueryBuilder queryContent(final String documentId) {
    return new ContentQueryBuilderImpl(documentId);
  }
}
