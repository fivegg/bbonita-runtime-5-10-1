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

import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.Type;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.RuntimeRecord;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

public interface LightActivityInstance extends RuntimeRecord {

  /**
   * Returns the UUID of the activity instance.
   * @return The UUID of the activity instance.
   */
  ActivityInstanceUUID getUUID();

  /**
   * Returns the value of the id attribute of the Activity element defined into the XPDL file.
   */
  String getActivityName();

  /**
   * Returns the iteration id of this activity
   */
  String getIterationId();

  /**
   * Returns the activity instance id of this activity (in case of multi instantiation)
   */
  String getActivityInstanceId();

  String getLoopId();

  /**
   * 
   * @return true if this activity is a task
   */
  boolean isTask();
  boolean isTimer();
  boolean isAutomatic();
  boolean isSubflow();

  /**
   * return null if this activity is not a task. Return the corresponding task instance if it is a task.
   */
  LightTaskInstance getTask();

  /**
   * Returns the date recorded after the activity is started.
   * @return The date recorded after the activity is started.
   */
  Date getStartedDate();

  /**
   * Returns the date recorded after the activity is finished.
   * @return The date recorded after the activity is finished.
   */
  Date getEndedDate();

  /**
   * Returns the date recorded when the activity.
   * @return The date recorded when the activity becomes READY.
   */
  Date getReadyDate();

  /**
   * Returns the current state.
   * @return The current state.
   */
  ActivityState getState();

  String getActivityLabel();
  String getActivityDescription();
  Date getLastUpdateDate();
  Date getExpectedEndDate();

  int getPriority();

  ActivityDefinitionUUID getActivityDefinitionUUID();

  Type getType();

  ProcessInstanceUUID getSubflowProcessInstanceUUID();

  /**
	 * Returns the evaluated dynamic label based on corresponding ActivityDefinition dynamic label.
	 */
	String getDynamicLabel();
	
	/**
	 * Returns the evaluated dynamic description based on corresponding ActivityDefinition dynamic description.
	 */
	String getDynamicDescription();
	
	/**
	 * Returns the evaluated dynamic execution summary. This value is only relevant when the activity is in state {@link org.ow2.bonita.facade.runtime.ActivityState.FINISHED}
	 */
	String getDynamicExecutionSummary();
}
