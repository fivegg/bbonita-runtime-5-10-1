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
package org.ow2.bonita.util;

import java.util.Comparator;

import org.ow2.bonita.facade.paging.ProcessInstanceCriterion;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class InternalProcessInstanceComparator implements Comparator<InternalProcessInstance> {
  
  private ProcessInstanceCriterion pagingCriterion;
  
  public InternalProcessInstanceComparator(ProcessInstanceCriterion pagingCriterion){
    this.pagingCriterion = pagingCriterion;
  }
  
  public int compare(InternalProcessInstance o1, InternalProcessInstance o2) {
    switch (pagingCriterion) {
      case LAST_UPDATE_ASC: return o1.getLastUpdate().compareTo(o2.getLastUpdate());      
      case STARTED_DATE_ASC: return o1.getStartedDate().compareTo(o2.getStartedDate());
      
      case ENDED_DATE_ASC: 
        if (o1.getEndedDate() == null){
          if (o2.getEndedDate() == null){
            return 0;
          }
          return 1;
        }
        return o1.getEndedDate().compareTo(o2.getEndedDate());
        
      case INSTANCE_NUMBER_ASC: return ((Long)o1.getNb()).compareTo(o2.getNb());
      case INSTANCE_UUID_ASC: return o1.getUUID().getValue().compareTo(o2.getUUID().getValue());
      
      case LAST_UPDATE_DESC: return o2.getLastUpdate().compareTo(o1.getLastUpdate());        
      case STARTED_DATE_DESC: return o2.getStartedDate().compareTo(o1.getStartedDate());
        
      case ENDED_DATE_DESC:
        if (o1.getEndedDate() == null){
          if (o2.getEndedDate() == null){
            return 0;
          }
          return -1;
        }
        return o2.getEndedDate().compareTo(o1.getEndedDate());
        
      case INSTANCE_NUMBER_DESC: return ((Long)o2.getNb()).compareTo(o1.getNb());        
      case INSTANCE_UUID_DESC: return o2.getUUID().getValue().compareTo(o1.getUUID().getValue());        
      case DEFAULT: return o2.getLastUpdate().compareTo(o1.getLastUpdate());
      }
    
    return 0;
  }

}
