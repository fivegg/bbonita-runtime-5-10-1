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
package org.ow2.bonita.facade.privilege;

import java.util.Set;

import org.ow2.bonita.facade.def.majorElement.NamedElement;

/**
 * @author Nicolas Chabanoles
 * @author Rodrigue Le Gall
 */
public interface Rule extends NamedElement, Comparable<Rule> {

  public static enum RuleType {
    /* Processes / instances */
    /* Simple Users */
    PROCESS_START, PROCESS_READ, PROCESS_INSTANTIATION_DETAILS_VIEW, PROCESS_MANAGE, PROCESS_ADD_COMMENT, PROCESS_PDF_EXPORT,
    /* Super Users*/
    PROCESS_INSTALL,
    
    /* Activities */
    /* Simple Users */
    ACTIVITY_READ, ACTIVITY_DETAILS_READ, ASSIGN_TO_ME_STEP, ASSIGN_TO_STEP, UNASSIGN_STEP, CHANGE_PRIORITY_STEP, SUSPEND_STEP, RESUME_STEP, 
    /* Super Users*/
    SKIP_STEP,
    
    /* Categories */
    /* Simple Users */
    CATEGORY_READ, 
    /* Super Users*/
    
    /* Reports */
    /* Simple Users */
    REPORT_VIEW,
    /* Super Users*/
    REPORT_INSTALL, REPORT_MANAGE,
    
    /* User XP */
    /* Simple Users & Super Users*/
    LOGOUT,
    
    /* User profile */
    /* Simple Users */
    DELEGEE_UPDATE, PASSWORD_UPDATE,
    
    /* Custom */
    CUSTOM
    ;
  }

  @Deprecated
  long getId();

  String getUUID();
  
  Set<String> getItems();
  
  Set<String> getEntities();
  
  Set<String> getUsers();
  
  Set<String> getRoles();
  
  Set<String> getGroups();
  
  Set<String> getMemberships();

  RuleType getType();
}
