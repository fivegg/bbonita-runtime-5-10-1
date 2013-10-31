/**
 * Copyright (C) 2009-2013 BonitaSoft S.A.
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
package org.ow2.bonita.persistence;

import java.util.List;
import java.util.Set;

import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.runtime.event.EventCouple;
import org.ow2.bonita.runtime.event.IncomingEventInstance;
import org.ow2.bonita.runtime.event.Job;
import org.ow2.bonita.runtime.event.JobLock;
import org.ow2.bonita.runtime.event.OutgoingEventInstance;

/**
 * @author Charles Souillard, Matthieu Chaffotte
 */
public interface EventDbSession extends DbSession {

  Set<IncomingEventInstance> getIncomingEvents(ProcessInstanceUUID instanceUUID);

  Set<IncomingEventInstance> getIncomingEvents(ActivityDefinitionUUID activityUUID);

  Set<IncomingEventInstance> getIncomingEvents(ActivityInstanceUUID activityUUID);

  Set<IncomingEventInstance> getIncomingEvents();

  Set<IncomingEventInstance> getIncomingEvents(String eventName, String toProcessName, String toActivityName,
      ActivityInstanceUUID activityUUID);

  IncomingEventInstance getIncomingEvent(long incomingId);

  IncomingEventInstance getIncomingEvent(ProcessInstanceUUID instanceUUID, String name);

  Set<IncomingEventInstance> getBoundaryIncomingEvents(ActivityInstanceUUID activityUUID);

  Set<OutgoingEventInstance> getOutgoingEvents(ProcessInstanceUUID instanceUUID);

  Set<OutgoingEventInstance> getOutgoingEvents();

  Set<OutgoingEventInstance> getOutgoingEvents(String eventName, String toProcessName, String toActivityName,
      ActivityInstanceUUID activityUUID);

  OutgoingEventInstance getOutgoingEvent(long outgoingId);

  Set<OutgoingEventInstance> getBoundaryOutgoingEvents(ActivityInstanceUUID activityUUID);

  Set<OutgoingEventInstance> getOverdueEvents();

  Set<IncomingEventInstance> getActivityIncomingEvents(ActivityInstanceUUID activityUUID);

  Set<IncomingEventInstance> getSignalIncomingEvents(String signal);

  IncomingEventInstance getSignalStartIncomingEvent(List<String> processName, String signalCode);

  List<IncomingEventInstance> getMessageStartIncomingEvents(Set<String> processNames);

  void deleteIncompatibleEvents(OutgoingEventInstance outgoing);

  Job getJob(Long jobId);

  Job getJob(ActivityDefinitionUUID activityUUID);

  List<Job> getExecutableJobs(String processUUID);
  
  List<String> getNonLockedProcessesHavingJobs(Set<String> lockedProcessUUIDs, int maxResult);
  
  Long getNextJobDueDate(final Set<String> processUUIDsToExclude);

  List<EventCouple> getMessageEventCouples();

  List<Job> getJobs();

  List<Job> getJobsOfExecution(String executionEventUUID);

  List<Job> getJobsOfInstance(ProcessInstanceUUID instanceUUID);

  List<Job> getJobs(String eventType);

  List<Job> getJobs(String eventType, ProcessInstanceUUID instanceUUID);

  List<Job> getJobs(String eventType, String eventUUID);
  
  List<Job> getJobsWithoutProcessUUID(int fromIndex, int maxResults);

  List<EventCouple> getCorrelationKeyMessageEventCouples(int maxCouples);

  Long getNextEventDueDate();

  Long getNextExpressionEventDueDate();

  JobLock getJobLock(String processUUID);

  List<IncomingEventInstance> getMessageEventSubProcessIncomingEvents(ProcessInstanceUUID eventSubProcessRootInstanceUUID, long outgoingId);

}
