package com.neverpile.eureka.client.core;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(value = "_links", ignoreUnknown = true)
public class ContentElement {
  private Instant versionTimestamp;
  private String role;
  private String fileName;
  private String type;
  private String id;
  private long length;
  private Digest digest;
  private EncryptionType encryption;

  public ContentElement() {
    // POJO
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

  public EncryptionType getEncryption() {
    return encryption;
  }

  public void setEncryption(final EncryptionType encryption) {
    this.encryption = encryption;
  }

  /**
   * Return the enclosing document's version timestamp. 
   * 
   * @return the version timestamp
   */
  public Instant getVersionTimestamp() {
    return versionTimestamp;
  }

  public void setVersionTimestamp(final Instant versionTimestamp) {
    this.versionTimestamp = versionTimestamp;
  }
}
