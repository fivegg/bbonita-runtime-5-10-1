/**
 * Copyright (C) 2007  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
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
package org.ow2.bonita.facade.runtime;

import java.io.Serializable;

import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

/**
 * Interface for getting recorded (runtime) information common to {@link ProcessInstance} and {@link ActivityInstance}.
 * @author Pierre Vigneras
 */
public interface RuntimeRecord extends Serializable {


  /**
   * Returns the process processDefinitionUUID (defined into the WorkflowProcess element
   * of the imported xpdl file).
   * @return The process processDefinitionUUID.
   */
  ProcessDefinitionUUID getProcessDefinitionUUID();

  /**
   * Returns the instance processDefinitionUUID (generated at the creation of the instance).
   * @return The instance processDefinitionUUID.
   */
  ProcessInstanceUUID getProcessInstanceUUID();
  
  ProcessInstanceUUID getRootInstanceUUID();
}
