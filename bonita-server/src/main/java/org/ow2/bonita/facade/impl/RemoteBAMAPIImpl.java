/**
 * Copyright (C) 2010-2012 BonitaSoft S.A.
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

import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.facade.APIAccessor;
import org.ow2.bonita.facade.BAMAPI;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.Type;
import org.ow2.bonita.facade.exception.MonitoringException;
import org.ow2.bonita.facade.internal.RemoteBAMAPI;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.identity.auth.DomainOwner;
import org.ow2.bonita.identity.auth.PasswordOwner;
import org.ow2.bonita.identity.auth.RESTUserOwner;
import org.ow2.bonita.identity.auth.UserOwner;

/**
 * 
 * @author Elias Ricken de Medeiros, Matthieu Chaffotte
 * 
 */
public class RemoteBAMAPIImpl implements RemoteBAMAPI {

  protected Map<String, BAMAPI> apis = new HashMap<String, BAMAPI>();

  protected BAMAPI getAPI(final Map<String, String> options) {
    if (options == null) {
      throw new IllegalArgumentException("The options are null or not well set.");
    }
    final String queryList = options.get(APIAccessor.QUERYLIST_OPTION);
    final String user = options.get(APIAccessor.USER_OPTION);
    final String domain = options.get(APIAccessor.DOMAIN_OPTION);
    UserOwner.setUser(user);
    DomainOwner.setDomain(domain);

    final String restUser = options.get(APIAccessor.REST_USER_OPTION);
    if (restUser != null) {
      RESTUserOwner.setUser(restUser);
      final String restPswd = options.get(APIAccessor.PASSWORD_HASH_OPTION);
      PasswordOwner.setPassword(restPswd);
    }

    if (!apis.containsKey(queryList)) {
      apis.put(queryList, new StandardAPIAccessorImpl().getBAMAPI(queryList));
    }
    return apis.get(queryList);
  }

  @Override
  public List<Integer> getNumberOfExecutingCasesPerDay(final Date since, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getNumberOfExecutingCasesPerDay(since);
  }

  @Override
  public List<Integer> getNumberOfFinishedCasesPerDay(final Date since, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getNumberOfFinishedCasesPerDay(since);
  }

  @Override
  public int getNumberOfOpenSteps(final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfOpenSteps();
  }

  @Override
  public List<Integer> getNumberOfOpenStepsPerDay(final Date since, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getNumberOfOpenStepsPerDay(since);
  }

  @Override
  public int getNumberOfOverdueSteps(final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfOverdueSteps();
  }

  @Override
  public int getNumberOfStepsAtRisk(final int remainingDays, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfStepsAtRisk(remainingDays);
  }

  @Override
  public int getNumberOfUserOpenSteps(final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfUserOpenSteps();
  }

  @Override
  public int getNumberOfUserOverdueSteps(final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfUserOverdueSteps();
  }

  @Override
  public int getNumberOfUserStepsAtRisk(final int remainingDays, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getNumberOfUserStepsAtRisk(remainingDays);
  }

  @Override
  public int getNumberOfFinishedSteps(final int priority, final Date since, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getNumberOfFinishedSteps(priority, since);
  }

  @Override
  public int getNumberOfOpenSteps(final int priority, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfOpenSteps(priority);
  }

  @Override
  public int getNumberOfUserFinishedSteps(final int priority, final Date since, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getNumberOfUserFinishedSteps(priority, since);
  }

  @Override
  public int getNumberOfUserOpenSteps(final int priority, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfUserOpenSteps(priority);
  }

  @Override
  public List<Long> getActivityInstancesExecutionTime(final Date since, final Date until,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getActivityInstancesExecutionTime(since, until);
  }

  @Override
  public List<Long> getActivityInstancesExecutionTime(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getActivityInstancesExecutionTime(processUUID, since, until);
  }

  @Override
  public List<Long> getActivityInstancesExecutionTime(final ActivityDefinitionUUID activityUUID, final Date since,
      final Date until, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getActivityInstancesExecutionTime(activityUUID, since, until);
  }

  @Override
  public List<Long> getActivityInstancesExecutionTimeFromActivityUUIDs(final Set<ActivityDefinitionUUID> activityUUIDs,
      final Date since, final Date until, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getActivityInstancesExecutionTimeFromActivityUUIDs(activityUUIDs, since, until);
  }

  @Override
  public List<Long> getActivityInstancesExecutionTimeFromProcessUUIDs(final Set<ProcessDefinitionUUID> processUUIDs,
      final Date since, final Date until, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getActivityInstancesExecutionTimeFromProcessUUIDs(processUUIDs, since, until);
  }

  @Override
  public List<Long> getProcessInstancesDuration(final Date since, final Date until, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getProcessInstancesDuration(since, until);
  }

  @Override
  public List<Long> getProcessInstancesDuration(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getProcessInstancesDuration(processUUID, since, until);
  }

  @Override
  public List<Long> getProcessInstancesDuration(final Set<ProcessDefinitionUUID> processUUIDs, final Date since,
      final Date until, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getProcessInstancesDuration(processUUIDs, since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTime(final Date since, final Date until, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getTaskInstancesWaitingTime(since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTime(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getTaskInstancesWaitingTime(processUUID, since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTime(final ActivityDefinitionUUID taskUUID, final Date since,
      final Date until, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getTaskInstancesWaitingTime(taskUUID, since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeFromProcessUUIDs(final Set<ProcessDefinitionUUID> processUUIDs,
      final Date since, final Date until, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getTaskInstancesWaitingTimeFromProcessUUIDs(processUUIDs, since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeFromTaskUUIDs(final Set<ActivityDefinitionUUID> taskUUIDs,
      final Date since, final Date until, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getTaskInstancesWaitingTimeFromTaskUUIDs(taskUUIDs, since, until);
  }

  @Override
  public List<Long> getActivityInstancesDuration(final Date since, final Date until, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getActivityInstancesDuration(since, until);
  }

  @Override
  public List<Long> getActivityInstancesDuration(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getActivityInstancesDuration(processUUID, since, until);
  }

  @Override
  public List<Long> getActivityInstancesDuration(final ActivityDefinitionUUID activityUUID, final Date since,
      final Date until, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getActivityInstancesDuration(activityUUID, since, until);
  }

  @Override
  public List<Long> getActivityInstancesDurationByActivityType(final Type activityType, final Date since,
      final Date until, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getActivityInstancesDurationByActivityType(activityType, since, until);
  }

  @Override
  public List<Long> getActivityInstancesDurationByActivityType(final Type activityType,
      final ProcessDefinitionUUID processUUID, final Date since, final Date until, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getActivityInstancesDurationByActivityType(activityType, processUUID, since, until);
  }

  @Override
  public List<Long> getActivityInstancesDurationByActivityTypeFromProcessUUIDs(final Type activityType,
      final Set<ProcessDefinitionUUID> processUUIDs, final Date since, final Date until,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getActivityInstancesDurationByActivityTypeFromProcessUUIDs(activityType, processUUIDs,
        since, until);
  }

  @Override
  public List<Long> getActivityInstancesDurationFromActivityUUIDs(final Set<ActivityDefinitionUUID> activityUUIDs,
      final Date since, final Date until, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getActivityInstancesDurationFromActivityUUIDs(activityUUIDs, since, until);
  }

  @Override
  public List<Long> getActivityInstancesDurationFromProcessUUIDs(final Set<ProcessDefinitionUUID> processUUIDs,
      final Date since, final Date until, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getActivityInstancesDurationFromProcessUUIDs(processUUIDs, since, until);
  }

  @Override
  public long getNumberOfCreatedProcessInstances(final Date since, final Date until, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getNumberOfCreatedProcessInstances(since, until);
  }

  @Override
  public long getNumberOfCreatedProcessInstances(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfCreatedProcessInstances(processUUID, since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeOfUser(final String username, final Date since, final Date until,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getTaskInstancesWaitingTimeOfUser(username, since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeOfUser(final String username, final ProcessDefinitionUUID processUUID,
      final Date since, final Date until, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getTaskInstancesWaitingTimeOfUser(username, processUUID, since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeOfUser(final String username, final ActivityDefinitionUUID taskUUID,
      final Date since, final Date until, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getTaskInstancesWaitingTimeOfUser(username, taskUUID, since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeOfUserFromProcessUUIDs(final String username,
      final Set<ProcessDefinitionUUID> processUUIDs, final Date since, final Date until,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getTaskInstancesWaitingTimeOfUserFromProcessUUIDs(username, processUUIDs, since, until);
  }

  @Override
  public List<Long> getTaskInstancesWaitingTimeOfUserFromTaskUUIDs(final String username,
      final Set<ActivityDefinitionUUID> taskUUIDs, final Date since, final Date until, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getTaskInstancesWaitingTimeOfUserFromTaskUUIDs(username, taskUUIDs, since, until);
  }

  @Override
  public long getNumberOfCreatedActivityInstances(final Date since, final Date until, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getNumberOfCreatedActivityInstances(since, until);
  }

  @Override
  public long getNumberOfCreatedActivityInstances(final ProcessDefinitionUUID processUUID, final Date since,
      final Date until, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfCreatedActivityInstances(processUUID, since, until);
  }

  @Override
  public long getNumberOfCreatedActivityInstances(final ActivityDefinitionUUID activityUUID, final Date since,
      final Date until, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfCreatedActivityInstances(activityUUID, since, until);
  }

  @Override
  public long getNumberOfCreatedActivityInstancesByActivityType(final Type activityType, final Date since,
      final Date until, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfCreatedActivityInstancesByActivityType(activityType, since, until);
  }

  @Override
  public long getNumberOfCreatedActivityInstancesByActivityType(final Type activityType,
      final ProcessDefinitionUUID processUUID, final Date since, final Date until, final Map<String, String> options)
      throws RemoteException {
    return getAPI(options).getNumberOfCreatedActivityInstancesByActivityType(activityType, processUUID, since, until);
  }

  @Override
  public long getNumberOfCreatedActivityInstancesByActivityTypeFromProcessUUIDs(final Type activityType,
      final Set<ProcessDefinitionUUID> processUUIDs, final Date since, final Date until,
      final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfCreatedActivityInstancesByActivityTypeFromProcessUUIDs(activityType,
        processUUIDs, since, until);
  }

  @Override
  public long getNumberOfCreatedActivityInstancesFromActivityUUIDs(final Set<ActivityDefinitionUUID> activityUUIDs,
      final Date since, final Date until, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfCreatedActivityInstancesFromActivityUUIDs(activityUUIDs, since, until);
  }

  @Override
  public long getNumberOfCreatedActivityInstancesFromProcessUUIDs(final Set<ProcessDefinitionUUID> processUUIDs,
      final Date since, final Date until, final Map<String, String> options) throws RemoteException {
    return getAPI(options).getNumberOfCreatedActivityInstancesFromProcessUUIDs(processUUIDs, since, until);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ow2.bonita.facade.internal.RemoteBAMAPI#getSystemLoadAverage(java.util .Map)
   */
  @Override
  public double getSystemLoadAverage(final Map<String, String> options) throws RemoteException, MonitoringException {
    return getAPI(options).getSystemLoadAverage();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ow2.bonita.facade.internal.RemoteBAMAPI#getMemoryUsage(java.util.Map)
   */
  @Override
  public long getCurrentMemoryUsage(final Map<String, String> options) throws RemoteException, MonitoringException {
    return getAPI(options).getCurrentMemoryUsage();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ow2.bonita.facade.internal.RemoteBAMAPI#getMemoryUsagePercentage(java .util.Map)
   */
  @Override
  public float getMemoryUsagePercentage(final Map<String, String> options) throws RemoteException, MonitoringException {
    return getAPI(options).getMemoryUsagePercentage();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ow2.bonita.facade.internal.RemoteBAMAPI#getUpTime(java.util.Map)
   */
  @Override
  public long getUpTime(final Map<String, String> options) throws RemoteException, MonitoringException {
    return getAPI(options).getUpTime();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ow2.bonita.facade.internal.RemoteBAMAPI#getStartTime(java.util.Map)
   */
  @Override
  public long getStartTime(final Map<String, String> options) throws RemoteException, MonitoringException {
    return getAPI(options).getStartTime();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ow2.bonita.facade.internal.RemoteBAMAPI#getTotalThreadsCpuTime(java .util.Map)
   */
  @Override
  public long getTotalThreadsCpuTime(final Map<String, String> options) throws RemoteException, MonitoringException {
    return getAPI(options).getTotalThreadsCpuTime();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ow2.bonita.facade.internal.RemoteBAMAPI#getThreadCount(java.util.Map)
   */
  @Override
  public int getThreadCount(final Map<String, String> options) throws RemoteException, MonitoringException {
    return getAPI(options).getThreadCount();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ow2.bonita.facade.internal.RemoteBAMAPI#getOSArch(java.util.Map)
   */
  @Override
  public String getOSArch(final Map<String, String> options) throws RemoteException, MonitoringException {
    return getAPI(options).getOSArch();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ow2.bonita.facade.internal.RemoteBAMAPI#getOSName(java.util.Map)
   */
  @Override
  public String getOSName(final Map<String, String> options) throws RemoteException, MonitoringException {
    return getAPI(options).getOSName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ow2.bonita.facade.internal.RemoteBAMAPI#getOSVersion(java.util.Map)
   */
  @Override
  public String getOSVersion(final Map<String, String> options) throws RemoteException, MonitoringException {
    return getAPI(options).getOSVersion();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ow2.bonita.facade.internal.RemoteBAMAPI#getJvmName(java.util.Map)
   */
  @Override
  public String getJvmName(final Map<String, String> options) throws RemoteException, MonitoringException {
    return getAPI(options).getJvmName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ow2.bonita.facade.internal.RemoteBAMAPI#getJvmVendor(java.util.Map)
   */
  @Override
  public String getJvmVendor(final Map<String, String> options) throws RemoteException, MonitoringException {
    return getAPI(options).getJvmVendor();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ow2.bonita.facade.internal.RemoteBAMAPI#getJvmVersion(java.util.Map)
   */
  @Override
  public String getJvmVersion(final Map<String, String> options) throws RemoteException, MonitoringException {
    return getAPI(options).getJvmVersion();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ow2.bonita.facade.internal.RemoteBAMAPI#getAvailableProcessors(java .util.Map)
   */
  @Override
  public int getAvailableProcessors(final Map<String, String> options) throws RemoteException, MonitoringException {
    return getAPI(options).getAvailableProcessors();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ow2.bonita.facade.internal.RemoteBAMAPI#getJvmSystemProperties(java .util.Map)
   */
  @Override
  public Map<String, String> getJvmSystemProperties(final Map<String, String> options) throws RemoteException,
      MonitoringException {
    return getAPI(options).getJvmSystemProperties();
  }

}
