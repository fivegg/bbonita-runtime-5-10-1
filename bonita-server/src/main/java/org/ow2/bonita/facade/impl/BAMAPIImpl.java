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
package org.ow2.bonita.facade.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.ow2.bonita.facade.BAMAPI;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.Type;
import org.ow2.bonita.facade.exception.MonitoringException;
import org.ow2.bonita.facade.monitoring.model.JvmMBean;
import org.ow2.bonita.facade.monitoring.model.impl.MBeanUtil;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.DateUtil;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.Misc;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class BAMAPIImpl implements BAMAPI {

  private final String queryList;

  protected BAMAPIImpl(final String queryList) {
    this.queryList = queryList;
  }

  private String getQueryList() {
    return queryList;
  }

  @Override
  public List<Integer> getNumberOfExecutingCasesPerDay(final Date since) {
    final Date now = new Date();
    return EnvTool.getAllQueriers(getQueryList()).getNumberOfExecutingCasesPerDay(since, now);
  }

  @Override
  public List<Integer> getNumberOfFinishedCasesPerDay(final Date since) {
    final Date now = new Date();
    return EnvTool.getAllQueriers(getQueryList()).getNumberOfFinishedCasesPerDay(since, now);
  }

  @Override
  public int getNumberOfOpenSteps() {
    return EnvTool.getJournalQueriers(getQueryList()).getNumberOfOpenSteps();
  }

  @Override
  public List<Integer> getNumberOfOpenStepsPerDay(final Date since) {
    final Date now = new Date();
    return EnvTool.getAllQueriers(getQueryList()).getNumberOfOpenStepsPerDay(since, now);
  }

  @Override
  public int getNumberOfOverdueSteps() {
    final Date now = new Date();
    return EnvTool.getJournalQueriers(getQueryList()).getNumberOfOverdueSteps(now);
  }

  @Override
  public int getNumberOfStepsAtRisk(final int remainingDays) {
    if (remainingDays < 0) {
      throw new IllegalArgumentException("The number of remaining days is negative");
    }
    final Date currentDate = new Date();
    final Date beginningOfTheDay = DateUtil.getBeginningOfTheDay(currentDate);
    final Date atRisk = DateUtil.backTo(beginningOfTheDay, -(remainingDays + 1));
    return EnvTool.getJournalQueriers(getQueryList()).getNumberOfStepsAtRisk(currentDate, atRisk);
  }

  @Override
  public int getNumberOfUserOpenSteps() {
    final String userId = EnvTool.getUserId();
    return EnvTool.getJournalQueriers(getQueryList()).getNumberOfUserOpenSteps(userId);
  }

  @Override
  public int getNumberOfUserOverdueSteps() {
    final Date now = new Date();
    final String userId = EnvTool.getUserId();
    return EnvTool.getJournalQueriers(getQueryList()).getNumberOfUserOverdueSteps(userId, now);
  }

  @Override
  public int getNumberOfUserStepsAtRisk(final int remainingDays) {
    if (remainingDays < 0) {
      throw new IllegalArgumentException("The number of remaining days is negative");
    }
    final String userId = EnvTool.getUserId();
    final Date currentDate = new Date();
    final Date beginningOfTheDay = DateUtil.getBeginningOfTheDay(currentDate);
    final Date atRisk = DateUtil.backTo(beginningOfTheDay, -(remainingDays + 1));
    return EnvTool.getJournalQueriers(getQueryList()).getNumberOfUserStepsAtRisk(userId, currentDate, atRisk);
  }

  @Override
  public int getNumberOfFinishedSteps(final int priority, final Date since) {
    return EnvTool.getAllQueriers(getQueryList()).getNumberOfFinishedSteps(priority, since);
  }

  @Override
  public int getNumberOfOpenSteps(final int priority) {
    return EnvTool.getJournalQueriers(getQueryList()).getNumberOfOpenSteps(priority);
  }

  @Override
  public int getNumberOfUserFinishedSteps(final int priority, final Date since) {
    final String userId = EnvTool.getUserId();
    return EnvTool.getAllQueriers(getQueryList()).getNumberOfUserFinishedSteps(userId, priority, since);
  }

  @Override
  public int getNumberOfUserOpenSteps(final int priority) {
    final String userId = EnvTool.getUserId();
    return EnvTool.getJournalQueriers(getQueryList()).getNumberOfUserOpenSteps(userId, priority);
  }

  @Override
  public List<Long> getProcessInstancesDuration(final Date since, final Date until) {
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }

    return EnvTool.getHistoryQueriers(getQueryList()).getProcessInstancesDuration(since, until);
  }

  @Override
  public List<Long> getProcessInstancesDuration(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until) {
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }

    return EnvTool.getHistoryQueriers(getQueryList()).getProcessInstancesDuration(processUUID, since, until);
  }

  @Override
  public List<Long> getProcessInstancesDuration(final Set<ProcessDefinitionUUID> processUUIDs, final Date since,
      final Date until) {
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }
    if (processUUIDs == null || processUUIDs.isEmpty()) {
      return Collections.emptyList();
    }

    return EnvTool.getHistoryQueriers(getQueryList()).getProcessInstancesDurationFromProcessUUIDs(processUUIDs, since,
        until);
  }

  @Override
  public List<Long> getActivityInstancesExecutionTime(final Date since, final Date until) {
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }

    return EnvTool.getAllQueriers(getQueryList()).getActivityInstancesExecutionTime(since, until);
  }

  @Override
  public List<Long> getActivityInstancesExecutionTime(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until) {
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }

    return EnvTool.getAllQueriers(getQueryList()).getActivityInstancesExecutionTime(processUUID, since, until);
  }

  @Override
  public List<Long> getActivityInstancesExecutionTimeFromProcessUUIDs(final Set<ProcessDefinitionUUID> processUUIDs,
      final Date since, final Date until) {
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }
    if (processUUIDs == null || processUUIDs.isEmpty()) {
      return Collections.emptyList();
    }

    return EnvTool.getAllQueriers(getQueryList()).getActivityInstancesExecutionTimeFromProcessUUIDs(processUUIDs,
        since, until);
  }

  @Override
  public List<Long> getActivityInstancesExecutionTime(final ActivityDefinitionUUID activityUUID, final Date since,
      final Date until) {
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }

    return EnvTool.getAllQueriers(getQueryList()).getActivityInstancesExecutionTime(activityUUID, since, until);
  }

  @Override
  public List<Long> getActivityInstancesExecutionTimeFromActivityUUIDs(final Set<ActivityDefinitionUUID> activityUUIDs,
      final Date since, final Date until) {
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }
    if (activityUUIDs == null || activityUUIDs.isEmpty()) {
      return Collections.emptyList();
    }

    return EnvTool.getAllQueriers(getQueryList()).getActivityInstancesExecutionTimeFromActivityUUIDs(activityUUIDs,
        since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTime(final Date since, final Date until) {
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }

    return EnvTool.getAllQueriers(getQueryList()).getTaskInstancesWaitingTime(since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTime(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until) {
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }

    return EnvTool.getAllQueriers(getQueryList()).getTaskInstancesWaitingTime(processUUID, since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeFromProcessUUIDs(final Set<ProcessDefinitionUUID> processUUIDs,
      final Date since, final Date until) {
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }
    if (processUUIDs == null || processUUIDs.isEmpty()) {
      return Collections.emptyList();
    }

    return EnvTool.getAllQueriers(getQueryList()).getTaskInstancesWaitingTimeFromProcessUUIDs(processUUIDs, since,
        until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTime(final ActivityDefinitionUUID taskUUID, final Date since,
      final Date until) {
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }

    return EnvTool.getAllQueriers(getQueryList()).getTaskInstancesWaitingTime(taskUUID, since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeFromTaskUUIDs(final Set<ActivityDefinitionUUID> tasksUUIDs,
      final Date since, final Date until) {
    Misc.checkArgsNotNull(tasksUUIDs);
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }
    if (tasksUUIDs.isEmpty()) {
      return Collections.emptyList();
    }

    return EnvTool.getAllQueriers(getQueryList()).getTaskInstancesWaitingTimeFromTaskUUIDs(tasksUUIDs, since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeOfUser(final String username, final Date since, final Date until) {
    Misc.checkArgsNotNull(username);
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }

    return EnvTool.getAllQueriers(getQueryList()).getTaskInstancesWaitingTimeOfUser(username, since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeOfUser(final String username, final ProcessDefinitionUUID processUUID,
      final Date since, final Date until) {
    Misc.checkArgsNotNull(username, processUUID);
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }

    return EnvTool.getAllQueriers(getQueryList())
        .getTaskInstancesWaitingTimeOfUser(username, processUUID, since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeOfUserFromProcessUUIDs(final String username,
      final Set<ProcessDefinitionUUID> processUUIDs, final Date since, final Date until) {
    Misc.checkArgsNotNull(username, processUUIDs);
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }
    if (processUUIDs.isEmpty()) {
      return Collections.emptyList();
    }

    return EnvTool.getAllQueriers(getQueryList()).getTaskInstancesWaitingTimeOfUserFromProcessUUIDs(username,
        processUUIDs, since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeOfUser(final String username, final ActivityDefinitionUUID taskUUID,
      final Date since, final Date until) {
    Misc.checkArgsNotNull(username, taskUUID);
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }

    return EnvTool.getAllQueriers(getQueryList()).getTaskInstancesWaitingTimeOfUser(username, taskUUID, since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeOfUserFromTaskUUIDs(final String username,
      final Set<ActivityDefinitionUUID> tasksUUIDs, final Date since, final Date until) {
    Misc.checkArgsNotNull(username, tasksUUIDs);
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }
    if (tasksUUIDs.isEmpty()) {
      return Collections.emptyList();
    }

    return EnvTool.getAllQueriers(getQueryList()).getTaskInstancesWaitingTimeOfUserFromTaskUUIDs(username, tasksUUIDs,
        since, until);
  }

  @Override
  public List<Long> getActivityInstancesDuration(final Date since, final Date until) {
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }
    return EnvTool.getAllQueriers(getQueryList()).getActivityInstancesDuration(since, until);
  }

  @Override
  public List<Long> getActivityInstancesDuration(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until) {
    Misc.checkArgsNotNull(processUUID);
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }

    return EnvTool.getAllQueriers(getQueryList()).getActivityInstancesDuration(processUUID, since, until);
  }

  @Override
  public List<Long> getActivityInstancesDurationFromProcessUUIDs(final Set<ProcessDefinitionUUID> processUUIDs,
      final Date since, final Date until) {
    Misc.checkArgsNotNull(processUUIDs);
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }
    if (processUUIDs.isEmpty()) {
      return Collections.emptyList();
    }

    return EnvTool.getAllQueriers(getQueryList()).getActivityInstancesDurationFromProcessUUIDs(processUUIDs, since,
        until);
  }

  @Override
  public List<Long> getActivityInstancesDuration(final ActivityDefinitionUUID activityUUID, final Date since,
      final Date until) {
    Misc.checkArgsNotNull(activityUUID);
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }

    return EnvTool.getAllQueriers(getQueryList()).getActivityInstancesDuration(activityUUID, since, until);
  }

  @Override
  public List<Long> getActivityInstancesDurationFromActivityUUIDs(final Set<ActivityDefinitionUUID> activityUUIDs,
      final Date since, final Date until) {
    Misc.checkArgsNotNull(activityUUIDs);
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }
    if (activityUUIDs.isEmpty()) {
      return Collections.emptyList();
    }

    return EnvTool.getAllQueriers(getQueryList()).getActivityInstancesDurationFromActivityUUIDs(activityUUIDs, since,
        until);
  }

  @Override
  public List<Long> getActivityInstancesDurationByActivityType(final Type activityType, final Date since,
      final Date until) {
    Misc.checkArgsNotNull(activityType);
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }

    return EnvTool.getAllQueriers(getQueryList())
        .getActivityInstancesDurationByActivityType(activityType, since, until);
  }

  @Override
  public List<Long> getActivityInstancesDurationByActivityType(final Type activityType,
      final ProcessDefinitionUUID processUUID, final Date since, final Date until) {
    Misc.checkArgsNotNull(activityType, processUUID);
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }

    return EnvTool.getAllQueriers(getQueryList()).getActivityInstancesDurationByActivityType(activityType, processUUID,
        since, until);
  }

  @Override
  public List<Long> getActivityInstancesDurationByActivityTypeFromProcessUUIDs(final Type activityType,
      final Set<ProcessDefinitionUUID> processUUIDs, final Date since, final Date until) {
    if (processUUIDs == null || processUUIDs.isEmpty()) {
      return Collections.emptyList();
    }
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }

    return EnvTool.getAllQueriers(getQueryList()).getActivityInstancesDurationByActivityTypeFromProcessUUIDs(
        activityType, processUUIDs, since, until);
  }

  @Override
  public long getNumberOfCreatedProcessInstances(final Date since, final Date until) {
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }

    return EnvTool.getAllQueriers(getQueryList()).getNumberOfCreatedProcessInstances(since, until);
  }

  @Override
  public long getNumberOfCreatedProcessInstances(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until) {
    Misc.checkArgsNotNull(processUUID);
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }

    return EnvTool.getAllQueriers(getQueryList()).getNumberOfCreatedProcessInstances(processUUID, since, until);
  }

  @Override
  public long getNumberOfCreatedActivityInstances(final Date since, final Date until) {
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }

    return EnvTool.getAllQueriers(getQueryList()).getNumberOfCreatedActivityInstances(since, until);
  }

  @Override
  public long getNumberOfCreatedActivityInstances(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until) {
    Misc.checkArgsNotNull(processUUID);
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }
    return EnvTool.getAllQueriers(getQueryList()).getNumberOfCreatedActivityInstances(processUUID, since, until);
  }

  @Override
  public long getNumberOfCreatedActivityInstancesFromProcessUUIDs(final Set<ProcessDefinitionUUID> processUUIDs,
      final Date since, final Date until) {
    if (processUUIDs == null || processUUIDs.isEmpty()) {
      return 0;
    }
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }
    return EnvTool.getAllQueriers(getQueryList()).getNumberOfCreatedActivityInstancesFromProcessUUIDs(processUUIDs,
        since, until);
  }

  @Override
  public long getNumberOfCreatedActivityInstances(final ActivityDefinitionUUID activityUUID, final Date since,
      final Date until) {
    Misc.checkArgsNotNull(activityUUID);
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }
    return EnvTool.getAllQueriers(getQueryList()).getNumberOfCreatedActivityInstances(activityUUID, since, until);
  }

  @Override
  public long getNumberOfCreatedActivityInstancesFromActivityUUIDs(final Set<ActivityDefinitionUUID> activityUUIDs,
      final Date since, final Date until) {
    if (activityUUIDs == null || activityUUIDs.isEmpty()) {
      return 0;
    }
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }
    return EnvTool.getAllQueriers(getQueryList()).getNumberOfCreatedActivityInstancesFromActivityUUIDs(activityUUIDs,
        since, until);
  }

  @Override
  public long getNumberOfCreatedActivityInstancesByActivityType(final Type activityType, final Date since,
      final Date until) {
    Misc.checkArgsNotNull(activityType);
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }
    return EnvTool.getAllQueriers(getQueryList()).getNumberOfCreatedActivityInstancesByActivityType(activityType,
        since, until);
  }

  @Override
  public long getNumberOfCreatedActivityInstancesByActivityType(final Type activityType,
      final ProcessDefinitionUUID processUUID, final Date since, final Date until) {
    Misc.checkArgsNotNull(activityType, processUUID);
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }
    return EnvTool.getAllQueriers(getQueryList()).getNumberOfCreatedActivityInstancesByActivityType(activityType,
        processUUID, since, until);
  }

  @Override
  public long getNumberOfCreatedActivityInstancesByActivityTypeFromProcessUUIDs(final Type activityType,
      final Set<ProcessDefinitionUUID> processUUIDs, final Date since, final Date until) {
    Misc.checkArgsNotNull(activityType);
    if (processUUIDs == null || processUUIDs.isEmpty()) {
      return 0;
    }
    if (since.getTime() > until.getTime()) {
      throw new IllegalArgumentException("The since date is greater than the until date");
    }
    return EnvTool.getAllQueriers(getQueryList()).getNumberOfCreatedActivityInstancesByActivityTypeFromProcessUUIDs(
        activityType, processUUIDs, since, until);
  }

  @Override
  public long getCurrentMemoryUsage() throws MonitoringException {
    long result = 0;
    final MBeanServer mbeanServer = MBeanUtil.getMBeanServer();
    try {
      result = (Long) mbeanServer.getAttribute(new ObjectName(JvmMBean.JVM_MBEAN_NAME), "CurrentMemoryUsage");
    } catch (final MalformedObjectNameException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final MBeanException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final ReflectionException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final AttributeNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final javax.management.InstanceNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    }
    return result;
  }

  @Override
  public float getMemoryUsagePercentage() throws MonitoringException {
    float result = 0;
    final MBeanServer mbeanServer = MBeanUtil.getMBeanServer();
    try {
      result = (Float) mbeanServer.getAttribute(new ObjectName(JvmMBean.JVM_MBEAN_NAME), "MemoryUsagePercentage");
    } catch (final MalformedObjectNameException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final MBeanException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final ReflectionException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final AttributeNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final javax.management.InstanceNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    }
    return result;
  }

  @Override
  public double getSystemLoadAverage() throws MonitoringException {
    double result = 0;
    final MBeanServer mbeanServer = MBeanUtil.getMBeanServer();
    try {
      result = (Double) mbeanServer.getAttribute(new ObjectName(JvmMBean.JVM_MBEAN_NAME), "SystemLoadAverage");
    } catch (final MalformedObjectNameException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final MBeanException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final ReflectionException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final AttributeNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final javax.management.InstanceNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    }
    return result;
  }

  @Override
  public long getUpTime() throws MonitoringException {
    long result = 0;
    final MBeanServer mbeanServer = MBeanUtil.getMBeanServer();
    try {
      result = (Long) mbeanServer.getAttribute(new ObjectName(JvmMBean.JVM_MBEAN_NAME), "UpTime");
    } catch (final MalformedObjectNameException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final MBeanException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final ReflectionException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final AttributeNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final javax.management.InstanceNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    }
    return result;
  }

  @Override
  public long getStartTime() throws MonitoringException {
    long result = 0;
    final MBeanServer mbeanServer = MBeanUtil.getMBeanServer();

    try {
      result = (Long) mbeanServer.getAttribute(new ObjectName(JvmMBean.JVM_MBEAN_NAME), "StartTime");
    } catch (final AttributeNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final javax.management.InstanceNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final MalformedObjectNameException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final MBeanException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final ReflectionException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final NullPointerException e) {
      throw new MonitoringException(e.getMessage(), e);
    }

    return result;
  }

  @Override
  public long getTotalThreadsCpuTime() throws MonitoringException {
    long result = 0;
    final MBeanServer mbeanServer = MBeanUtil.getMBeanServer();
    try {
      result = (Long) mbeanServer.getAttribute(new ObjectName(JvmMBean.JVM_MBEAN_NAME), "TotalThreadsCpuTime");
    } catch (final MalformedObjectNameException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final MBeanException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final ReflectionException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final AttributeNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final javax.management.InstanceNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    }
    return result;
  }

  @Override
  public int getThreadCount() throws MonitoringException {
    int result = 0;
    final MBeanServer mbeanServer = MBeanUtil.getMBeanServer();
    try {
      result = (Integer) mbeanServer.getAttribute(new ObjectName(JvmMBean.JVM_MBEAN_NAME), "ThreadCount");
    } catch (final MalformedObjectNameException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final MBeanException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final ReflectionException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final AttributeNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final javax.management.InstanceNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    }
    return result;
  }

  @Override
  public int getAvailableProcessors() throws MonitoringException {
    int result;
    final MBeanServer mbeanServer = MBeanUtil.getMBeanServer();
    try {
      result = (Integer) mbeanServer.getAttribute(new ObjectName(JvmMBean.JVM_MBEAN_NAME), "AvailableProcessors");
    } catch (final MalformedObjectNameException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final MBeanException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final ReflectionException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final AttributeNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final javax.management.InstanceNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    }
    return result;
  }

  @Override
  public String getOSArch() throws MonitoringException {
    String result;
    final MBeanServer mbeanServer = MBeanUtil.getMBeanServer();
    try {
      result = (String) mbeanServer.getAttribute(new ObjectName(JvmMBean.JVM_MBEAN_NAME), "OSArch");
    } catch (final MalformedObjectNameException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final MBeanException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final ReflectionException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final AttributeNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final javax.management.InstanceNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    }
    return result;
  }

  @Override
  public String getOSName() throws MonitoringException {
    String result;
    final MBeanServer mbeanServer = MBeanUtil.getMBeanServer();
    try {
      result = (String) mbeanServer.getAttribute(new ObjectName(JvmMBean.JVM_MBEAN_NAME), "OSName");
    } catch (final MalformedObjectNameException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final MBeanException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final ReflectionException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final AttributeNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final javax.management.InstanceNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    }
    return result;
  }

  @Override
  public String getOSVersion() throws MonitoringException {
    String result;
    final MBeanServer mbeanServer = MBeanUtil.getMBeanServer();
    try {
      result = (String) mbeanServer.getAttribute(new ObjectName(JvmMBean.JVM_MBEAN_NAME), "OSVersion");
    } catch (final MalformedObjectNameException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final MBeanException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final ReflectionException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final AttributeNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final javax.management.InstanceNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    }
    return result;
  }

  @Override
  public String getJvmName() throws MonitoringException {
    String result;
    final MBeanServer mbeanServer = MBeanUtil.getMBeanServer();
    try {
      result = (String) mbeanServer.getAttribute(new ObjectName(JvmMBean.JVM_MBEAN_NAME), "JvmName");
    } catch (final MalformedObjectNameException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final MBeanException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final ReflectionException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final AttributeNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final javax.management.InstanceNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    }
    return result;
  }

  @Override
  public String getJvmVendor() throws MonitoringException {
    String result;
    final MBeanServer mbeanServer = MBeanUtil.getMBeanServer();
    try {
      result = (String) mbeanServer.getAttribute(new ObjectName(JvmMBean.JVM_MBEAN_NAME), "JvmVendor");
    } catch (final MalformedObjectNameException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final MBeanException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final ReflectionException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final AttributeNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final javax.management.InstanceNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    }
    return result;
  }

  @Override
  public String getJvmVersion() throws MonitoringException {
    String result;
    final MBeanServer mbeanServer = MBeanUtil.getMBeanServer();
    try {
      result = (String) mbeanServer.getAttribute(new ObjectName(JvmMBean.JVM_MBEAN_NAME), "JvmVersion");
    } catch (final MalformedObjectNameException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final MBeanException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final ReflectionException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final AttributeNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final javax.management.InstanceNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, String> getJvmSystemProperties() throws MonitoringException {
    final MBeanServer mbeanServer = MBeanUtil.getMBeanServer();
    try {
      return (Map<String, String>) mbeanServer.getAttribute(new ObjectName(JvmMBean.JVM_MBEAN_NAME),
          "JvmSystemProperties");
    } catch (final MalformedObjectNameException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final MBeanException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final ReflectionException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final AttributeNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    } catch (final javax.management.InstanceNotFoundException e) {
      throw new MonitoringException(e.getMessage(), e);
    }
  }

}
