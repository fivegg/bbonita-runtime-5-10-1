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

import java.util.HashSet;
import java.util.Set;

import org.ow2.bonita.facade.uuid.CategoryUUID;

/**
 * @author Nicolas Chabanoles
 * 
 */
public class CategoryRuleImpl extends RuleImpl {


  private static final long serialVersionUID = -5847012186289202711L;

  protected CategoryRuleImpl() {
    super();
  }
  
  public CategoryRuleImpl(RuleType ruleType) {
    super(ruleType);
  }
  
  public CategoryRuleImpl(String name, String label, String description, RuleType ruleType, Set<CategoryUUID> categories) {
    super(name, label, description, ruleType, categories);
  }

  public CategoryRuleImpl(CategoryRuleImpl src) {
    super(src);
  }
  
  public void addCategories(Set<CategoryUUID> categories) {
    super.addExceptions(categories);
  }
  
  public void removeCategories(Set<CategoryUUID> categories) {
    super.removeExceptions(categories);
  }

  public void setCategories(Set<CategoryUUID> categories) {
    super.setExceptions(categories);
  }
  
  public Set<CategoryUUID> getCategories() {
    
    Set<CategoryUUID> categories = new HashSet<CategoryUUID>();
    for (String exceptionUUID : getItems()) {
      categories.add(new CategoryUUID(exceptionUUID));
    }
    return categories;
  }
}
