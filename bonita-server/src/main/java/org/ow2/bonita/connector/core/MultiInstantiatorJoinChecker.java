/**
 * Copyright (C) 2010  BonitaSoft S.A.
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
package org.ow2.bonita.connector.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.light.LightActivityInstance;
import org.ow2.bonita.util.AccessorUtil;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class MultiInstantiatorJoinChecker extends MultipleInstancesJoinChecker {

  private MultiInstantiator instantiator;
  private Map<String, Object[]> instantiatorParameters;
  private String className;

  public Map<String, Object[]> getInstantiatorParameters() {
    return instantiatorParameters;
  }

  public void setInstantiator(MultiInstantiator instantiator) {
    this.instantiator = instantiator;
  }

  public void setInstantiatorParameters(Map<String, Object[]> instantiatorParameters) {
    this.instantiatorParameters = instantiatorParameters;
  }

  public void setInstantiatorParameters(List<List<Object>> instantiatorParameters) {
    this.instantiatorParameters = bonitaListToArrayMap(instantiatorParameters, String.class, Object.class);
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getClassName() {
    return this.className;
  }

  @Override
  protected boolean isJoinOK() throws Exception {
    instantiator.execute();
    int join = instantiator.getJoinNumber();
    final APIAccessor accessor = new StandardAPIAccessorImpl();
    final QueryRuntimeAPI queryRuntimeAPI = accessor.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_JOURNAL_KEY);
    Set<LightActivityInstance> activities = queryRuntimeAPI.getLightActivityInstances(getProcessInstanceUUID(), getActivityName());
    int count = 0;
    for (LightActivityInstance activityInstance : activities) {
      if (ActivityState.READY.equals(activityInstance.getState())
          && getIterationId().equals(activityInstance.getIterationId())) {
        count++;
      }
    }
    int finishedactivities = activities.size() - count;
    return join <= finishedactivities;
  }

}
