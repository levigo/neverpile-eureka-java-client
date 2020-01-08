package com.neverpile.eureka.client.content;

import java.io.File;
import java.io.InputStream;
import java.util.function.Supplier;

import org.springframework.http.MediaType;

public interface ContentElementBuilder<P> {
  ContentElementBuilder<P> fileName(String name);

  ContentElementBuilder<P> role(String role);
  
  ContentElementBuilder<P> mediaType(String mediaType);
  
  ContentElementBuilder<P> mediaType(MediaType mediaType);
  
  ContentElementBuilder<P> content(InputStream stream);
  
  ContentElementBuilder<P> content(Supplier<InputStream> stream);
  
  ContentElementBuilder<P> content(byte[] content);
  
  ContentElementBuilder<P> content(File content);
  
  P attach();
}
