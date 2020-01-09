package com.neverpile.eureka.client.impl.feign;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.neverpile.eureka.client.core.DocumentDto;
import com.neverpile.eureka.client.core.DocumentFacet;

public class FacetedDocumentDtoModule extends SimpleModule {
  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(FacetedDocumentDtoModule.class);

  private final List<? extends DocumentFacet<?>> facets;

  public FacetedDocumentDtoModule(final List<? extends DocumentFacet<?>> facets) {
    super(FacetedDocumentDtoModule.class.getSimpleName(), Version.unknownVersion());

    this.facets = facets;
    // setSerializerModifier(new FacetedDocumentDtoSerializerModifier());
    setDeserializerModifier(new FacetedDocumentDtoDeserializerModifier());
  }

  public class FacetedDocumentDtoDeserializerModifier extends BeanDeserializerModifier {
    public class FacetDeserializer extends BeanDeserializer {
      private static final long serialVersionUID = 1L;

      public FacetDeserializer(final BeanDeserializerBase base) {
        super(base);
      }

      @Override
      protected void handleUnknownProperty(final JsonParser p, final DeserializationContext ctxt,
          final Object beanOrClass, final String propName) throws IOException {
        // silently ignore HATEOAS links
        if ("_links".equals(propName)) {
          p.skipChildren();
          return;
        }

        Optional<? extends DocumentFacet<?>> facet = facets.stream().filter(
            f -> f.getName().equals(propName)).findFirst();

        if (!facet.isPresent())
          LOGGER.warn("Unknown property {} - does the server use facets we don't know about?", propName);

        DocumentFacet<?> f = facet.get();
        
        JavaType valueType = f.getValueType(ctxt.getTypeFactory());
        JsonDeserializer<Object> deserializer = ctxt.findRootValueDeserializer(valueType);
        Object value = deserializer.deserialize(p, ctxt);
        ((DocumentDto) beanOrClass).facet(f.getName(), value);
      }
    }

    @Override
    public JsonDeserializer<?> modifyDeserializer(final DeserializationConfig config, final BeanDescription beanDesc,
        final JsonDeserializer<?> deserializer) {
      if (beanDesc.getBeanClass() == DocumentDto.class) {
        return new FacetDeserializer((BeanDeserializerBase) deserializer);
      }
      return deserializer;
    }
  }
}