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
package org.ow2.bonita.services.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.persistence.EventDbSession;
import org.ow2.bonita.runtime.event.EventAdded;
import org.ow2.bonita.runtime.event.EventConstants;
import org.ow2.bonita.runtime.event.EventCouple;
import org.ow2.bonita.runtime.event.EventInstance;
import org.ow2.bonita.runtime.event.IncomingEventInstance;
import org.ow2.bonita.runtime.event.Job;
import org.ow2.bonita.runtime.event.JobLock;
import org.ow2.bonita.runtime.event.OutgoingEventInstance;
import org.ow2.bonita.runtime.model.Execution;
import org.ow2.bonita.services.EventService;
import org.ow2.bonita.util.EnvTool;

/**
 * 
 * @author Charles Souillard, Matthieu Chaffotte, Elias Ricken de Medeiros
 * 
 */
public class DbThreadEventService implements EventService {

  private static final Logger LOG = Logger.getLogger(DbThreadEventService.class.getName());

  private String persistenceServiceName;

  protected DbThreadEventService() {
  }

  public DbThreadEventService(final String persistenceServiceName) {
    super();
    this.persistenceServiceName = persistenceServiceName;
  }

  protected EventDbSession getDbSession() {
    return EnvTool.getEventServiceDbSession(persistenceServiceName);
  }

  private void refreshMatcher() {
    EnvTool.getEventExecutor().refreshEventMatcher();
  }

  @Override
  @SuppressWarnings("deprecation")
  public void enableEventsInFailureIncomingEvents(final ActivityInstanceUUID activityUUID) {
    final Execution execution = EnvTool.getJournal().getExecutionOnActivity(activityUUID.getProcessInstanceUUID(),
        activityUUID);
    final List<Job> jobs = getDbSession().getJobsOfExecution(execution.getEventUUID());
    final int retries = EnvTool.getEventExecutor().getRetries();
    for (final Job job : jobs) {
      if (job.getRetries() == 0) {
        job.setRetries(retries);
      }
    }
    refreshJobExecutor();
  }

  @Override
  public void enablePermanentEventsInFailure(final ActivityDefinitionUUID activityUUID) {
    final Job job = getDbSession().getJob(activityUUID);
    final int retries = EnvTool.getEventExecutor().getRetries();
    if (job.getRetries() == 0) {
      job.setRetries(retries);
    }
    refreshJobExecutor();
  }

  @Override
  public void fire(final OutgoingEventInstance outgoingEventInstance) {
    getDbSession().save(outgoingEventInstance);
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Firing event outgoing event: " + outgoingEventInstance + "...");
    }
    refreshMatcher();
  }

  @Override
  public void subscribe(final IncomingEventInstance incomingEventInstance) {
    final long originalEnableTime = incomingEventInstance.getEnableTime();
    incomingEventInstance.setEnableTime(Long.MAX_VALUE);
    getDbSession().save(incomingEventInstance);
    EnvTool.getTransaction().registerSynchronization(
        new EventAdded(getDbSession(), incomingEventInstance.getId(), originalEnableTime));
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Firing event incoming event: " + incomingEventInstance + "...");
    }
    refreshMatcher();
  }

  @Override
  public Set<IncomingEventInstance> getIncomingEvents() {
    final Set<IncomingEventInstance> incomings = getDbSession().getIncomingEvents();
    if (incomings == null) {
      return Collections.emptySet();
    }
    return incomings;
  }

  @Override
  public Set<OutgoingEventInstance> getOutgoingEvents() {
    final Set<OutgoingEventInstance> outgoings = getDbSession().getOutgoingEvents();
    if (outgoings == null) {
      return Collections.emptySet();
    }
    return outgoings;
  }

  @Override
  public Set<OutgoingEventInstance> getOutgoingEvents(final String eventName, final String toProcessName,
      final String toActivityName, final ActivityInstanceUUID activityUUID) {
    final Set<OutgoingEventInstance> outgoings = getDbSession().getOutgoingEvents(eventName, toProcessName,
        toActivityName, activityUUID);
    if (outgoings == null) {
      return Collections.emptySet();
    }
    return outgoings;
  }

  @Override
  public Set<IncomingEventInstance> getIncomingEvents(final String eventName, final String toProcessName,
      final String toActivityName, final ActivityInstanceUUID activityUUID) {
    final Set<IncomingEventInstance> incomings = getDbSession().getIncomingEvents(eventName, toProcessName,
        toActivityName, activityUUID);
    if (incomings == null) {
      return Collections.emptySet();
    }
    return incomings;
  }

  @Override
  public Set<OutgoingEventInstance> getOutgoingEvents(final ProcessInstanceUUID instanceUUID) {
    final Set<OutgoingEventInstance> outgoings = getDbSession().getOutgoingEvents(instanceUUID);
    if (outgoings == null) {
      return Collections.emptySet();
    }
    return outgoings;
  }

  @Override
  public Set<IncomingEventInstance> getIncomingEvents(final ProcessInstanceUUID instanceUUID) {
    final Set<IncomingEventInstance> incomings = getDbSession().getIncomingEvents(instanceUUID);
    if (incomings == null) {
      return Collections.emptySet();
    }
    return incomings;
  }

  public Set<IncomingEventInstance> getIncomingEvents(final ActivityDefinitionUUID activityUUID) {
    final Set<IncomingEventInstance> incomings = getDbSession().getIncomingEvents(activityUUID);
    if (incomings == null) {
      return Collections.emptySet();
    }
    return incomings;
  }

  @Override
  public Set<IncomingEventInstance> getIncomingEvents(final ActivityInstanceUUID activityUUID) {
    final Set<IncomingEventInstance> incomings = getDbSession().getIncomingEvents(activityUUID);
    if (incomings == null) {
      return Collections.emptySet();
    }
    return incomings;
  }

  @Override
  public Set<IncomingEventInstance> getBoundaryIncomingEvents(final ActivityInstanceUUID activityUUID) {
    final Set<IncomingEventInstance> incomings = getDbSession().getBoundaryIncomingEvents(activityUUID);
    if (incomings == null) {
      return Collections.emptySet();
    }
    return incomings;
  }

  @Override
  public Set<OutgoingEventInstance> getBoundaryOutgoingEvents(final ActivityInstanceUUID activityUUID) {
    final Set<OutgoingEventInstance> outgoings = getDbSession().getBoundaryOutgoingEvents(activityUUID);
    if (outgoings == null) {
      return Collections.emptySet();
    }
    return outgoings;
  }

  @Override
  public IncomingEventInstance getIncomingEvent(final long incomingId) {
    return getDbSession().getIncomingEvent(incomingId);
  }

  @Override
  public OutgoingEventInstance getOutgoingEvent(final long outgoingId) {
    return getDbSession().getOutgoingEvent(outgoingId);
  }

  @Override
  public IncomingEventInstance getIncomingEvent(final ProcessInstanceUUID instanceUUID, final String name) {
    return getDbSession().getIncomingEvent(instanceUUID, name);
  }

  public void removeFiredEvent(final OutgoingEventInstance outgoing) {
    getDbSession().deleteIncompatibleEvents(outgoing);
    getDbSession().delete(outgoing);
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Removing outgoing event: " + outgoing + "...");
    }
  }

  @Override
  public Set<OutgoingEventInstance> getOverdueEvents() {
    return getDbSession().getOverdueEvents();
  }

  @Override
  public Set<IncomingEventInstance> getSignalIncomingEvents(final String signal) {
    return getDbSession().getSignalIncomingEvents(signal);
  }

  public void removeSubscription(final IncomingEventInstance incoming) {
    getDbSession().delete(incoming);
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Removing incoming event: " + incoming + "...");
    }
  }

  @Override
  public void removeSubscriptions(final ProcessInstanceUUID instanceUUID) {
    final Set<IncomingEventInstance> events = getIncomingEvents(instanceUUID);
    if (events != null && !events.isEmpty()) {
      for (final IncomingEventInstance event : events) {
        removeSubscription(event);
      }
    }
  }

  @Override
  public void removeSubscriptions(final ActivityDefinitionUUID activityUUID) {
    final Set<IncomingEventInstance> events = getIncomingEvents(activityUUID);
    if (events != null && !events.isEmpty()) {
      for (final IncomingEventInstance event : events) {
        removeSubscription(event);
      }
    }
  }

  @Override
  public void removeFiredEvents(final ProcessInstanceUUID instanceUUID) {
    final Set<OutgoingEventInstance> events = getOutgoingEvents(instanceUUID);
    if (events != null && !events.isEmpty()) {
      for (final OutgoingEventInstance event : events) {
        removeFiredEvent(event);
      }
    }
  }

  @Override
  public void removeEvent(final EventInstance event) {
    if (event instanceof OutgoingEventInstance) {
      removeFiredEvent((OutgoingEventInstance) event);
    } else {
      removeSubscription((IncomingEventInstance) event);
    }
  }

  @Override
  public IncomingEventInstance getSignalStartIncomingEvent(final List<String> processNames, final String signalCode) {
    return getDbSession().getSignalStartIncomingEvent(processNames, signalCode);
  }

  @Override
  public List<IncomingEventInstance> getMessageStartIncomingEvents(final Set<String> processNames) {
    return getDbSession().getMessageStartIncomingEvents(processNames);
  }

  @Override
  public void storeJob(final Job job) {
    final int retries = EnvTool.getEventExecutor().getRetries();
    job.setRetries(retries);
    getDbSession().save(job);
    if (LOG.isLoggable(Level.INFO)) {
      LOG.info("New job: " + job + "...");
    }
    refreshJobExecutor();
  }

  private void refreshJobExecutor() {
    EnvTool.getEventExecutor().refreshJobExecutor();
  }

  @Override
  public Job getJob(final Long jobId) {
    return getDbSession().getJob(jobId);
  }

  @Override
  public void removeJob(final Job job) {
    getDbSession().delete(job);
  }

  @Override
  public void removeJob(final ActivityDefinitionUUID activityUUID) {
    final Job job = getDbSession().getJob(activityUUID);
    if (job != null) {
      getDbSession().delete(job);
    }
  }

  @Override
  public List<String> getNonLockedProcessesHavingJobs(final Set<String> lockedProcessUUIDs, final int maxResult) {
    return getDbSession().getNonLockedProcessesHavingJobs(lockedProcessUUIDs, maxResult);
  }
  
  @Override
  public List<Job> getExecutableJobs(final String processUUI) {
    return getDbSession().getExecutableJobs(processUUI);
  }

  @Override
  public Long getNextJobDueDate(final Set<String> processUUIDsToExclude) {
    return getDbSession().getNextJobDueDate(processUUIDsToExclude);
  }

  @Override
  public List<EventCouple> getMessageEventCouples() {
    return getDbSession().getMessageEventCouples();
  }

  @Override
  public List<Job> getJobs() {
    return getDbSession().getJobs();
  }

  @Override
  public void removeJobs(final String executionEventUUID) {
    final List<Job> jobs = getDbSession().getJobsOfExecution(executionEventUUID);
    for (final Job job : jobs) {
      getDbSession().delete(job);
    }
  }

  @Override
  public void removeJobs(final ProcessInstanceUUID instanceUUID) {
    final List<Job> jobs = getDbSession().getJobsOfInstance(instanceUUID);
    for (final Job job : jobs) {
      getDbSession().delete(job);
    }
  }

  @Override
  public List<Job> getTimerJobs() {
    return getDbSession().getJobs(EventConstants.TIMER);
  }

  @Override
  public List<Job> getTimerJobs(final ProcessInstanceUUID instanceUUID) {
    return getDbSession().getJobs(EventConstants.TIMER, instanceUUID);
  }

  @Override
  public List<Job> getTimerJobs(final String eventUUID) {
    return getDbSession().getJobs(EventConstants.TIMER, eventUUID);
  }
  
  @Override
  public List<Job> getJobsWithoutProcessUUID(final int fromIndex, final int maxResults) {
    return getDbSession().getJobsWithoutProcessUUID(fromIndex, maxResults);
  }

  @Override
  public List<EventCouple> getCorrelationKeyMessageEventCouples(final int maxCouples) {
    return getDbSession().getCorrelationKeyMessageEventCouples(maxCouples);
  }

  @Override
  public Long getNextEventDueDate() {
    return getDbSession().getNextEventDueDate();
  }

  @Override
  public Long getNextExpressionEventDueDate() {
    return getDbSession().getNextExpressionEventDueDate();
  }

  @Override
  public void lockRootInstance(final ProcessInstanceUUID rootInstanceUUID) {
    final JobLock lock = new JobLock(rootInstanceUUID);
    getDbSession().save(lock);
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Save lock on " + rootInstanceUUID);
    }
  }

  @Override
  public void lockProcessDefinition(final ProcessDefinitionUUID definitionUUID) {
    final JobLock lock = new JobLock(definitionUUID);
    getDbSession().save(lock);
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Save lock on " + definitionUUID);
    }
  }

  @Override
  public void removeLock(final ProcessDefinitionUUID definitionUUID) {
    final JobLock lock = getDbSession().getJobLock(definitionUUID.getValue());
    if (lock != null) {
      getDbSession().delete(lock);
    }
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Delete lock on " + definitionUUID);
    }
  }

  @Override
  public void removeLock(final ProcessInstanceUUID rootInstanceUUID) {
    final JobLock lock = getDbSession().getJobLock(rootInstanceUUID.getValue());
    if (lock != null) {
      getDbSession().delete(lock);
    }
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Delete lock on " + rootInstanceUUID);
    }
  }

  @Override
  public List<IncomingEventInstance> getMessageEventSubProcessIncomingEvents(final ProcessInstanceUUID eventSubProcessRootInstanceUUID, final long outgoingId) {
    return getDbSession().getMessageEventSubProcessIncomingEvents(eventSubProcessRootInstanceUUID, outgoingId);
  }

}
