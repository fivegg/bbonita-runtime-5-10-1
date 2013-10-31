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
package org.ow2.bonita.facade;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.facade.def.majorElement.ActivityDefinition.Type;
import org.ow2.bonita.facade.exception.MonitoringException;
import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;

/**
 * This class provides a brief overview of Bonita Business Activity Monitoring.<br>
 * Note: Using this API can affect engine performances.
 * @author Matthieu Chaffotte, Elias Ricken de Medeiros
 *
 */
public interface BAMAPI {

  /**
   * Gets the list of the number of executing cases (process instances) per day of every users.
   * @param since the starting date
   * @return the list of executing cases number per day
   */
  List<Integer> getNumberOfExecutingCasesPerDay(Date since);

  /**
   * Gets the list of the number of the finished cases (process instances) per day of every users.
   * @param since the starting date
   * @return the list of finish cases number per day
   */
  List<Integer> getNumberOfFinishedCasesPerDay(Date since);

  /**
   * Gets the total number of finished steps of every users since the given date
   * according to the step priority.
   * @param priority the step priority
   * @param since 
   * @return the number of finished steps.
   */
  int getNumberOfFinishedSteps(int priority, Date since);

  /**
   * Gets the total number of open steps (i.e. tasks) of every users.
   * An open step is a step which is on time.
   * @return the number of open steps
   */
  int getNumberOfOpenSteps();

  /**
   * Gets the total number of open steps (i.e. tasks) of every users according to the step priority.
   * @param priority the step priority
   * @return the number of open steps
   */
  int getNumberOfOpenSteps(int priority);

  /**
   * Gets the number of open steps of every users for every days since the given day.
   * @param since the day 
   * @return a list the number of open steps for each day from the given day
   */
  List<Integer> getNumberOfOpenStepsPerDay(Date since);

  /**
   * Gets the total number of overdue steps (i.e. tasks) of every users.
   * An overdue task is a task which its ended date is exceeded.
   * @return the number of overdue steps.
   */
  int getNumberOfOverdueSteps();

  /**
   * Gets the total number of steps at risk (i.e. tasks) of every users. A step at risk is
   * a step which its ended date is close to exceed according to the remaining days.
   * @param remainingDays the remaining days before the ended step date is exceeded.
   * @return the number of steps at risk of every users.
   */
  int getNumberOfStepsAtRisk(int remainingDays);

  /**
   * Gets the total number of finished steps of the logged user since the given date according
   * to the step priority.
   * @param priority the step priority
   * @param since 
   * @return the number of finished steps.
   */
  int getNumberOfUserFinishedSteps(int priority, Date since);

  /**
   * Gets the number of open steps of the logged user.
   * @return the number of open steps of the logged user
   */
  int getNumberOfUserOpenSteps();

  /**
   * Gets the number of open steps of the logged user according to the step priority.
   * @param priority the step priority
   * @return the number of open step of the logged user
   */
  int getNumberOfUserOpenSteps(int priority);

  /**
   * Gets the number of overdue steps (i.e. tasks) of the logged user.
   * An overdue task is a task which its ended date is exceeded.
   * @return the number of overdue steps of the logged user.
   */
  int getNumberOfUserOverdueSteps();

  /**
   * Gets the number of steps at risk (i.e. tasks) of the logged user.
   * A step at risk is a step which its ended date is close to exceed according to the remaining days.
   * @param remainingDays the remaining days before the ended step date is exceeded.
   * @return the number of steps at risk of the logged user.
   */
  int getNumberOfUserStepsAtRisk(int remainingDays);
  
  /**
   * Gets the list of the duration of all finished processes instances in the given interval.
   * @param since
   * @param until
   * @return the list of the duration of all finished processes instances in the given interval
   */
  List<Long> getProcessInstancesDuration(Date since, Date until);
  
  /**
   * Gets the list of the duration of all finished processes instances for the given processUUID in the given interval.
   * @param processUUID
   * @param since
   * @param until
   * @return the list of the duration of all finished processes instances for the given processUUID in the given interval.
   */
  List<Long> getProcessInstancesDuration(ProcessDefinitionUUID processUUID, Date since, Date until);
  
  /**
   * Gets the list of the duration of all finished processes instances for the given processUUIDs in the given interval.
   * @param processUUIDs
   * @param since
   * @param until
   * @return the list of the duration of all finished processes instances for the given processUUIDs in the given interval.
   */
  List<Long> getProcessInstancesDuration(Set<ProcessDefinitionUUID> processUUIDs, Date since, Date until);
  
  /**
   * Gets the list of the execution time of all finished activity instances in the given interval.
   * @param since
   * @param until
   * @return the list of the execution time of all finished activity instances in the given interval.
   */
  List<Long> getActivityInstancesExecutionTime(Date since, Date until);
  
  /**
   * Gets the list of the execution time of all finished activity instances for the given processUUID in the given interval.
   * @param processUUID
   * @param since
   * @param until
   * @return the list of the execution time of all finished activity instances for the given processUUID in the given interval.
   */
  List<Long> getActivityInstancesExecutionTime (ProcessDefinitionUUID processUUID, Date since, Date until);
  
  /**
   * Gets the list of the execution time of all finished activity instances for the given processUUIDs in the given interval.
   * @param processUUIDs
   * @param since
   * @param until
   * @return the list of the execution time of all finished activity instances for the given processUUIDs in the given interval.
   */
  List<Long> getActivityInstancesExecutionTimeFromProcessUUIDs(Set<ProcessDefinitionUUID> processUUIDs, Date since, Date until);
  
  /**
   * Gets the list of the execution time of all finished activity instances for the given activityUUID in the given interval.
   * @param activityUUID
   * @param since
   * @param until
   * @return the list of the execution time of all finished activity instances for the given activityUUID in the given interval.
   */
  List<Long> getActivityInstancesExecutionTime(ActivityDefinitionUUID activityUUID,  Date since, Date until);
  
  /**
   * Gets the list of the execution time of all finished activity instances for the given activityUUIDs in the given interval.
   * @param activityUUIDs
   * @param since
   * @param until
   * @return the list of the execution time of all finished activity instances for the given activityUUIDs in the given interval.
   */
  List<Long> getActivityInstancesExecutionTimeFromActivityUUIDs(Set<ActivityDefinitionUUID> activityUUIDs, Date since, Date until);
  
  /**
   * Gets the list of the waiting time of all finished human tasks in the given interval.
   * @param since
   * @param until
   * @return the list of the waiting time of all finished human tasks in the given interval.
   */
  List<Long> getTaskInstancesWaitingTime(Date since, Date until);
  
  /**
   * Gets the list of the waiting time of all finished human tasks for the given processUUID in the given interval.
   * @param processUUID
   * @param since
   * @param until
   * @return the list of the waiting time of all finished human tasks for the given processUUID in the given interval.
   */
  List<Long> getTaskInstancesWaitingTime(ProcessDefinitionUUID processUUID, Date since, Date until);

  /**
   * Gets the list of the waiting time of all finished human tasks for the given processUUIDs in the given interval.
   * @param processUUIDs
   * @param since
   * @param until
   * @return the list of the waiting time of all finished human tasks for the given processUUIDs in the given interval.
   */
  List<Long> getTaskInstancesWaitingTimeFromProcessUUIDs(Set<ProcessDefinitionUUID> processUUIDs, Date since, Date until);
  
  /**
   * Gets the list of the waiting time of all finished human tasks for the given taskUUID in the given interval.
   * @param taskUUID
   * @param since
   * @param until
   * @return the list of the waiting time of all finished human tasks for the given taskUUID in the given interval.
   */
  List<Long> getTaskInstancesWaitingTime(ActivityDefinitionUUID taskUUID, Date since, Date until);
  
  /**
   * Gets the list of the waiting time of all finished human tasks for the given taskUUIDs in the given interval.
   * @param tasksUUIDs
   * @param since
   * @param until
   * @return the list of the waiting time of all finished human tasks for the given taskUUIDs in the given interval.
   */
  List<Long> getTaskInstancesWaitingTimeFromTaskUUIDs(Set<ActivityDefinitionUUID> taskUUIDs, Date since, Date until);
  
  /**
   * Gets the list of the waiting time of all finished human tasks for the given user in the given interval.
   * @param username
   * @param since
   * @param until
   * @return the list of the waiting time of all finished human tasks for the given user in the given interval.
   */
  List<Long> getTaskInstancesWaitingTimeOfUser(String username, Date since, Date until);
  
  /**
   * Gets the list of the waiting time of all finished human tasks for the given user, ProcessDefintionUUID and interval.
   * @param username
   * @param processUUID
   * @param since
   * @param until
   * @return the list of the waiting time of all finished human tasks for the given user, ProcessDefintionUUID and interval.
   */
  List<Long> getTaskInstancesWaitingTimeOfUser(String username, ProcessDefinitionUUID processUUID, Date since, Date until);
  
  /**
   * Gets the list of the waiting time of all finished human tasks for the given user, ProcessDefintionUUIDs and interval.
   * @param username
   * @param processUUIDs
   * @param since
   * @param until
   * @return the list of the waiting time of all finished human tasks for the given user, ProcessDefintionUUIDs and interval. 
   */
  List<Long> getTaskInstancesWaitingTimeOfUserFromProcessUUIDs(String username, Set<ProcessDefinitionUUID> processUUIDs, Date since, Date until);
  
  /**
   * Gets the list of the waiting time of all finished human tasks for the given user, taskUUID and interval.
   * @param username
   * @param taskUUID
   * @param since
   * @param until
   * @return the list of the waiting time of all finished human tasks for the given user, taskUUID and interval.
   */
  List<Long> getTaskInstancesWaitingTimeOfUser(String username, ActivityDefinitionUUID taskUUID, Date since, Date until);
  
  /**
   * Gets the list of the waiting time of all finished human tasks for the given user, taskUUIDs and interval.
   * @param username
   * @param tasksUUIDs
   * @param since
   * @param until
   * @return the list of the waiting time of all finished human tasks for the given user, taskUUIDs and interval.
   */
  List<Long> getTaskInstancesWaitingTimeOfUserFromTaskUUIDs(String username, Set<ActivityDefinitionUUID> taskUUIDs, Date since, Date until);
  
  /**
   * Gets the list of the duration of all finished activity instances for the given interval.
   * @param since
   * @param until
   * @return the list of the duration of all finished activity instances for the given interval.
   */
  List<Long> getActivityInstancesDuration(Date since, Date until);
  
  /**
   * Gets the list of the duration of all finished activity instances for the given ProcessDefintionUUID and interval.
   * @param processUUID
   * @param since
   * @param until
   * @return
   */
  List<Long> getActivityInstancesDuration(ProcessDefinitionUUID processUUID, Date since, Date until);
  
  /**
   * Gets the list of the duration of all finished activity instances for the given ProcessDefintionUUIDs and interval.
   * @param processUUIDs
   * @param since
   * @param until
   * @return the list of the duration of all finished activity instances for the given ProcessDefintionUUIDs and interval.
   */
  List<Long> getActivityInstancesDurationFromProcessUUIDs(Set<ProcessDefinitionUUID> processUUIDs, Date since, Date until);
  
  /**
   * Gets the list of the duration of all finished activity instances for the given ActivityDefintionUUID and interval.
   * @param activityUUID
   * @param since
   * @param until
   * @return the list of the duration of all finished activity instances for the given ActivityDefintionUUID and interval.
   */
  List<Long> getActivityInstancesDuration(ActivityDefinitionUUID activityUUID, Date since, Date until);
  
  /**
   * Gets the list of the duration of all finished activity instances for the given ActivityDefintionUUIDs and interval.
   * @param activityUUIDs
   * @param since
   * @param until
   * @return the list of the duration of all finished activity instances for the given ActivityDefintionUUIDs and interval.
   */
  List<Long> getActivityInstancesDurationFromActivityUUIDs(Set<ActivityDefinitionUUID> activityUUIDs, Date since, Date until);
  
  /**
   * Gets the list of the duration of all finished activity instances for the given type and interval.
   * @param activityType
   * @param since
   * @param until
   * @return the list of the duration of all finished activity instances for the given type and interval.
   */
  List<Long> getActivityInstancesDurationByActivityType(Type activityType, Date since, Date until);
  
  /**
   * Gets the list of the duration of all finished activity instances for the given type, ProcessDefinitionUUID and interval.
   * @param activityType
   * @param processUUID
   * @param since
   * @param until
   * @return the list of the duration of all finished activity instances for the given type, ProcessDefinitionUUID and interval.
   */
  List<Long> getActivityInstancesDurationByActivityType(Type activityType, ProcessDefinitionUUID processUUID, Date since, Date until);
  
  /**
   * Gets the list of the duration of all finished activity instances for the given type, ProcessDefinitionUUIDs and interval.
   * @param activityType
   * @param processUUIDs
   * @param since
   * @param until
   * @return the list of the duration of all finished activity instances for the given type, ProcessDefinitionUUIDs and interval.
   */
  List<Long> getActivityInstancesDurationByActivityTypeFromProcessUUIDs(Type activityType, Set<ProcessDefinitionUUID> processUUIDs, Date since, Date until);
  
  /**
   * Gets the number of created Process Instances in the given interval
   * @param since
   * @param until
   * @return the number of created Process Instances in the given interval
   */
  long getNumberOfCreatedProcessInstances(Date since, Date until);
  
  /**
   * Gets the number of created Process Instances for the the given ProcessDefinitionUUID and interval
   * @param processUUID
   * @param since
   * @param until
   * @return the number of created Process Instances for the the given ProcessDefinitionUUID and interval
   */
  long getNumberOfCreatedProcessInstances(ProcessDefinitionUUID processUUID, Date since, Date until);
  
  /**
   * Gets the number of created Activity Instances in the given interval
   * @param since
   * @param until
   * @return the number of created Activity Instances in the given interval
   */
  long getNumberOfCreatedActivityInstances(Date since, Date until);
  
  /**
   * Gets the number of created Activity Instances for the given ProcessDefinitionUUID and interval
   * @param processUUID ProcessDefinitionUUID
   * @param since
   * @param until
   * @return the number of created Activity Instances for the given ProcessDefinitionUUID and interval
   */
  long getNumberOfCreatedActivityInstances(ProcessDefinitionUUID processUUID, Date since, Date until);
  
  /**
   * Gets the number of created Activity Instances for the given ProcessDefinitionUUIDs in the given interval
   * @param processUUIDs set of ProcessDefinitionUUID
   * @param since
   * @param until
   * @return the number of created Activity Instances for the given ProcessDefinitionUUIDs  in the given interval
   */
  long getNumberOfCreatedActivityInstancesFromProcessUUIDs(Set<ProcessDefinitionUUID> processUUIDs, Date since, Date until);
  
  /**
   * Gets the number of created Activity Instances for the given ActivityDefinitionUUID in the given interval
   * @param activityUUID the ActivityDefinitionUUID
   * @param since
   * @param until
   * @return the number of created Activity Instances for the given ActivityDefinitionUUID in the given interval
   */
  long getNumberOfCreatedActivityInstances(ActivityDefinitionUUID activityUUID, Date since, Date until);
  
  /**
   * Gets the number of created Activity Instances for the given ActivityDefinitionUUIDs in the given interval
   * @param activityUUIDs ActivityDefinitionUUIDs
   * @param since
   * @param until
   * @return the number of created Activity Instances for the given ActivityDefinitionUUIDs in the given interval
   */
  long getNumberOfCreatedActivityInstancesFromActivityUUIDs(Set<ActivityDefinitionUUID> activityUUIDs, Date since, Date until);
  
  /**
   * Gets the number of created Activity Instances for the given type in the given interval
   * @param activityType the ActivityType
   * @param since
   * @param until
   * @return the number of created Activity Instances for the given type in the given interval
   */
  long getNumberOfCreatedActivityInstancesByActivityType(Type activityType, Date since, Date until);
  
  /**
   * Gets the number of created Activity Instances for the given type and ProcessDefinitionUUID in the given interval
   * @param activityType the activity type
   * @param processUUID the ProcesssDefinitionUUID
   * @param since
   * @param until
   * @return the number of created Activity Instances for the given type and ProcessDefinitionUUID in the given interval
   */
  long getNumberOfCreatedActivityInstancesByActivityType(Type activityType, ProcessDefinitionUUID processUUID, Date since, Date until);
  
  /**
   * the number of created Activity Instances for the given type and ProcessDefinitionUUIDs in the given interval
   * @param activityType the activity type
   * @param processUUIDs the set of ProcesssDefinitionUUID
   * @param since
   * @param until
   * @return the number of created Activity Instances for the given type and ProcessDefinitionUUIDs in the given interval
   */
  long getNumberOfCreatedActivityInstancesByActivityTypeFromProcessUUIDs(Type activityType, Set<ProcessDefinitionUUID> processUUIDs, Date since, Date until);

  /**
   * Get the sum of both heap and non-heap memory usage.
   *
   */
  public long getCurrentMemoryUsage() throws MonitoringException;  
  
  /**
   * Returns the percentage of memory used compare to maximum available memory.
   * This calculation is based on both the heap & non-heap maximum amount of memory that can be used.
 * @throws MonitoringException 
   */  
  public float getMemoryUsagePercentage() throws MonitoringException;
  
  /**
   * Returns the system load average for the last minute.
   * The system load average is the sum of the number of runnable entities queued to the available
   * processors and the number of runnable entities running on the available processors averaged over
   * a period of time. The way in which the load average is calculated is operating system specific
   * but is typically a damped time-dependent average.
   * 
   * If the load average is not available, a negative value is returned.
   */
  public double getSystemLoadAverage() throws MonitoringException;
  
  /**
   * Returns the number of milliseconds elapsed since the Java Virtual Machine started.
   */
  public long getUpTime() throws MonitoringException;  
  
  /**
   * Returns a timestamp (in millisecond) which indicates the date when the Java virtual
   * machine started.
   * Usually, a timestamp represents the time elapsed since the 1st of January, 1970.
   */
  public long getStartTime() throws MonitoringException;
  
  /**
   * Returns the total CPU time for all live threads in nanoseconds. It sums the CPU time
   * consumed by each live threads.
   */
  public long getTotalThreadsCpuTime() throws MonitoringException;
  
  /**
   * Returns the current number of live threads including both daemon and non-daemon threads.
   */
  public int getThreadCount() throws MonitoringException;
  
  /**
   * Returns the number of processors available to the Java virtual machine.
   */
  public int getAvailableProcessors() throws MonitoringException; 

  /**
   * Returns the operating system architecture
   */
  public String getOSArch() throws MonitoringException;
  
  /**
   * Return the OS name
   */
  public String getOSName() throws MonitoringException; 
  
  /**
   * Return the OS version
   */
  public String getOSVersion() throws MonitoringException; 
  
  /**
   * Returns the Java virtual machine implementation name
   */
  public String getJvmName() throws MonitoringException; 
  
  /**
   * Returns the Java virtual machine implementation vendor
   */
  public String getJvmVendor() throws MonitoringException; 
  
  /**
   * Returns the Java virtual machine implementation version
   */
  public String getJvmVersion() throws MonitoringException; 
  
  /**
   * Returns the Java virtual machine System properties list
   */
  public Map<String,String> getJvmSystemProperties() throws MonitoringException;
  
}