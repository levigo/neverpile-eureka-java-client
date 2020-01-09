package com.neverpile.eureka.client.impl.feign;

import static feign.form.ContentProcessor.CRLF;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.neverpile.eureka.client.content.ContentElementFacet;
import com.neverpile.eureka.client.core.CreationDateFacet;
import com.neverpile.eureka.client.core.DocumentDto;
import com.neverpile.eureka.client.core.ModificationDateFacet;
import com.neverpile.eureka.client.core.NeverpileClient;
import com.neverpile.eureka.client.metadata.MetadataFacet;

import feign.Feign;
import feign.RequestInterceptor;
import feign.codec.EncodeException;
import feign.form.ContentType;
import feign.form.FormEncoder;
import feign.form.MultipartFormContentProcessor;
import feign.form.multipart.AbstractWriter;
import feign.form.multipart.Output;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

public class EurekaClientBuilder {
  private final class DocumentDtoPartWriter extends AbstractWriter {
    private final ObjectMapper jackson;

    private DocumentDtoPartWriter(final ObjectMapper jackson) {
      this.jackson = jackson;
    }

    @Override
    public void write(final Output output, final String key, final Object value) throws EncodeException {
      writeFileMetadata(output, "__DOC", "", "application/json");

      try {
        jackson.writer().writeValue(new OutputStream() {
          @Override
          public void write(final int b) throws IOException {
            output.write(new byte[]{
                (byte) b
            });
          }

          public void write(final byte[] b) throws IOException {
            output.write(b);
          }

          public void write(final byte[] b, final int off, final int len) throws IOException {
            output.write(b, off, len);
          }
        }, value);
      } catch (IOException e) {
        throw new EncodeException("Can't encode __DOC part", e);
      }
    }

    @Override
    public boolean isApplicable(final Object value) {
      return value instanceof DocumentDto;
    }
  }

  private final class MultipartFileWriter extends AbstractWriter {
    @Override
    public void write(final Output output, final String boundary, final String key, final Object value) throws EncodeException {
      for (MultipartFile f : (MultipartFile[]) value) {
        output.write("--").write(boundary).write(CRLF);
        write(output, key, f);
        output.write(CRLF);
      }
    }

    public void write(final Output output, final String key, final MultipartFile mf) throws EncodeException {
      writeFileMetadata(output, mf.getName(), mf.getOriginalFilename(), mf.getContentType());

      try (InputStream is = mf.getInputStream()) {
        byte[] buf = new byte[4096];
        int length;
        while ((length = is.read(buf)) > 0) {
          output.write(buf, 0, length);
        }
      } catch (IOException e) {
        throw new EncodeException("Can't send multipart file", e);
      }
    }

    @Override
    public boolean isApplicable(final Object value) {
      return value instanceof MultipartFile[];
    }
  }

  private String baseURL;

  private final Feign.Builder builder;

  public EurekaClientBuilder() {
    builder = Feign.builder() //
        .errorDecoder(new FeignErrorDecoder()) //
        .decoder(createDecoder()) //
        .encoder(createEncoder());
  }

  private JacksonDecoder createDecoder() {
    return new JacksonDecoder(jackson());
  }

  private FormEncoder createEncoder() {
    ObjectMapper jackson = jackson();

    FormEncoder formEncoder = new FormEncoder(new JacksonEncoder(jackson));

    MultipartFormContentProcessor mfcp = (MultipartFormContentProcessor) formEncoder.getContentProcessor(
        ContentType.MULTIPART);

    mfcp.addFirstWriter(new DocumentDtoPartWriter(jackson));
    mfcp.addFirstWriter(new MultipartFileWriter());

    return formEncoder;
  }

  private ObjectMapper jackson() {
    return new ObjectMapper() //
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false) //
        .registerModules(jacksonModules());
  }

  private List<Module> jacksonModules() {
    return Arrays.asList(facetedDocumentModule(), new JavaTimeModule());
  }

  private FacetedDocumentDtoModule facetedDocumentModule() {
    // FIXME: improve discovery mechanism
    return new FacetedDocumentDtoModule(Arrays.asList( //
        new MetadataFacet(), //
        new ContentElementFacet(), //
        new CreationDateFacet(), //
        new ModificationDateFacet() //
    ));
  }

  public EurekaClientBuilder baseURL(final String baseURL) {
    this.baseURL = baseURL;
    return this;
  }

  public OAuth2ClientBuilder withOAuth2() {
    return new OAuth2ClientBuilder(this);
  }

  public EurekaClientBuilder requestInterceptor(final RequestInterceptor i) {
    builder.requestInterceptor(i);
    return this;
  }

  public NeverpileClient build() {
    return new FeignNeverpileClient(builder, baseURL);
  }

  public BasicAuthBuilder withBasicAuth() {
    return new BasicAuthBuilder(this);
  }
}
