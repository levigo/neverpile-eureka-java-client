package com.neverpile.eureka.client.impl.feign.metadata;

import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;

import com.neverpile.eureka.client.core.Metadata;
import com.neverpile.eureka.client.impl.feign.AbstractMetadataElementBuilderImpl;
import com.neverpile.eureka.client.metadata.XmlMetadataElementBuilder;

public class XmlMetadataElementBuilderImpl<P>
    extends
      AbstractMetadataElementBuilderImpl<P, XmlMetadataElementBuilderImpl<P>>
    implements
      XmlMetadataElementBuilder<P> {

  public XmlMetadataElementBuilderImpl(final P parent, final String schema, final Metadata metadata) {
    super(parent, schema, metadata);
    
    element.setContentType("application/xml");
  }

  @Override
  public P attach() {
    return parent;
  }

  @Override
  public XmlMetadataElementBuilder<P> content(final Element element) {
    try {
      TransformerFactory transFactory = TransformerFactory.newInstance();
      Transformer transformer = transFactory.newTransformer();
      
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      transformer.transform(new DOMSource(element), new StreamResult(baos));

      this.element.setContent(baos.toByteArray());
    } catch (IllegalArgumentException | TransformerFactoryConfigurationError | TransformerException e) {
      throw new RuntimeException(e);
    }
    
    return this;
  }


  @Override
  public XmlMetadataElementBuilder<P> content(final JAXBElement<?> je) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      JAXBContext.newInstance().createMarshaller().marshal(je, baos);
      element.setContent(baos.toByteArray());
      return this;
    } catch (JAXBException e) {
      throw new RuntimeException(e);
    }
  }
}
