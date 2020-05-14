package com.neverpile.eureka.client.impl.feign;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neverpile.eureka.client.content.ContentElementBuilder;
import com.neverpile.eureka.client.content.ContentElementBuilderImpl;
import com.neverpile.eureka.client.content.ContentElementFacet;
import com.neverpile.eureka.client.content.ContentElementListBuilder;
import com.neverpile.eureka.client.content.MultipartFile;
import com.neverpile.eureka.client.core.ContentElement;
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
  static final String VERSION_TIMESTAMP_HEADER = "X-NPE-Document-Version-Timestamp";
  private final DocumentServiceTarget documentServiceTarget;
  private final ObjectMapper objectMapper;

  private abstract class ContentQueryBuilderImpl implements ContentQueryBuilder {
    private final List<String> roles = new ArrayList<>();
    private final List<String> mediaTypes = new ArrayList<>();

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

      if (mediaTypes.isEmpty())
        mediaTypes.add("*/*");

      return contentElementResponse(doQuery(queryMap, mediaTypes));
    }

    protected abstract Response doQuery(Map<String, Object> queryMap, List<String> mediaTypes);

    private Map<String, Object> queryMap() {
      Map<String, Object> queryMap = new HashMap<>();
      queryMap.put("role", roles);
      return queryMap;
    }

    @Override
    public ContentElementResponse getOnly() throws IOException {
      Map<String, Object> queryMap = queryMap();
      queryMap.put("return", "only");

      if (mediaTypes.isEmpty())
        mediaTypes.add("*/*");

      return contentElementResponse(doQuery(queryMap, mediaTypes));
    }

    @Override
    public ContentElementSequence getAll() throws IOException {
      Map<String, Object> queryMap = queryMap();
      queryMap.put("return", "all");

      if (mediaTypes.isEmpty())
        mediaTypes.add("*/*");

      Response multipartResponse = doQuery(queryMap, mediaTypes);

      if (multipartResponse.status() != 200)
        throw new FeignServerException(multipartResponse.status(), multipartResponse.reason());

      try {
        Collection<String> ctHeaderValues = multipartResponse.headers().get("Content-Type");
        if (null == ctHeaderValues || ctHeaderValues.isEmpty())
          throw new IOException("Missing Content-Type header in response");

        MimeType contentType = new MimeType(ctHeaderValues.iterator().next());
        if (!contentType.match("multipart/mixed") || null == contentType.getParameter("boundary"))
          throw new IOException("Invalid Content-Type header in response: " + contentType);

        return new ContentElementSequence(multipartResponse.body().asInputStream(),
            contentType.getParameter("boundary").getBytes());
      } catch (MimeTypeParseException e) {
        throw new IOException("Unparseable Content-Type header in response", e);
      }
    }
  }

  public DocumentServiceImpl(final Feign feign, final String baseURI, final ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.documentServiceTarget = feign.newInstance(new Target.HardCodedTarget<>(DocumentServiceTarget.class, baseURI));
  }

  @Override
  public Document getDocument(final String documentId) {
    return documentServiceTarget.getDocument(documentId);
  }

  @Override
  public Document getDocumentVersion(final String documentId, final Instant versionTimestamp) {
    return documentServiceTarget.getDocumentVersion(documentId, versionTimestamp);
  }

  @Override
  public List<Instant> getVersions(final String documentId) {
    return documentServiceTarget.getVersions(documentId);
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
            .stream().findFirst().orElse("application/octet-stream");
      }

      @Override
      public Digest getDigest() {
        return digest;
      }

      @Override
      public InputStream getContent() throws IOException {
        return response.body().asInputStream();
      }

      @Override
      public Instant getVersionTimestamp() {
        return response.headers().getOrDefault(VERSION_TIMESTAMP_HEADER, Arrays.asList("-")) //
            .stream().findFirst().map(t -> t.length() > 1 ? Instant.parse(t) : null).orElse(null);
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
    return new ContentQueryBuilderImpl() {
      @Override
      protected Response doQuery(final Map<String, Object> queryMap, final List<String> mediaTypes) {
        return documentServiceTarget.queryContent(documentId, queryMap, mediaTypes);
      }
    };
  }

  @Override
  public ContentQueryBuilder queryContent(final String documentId, final Instant versionTimestamp) {
    return new ContentQueryBuilderImpl() {
      @Override
      protected Response doQuery(final Map<String, Object> queryMap, final List<String> mediaTypes) {
        return documentServiceTarget.queryContent(documentId, versionTimestamp, queryMap, mediaTypes);
      }
    };
  }

  public ContentElementListBuilder addContent(final String documentId) {
    return new ContentElementListBuilder() {
      private final List<MultipartFile> parts = new ArrayList<MultipartFile>();

      @Override
      public ContentElementBuilder<ContentElementListBuilder> element() {
        return new ContentElementBuilderImpl<ContentElementListBuilder>(this, parts::add);
      }

      @Override
      public List<ContentElement> save() {
        Document doc = documentServiceTarget.addContentElement(documentId, parts.toArray(new MultipartFile[parts.size()]));
        
        List<ContentElement> ces = doc.facet(ContentElementFacet.class).orElseThrow(() -> new IllegalStateException("Unexpected: no content element data"));
        if(ces.size() < parts.size())
          throw new IllegalStateException("Unexpected: content element list too short");
        
        ces.forEach(ce -> ce.setVersionTimestamp(doc.getVersionTimestamp()));
        
        return ces.subList(ces.size() - parts.size(), ces.size());
      }
    };
  }
  
  @Override
  public ContentElement addContentElement(final String documentId, final InputStream is, final String mediaType, final String role, final String filename) {
    MultipartFile f = new MultipartFile() {
      @Override
      public String getOriginalFilename() {
        return null != filename && filename.length() > 0 ? filename : "unknown.dat";
      }

      @Override
      public String getName() {
        return role;
      }

      @Override
      public InputStream getInputStream() throws IOException {
        return is;
      }

      @Override
      public String getContentType() {
        return mediaType;
      }
    };
    
    Document doc = documentServiceTarget.addContentElement(documentId, new MultipartFile[] {f});
    List<ContentElement> ces = doc.facet(ContentElementFacet.class).orElseThrow(() -> new IllegalStateException("Unexpected: no content element data"));
    if(ces.isEmpty())
      throw new IllegalStateException("Unexpected: empty content element list");
    
    // get the last content element
    ContentElement ce = ces.get(ces.size()-1);
    ce.setVersionTimestamp(doc.getVersionTimestamp());
    
    return ce;
  }

  @Override
  public ContentElement updateContentElement(final String documentId, final String contentElementId,
      final InputStream is, final String mediaType) {
    return responseToContentElement(
        documentServiceTarget.updateContentElement(is, documentId, contentElementId, mediaType));
  }

  private ContentElement responseToContentElement(final Response r) {
    ContentElement ce;
    try {
      ce = objectMapper.readValue(r.body().asInputStream(), ContentElement.class);
      ce.setVersionTimestamp(r.headers().getOrDefault(VERSION_TIMESTAMP_HEADER, Arrays.asList("-")) //
          .stream().findFirst().map(t -> t.length() > 1 ? Instant.parse(t) : null).orElse(null));
    } catch (IOException e) {
      throw new FeignClientException(500, "Can't unmarshal response: " + e.getMessage());
    }
    return ce;
  }

  @Override
  public ContentElement updateContentElement(final String documentId, final String contentElementId,
      final Supplier<InputStream> iss, final String mediaType) {
    return responseToContentElement(
        documentServiceTarget.updateContentElement(iss.get(), documentId, contentElementId, mediaType));
  }

  @Override
  public ContentElement updateContentElement(final String documentId, final String contentElementId, final byte[] data,
      final String mediaType) {
    return responseToContentElement(documentServiceTarget.updateContentElement(new ByteArrayInputStream(data),
        documentId, contentElementId, mediaType));
  }
}
