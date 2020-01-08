package com.neverpile.eureka.client.model;

import java.util.Arrays;
import java.util.Date;

import org.springframework.http.MediaType;

public class MetadataElement {
  private String schema;

  private String contentType = MediaType.APPLICATION_JSON_VALUE;

  private byte[] content;

  private EncryptionType encryption;

  private String keyHint;

  private Date dateCreated;

  private Date dateModified;
  
  public String getSchema() {
    return schema;
  }

  public void setSchema(final String schema) {
    this.schema = schema;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(final String format) {
    this.contentType = format;
  }

  public byte[] getContent() {
    return content;
  }

  public void setContent(final byte[] content) {
    this.content = content;
  }

  public EncryptionType getEncryption() {
    return encryption;
  }

  public void setEncryption(final EncryptionType encryption) {
    this.encryption = encryption;
  }

  public String getKeyHint() {
    return keyHint;
  }

  public void setKeyHint(final String keyHint) {
    this.keyHint = keyHint;
  }

  public Date getDateCreated() {
    return dateCreated;
  }

  public void setDateCreated(final Date dateCreated) {
    this.dateCreated = dateCreated;
  }

  public Date getDateModified() {
    return dateModified;
  }

  public void setDateModified(final Date dateModified) {
    this.dateModified = dateModified;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(content);
    result = prime * result + ((contentType == null) ? 0 : contentType.hashCode());
    result = prime * result + ((dateCreated == null) ? 0 : dateCreated.hashCode());
    result = prime * result + ((dateModified == null) ? 0 : dateModified.hashCode());
    result = prime * result + ((encryption == null) ? 0 : encryption.hashCode());
    result = prime * result + ((keyHint == null) ? 0 : keyHint.hashCode());
    result = prime * result + ((schema == null) ? 0 : schema.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (getClass() != obj.getClass())
      return false;
    MetadataElement other = (MetadataElement) obj;
    if (!Arrays.equals(content, other.content))
      return false;
    if (contentType == null) {
      if (other.contentType != null)
        return false;
    } else if (!contentType.equals(other.contentType))
      return false;
    if (dateCreated == null) {
      if (other.dateCreated != null)
        return false;
    } else if (!dateCreated.equals(other.dateCreated))
      return false;
    if (dateModified == null) {
      if (other.dateModified != null)
        return false;
    } else if (!dateModified.equals(other.dateModified))
      return false;
    if (encryption != other.encryption)
      return false;
    if (keyHint == null) {
      if (other.keyHint != null)
        return false;
    } else if (!keyHint.equals(other.keyHint))
      return false;
    if (schema == null) {
      return other.schema == null;
    } else {
      return schema.equals(other.schema);
    }
  }
}
