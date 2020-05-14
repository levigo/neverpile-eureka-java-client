// NOTE: this class was first published under the Apache Software License 1.1
// as part of apache axis.

// MultipartInputStream.java
// $Id: MultipartInputStream.java,v 1.8 2000/08/16 21:38:01 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package com.neverpile.eureka.client.impl.feign;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.neverpile.eureka.client.core.Digest;
import com.neverpile.eureka.client.core.DocumentService.ContentElementResponse;
import com.neverpile.eureka.client.core.HashAlgorithm;

/**
 * A content element sequence handles a multipart stream result from a content query. The sequence's
 * streams must be consumed in-sequence, i.e. once the sequence has been advanced to the next stream
 * using {@link #nextContentElement()}, a previous stream must no longer be read from.
 */
public class ContentElementSequence {
  private InputStream in = null;
  private byte boundary[] = null;

  private final class PartialStreamContentElementResponse implements ContentElementResponse {
    private final class PartialStreamInputStream extends InputStream {
      private boolean closed;
      private boolean partialStreamEOF;

      @Override
      public void close() throws IOException {
        closed = true;
      }

      public int available() throws IOException {
        verifyState();

        return in.available();
      }

      private void verifyState() throws IOException {
        PartialStreamContentElementResponse.this.verifyState();

        if (closed)
          throw new IOException("Stream is closed");
      }

      /**
       * Read one byte of data from the current part.
       * 
       * @return A byte of data, or <strong>-1</strong> if end of file.
       * @exception IOException If some IO error occured.
       */

      public int read() throws IOException {
        verifyState();
        
        if(partialStreamEOF)
          return -1;

        final int ch = in.read();
        switch (ch){
          case '\r' :
            // check for a boundary
            in.mark(boundary.length + 5);
            if (in.read() == '\n' && in.read() == '-' && in.read() == '-') {
              boolean isBoundary = true;
              for (int i = 0; i < boundary.length; i++)
                if (((byte) in.read()) != boundary[i]) {
                  isBoundary = false;
                  break;
                }

              if (isBoundary) {
                if (in.read() == '-') { // last part?
                  if (in.read() != '-')
                    throw new IOException("Invalid part boundary: missing second end-of-stream dash");
                  state = State.END_OF_STREAM;
                } else 
                  state = State.END_OF_PART;

                partialStreamEOF = true;
                in.reset();
                return -1;
              }
            } else {
              in.reset();
              return ch;
            }

            // not reached
          case -1 :
            state = State.END_OF_STREAM;
            partialStreamEOF = true;
            return -1;

          default :
            return ch;
        }
      }

      /**
       * Read n bytes of data from the current part.
       * 
       * @return the number of bytes data, read or <strong>-1</strong> if end of file.
       * @exception IOException If some IO error occured.
       */
      public int read(final byte b[], final int off, final int len) throws IOException {
        verifyState();

        int got = 0;
        int ch;
        while (got < len) {
          if ((ch = read()) == -1)
            return (got == 0) ? -1 : got;
          b[off + (got++)] = (byte) (ch & 0xFF);
        }
        return got;
      }

      public long skip(long n) throws IOException {
        verifyState();

        while ((--n >= 0) && (read() != -1))
          ;
        return n;
      }
    }

    private final int streamIndex;
    private final Digest digest;
    private final String mediaType;
    private final Instant versionTimestamp;

    private PartialStreamContentElementResponse(final int streamIndex, final Digest digest) {
      this.streamIndex = streamIndex;
      this.digest = digest;
      this.mediaType = currentHeaders.computeIfAbsent("content-type", t -> "application/octet-stream");
      
      String h = currentHeaders.getOrDefault("x-npe-document-version-timestamp", "-");
      this.versionTimestamp = h.length() > 1 ? Instant.parse(h) : null;
    }

    @Override
    public String getMediaType() {
      return mediaType;
    }

    @Override
    public Digest getDigest() {
      return digest;
    }

    @Override
    public Instant getVersionTimestamp() {
      return versionTimestamp;
    }
    
    @Override
    public InputStream getContent() throws IOException {
      verifyState();
      return new PartialStreamInputStream();
    }

    private void verifyState() {
      if (streamIndex != currentStreamIndex)
        throw new IllegalStateException("Already advanced to the next stream");
    }
  }

  private enum State {
    IDLE, IN_PART, END_OF_PART, END_OF_STREAM, CLOSED
  }

  private State state = State.IDLE;

  private final Map<String, String> currentHeaders = new HashMap<String, String>();

  private int currentStreamIndex = -1;

  // Skip to next input boundary, set stream at begining of content:
  // Returns true if boundary was found, false otherwise.
  protected boolean skipToBoundary() throws IOException {
    int ch;
    while ((ch = in.read()) != -1) {
      if (ch == '-') {
        if ((ch = in.read()) != '-')
          continue;

        for (int i = 0; i < boundary.length; i++)
          if (((byte) in.read()) != boundary[i])
            continue;


        // check line terminator
        switch (ch = in.read()){
          case '\r' :
            if (in.read() != '\n')
              throw new IOException("Invalid part boundary: no \\n");
            break;

          case '-' :
            // last part
            if (in.read() != '-')
              throw new IOException("Invalid part boundary: missing second end-of-stream dash");
            state = State.END_OF_STREAM;
            return false;

          default :
            throw new IOException(
                "Invalid part boundary: unexpected character following boundary separator: " + (char) ch);
        }

        // read header lines
        currentHeaders.clear();

        String previousFieldName = null;
        String headerLine;
        while ((headerLine = readLine()).length() > 0) {
          if (Character.isWhitespace(headerLine.charAt(0))) {
            if (null == previousFieldName)
              throw new IOException("Invalid part boundary: field value continuation but no current field");
            currentHeaders.put(previousFieldName,
                currentHeaders.get(previousFieldName) + "\r\n" + trimLeadingWhitespace(headerLine));
          }

          int idx = headerLine.indexOf(':');
          if (idx < 0)
            throw new IOException("Invalid part boundary: missing colon in header line");
          String fieldName = headerLine.substring(0, idx).trim().toLowerCase();
          String fieldValue = trimLeadingWhitespace(headerLine.substring(idx + 1));

          currentHeaders.put(fieldName, fieldValue);

          previousFieldName = fieldName;
        }

        state = State.IN_PART;
        return true;
      }
    }

    in.close();

    state = State.END_OF_STREAM;
    return false;
  }

  /**
   * Return the next content element response or <code>null</code> if no more elements were
   * contained in the stream.
   * 
   * @return the next content element response or <code>null</code>
   * @throws IOException
   */
  public ContentElementResponse nextContentElement() throws IOException {
    if (!advanceToNextStream())
      return null;

    String digestHeader = currentHeaders.getOrDefault("digest", "");

    Digest digest;
    if (!digestHeader.isEmpty()) {
      String[] split = digestHeader.split("=");
      if (split.length == 2) {
        try {
          HashAlgorithm algorithm = HashAlgorithm.fromValue(split[0].toUpperCase());
          digest = new Digest(algorithm, Base64.getDecoder().decode(split[1]));
        } catch (Exception e) {
          throw new IOException("Invalid Digest received: " + digestHeader, e);
        }
      } else
        digest = null;
    } else
      digest = null;

    return new PartialStreamContentElementResponse(++currentStreamIndex, digest);
  }

  private String trimLeadingWhitespace(final String s) {
    int i = 0;
    while (Character.isWhitespace(s.charAt(i)))
      i++;

    if (i > 0)
      return s.substring(i);
    else
      return s;
  }

  private String readLine() throws IOException {
    StringBuilder sb = new StringBuilder(80);
    int ch;
    while ((ch = in.read()) >= 0)
      if (ch == '\r') {
        if (in.read() != '\n')
          throw new IOException("Invalid part boundary: missing \\n in header line");
        break;
      } else
        sb.append((char) ch);

    return sb.toString();
  }

  /**
   * Switch to the next available part of data. One can interrupt the current part, and use this
   * method to switch to next part before current part was totally read.
   * 
   * @return A boolean <strong>true</strong> if there next partis ready, or <strong>false</strong>
   *         if this was the last part.
   */

  private boolean advanceToNextStream() throws IOException {
    switch (state){
      case IDLE :
      case END_OF_PART :
      case IN_PART :
        return skipToBoundary();

      default :
      case END_OF_STREAM :
        return false;

      case CLOSED :
        throw new IllegalStateException("Multipart stream is closed");
    }
  }

  /**
   * Construct a new multipart input stream.
   * 
   * @param in The initial (multipart) input stream.
   * @param boundary The input stream MIME boundary.
   */

  public ContentElementSequence(final InputStream in, final byte boundary[]) {
    this.in = (in.markSupported() ? in : new BufferedInputStream(in, boundary.length + 5));
    this.boundary = boundary;
  }

  public void close() throws IOException {
    in.close();
    state = State.CLOSED;
  }
}
