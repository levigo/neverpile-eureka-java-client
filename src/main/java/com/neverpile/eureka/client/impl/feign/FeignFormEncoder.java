package com.neverpile.eureka.client.impl.feign;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import feign.Request.Body;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;

public class FeignFormEncoder implements Encoder {

  private final List<HttpMessageConverter<?>> converters = new RestTemplate().getMessageConverters();

  public static final Charset UTF_8 = Charset.forName("UTF-8");

  private final ObjectMapper mapper;


  public FeignFormEncoder(final Iterable<Module> modules) {
    mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).configure(
        SerializationFeature.INDENT_OUTPUT, true).registerModules(modules);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void encode(final Object object, final Type bodyType, final RequestTemplate template) throws EncodeException {
    if (isFormRequest(bodyType)) {
      encodeMultipartFormRequest((Map<String, ?>) object, template);
    } else {
      try {
        template.body( //
            Body.encoded( //
                mapper.writerFor(mapper.getTypeFactory().constructType(bodyType)) //
                    .writeValueAsString(object).getBytes(UTF_8),
                UTF_8));
      } catch (JsonProcessingException e) {
        throw new EncodeException(e.getMessage(), e);
      }
    }
  }

  /**
   * Encodes the request as a multipart form. It can detect a single {@link MultipartFile}, an array
   * of {@link MultipartFile}s, or POJOs (that are converted to JSON).
   *
   * @param formMap
   * @param template
   * @throws EncodeException
   */
  private void encodeMultipartFormRequest(final Map<String, ?> formMap, final RequestTemplate template)
      throws EncodeException {
    if (formMap == null) {
      throw new EncodeException("Cannot encode request with null form.");
    }
    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    for (Entry<String, ?> entry : formMap.entrySet()) {
      Object value = entry.getValue();
      if (isMultipartFile(value)) {
        map.add(entry.getKey(), encodeMultipartFile((MultipartFile) value));
      } else if (isMultipartFileArray(value)) {
        encodeMultipartFiles(map, entry.getKey(), Arrays.asList((MultipartFile[]) value));
      } else {
        map.add(entry.getKey(), encodeJsonObject(value));
      }
    }
    HttpHeaders multipartHeaders = new HttpHeaders();
    multipartHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

    encodeRequest(map, multipartHeaders, template);
  }

  private boolean isMultipartFile(final Object object) {
    return object instanceof MultipartFile;
  }

  private boolean isMultipartFileArray(final Object o) {
    return o != null && o.getClass().isArray() && MultipartFile.class.isAssignableFrom(o.getClass().getComponentType());
  }

  /**
   * Wraps a single {@link MultipartFile} into a {@link HttpEntity} and sets the
   * {@code Content-type} header to {@code application/octet-stream}
   *
   * @param file
   * @return
   */
  private HttpEntity<?> encodeMultipartFile(final MultipartFile file) {
    HttpHeaders filePartHeaders = new HttpHeaders();
    filePartHeaders.setContentType(MediaType.parseMediaType(file.getContentType()));
    try {
      Resource multipartFileResource = new MultipartFileResource(file.getOriginalFilename(), file.getSize(),
          file.getInputStream());
      return new HttpEntity<>(multipartFileResource, filePartHeaders);
    } catch (IOException ex) {
      throw new EncodeException("Cannot encode request.", ex);
    }
  }

  /**
   * Fills the request map with {@link HttpEntity}s containing the given {@link MultipartFile}s.
   * Sets the {@code Content-type} header to {@code application/octet-stream} for each file.
   *
   * @param map the current request map.
   * @param name the name of the array field in the multipart form.
   * @param files
   */
  private void encodeMultipartFiles(final LinkedMultiValueMap<String, Object> map, final String name,
      final List<? extends MultipartFile> files) {
    try {
      for (MultipartFile file : files) {
        HttpHeaders filePartHeaders = new HttpHeaders();
        filePartHeaders.setContentType(MediaType.parseMediaType(file.getContentType()));
        Resource multipartFileResource = new MultipartFileResource(file.getOriginalFilename(), file.getSize(),
            file.getInputStream());
        map.add(name, new HttpEntity<>(multipartFileResource, filePartHeaders));
      }
    } catch (IOException ex) {
      throw new EncodeException("Cannot encode request.", ex);
    }
  }

  /**
   * Wraps an object into a {@link HttpEntity} and sets the {@code Content-type} header to
   * {@code application/json}
   *
   * @param o
   * @return
   */
  private HttpEntity<?> encodeJsonObject(final Object o) {
    HttpHeaders jsonPartHeaders = new HttpHeaders();
    jsonPartHeaders.setContentType(MediaType.APPLICATION_JSON);

    try {
      JavaType javaType = mapper.getTypeFactory().constructType(o.getClass());
      return new HttpEntity<>(mapper.writerFor(javaType).writeValueAsString(o), jsonPartHeaders);
    } catch (JsonProcessingException e) {
      throw new EncodeException(e.getMessage(), e);
    }
  }

  /**
   * Calls the conversion chain actually used by
   * {@link org.springframework.web.client.RestTemplate}, filling the body of the request template.
   *
   * @param value
   * @param requestHeaders
   * @param template
   * @throws EncodeException
   */
  @SuppressWarnings("unchecked")
  private void encodeRequest(final Object value, final HttpHeaders requestHeaders, final RequestTemplate template)
      throws EncodeException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    HttpOutputMessage dummyRequest = new HttpOutputMessageImpl(outputStream, requestHeaders);
    try {
      Class<?> requestType = value.getClass();
      MediaType requestContentType = requestHeaders.getContentType();
      for (HttpMessageConverter<?> messageConverter : converters) {
        if (messageConverter.canWrite(requestType, requestContentType)) {
          ((HttpMessageConverter<Object>) messageConverter).write(value, requestContentType, dummyRequest);
          break;
        }
      }
    } catch (IOException ex) {
      throw new EncodeException("Cannot encode request.", ex);
    }
    HttpHeaders headers = dummyRequest.getHeaders();
    if (headers != null) {
      for (Entry<String, List<String>> entry : headers.entrySet()) {
        template.header(entry.getKey(), entry.getValue());
      }
    }

    template.body(Body.encoded(outputStream.toByteArray(), UTF_8));
  }

  /**
   * Minimal implementation of {@link org.springframework.http.HttpOutputMessage}. It's needed to
   * provide the request body output stream to
   * {@link org.springframework.http.converter.HttpMessageConverter}s
   */
  private class HttpOutputMessageImpl implements HttpOutputMessage {

    private final OutputStream body;
    private final HttpHeaders headers;

    public HttpOutputMessageImpl(final OutputStream body, final HttpHeaders headers) {
      this.body = body;
      this.headers = headers;
    }

    @Override
    public OutputStream getBody() throws IOException {
      return body;
    }

    @Override
    public HttpHeaders getHeaders() {
      return headers;
    }

  }

  /**
   * Heuristic check for multipart requests.
   *
   * @param type
   * @return
   * @see feign.Types
   */
  static boolean isFormRequest(final Type type) {
    return MAP_STRING_WILDCARD.equals(type);
  }

  /**
   * Dummy resource class. Wraps file content and its original name.
   */
  static class MultipartFileResource extends InputStreamResource {

    private final String filename;
    private final long size;

    public MultipartFileResource(final String filename, final long size, final InputStream inputStream) {
      super(inputStream);
      this.size = size;
      this.filename = filename;
    }

    @Override
    public String getFilename() {
      return this.filename;
    }

    @Override
    public InputStream getInputStream() throws IOException, IllegalStateException {
      return super.getInputStream();
    }

    @Override
    public long contentLength() throws IOException {
      return size;
    }

  }

}
