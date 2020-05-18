package com.neverpile.eureka.client.content;

import java.util.List;

import com.neverpile.eureka.client.core.ContentElement;

public interface ContentElementListBuilder {
  ContentElementBuilder<ContentElementListBuilder> element();
  
  List<ContentElement> save();
}