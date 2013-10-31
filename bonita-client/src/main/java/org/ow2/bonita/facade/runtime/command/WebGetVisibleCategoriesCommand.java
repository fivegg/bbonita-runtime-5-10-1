/**
 * Copyright (C) 2010-2013 BonitaSoft S.A.
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.WebAPI;
import org.ow2.bonita.facade.impl.StandardAPIAccessorImpl;
import org.ow2.bonita.facade.privilege.PrivilegePolicy;
import org.ow2.bonita.facade.privilege.Rule;
import org.ow2.bonita.facade.privilege.Rule.RuleType;
import org.ow2.bonita.facade.runtime.Category;
import org.ow2.bonita.facade.uuid.CategoryUUID;
import org.ow2.bonita.util.Command;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class WebGetVisibleCategoriesCommand implements Command<Set<Category>> {

  private static final long serialVersionUID = 7823810918005448800L;
  private final String userUUID;
  private final Collection<String> roleUUIDs;
  private final Collection<String> groupUUIDs;
  private final Collection<String> membershipUUIDs;
  private final String entityID;

  public WebGetVisibleCategoriesCommand(final String userUUID, final Collection<String> roleUUIDs,
      final Collection<String> groupUUIDs, final Collection<String> membershipUUIDs, final String entityID) {
    super();
    this.userUUID = userUUID;
    this.roleUUIDs = roleUUIDs;
    this.groupUUIDs = groupUUIDs;
    this.membershipUUIDs = membershipUUIDs;
    this.entityID = entityID;
  }

  @Override
  public Set<Category> execute(final Environment environment) throws Exception {
    final APIAccessor accessor = new StandardAPIAccessorImpl();
    final ManagementAPI managementAPI = accessor.getManagementAPI();
    final WebAPI webAPI = accessor.getWebAPI();

    final Set<CategoryUUID> exceptions = new HashSet<CategoryUUID>();
    final List<Rule> applicableRules = managementAPI.getApplicableRules(RuleType.CATEGORY_READ, userUUID, roleUUIDs,
        groupUUIDs, membershipUUIDs, entityID);
    for (final Rule rule : applicableRules) {
      for (final String theCategoryID : rule.getItems()) {
        exceptions.add(new CategoryUUID(theCategoryID));
      }
    }

    final PrivilegePolicy currentPolicy = managementAPI.getRuleTypePolicy(RuleType.CATEGORY_READ);
    switch (currentPolicy) {
      case ALLOW_BY_DEFAULT:
        // The exceptions are the categories the entity cannot see.
        if (exceptions != null && !exceptions.isEmpty()) {
          return webAPI.getAllCategoriesByUUIDExcept(exceptions);
        } else {
          return webAPI.getAllCategories();
        }

      case DENY_BY_DEFAULT:
        // The exceptions are the categories the entity can see.
        if (exceptions.size() > 0) {
          final Set<Category> tempResult = webAPI.getCategoriesByUUIDs(exceptions);
          if (tempResult == null || tempResult.isEmpty()) {
            return Collections.emptySet();
          } else {
            return tempResult;
          }
        } else {
          return Collections.emptySet();
        }
      default:
        throw new IllegalArgumentException();
    }
  }

}
