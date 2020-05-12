package com.neverpile.eureka.client.core;

import java.io.IOException;

import com.neverpile.eureka.client.core.DocumentService.ContentElementResponse;
import com.neverpile.eureka.client.impl.feign.ContentElementSequence;

/**
 * A builder for content element queries.
 */
public interface ContentQueryBuilder {

  /**
   * Query for the given mediaType. Multiple calls to this method will yield a query for any of the
   * given types.
   * 
   * @param mediaType the media type
   * @return this builder instance
   */
  ContentQueryBuilder withMediaType(String mediaType);

  /**
   * Query for the given content element role. Multiple calls to this method will yield a query for
   * any of the given roles.
   * 
   * @param role the role
   * @return this builder instance
   */
  ContentQueryBuilder withRole(String role);

  /**
   * Execute the query and return the first of any matching content element
   * 
   * @return the ContentElementResponse for the first match
   * @throws IOException
   */
  ContentElementResponse getFirst() throws IOException;

  /**
   * Execute the query and return the only matching content element. Return an error, if more than
   * one match was found.
   * 
   * @return the ContentElementResponse for the only match
   * @throws IOException
   */
  ContentElementResponse getOnly() throws IOException;

  /**
   * Return all matching content elements as a {@link ContentElementSequence}. All content element
   * properties must be retrieved from the headers provided in the stream.
   * 
   * @return
   * @throws IOException
   */
  ContentElementSequence getAll() throws IOException;

}