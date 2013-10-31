/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.ow2.bonita.runtime.event;

import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

/**
 * @author Matthieu Chaffotte
 * 
 */
public final class JobBuilder {

  public static Job boundaryTimerJob(final String eventName, final ProcessInstanceUUID rootProcessUUID,
      final String executionUUID, final long fireTime, final ProcessInstanceUUID instanceUUID) {
    return new Job(eventName, EventConstants.BOUNDARY, EventConstants.TIMER, rootProcessUUID.getValue(), executionUUID,
        null, null, fireTime, instanceUUID);
  }

  public static Job startTimerJob(final String eventName, final ActivityDefinitionUUID definitionUUID,
      final String timerExpression, final long fireTime) {
    return new Job(eventName, EventConstants.START, EventConstants.TIMER, definitionUUID.getProcessUUID().getValue(),
        null, definitionUUID, timerExpression, fireTime, null);
  }

  public static Job intermediateTimerJob(final String eventName, final ProcessInstanceUUID rootProcessUUID,
      final String executionEventUUID, final long fireTime, final ProcessInstanceUUID instanceUUID) {
    return new Job(eventName, EventConstants.INTERMEDIATE, EventConstants.TIMER, rootProcessUUID.getValue(),
        executionEventUUID, null, null, fireTime, instanceUUID);
  }

  public static Job deadlineJob(final String eventName, final ProcessInstanceUUID rootProcessUUID,
      final String executionEventUUID, final long fireTime, final ProcessInstanceUUID instanceUUID) {
    return new Job(eventName, EventConstants.DEADLINE, EventConstants.TIMER, rootProcessUUID.getValue(),
        executionEventUUID, null, null, fireTime, instanceUUID);
  }

  public static Job asyncJob(final String eventName, final ProcessInstanceUUID rootProcessUUID,
      final String executionEventUUID, final ProcessInstanceUUID instanceUUID) {
    return new Job(eventName, EventConstants.ASYNC, EventConstants.ASYNC, rootProcessUUID.getValue(),
        executionEventUUID, null, null, System.currentTimeMillis(), instanceUUID);
  }

  public static Job startSignalJob(final String eventName, final ActivityDefinitionUUID definitionUUID) {
    return new Job(eventName, EventConstants.START, EventConstants.SIGNAL, definitionUUID.getProcessUUID().getValue(),
        null, definitionUUID, null, System.currentTimeMillis(), null);
  }

  public static Job boundarySignalJob(final String eventName, final ProcessInstanceUUID rootProcessUUID,
      final String executionUUID, final ProcessInstanceUUID instanceUUID) {
    return new Job(eventName, EventConstants.BOUNDARY, EventConstants.SIGNAL, rootProcessUUID.getValue(),
        executionUUID, null, null, System.currentTimeMillis(), instanceUUID);
  }

  public static Job intermediateSignalJob(final String eventName, final ProcessInstanceUUID rootProcessUUID,
      final String executionEventUUID, final ProcessInstanceUUID instanceUUID) {
    return new Job(eventName, EventConstants.INTERMEDIATE, EventConstants.SIGNAL, rootProcessUUID.getValue(),
        executionEventUUID, null, null, System.currentTimeMillis(), instanceUUID);
  }

  public static Job startErrorJob(final String eventName, final ActivityDefinitionUUID definitionUUID) {
    return new Job(eventName, EventConstants.START, EventConstants.ERROR, definitionUUID.getProcessUUID().getValue(),
        null, definitionUUID, null, System.currentTimeMillis(), null);
  }

  public static Job boundaryErrorJob(final String eventName, final ProcessInstanceUUID rootProcessUUID,
      final String executionUUID, final ProcessInstanceUUID instanceUUID) {
    return new Job(eventName, EventConstants.BOUNDARY, EventConstants.ERROR, rootProcessUUID.getValue(), executionUUID,
        null, null, System.currentTimeMillis(), instanceUUID);
  }

  public static Job connectorsAutomaticOnEnterJob(final String eventName, final ProcessInstanceUUID rootProcessUUID,
      final String executionUUID, final ProcessInstanceUUID instanceUUID) {
    return new Job(eventName, EventConstants.EXECUTE_CONNECTORS_AUTOMATIC_ON_ENTER,
        EventConstants.EXECUTE_CONNECTORS_AUTOMATIC_ON_ENTER, rootProcessUUID.getValue(), executionUUID, null, null,
        System.currentTimeMillis(), instanceUUID);
  }

  public static Job signalConnectorsAutoOnEnterExecutedJob(final String eventName,
      final ProcessInstanceUUID rootProcessUUID, final String executionUUID, final ProcessInstanceUUID instanceUUID) {
    return new Job(eventName, EventConstants.CONNECTORS_AUTOMATIC_ON_ENTER_EXECUTED,
        EventConstants.CONNECTORS_AUTOMATIC_ON_ENTER_EXECUTED, rootProcessUUID.getValue(), executionUUID, null, null,
        System.currentTimeMillis(), instanceUUID);
  }

}
