package com.neverpile.eureka.client.core;

public class ContentElementDto {

  private String role;
  private String fileName;
  private String type;
  private String id;
  private long length;
  private Digest digest;


  public ContentElementDto() {
  }

  public String getOriginalFilename() {
    return fileName;
  }

  
  public String getType() {
    return type;
  }
  
  public String getRole() {
    return role;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(final String fileName) {
    this.fileName = fileName;
  }
  
  public void setRole(final String role) {
    this.role = role;
  }

  public void setType(final String contentType) {
    this.type = contentType;
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public long getLength() {
    return length;
  }

  public void setLength(final long length) {
    this.length = length;
  }
 
  public Digest getDigest() {
    return digest;
  }
  
  public void setDigest(final Digest digest) {
    this.digest = digest;
  }
}
