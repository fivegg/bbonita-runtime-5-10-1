/**
 * Copyright (C) 2009  BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 */
package org.ow2.bonita.facade.runtime.command;

import java.io.Serializable;
import java.util.List;

public class WebSearchResult<E> implements Serializable {

  private static final long serialVersionUID = -2017489006378396882L;
  private int count;
  private List<E> elements;

  /**
   * Default constructor.
   * 
   * @param userId
   * @param fromIndex
   * @param pageSize
   */
  public WebSearchResult(List<E> elements, int matchingElementsCount) {
    this.count = matchingElementsCount;
    this.elements = elements;
  }

  public List<E> getSearchResults() {
    return elements;
  }

  public int getSearchMatchingElementsCount() {
    return count;
  }
}
