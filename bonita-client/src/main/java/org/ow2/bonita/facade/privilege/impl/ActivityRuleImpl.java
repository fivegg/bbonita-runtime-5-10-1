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
package org.ow2.bonita.facade.privilege.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;

/**
 * @author Anthony Birembaut
 * 
 */
public class ActivityRuleImpl extends RuleImpl {

  private static final long serialVersionUID = -1096929781625084230L;

  protected ActivityRuleImpl() {
    super();
  }
  
  public ActivityRuleImpl(RuleType ruleType) {
    super(ruleType);
  }
  
  public ActivityRuleImpl(String name, String label, String description, RuleType ruleType, Set<ActivityDefinitionUUID> activities) {
    super(name, label, description, ruleType, activities);
  }

  public ActivityRuleImpl(ActivityRuleImpl src) {
    super(src);
  }
  
  public void addActivities(Collection<ActivityDefinitionUUID> activities) {
    super.addExceptions(activities);
  }
  
  public void removeActivities(Set<ActivityDefinitionUUID> activities) {
    super.removeExceptions(activities);
  }

  public void setActivities(Set<ActivityDefinitionUUID> activities) {
    super.setExceptions(activities);
  }
  
  public Set<ActivityDefinitionUUID> getActivities() {
    
    Set<ActivityDefinitionUUID> activitiesUUIDs = new HashSet<ActivityDefinitionUUID>();
    for (String definitionUUID : getItems()) {
      activitiesUUIDs.add(new ActivityDefinitionUUID(definitionUUID));
    }
    return activitiesUUIDs;
  }
}
