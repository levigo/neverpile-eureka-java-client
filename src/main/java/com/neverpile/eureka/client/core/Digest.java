package com.neverpile.eureka.client.core;

import java.util.Arrays;
import java.util.Objects;

public class Digest {
  private HashAlgorithm algorithm;
  
  private byte[] bytes;

  public Digest() {
  }
  
  public Digest(final HashAlgorithm algorithm, final byte[] hashBytes) {
    this.algorithm = algorithm;
    bytes = hashBytes;
  }

  public HashAlgorithm getAlgorithm() {
    return algorithm;
  }

  public void setAlgorithm(final HashAlgorithm algorithm) {
    this.algorithm = algorithm;
  }

  public byte[] getBytes() {
    return bytes;
  }

  public void setBytes(final byte[] digestBytes) {
    this.bytes = digestBytes;
  }

  @Override
  public int hashCode() {
    return Objects.hash(algorithm, bytes);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Digest other = (Digest) obj;
    if (algorithm != other.algorithm)
      return false;
    if (!Arrays.equals(bytes, other.bytes))
      return false;
    return true;
  }
}