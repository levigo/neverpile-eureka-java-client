package com.neverpile.eureka.client.content;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ContentElementBuilderImpl<P> implements ContentElementBuilder<P> {

  private final P documentBuilderImpl;

  private String name;

  private String role;

  private String mediaType;

  private Supplier<InputStream> streamSupplier;

  private final Consumer<MultipartFile> fileConsumer;

  public ContentElementBuilderImpl(final P documentBuilderImpl, final Consumer<MultipartFile> fileConsumer) {
    this.documentBuilderImpl = documentBuilderImpl;
    this.fileConsumer = fileConsumer;
  }

  @Override
  public ContentElementBuilder<P> fileName(final String name) {
    this.name = name;
    return this;
  }

  @Override
  public ContentElementBuilder<P> role(final String role) {
    this.role = role;
    return this;
  }

  @Override
  public ContentElementBuilder<P> mediaType(final String mediaType) {
    this.mediaType = mediaType;
    return this;
  }

  @Override
  public ContentElementBuilder<P> content(final InputStream stream) {
    streamSupplier = () -> stream;
    return this;
  }

  @Override
  public ContentElementBuilder<P> content(final Supplier<InputStream> stream) {
    this.streamSupplier = stream;
    return this;
  }

  @Override
  public ContentElementBuilder<P> content(final byte[] content) {
    streamSupplier = () -> new ByteArrayInputStream(content);
    return this;
  }

  @Override
  public ContentElementBuilder<P> content(final File content) {
    streamSupplier = () -> {
      try {
        return new FileInputStream(content);
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }
    };
    return this;
  }

  @Override
  public P attach() {
    fileConsumer.accept(new MultipartFile() {
      @Override
      public String getOriginalFilename() {
        return null != name && name.length() > 0 ? name : "unknown.dat";
      }

      @Override
      public String getName() {
        return role;
      }

      @Override
      public InputStream getInputStream() throws IOException {
        return streamSupplier.get();
      }

      @Override
      public String getContentType() {
        return mediaType;
      }
    });
    return documentBuilderImpl;
  }

}
