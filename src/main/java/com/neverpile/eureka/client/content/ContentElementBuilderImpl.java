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

  private Supplier<Long> sizeSupplier = new Supplier<Long>() {
    @Override
    public Long get() {
      return -1L;
    }
  };

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
    streamSupplier = new Supplier<InputStream>() {
      @Override
      public InputStream get() {
        return stream;
      }
    };
    return this;
  }

  @Override
  public ContentElementBuilder<P> content(final Supplier<InputStream> stream) {
    this.streamSupplier = stream;
    return this;
  }

  @Override
  public ContentElementBuilder<P> content(final byte[] content) {
    streamSupplier = new Supplier<InputStream>() {
      @Override
      public InputStream get() {
        return new ByteArrayInputStream(content);
      }
    };
    sizeSupplier = new Supplier<Long>() {
      @Override
      public Long get() {
        return (long) content.length;
      }
    };
    return this;
  }

  @Override
  public ContentElementBuilder<P> content(final File content) {
    streamSupplier = new Supplier<InputStream>() {
      @Override
      public InputStream get() {
        try {
          return new FileInputStream(content);
        } catch (FileNotFoundException e) {
          throw new RuntimeException(e);
        }
      }
    };
    sizeSupplier = new Supplier<Long>() {
      @Override
      public Long get() {
        return content.length();
      }
    };
    return this;
  }

  @Override
  public P attach() {
    fileConsumer.accept(new MultipartFile() {
      @Override
      public void transferTo(final File dest) throws IOException, IllegalStateException {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean isEmpty() {
        return false;
      }

      @Override
      public long getSize() {
        return sizeSupplier.get();
      }

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
