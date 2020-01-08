package com.neverpile.eureka.client.impl.feign;

import java.util.ArrayList;
import java.util.List;

import com.neverpile.eureka.client.content.ContentElementBuilder;
import com.neverpile.eureka.client.core.DocumentBuilder;
import com.neverpile.eureka.client.core.DocumentDto;
import com.neverpile.eureka.client.core.DocumentFacetBuilder;

public class DocumentBuilderImpl implements DocumentBuilder {

  private final DocumentDto document = new DocumentDto();
  
  private final DocumentServiceTarget documentServiceTarget;
  
  private final List<MultipartFile> parts = new ArrayList<MultipartFile>();

  public DocumentBuilderImpl(final DocumentServiceTarget documentServiceTarget) {
    this.documentServiceTarget = documentServiceTarget;
  }

  @Override
  public DocumentBuilder id(final String id) {
    document.setDocumentId(id);
    return this;
  }

  @SuppressWarnings("unchecked")
  public <F extends DocumentFacetBuilder<DocumentBuilder>> F facet(final F facetBuilder) {
    ((DocumentFacetBuilderInternal<DocumentBuilder>)facetBuilder).init(this, document);
    return facetBuilder;
  }

  @Override
  public ContentElementBuilder<DocumentBuilder> contentElement(final String id) {
    return new ContentElementBuilderImpl(this);
  }

  @Override
  public DocumentDto save() {
    return documentServiceTarget.uploadDocumentWithContent(document, parts.toArray(new MultipartFile[parts.size()]));
  }

  public void add(final MultipartFile multipartFile) {
    parts.add(multipartFile);
  }

}
