/**
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.bonita.facade.monitoring.model;

import java.util.Map;

/**
 * @author Christophe Havard
 * 
 */
public interface JvmMBean {
  public static final String JVM_MBEAN_NAME = "Bonitasoft:name=JVM,type=JVMMBean";

  /**
   * Returns the sum of both heap and non-heap memory usage.
   */
  public long getCurrentMemoryUsage();

  /**
   * Returns the percentage of memory used compare to maximum available memory.
   * This calculation is based on both the heap & non-heap maximum amount of
   * memory that can be used.
   */
  public float getMemoryUsagePercentage();

  /**
   * Returns the system load average for the last minute. The system load
   * average is the sum of the number of runnable entities queued to the
   * available processors and the number of runnable entities running on the
   * available processors averaged over a period of time. The way in which the
   * load average is calculated is operating system specific but is typically a
   * damped time-dependent average.
   * 
   * If the load average is not available, a negative value is returned.
   */
  public double getSystemLoadAverage();

  /**
   * Returns the number of milliseconds elapsed since the Java Virtual Machine
   * started.
   */
  public long getUpTime();

  /**
   * Returns a timestamp (in millisecond) which indicates the date when the Java
   * virtual machine started. Usually, a timestamp represents the time elapsed
   * since the 1st of January, 1970.
   */
  public long getStartTime();

  /**
   * Returns the total CPU time for all live threads in nanoseconds. It sums the
   * CPU time consumed by each live threads.
   */
  public long getTotalThreadsCpuTime();

  /**
   * Returns the current number of live threads including both daemon and
   * non-daemon threads.
   */
  public int getThreadCount();

  /**
   * Returns the operating system architecture
   */
  public String getOSArch();

  /**
   * Returns the number of processors available to the Java virtual machine.
   */
  public int getAvailableProcessors();

  /**
   * Return the OS name
   */
  public String getOSName();

  /**
   * Return the OS version
   */
  public String getOSVersion();

  /**
   * Returns the Java virtual machine implementation name
   */
  public String getJvmName();

  /**
   * Returns the Java virtual machine implementation vendor
   */
  public String getJvmVendor();

  /**
   * Returns the Java virtual machine implementation version
   */
  public String getJvmVersion();

  /**
   * Returns the Java virtual machine System Properties
   */
  public Map<String, String> getJvmSystemProperties();

  /**
   * Make the MBean available through the default MBeanServer.
   * 
   * @throws MBeanStartException
   * @throws TransactionCreationException
   * @throws IdentityException
   * @throws HandlerRegistrationException
   * @throws FireEventException
   * @throws BadTransactionStateException
   * @throws TransactionCommitException
   * @throws TransactionPrepareException
   * @throws Exception
   */
  public void start() throws MBeanStartException;

  /**
   * Make the MBean unavailable through the default MBeanServer.
   * 
   * @throws MBeanStopException
   * @throws HandlerUnregistrationException
   */
  public void stop() throws MBeanStopException;
}
