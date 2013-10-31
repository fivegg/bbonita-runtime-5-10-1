/**
 * Copyright (C) 2009  BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 */
package org.ow2.bonita.facade.runtime.command;

import java.util.ArrayList;
import java.util.List;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.identity.Group;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.search.SearchQueryBuilder;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.Command;

public class WebSearchGroups implements Command<WebSearchResult<Group>> {

  private static final long serialVersionUID = 3672765528271277446L;
  private int fromIndex;
  private int pageSize;
  private boolean searchInHistory;
  private SearchQueryBuilder query;

  /**
   * Default constructor.
   * 
   * @param userId
   * @param fromIndex
   * @param pageSize
   */
  public WebSearchGroups(SearchQueryBuilder query, int fromIndex, int pageSize, boolean searchInHistory) {
    this.fromIndex = fromIndex;
    this.pageSize = pageSize;
    this.searchInHistory = searchInHistory;
    this.query = query;
  }

  public WebSearchResult<Group> execute(Environment environment) throws Exception {
    final APIAccessor accessor = new StandardAPIAccessorImpl();
    final QueryRuntimeAPI queryRuntimeAPI;
    if (this.searchInHistory) {
      queryRuntimeAPI = accessor.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_HISTORY_KEY);
    } else {
      queryRuntimeAPI = accessor.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_JOURNAL_KEY);
    }
    final int nbOfPossibleResults = queryRuntimeAPI.search(query);
    if (nbOfPossibleResults > fromIndex) {
      final List<Group> users = queryRuntimeAPI.search(query, fromIndex, pageSize);
      if (users != null) {
        return new WebSearchResult<Group>(users, nbOfPossibleResults);
      }
    }
    return new WebSearchResult<Group>(new ArrayList<Group>(), nbOfPossibleResults);
  }

}
