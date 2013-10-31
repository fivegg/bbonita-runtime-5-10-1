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
import org.ow2.bonita.facade.identity.User;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.search.SearchQueryBuilder;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.Command;

public class WebSearchUsers implements Command<WebSearchResult<User>> {

  private static final long serialVersionUID = -5386146444505586809L;
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
  public WebSearchUsers(SearchQueryBuilder query, int fromIndex, int pageSize, boolean searchInHistory) {
    this.fromIndex = fromIndex;
    this.pageSize = pageSize;
    this.searchInHistory = searchInHistory;
    this.query = query;
  }

  public WebSearchResult<User> execute(Environment environment) throws Exception {
    final APIAccessor accessor = new StandardAPIAccessorImpl();
    final QueryRuntimeAPI queryRuntimeAPI;
    if (this.searchInHistory) {
      queryRuntimeAPI = accessor.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_HISTORY_KEY);
    } else {
      queryRuntimeAPI = accessor.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_JOURNAL_KEY);
    }
    final int nbOfPossibleResults = queryRuntimeAPI.search(query);
    if (nbOfPossibleResults > fromIndex) {
      final List<User> users = queryRuntimeAPI.search(query, fromIndex, pageSize);
      if (users != null) {
        return new WebSearchResult<User>(users, nbOfPossibleResults);
      }
    }
    return new WebSearchResult<User>(new ArrayList<User>(), nbOfPossibleResults);
  }

}
