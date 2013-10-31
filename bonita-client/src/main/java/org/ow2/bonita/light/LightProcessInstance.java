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
package org.ow2.bonita.light;

import java.util.Date;

import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.RuntimeRecord;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

/**
 * 
 * @author Charles Souillard, Matthieu Chaffotte
 * 
 */
public interface LightProcessInstance extends RuntimeRecord {

  /**
   * Returns the UUID of the process instance.
   * 
   * @return The UUID of the process instance.
   */
  ProcessInstanceUUID getUUID();

  ProcessInstanceUUID getRootInstanceUUID();

  /**
   * If the instance is created to execute a subflow it returns the instance
   * processDefinitionUUID of the instance creating this instance of subflow,
   * otherwise it returns null.
   * 
   * @return the ProcessInstanceUUID of the parent instance (case of subflow)
   *         otherwise null.
   */
  ProcessInstanceUUID getParentInstanceUUID();

  /**
   * In case of a sub-process, it returns the activityInstanceUUID of the
   * activity which starts this sub-process. It returns null otherwise
   * 
   * @return the ActivityInstanceUUID of the parent activity (sub-process case);
   *         null otherwise.
   */
  ActivityInstanceUUID getParentActivityUUID();

  /**
   * Returns the date recorded when the instance has been created and started.
   * 
   * @return the date recorded when the instance has been created and started.
   */
  Date getStartedDate();

  /**
   * Returns the date recorded when the instance is finished.
   * 
   * @return the date recorded when the instance is finished.
   */
  Date getEndedDate();

  /**
   * Returns the user who created and started the process instance.
   * 
   * @return the user who created and started the process instance.
   */
  String getStartedBy();

  /**
   * Returns the user who finished the process instance.
   * 
   * @return the user who finished the process instance.
   */
  String getEndedBy();

  /**
   * Returns the current instance state.
   * 
   * @return the current instance state.
   */
  InstanceState getInstanceState();

  long getNb();

  /**
   * Gets the last date when the process has been updated.
   * 
   * @return the last date when the process has been updated
   */
  Date getLastUpdate();

  /**
   * Returns true if the instance has been archived.
   */
  boolean isArchived();

}
