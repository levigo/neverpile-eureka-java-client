package com.neverpile.eureka.client.metadata;

import javax.xml.bind.JAXBElement;

import org.w3c.dom.Element;

public interface XmlMetadataElementBuilder<P> {

  XmlMetadataElementBuilder<P> content(Element element);
  
  XmlMetadataElementBuilder<P> content(JAXBElement<?> element);
  
  XmlMetadataElementBuilder<P> content(String xml);
  
  XmlMetadataElementBuilder<P> content(byte[] xml);

  P attach();
  
}
