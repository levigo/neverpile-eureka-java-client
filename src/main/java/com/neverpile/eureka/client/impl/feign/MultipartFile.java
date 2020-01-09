package com.neverpile.eureka.client.impl.feign;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface MultipartFile {

  void transferTo(File dest) throws IOException, IllegalStateException;

  boolean isEmpty();

  long getSize();

  String getOriginalFilename();

  String getName();

  InputStream getInputStream() throws IOException;

  String getContentType();

}
