package com.neverpile.eureka.client.content;

import java.io.IOException;
import java.io.InputStream;

public interface MultipartFile {

  String getOriginalFilename();

  String getName();

  InputStream getInputStream() throws IOException;

  String getContentType();

}
