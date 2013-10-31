/**
 * Copyright (C) 2009  BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 **/
package org.ow2.bonita.facade.runtime.command;

import java.util.List;
import java.util.Set;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.QueryDefinitionAPI;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.Command;

/**
 * List the process instances the user is involved in. The list is limited to
 * pageSize elements. Only instances of processes having the given category are
 * considered.
 * 
 * @author Nicolas Chabanoles
 * 
 */
public class WebGetLightProcessInstancesFromCategoryCommand implements Command<List<LightProcessInstance>> {

  private static final long serialVersionUID = 6552317918633404796L;
  private final String userId;
  private final int fromIndex;
  private final int pageSize;
  private final boolean searchInHistory;
  private final String category;

  /**
   * Default constructor.
   * 
   * @param userId
   * @param fromIndex
   * @param pageSize
   */
  public WebGetLightProcessInstancesFromCategoryCommand(final String userId, final String categoryName,
      final int fromIndex, final int pageSize, final boolean searchInHistory) {
    this.userId = userId;
    category = categoryName;
    this.fromIndex = fromIndex;
    this.pageSize = pageSize;
    this.searchInHistory = searchInHistory;
  }

  @Override
  public List<LightProcessInstance> execute(final Environment environment) throws Exception {
    final APIAccessor accessor = new StandardAPIAccessorImpl();
    final QueryRuntimeAPI queryRuntimeAPI;
    final QueryDefinitionAPI queryDefinitionAPI;
    if (searchInHistory) {
      queryRuntimeAPI = accessor.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_HISTORY_KEY);
      // There is no way to know where the process definition is.
      queryDefinitionAPI = accessor.getQueryDefinitionAPI();
    } else {
      queryRuntimeAPI = accessor.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_JOURNAL_KEY);
      queryDefinitionAPI = accessor.getQueryDefinitionAPI(AccessorUtil.QUERYLIST_JOURNAL_KEY);
    }

    final Set<ProcessDefinitionUUID> processes = queryDefinitionAPI.getProcessUUIDs(category);
    return queryRuntimeAPI.getLightParentProcessInstancesWithInvolvedUser(userId, fromIndex, pageSize, processes);
  }

}
